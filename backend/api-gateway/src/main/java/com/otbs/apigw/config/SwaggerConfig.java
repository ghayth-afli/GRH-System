package com.otbs.apigw.config;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private SwaggerUiConfigProperties swaggerUiConfigProperties;

    @PostConstruct
    public void configureSwaggerUi() {
        Set<SwaggerUiConfigProperties.SwaggerUrl> urls = discoveryClient.getServices().stream()
                .filter(service -> !service.equals("eureka-server"))
                .map(service -> {
                    SwaggerUiConfigProperties.SwaggerUrl url = new SwaggerUiConfigProperties.SwaggerUrl();
                    url.setName(service);
                    url.setUrl("/" + service + "/v3/api-docs");
                    return url;
                })
                .collect(Collectors.toSet());
        swaggerUiConfigProperties.setUrls(urls);
    }
}
