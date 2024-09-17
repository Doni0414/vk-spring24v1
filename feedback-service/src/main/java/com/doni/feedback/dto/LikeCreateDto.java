package com.doni.feedback.dto;

import jakarta.validation.constraints.NotNull;

public record LikeCreateDto(
        @NotNull(message = "{feedback-api.likes.create.errors.publication_id_is_null}")
        Integer publicationId
) {
}
