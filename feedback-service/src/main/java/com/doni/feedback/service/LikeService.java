package com.doni.feedback.service;

import com.doni.feedback.dto.LikeReadDto;
import com.doni.feedback.entity.Like;

import java.util.List;
import java.util.Optional;

public interface LikeService {

    List<LikeReadDto> findLikesByPublicationId(Integer publicationId);

    LikeReadDto createLike(Integer publicationId, String subject);

    LikeReadDto findLikeByPublicationIdAndUserId(Integer publicationId, String userId);

    void deleteLike(Integer publicationId, String userId, String currentUserId);
}
