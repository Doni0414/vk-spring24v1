package com.doni.messenger.dto;

import java.util.Set;

public record GroupReadDto(
        Integer id,
        String title,
        String description,
        String ownerId,
        Set<GroupMemberReadDto> groupMembers) {
}
