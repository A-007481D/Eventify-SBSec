package com.eventify.controller;

import com.eventify.dto.ErrorResponse;
import com.eventify.security.TokenBlacklistService;
import com.eventify.security.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final TokenBlacklistService tokenBlacklistService;
    private final TokenService tokenService;
    private static final String BEARER = "Bearer ";

    public AuthController(TokenBlacklistService tokenBlacklistService, TokenService tokenService) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.tokenService = tokenService;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value(),
                            "Bad Request",
                            "Authorization header is missing or invalid",
                            "/api/auth/logout"
                    ));
        }
        try {
            String token = authHeader.substring(BEARER.length());
            
            if (tokenService.decryptToken(token) == null) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                "Invalid token",
                                "/api/auth/logout"
                        ));
            }
            
            tokenBlacklistService.blacklistToken(token, 0);
            SecurityContextHolder.clearContext();
            
            return ResponseEntity.ok().body("{\"message\":\"Logged out successfully\"}");
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            LocalDateTime.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Internal Server Error",
                            "An error occurred during logout",
                            "/api/auth/logout"
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
