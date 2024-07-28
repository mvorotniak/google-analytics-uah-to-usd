package com.demo.googleanalyticsuahtousd.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExchangeRateConfiguration {
    
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    
}
