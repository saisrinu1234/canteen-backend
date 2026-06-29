package com.example.canteen.login;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User1 user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(403).body("Email already registered");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Force default role
        user.setRole("ROLE_USER");

        userRepository.save(user);

        // Clear password before sending back
        user.setPassword(null);

        return ResponseEntity.ok(user);
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
            HttpServletResponse response) {
        Optional<User1> optionaluser = userRepository.findByEmail(request.getEmail());

        if (optionaluser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found");
        }

        User1 user = optionaluser.get(); // ✔ now safe
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        Cookie cookie = new Cookie("refreshToken", refreshToken.getToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
        LoginResponse responseBody = new LoginResponse(accessToken, user.getRole());
        return ResponseEntity.ok(responseBody);
    }

    // REFRESH
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue("refreshToken") String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        refreshTokenService.verifyExpiration(refreshToken);

        String accessToken = jwtService.generateAccessToken(refreshToken.getUser());

        return ResponseEntity.ok(accessToken);
    }

    // LOGOUT
    // @PostMapping("/logout")
    // public ResponseEntity<?> logout(@CookieValue("refreshToken") String token,
    // HttpServletResponse response) {

    // refreshTokenRepository.findByToken(token)
    // .ifPresent(refreshTokenRepository::delete);

    // Cookie cookie = new Cookie("refreshToken", null);
    // cookie.setMaxAge(0);
    // cookie.setPath("/");
    // response.addCookie(cookie);

    // return ResponseEntity.ok("Logged out successfully");
    // }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request,
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response) {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String accessToken = header.substring(7);

            long ttl = jwtService.getRemainingTime(accessToken);

            tokenBlacklistService.blacklistToken(accessToken, ttl);
        }

        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/get/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        Optional<User1> user = userRepository.findByEmail(email);
        if (user.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found");
        User1 userDetails = user.get();
        return ResponseEntity.ok(userDetails);

    }

    @PutMapping("/update/{email}")
    public ResponseEntity<?> updateUser(
            @PathVariable String email,
            @RequestBody User1 updatedUser) {

        Optional<User1> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User Not Found");
        }

        User1 user = optionalUser.get();

        user.setName(updatedUser.getName());
        user.setPhone(updatedUser.getPhone());

        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

}