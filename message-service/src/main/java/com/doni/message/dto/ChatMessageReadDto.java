package com.doni.message.dto;

public record ChatMessageReadDto(
        Long id,
        String text,
        String authorId,
        Integer chatId) {
}
