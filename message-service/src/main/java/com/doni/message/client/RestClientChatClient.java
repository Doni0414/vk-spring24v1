package com.doni.message.client;

import com.doni.message.entity.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@RequiredArgsConstructor
public class RestClientChatClient implements ChatClient {
    private final RestClient restClient;

    private static final ParameterizedTypeReference<Optional<Chat>> CHAT_TYPE_REFERENCE = new ParameterizedTypeReference<>() {
    };

    @Override
    public Optional<Chat> findChat(Integer chatId) {
        try {
            return restClient.get()
                    .uri("/messenger-api/chats/%d".formatted(chatId))
                    .retrieve()
                    .body(CHAT_TYPE_REFERENCE);
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        }
    }
}
