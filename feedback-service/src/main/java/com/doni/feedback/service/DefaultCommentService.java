package com.doni.feedback.service;

import com.doni.feedback.client.PublicationClient;
import com.doni.feedback.dto.CommentReadDto;
import com.doni.feedback.entity.Comment;
import com.doni.feedback.exception.UserIsNotOwnerException;
import com.doni.feedback.mapper.CommentMapper;
import com.doni.feedback.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DefaultCommentService implements CommentService {
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final PublicationClient publicationClient;

    @Override
    public List<CommentReadDto> findCommentsByPublicationId(Integer publicationId) {
        publicationClient.findPublication(publicationId)
                .orElseThrow(() -> new NoSuchElementException("feedback-api.comments.read.errors.publication_is_not_found"));
        List<Comment> comments = commentRepository.findAllByPublicationId(publicationId);
        return commentMapper.commentsToCommentReadDtos(comments);
    }

    @Override
    @Transactional
    public CommentReadDto createComment(String text, Integer publicationId, String userId) {
        publicationClient.findPublication(publicationId)
                .orElseThrow(() -> new NoSuchElementException("feedback-api.comments.create.errors.publication_is_not_found"));
        Comment comment = Comment.builder()
                .text(text)
                .publicationId(publicationId)
                .userId(userId)
                .build();
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.commentToCommentReadDto(savedComment);
    }

    @Override
    public CommentReadDto findComment(Integer commentId) {
        Comment comment = findCommentById(commentId);
        return commentMapper.commentToCommentReadDto(comment);
    }

    private Comment findCommentById(Integer commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("feedback-api.comments.errors.comment_is_not_found"));
    }

    @Override
    @Transactional
    public void updateComment(Integer commentId, String text, String userId) {
        Comment comment = findCommentById(commentId);
        if (!comment.getUserId().equals(userId)) {
            throw new UserIsNotOwnerException("feedback-api.comments.update.errors.user_is_not_owner");
        }
        comment.setText(text);
    }

    @Override
    public void deleteComment(Integer commentId, String userId) {
        Comment comment = findCommentById(commentId);
        if (!comment.getUserId().equals(userId)) {
            throw new UserIsNotOwnerException("feedback-api.comments.delete.errors.user_is_not_owner");
        }
        commentRepository.deleteById(commentId);
    }
}
