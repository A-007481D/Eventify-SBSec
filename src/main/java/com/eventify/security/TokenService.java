package com.eventify.security;

import com.eventify.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class TokenService {
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private static final String SECRET_KEY = "MySecretKey12345"; // 16 characters for AES-128
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private final ObjectMapper objectMapper;

    public TokenService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String generateToken(User user) {
        log.debug("Generating token for user: {} with role: {}", user.getEmail(), user.getRole());
        try {
            String role = user.getRole().startsWith("ROLE_") ? user.getRole() : "ROLE_" + user.getRole();
            UserInfo userInfo = new UserInfo(user.getId(), user.getEmail(), role);
            log.debug("Created UserInfo: id={}, email={}, role={}", userInfo.id, userInfo.email, userInfo.role);
            
            String json = objectMapper.writeValueAsString(userInfo);
            log.debug("JSON to encrypt: {}", json);

            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(json.getBytes());
            
            String token = Base64.getEncoder().encodeToString(encrypted);
            log.debug("Successfully generated token for user: {}, role: {}", user.getEmail(), role);
            
            return token;
        } catch (Exception e) {
            log.error("Error generating token for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Error generating token: " + e.getMessage(), e);
        }
    }

    public UserInfo decryptToken(String token) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decoded = Base64.getDecoder().decode(token);
            byte[] decrypted = cipher.doFinal(decoded);
            String json = new String(decrypted);
            
            UserInfo userInfo = objectMapper.readValue(json, UserInfo.class);
            
            if (userInfo.role != null && !userInfo.role.startsWith("ROLE_")) {
                userInfo.role = "ROLE_" + userInfo.role;
                log.debug("Added ROLE_ prefix to role: {}", userInfo.role);
            }
            
            log.debug("Decrypted token for user: {}, role: {}", userInfo.email, userInfo.role);
            return userInfo;
        } catch (Exception e) {
            return null;
        }
    }

    public static class UserInfo {
        public Long id;
        public String email;
        public String role;

        public UserInfo() {
        }

        public UserInfo(Long id, String email, String role) {
            this.id = id;
            this.email = email;
            this.role = role;
        }
    }
}