package com.finalproject.ecommerce.ecommerce.iam.infrastructure.authorization.sfs.pipeline;

import com.finalproject.ecommerce.ecommerce.iam.infrastructure.authorization.sfs.model.UsernamePasswordAuthenticationTokenBuilder;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.tokens.jwt.BearerTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private final BearerTokenService tokenService;
    private final UserDetailsService userDetailsService;

    public BearerAuthorizationRequestFilter(BearerTokenService tokenService, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-resources") || path.startsWith("/webjars")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = tokenService.getBearerTokenFrom(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (tokenService.validateToken(token)) {

                String username = tokenService.getUsernameFromToken(token);
                var userDetails = userDetailsService.loadUserByUsername(username);

                var authentication = UsernamePasswordAuthenticationTokenBuilder.build(userDetails, request);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("User '{}' authenticated successfully", username);

            } else {
                log.warn("Invalid JWT token received");
            }

        } catch (Exception ex) {
            log.error("JWT authentication failed: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
