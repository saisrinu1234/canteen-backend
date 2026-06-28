package com.example.canteen.configuration;

import com.example.canteen.login.JwtService;
import com.example.canteen.login.TokenBlacklistService;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String ipAddress = request.getRemoteAddr();
        System.out.println("CLIENT IP ADDRESS: " + ipAddress);

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            if (tokenBlacklistService.isBlacklisted(token)) {

                response.setStatus(401);
                response.getWriter().write("Token revoked");

                return;
            }

            if (jwtService.validateToken(token)) {

                String email = jwtService.extractEmail(token);
                String role = jwtService.extractRole(token);
                System.out.println("ROLE FROM TOKEN: " + role);
                System.out.println("AUTHORITIES:" + role);

                // 🔥 IMPORTANT: must start with ROLE_
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(() -> role) // no prefix
                );
                System.out.println("ROLE FROM TOKEN: " + role);

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}