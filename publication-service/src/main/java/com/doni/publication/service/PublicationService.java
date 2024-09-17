package com.doni.publication.service;

import com.doni.publication.dto.PublicationReadDto;
import com.doni.publication.entity.Publication;

import java.util.List;
import java.util.Optional;

public interface PublicationService {
    List<PublicationReadDto> findAllPublications();

    PublicationReadDto createPublication(String title, String description, String userId);

    PublicationReadDto findPublication(Integer publicationId);

    void updatePublication(Integer publicationId, String title, String description, String userId);

    void deletePublication(Integer publicationId, String userId);

    List<PublicationReadDto> findAllPublicationsByUserId(String userId);
}
