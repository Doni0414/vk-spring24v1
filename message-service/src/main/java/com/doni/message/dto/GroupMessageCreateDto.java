package com.doni.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GroupMessageCreateDto(
        @NotNull(message = "{message-api.group-messages.create.errors.text_is_null}")
        @NotBlank(message = "{message-api.group-messages.create.errors.text_is_blank}")
        @Size(min = 1, max = 2000, message = "{message-api.group-messages.create.errors.text_has_invalid_size}")
        String text,

        @NotNull(message = "{message-api.group-messages.create.errors.group_id_is_null}")
        Integer groupId) {
}
