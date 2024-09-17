package com.doni.message.entity;

public record Group(
        Integer id,
        String title,
        String description,
        String ownerId) {
}
