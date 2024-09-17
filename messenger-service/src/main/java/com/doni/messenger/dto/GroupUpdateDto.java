package com.doni.messenger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GroupUpdateDto(
        @NotNull(message = "{messenger-api.groups.update.errors.title_is_null}")
        @NotBlank(message = "{messenger-api.groups.update.errors.title_is_blank}")
        @Size(min = 1, max = 100, message = "{messenger-api.groups.update.errors.title_has_invalid_size}")
        String title,
        @Size(max = 2000, message = "{messenger-api.groups.update.errors.description_has_invalid_size}")
        String description) {
}
