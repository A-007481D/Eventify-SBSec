package com.eventify.security;

import com.eventify.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class TokenService {

    private static final String SECRET_KEY = "TO_CHANGE_LATER_USING_AES_128_16CHARS";
    private static final String ALGORITHM = "AES";
    private static final ObjectMapper mapper = new ObjectMapper();

    public String generateToken(User user) {
        try {
            UserInfo userInfo = new UserInfo(user.getId(), user.getEmail(), user.getRole());
            String json = mapper.writeValueAsString(userInfo);

            // Encrypt
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(json.getBytes());

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error generating token", e);
        }
    }



    public UserInfo decryptToken(String token) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decoded = Base64.getDecoder().decode(token);
            byte[] decrypted = cipher.doFinal(decoded);
            String json = new String(decrypted);

            return mapper.readValue(json, UserInfo.class);
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