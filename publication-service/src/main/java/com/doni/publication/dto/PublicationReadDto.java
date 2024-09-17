package com.doni.publication.dto;

public record PublicationReadDto(
        Integer id,
        String title,
        String description,
        String userId) {
}
