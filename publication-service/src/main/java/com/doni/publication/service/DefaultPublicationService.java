package com.doni.publication.service;

import com.doni.publication.dto.PublicationReadDto;
import com.doni.publication.entity.Publication;
import com.doni.publication.mapper.PublicationMapper;
import com.doni.publication.repository.PublicationRepository;
import com.doni.publication.exception.UserIsNotOwnerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultPublicationService implements PublicationService {
    private final PublicationMapper publicationMapper;
    private final PublicationRepository publicationRepository;

    @Override
    public List<PublicationReadDto> findAllPublications() {
        List<Publication> publications = publicationRepository.findAll();
        return publicationMapper.publicationsToPublicationReadDtos(publications);
    }

    @Override
    @Transactional
    public PublicationReadDto createPublication(String title, String description, String userId) {
        System.out.println("UserId: " + userId);
        Publication publication = Publication.builder()
                .title(title)
                .description(description)
                .userId(userId)
                .build();
        Publication savedPublication = publicationRepository.save(publication);
        return publicationMapper.publicationToPublicationReadDto(savedPublication);
    }

    @Override
    public PublicationReadDto findPublication(Integer publicationId) {
        Publication publication = findPublicationById(publicationId);
        return publicationMapper.publicationToPublicationReadDto(publication);
    }

    private Publication findPublicationById(Integer publicationId) {
        return publicationRepository.findById(publicationId)
                .orElseThrow(() -> new NoSuchElementException("publication-api.publications.errors.publication_is_not_found"));
    }

    @Override
    @Transactional
    public void updatePublication(Integer publicationId, String title, String description, String userId) {
        Publication publication = findPublicationById(publicationId);
        if (!publication.getUserId().equals(userId)) {
            throw new UserIsNotOwnerException("publication-api.publications.update.errors.user_is_not_owner");
        }
        publication.setTitle(title);
        publication.setDescription(description);

    }

    @Override
    @Transactional
    public void deletePublication(Integer publicationId, String userId) {
        Publication publication = findPublicationById(publicationId);
        if (!publication.getUserId().equals(userId)) {
            throw new UserIsNotOwnerException("publication-api.publications.delete.errors.user_is_not_owner");
        }
        publicationRepository.deleteById(publicationId);
    }

    @Override
    public List<PublicationReadDto> findAllPublicationsByUserId(String userId) {
        List<Publication> publications = publicationRepository.findAllByUserId(userId);
        return publicationMapper.publicationsToPublicationReadDtos(publications);
    }
}
