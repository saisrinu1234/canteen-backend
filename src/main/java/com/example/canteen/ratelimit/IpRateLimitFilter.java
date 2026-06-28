package com.example.canteen.ratelimit;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class IpRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public IpRateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only protect login endpoint
        if (path.equals("/auth/login")) {

            String ip = getClientIp(request);

            if (!rateLimitService.allowRequest(ip)) {

                response.setStatus(429);
                response.setContentType("application/json");

                response.getWriter().write("""
                    {
                      "error":"Too many login attempts",
                      "status":429
                    }
                    """);

                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {

        String forwardedFor =
                request.getHeader("X-Forwarded-For");

        if (forwardedFor != null &&
                !forwardedFor.isBlank()) {

            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}