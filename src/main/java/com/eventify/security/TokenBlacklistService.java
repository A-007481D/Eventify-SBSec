package com.eventify.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {
    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);
    private static final int MAX_TOKENS = 10000; // Prevent memory issues
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();


    public void blacklistToken(String token, long expirationTime) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Attempted to blacklist null or empty token");
            return;
        }
        
        if (blacklistedTokens.size() >= MAX_TOKENS) {
            log.warn("Blacklist is full ({} tokens), clearing to prevent memory issues", MAX_TOKENS);
            blacklistedTokens.clear();
        }
        
        // log the first few chars security
        String tokenPreview = token.length() > 8 ?
            token.substring(0, 4) + "..." + token.substring(token.length() - 4) : 
            "[short-token]";
            
        if (blacklistedTokens.add(token)) {
            log.info("Token blacklisted: {}", tokenPreview);
        } else {
            log.debug("Token already in blacklist: {}", tokenPreview);
        }
        
        log.debug("Total blacklisted tokens: {}", blacklistedTokens.size());
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Null or empty token provided for blacklist check");
            return true;
        }
        
        boolean isBlacklisted = blacklistedTokens.contains(token);
        if (isBlacklisted) {
            String tokenPreview = token.length() > 8 ? 
                token.substring(0, 4) + "..." + token.substring(token.length() - 4) : 
                "[short-token]";
            log.warn("Blacklisted token access attempt: {}", tokenPreview);
        }
        
        log.debug("Token blacklist check: {}", isBlacklisted ? "BLACKLISTED" : "CLEAN");
        return isBlacklisted;
    }

    @Scheduled(fixedRate = 3600000) // run every hour
    public void cleanupExpiredTokens() {
        int initialSize = blacklistedTokens.size();
        log.info("Running token cleanup, current blacklist size: {}", initialSize);
        
        // reach max size -> clear the blacklist
        if (initialSize > MAX_TOKENS * 0.9) {
            int cleared = initialSize;
            blacklistedTokens.clear();
            log.warn("Cleared all {} tokens from blacklist to prevent memory issues", cleared);
        } else if (initialSize > 0) {
            log.debug("Current blacklist status: {} tokens, {}% of capacity",
                     initialSize, 
                     (initialSize * 100) / MAX_TOKENS);
        }
        
        log.info("Token cleanup complete. New blacklist size: {} (was: {})", 
                blacklistedTokens.size(), initialSize);
    }
}
