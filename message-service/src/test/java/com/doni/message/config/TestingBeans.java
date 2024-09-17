package com.doni.message.config;

import com.doni.message.client.RestClientChatClient;
import com.doni.message.client.RestClientGroupClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestClient;

import static org.mockito.Mockito.mock;

@Configuration
public class TestingBeans {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return mock(ClientRegistrationRepository.class);
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository() {
        return mock(OAuth2AuthorizedClientRepository.class);
    }

    @Bean
    @Primary
    public RestClientGroupClient testRestClientGroupClient(
            @Value("${vk.services.messenger.url}") String baseUrl) {
        return new RestClientGroupClient(RestClient.builder()
                .baseUrl(baseUrl)
                .build());
    }

    @Bean
    @Primary
    public RestClientChatClient testRestClientChatClient(
            @Value("${vk.services.messenger.url}") String baseUrl) {
        return new RestClientChatClient(RestClient.builder()
                .baseUrl(baseUrl)
                .build());
    }
}
