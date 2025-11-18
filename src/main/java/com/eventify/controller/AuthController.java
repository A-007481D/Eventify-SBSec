package com.eventify.controller;

import com.eventify.dto.ErrorResponse;
import com.eventify.security.TokenBlacklistService;
import com.eventify.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final TokenBlacklistService tokenBlacklistService;
    private final TokenService tokenService;
    private static final String BEARER = "Bearer ";

    public AuthController(TokenBlacklistService tokenBlacklistService, TokenService tokenService) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.tokenService = tokenService;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {
        
        log.debug("Logout request received");
        
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            log.warn("Invalid or missing Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            LocalDateTime.now(),
                            HttpStatus.UNAUTHORIZED.value(),
                            "Unauthorized",
                            "Missing or invalid Authorization header",
                            request.getRequestURI()
                    ));
        }

        try {
            String token = authHeader.substring(BEARER.length());
            log.debug("Extracted token for logout");
            
            TokenService.UserInfo userInfo = tokenService.decryptToken(token);
            if (userInfo == null) {
                log.warn("Invalid token provided for logout");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                "Invalid token",
                                request.getRequestURI()
                        ));
            }
            
            log.debug("Blacklisting token for user: {}", userInfo.email);

            tokenBlacklistService.blacklistToken(token, 0);

            SecurityContextHolder.clearContext();
            log.info("User {} logged out successfully", userInfo.email);
            
            return ResponseEntity.ok(Collections.singletonMap("message", "Logged out successfully"));
            
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Internal Server Error",
                            "An error occurred during logout",
                            request.getRequestURI()
                    ));
        }
    }
    
    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAllDevices(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok("{\"message\":\"Logged out from all devices\"}");
        }
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        "No active session found",
                        "/api/auth/logout-all"
                ));
    }
}
