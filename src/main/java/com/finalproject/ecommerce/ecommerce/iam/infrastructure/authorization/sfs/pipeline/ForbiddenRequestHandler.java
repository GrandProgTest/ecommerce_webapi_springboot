package com.finalproject.ecommerce.ecommerce.iam.infrastructure.authorization.sfs.pipeline;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class ForbiddenRequestHandler implements AccessDeniedHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForbiddenRequestHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        LOGGER.error("Forbidden request: User does not have the required permissions to access this resource. Details: {}", accessDeniedException.getMessage());
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: You don't have permission to access this resource");
    }
}
