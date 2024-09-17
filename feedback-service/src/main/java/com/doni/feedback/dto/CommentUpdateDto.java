package com.doni.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentUpdateDto(
        @NotNull(message = "{feedback-api.comments.create.errors.text_is_null}")
        @NotBlank(message = "{feedback-api.comments.create.errors.text_is_blank}")
        @Size(min = 1, max = 2000, message = "{feedback-api.comments.create.errors.text_has_invalid_size}")
        String text) {
}
