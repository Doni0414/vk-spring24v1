package com.doni.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatMessageCreateDto(
        @NotNull(message = "{message-api.chat-messages.create.errors.text_is_null}")
        @NotBlank(message = "{message-api.chat-messages.create.errors.text_is_blank}")
        @Size(min = 1, max = 2000, message = "{message-api.chat-messages.create.errors.text_has_invalid_size}")
        String text,
        @NotNull(message = "{message-api.chat-messages.create.errors.chat_id_is_null}")
        Integer chatId) {
}
