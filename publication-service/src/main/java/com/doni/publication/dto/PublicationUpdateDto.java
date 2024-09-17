package com.doni.publication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PublicationUpdateDto(
        @NotNull(message = "{publication-api.publications.create.errors.title_is_null}")
        @NotBlank(message = "{publication-api.publications.create.errors.title_is_blank}")
        @Size(min = 3, max = 200, message = "{publication-api.publications.create.errors.title_size_is_invalid}")
        String title,

        @Size(max = 2000, message = "{publication-api.publications.create.errors.description_size_is_invalid}")
        String description) {
}
