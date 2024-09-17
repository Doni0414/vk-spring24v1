package com.doni.feedback.entity;

public record Publication(
        Integer id,
        String title,
        String description,
        String userId) {
}
