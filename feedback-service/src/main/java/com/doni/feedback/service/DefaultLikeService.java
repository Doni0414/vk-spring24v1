package com.doni.feedback.service;

import com.doni.feedback.client.PublicationClient;
import com.doni.feedback.dto.LikeReadDto;
import com.doni.feedback.entity.Like;
import com.doni.feedback.entity.Publication;
import com.doni.feedback.exception.LikeExistsException;
import com.doni.feedback.exception.UserIsNotOwnerException;
import com.doni.feedback.mapper.LikeMapper;
import com.doni.feedback.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultLikeService implements LikeService {
    private final LikeMapper likeMapper;
    private final LikeRepository likeRepository;
    private final PublicationClient publicationClient;

    @Override
    public List<LikeReadDto> findLikesByPublicationId(Integer publicationId) {
        publicationClient.findPublication(publicationId)
                .orElseThrow(() -> new NoSuchElementException("feedback-api.likes.read.errors.publication_is_not_found"));
        List<Like> likes = likeRepository.findAllByPublicationId(publicationId);
        return likeMapper.likesToLikeReadDtos(likes);
    }

    @Override
    @Transactional
    public LikeReadDto createLike(Integer publicationId, String userId) {
        publicationClient.findPublication(publicationId)
                .orElseThrow(() -> new NoSuchElementException("feedback-api.likes.create.errors.publication_is_not_found"));
        likeRepository.findByPublicationIdAndUserId(publicationId, userId)
                .ifPresent(like -> {
                    throw new LikeExistsException("feedback-api.likes.create.errors.user_has_already_like_publication");
                });

        Like like = Like.builder()
                .publicationId(publicationId)
                .userId(userId)
                .build();
        Like savedLike = likeRepository.save(like);
        return likeMapper.likeToLikeReadDto(savedLike);
    }

    @Override
    public LikeReadDto findLikeByPublicationIdAndUserId(Integer publicationId, String userId) {
        Like like = findLike(publicationId, userId);
        return likeMapper.likeToLikeReadDto(like);
    }

    private Like findLike(Integer publicationId, String userId) {
        return likeRepository.findByPublicationIdAndUserId(publicationId, userId)
                .orElseThrow(() -> new NoSuchElementException("feedback-api.likes.errors.like_is_not_found"));
    }

    @Override
    @Transactional
    public void deleteLike(Integer publicationId, String userId, String currentUserId) {
        Like like = findLike(publicationId, userId);
        if (like.getUserId().equals(currentUserId)) {
            likeRepository.deleteByPublicationIdAndUserId(publicationId, userId);
        } else {
            throw new UserIsNotOwnerException("feedback-api.likes.delete.errors.user_is_not_owner");
        }
    }
}
