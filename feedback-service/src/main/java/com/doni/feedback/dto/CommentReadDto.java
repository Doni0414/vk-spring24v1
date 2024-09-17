package com.doni.feedback.dto;

public record CommentReadDto(
        Integer id,
        String text,
        Integer publicationId,
        String userId) {
}
