package com.otbs.apigw.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private final DiscoveryClient discoveryClient;
    private final SwaggerUiConfigProperties swaggerUiConfigProperties;
    @PostConstruct
    public void configureSwaggerUi() {
        Set<SwaggerUiConfigProperties.SwaggerUrl> urls = discoveryClient.getServices().stream()
                .filter(service -> !"eureka-server".equalsIgnoreCase(service))
                .map(service -> {
                    String urlPath = String.format("/%s/v3/api-docs", service);
                    return new SwaggerUiConfigProperties.SwaggerUrl(service, urlPath, null);
                })
                .collect(Collectors.toSet());

        swaggerUiConfigProperties.setUrls(urls);
    }
}
