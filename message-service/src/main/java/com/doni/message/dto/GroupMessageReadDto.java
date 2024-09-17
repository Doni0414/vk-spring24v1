package com.doni.message.dto;

public record GroupMessageReadDto(
        Long id,
        String text,
        String authorId,
        Integer groupId) {
}
