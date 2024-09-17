package com.doni.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.eureka.RestTemplateTimeoutProperties;
import org.springframework.cloud.netflix.eureka.http.DefaultEurekaClientHttpRequestFactorySupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;

import java.util.List;

@Configuration
@ConditionalOnProperty(name = "eureka.client.enabled", havingValue = "true")
public class DiscoveryBeans {

    @Bean
    public DefaultEurekaClientHttpRequestFactorySupplier defaultEurekaClientHttpRequestFactorySupplier(
            RestTemplateTimeoutProperties restTemplateTimeoutProperties,
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService authorizedClientService) {

        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);

        return new DefaultEurekaClientHttpRequestFactorySupplier(restTemplateTimeoutProperties,
                List.of((httpRequest, entityDetails, httpContext) -> {
                    if (!httpRequest.containsHeader(HttpHeaders.AUTHORIZATION)) {
                        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(OAuth2AuthorizeRequest
                                        .withClientRegistrationId("discovery")
                                        .principal("api-gateway")
                                        .build())
                                .block();

                        httpRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(authorizedClient.getAccessToken().getTokenValue()));
                    }
                }));
    }
}
