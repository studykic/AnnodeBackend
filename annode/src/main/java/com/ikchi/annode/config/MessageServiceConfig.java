package com.ikchi.annode.config;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageServiceConfig {

    @Value("${app.sms.api.key}")
    private String apiKey;

    @Value("${app.sms.api.secretKey}")
    private String apiSecretKey;


    @Bean
    public DefaultMessageService defaultMessageService() {

        return NurigoApp.INSTANCE.initialize(apiKey, apiSecretKey, "https://api.coolsms.co.kr");
    }

}
