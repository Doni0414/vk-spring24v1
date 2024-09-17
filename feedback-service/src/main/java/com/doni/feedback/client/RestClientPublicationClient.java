package com.doni.feedback.client;

import com.doni.feedback.entity.Publication;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@RequiredArgsConstructor
public class RestClientPublicationClient implements PublicationClient {
    private final RestClient restClient;
    private static final ParameterizedTypeReference<Optional<Publication>> PUBLICATION_TYPE_REFERENCE = new ParameterizedTypeReference<Optional<Publication>>() {
    };

    @Override
    public Optional<Publication> findPublication(Integer publicationId) {
        try {
            return restClient.get()
                    .uri("/publication-api/publications/%d".formatted(publicationId))
                    .retrieve()
                    .body(PUBLICATION_TYPE_REFERENCE);
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        }
    }
}
