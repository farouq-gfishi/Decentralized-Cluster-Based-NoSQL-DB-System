package com.atypon.nosql.affinitynode.configuration;

import com.atypon.nosql.affinitynode.indexing.HashIndexing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NodeConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public HttpHeaders headers() {
        return new HttpHeaders();
    }

    @Bean
    public HashIndexing hashIndexing() {
        return new HashIndexing();
    }

}
