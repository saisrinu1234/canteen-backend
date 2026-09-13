package com.example.canteen.login;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.canteen.login.passwordReset.ForgotPasswordRequest;
import com.example.canteen.login.passwordReset.PasswordResetService;
import com.example.canteen.login.passwordReset.ResetPasswordRequest;
import com.example.canteen.mailing.ApiResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordResetService passwordResetService; // Constructor Injection public

    AuthController(UserRepository userRepository, JwtService jwtService, RefreshTokenService refreshTokenService,
            RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder,
            TokenBlacklistService tokenBlacklistService, PasswordResetService passwordResetService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklistService = tokenBlacklistService;
        this.passwordResetService = passwordResetService;
    }

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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid UserName or Password");
        }

        User1 user = optionaluser.get(); // ✔ now safe
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid UserName or Password");
        }

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        Cookie cookie = new Cookie("refreshToken", refreshToken.getToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
        LoginResponse responseBody = new LoginResponse(accessToken, refreshToken.getToken(), user.getRole());
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/user/existence")
    public ResponseEntity<Boolean> userExists(@RequestParam String email) {
        return ResponseEntity.ok(userRepository.existsByEmail(email));
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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        try {

            passwordResetService.forgotPassword(
                    request.getEmail());

            return ResponseEntity.ok(
                    "Password reset link sent to your email");

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body("Something went wrong");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        try {

            passwordResetService.resetPassword(
                    request.getToken(),
                    request.getPassword());

            return ResponseEntity.ok(
                    "Password reset successfully");

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

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

    @GetMapping("/get/profile")
    public ResponseEntity<?> getUserByEmail(Principal principal) {
        Optional<User1> user = userRepository.findByEmail(principal.getName());
        if (user.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found");
        User1 userDetails = user.get();
        return ResponseEntity.ok(userDetails);

    }

    @PutMapping("/update/profile")
    public ResponseEntity<?> updateUser(
            Principal principal,
            @RequestBody User1 updatedUser) {

        Optional<User1> optionalUser = userRepository.findByEmail(principal.getName());

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