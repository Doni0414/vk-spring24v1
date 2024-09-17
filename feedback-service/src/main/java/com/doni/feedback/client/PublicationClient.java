package com.doni.feedback.client;

import com.doni.feedback.entity.Publication;

import java.util.Optional;

public interface PublicationClient {
    Optional<Publication> findPublication(Integer publicationId);
}
