package com.doni.feedback.service;

import com.doni.feedback.dto.CommentReadDto;
import com.doni.feedback.entity.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    List<CommentReadDto> findCommentsByPublicationId(Integer publicationId);

    CommentReadDto createComment(String text, Integer publicationId, String userId);

    CommentReadDto findComment(Integer commentId);

    void updateComment(Integer commentId, String text, String userId);

    void deleteComment(Integer commentId, String userId);
}
