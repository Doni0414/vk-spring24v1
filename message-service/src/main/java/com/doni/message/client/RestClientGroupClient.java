package com.doni.message.client;

import com.doni.message.entity.Group;
import com.doni.message.exception.UserIsNotGroupParticipantException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@RequiredArgsConstructor
public class RestClientGroupClient implements GroupClient {
    private final RestClient restClient;
    private static final ParameterizedTypeReference<Optional<Group>> GROUP_TYPE_REFERENCE = new ParameterizedTypeReference<>() {
    };

    @Override
    public Optional<Group> findGroup(Integer groupId) {
        try {
            return restClient.get()
                    .uri("/messenger-api/groups/%d".formatted(groupId))
                    .retrieve()
                    .body(GROUP_TYPE_REFERENCE);
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        }
    }
}
