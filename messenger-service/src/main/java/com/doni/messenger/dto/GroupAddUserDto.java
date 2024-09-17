package com.doni.messenger.dto;

import jakarta.validation.constraints.NotNull;

public record GroupAddUserDto(
        @NotNull(message = "{messenger-api.groups.add-user.errors.user_is_null}")
        String userId) {
}
