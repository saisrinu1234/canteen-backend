package com.example.canteen.configuration;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.canteen.login.JwtService;
import com.example.canteen.login.RefreshToken;
import com.example.canteen.login.RefreshTokenService;
import com.example.canteen.login.User1;
import com.example.canteen.login.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String googleId = oauthUser.getAttribute("sub");

        User1 user = userRepository.findByEmail(email)
                .orElseGet(() -> {

                    User1 u = new User1();
                    u.setEmail(email);
                    u.setName(name);

                    u.setRole("ROLE_USER");

                    u.setProvider("GOOGLE");
                    u.setProviderId(googleId);

                    return userRepository.save(u);
                });

        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        Cookie cookie = new Cookie("refreshToken",
                refreshToken.getToken());

        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(cookie);

        response.sendRedirect(
                //"http://localhost:5173/oauth-success?token=" +
                  "https://canteen-ui-lilac.vercel.app/oauth-success?token=" +
                        accessToken +
                        "&role=" +
                        user.getRole() +
                        "&email=" + user.getEmail());
    }
}