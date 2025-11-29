package com.example.baoNgoCv.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import vn.payos.PayOS;

@Configuration
public class PayOSConfig {
    @Value("${PAYOS_CLIENT_ID}")
    private String clientId;

    @Value("${PAYOS_API_KEY}")
    private String apiKey;

    @Value("${PAYOS_CHECKSUM_KEY}")
    private String checksumKey;

    @Bean
    @Primary
    public PayOS payOS() {
        return new PayOS(clientId, apiKey, checksumKey);
    }
}
