package com.example.bankcards.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class EncryptionConfig {

    @Bean
    public SecretKey cardEncryptionKey(@Value("${card.encryption.key.base64}") String base64) {
        return new SecretKeySpec(Base64.getDecoder().decode(base64), "AES");
    }
}
