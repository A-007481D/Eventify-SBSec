package com.eventify.security;

import com.eventify.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import org.springframework.http.HttpStatus;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    private final TokenService tokenService;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    public TokenAuthenticationFilter(TokenService tokenService, 
                                     CustomUserDetailsService userDetailsService,
                                     TokenBlacklistService tokenBlacklistService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("Processing request: {} {}", method, requestURI);
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(requestURI, method)) {
            log.debug("Skipping authentication for public endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        if (token == null) {
            log.warn("No JWT token found in request");
            SecurityContextHolder.clearContext();
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing or invalid authorization token");
            return;
        }

        try {
            log.debug("Found JWT token in request");
            
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("Attempted to use blacklisted token");
                throw new BadCredentialsException("Token has been invalidated");
            }
            

            TokenService.UserInfo userInfo = tokenService.decryptToken(token);
            if (userInfo == null) {
                log.warn("Failed to decrypt or validate token");
                throw new BadCredentialsException("Invalid token");
            }
            
            log.debug("Successfully decrypted token for user: {}", userInfo.email);
            
            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(userInfo.email);
            
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, 
                null,
                userDetails.getAuthorities()
            );
            
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Successfully authenticated user: {}", userInfo.email);
            
        } catch (BadCredentialsException e) {
            log.warn("Authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            response.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
            return;
        } catch (Exception e) {
            log.error("Unexpected error during authentication: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred during authentication");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private boolean isPublicEndpoint(String uri, String method) {
        if (uri.startsWith("/api/public/")) {
            return true;
        }
        
        if (uri.equals("/api/auth/logout") && "POST".equalsIgnoreCase(method)) {
            return false;
        }
        
        return false;
    }
}
