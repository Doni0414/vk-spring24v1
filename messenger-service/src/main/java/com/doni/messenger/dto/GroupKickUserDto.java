package com.doni.messenger.dto;

import jakarta.validation.constraints.NotNull;

public record GroupKickUserDto(
        @NotNull(message = "{messenger-api.groups.kick-user.errors.user_is_null}")
        String userId) {
}
