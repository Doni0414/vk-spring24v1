package com.doni.messenger.dto;

import jakarta.validation.constraints.NotNull;

public record ChatCreateDto(
        @NotNull(message = "{messenger-api.chats.create.errors.user_id_is_null}")
        String userId
) {
}
