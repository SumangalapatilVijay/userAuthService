package com.example.authservice.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.util.Base64;

@Configuration
public class SecurityConfig {
    @Bean
    public SecretKey secretKey() {
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        byte[] encodedKey = secretKey.getEncoded();
        String encodedKeyBase64 = Base64.getEncoder().encodeToString(encodedKey);

        System.out.println("Secret Key (Base64): " + encodedKeyBase64);

        return secretKey;
    }

}
