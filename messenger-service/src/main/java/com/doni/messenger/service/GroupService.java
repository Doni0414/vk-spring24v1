package com.doni.messenger.service;

import com.doni.messenger.dto.GroupReadDto;
import com.doni.messenger.entity.Group;

import java.util.List;
import java.util.Optional;

public interface GroupService {
    List<GroupReadDto> findAllByUserId(String userId);

    GroupReadDto createGroup(String title, String description, String userId);

    Optional<GroupReadDto> findGroup(Integer groupId, String userId);

    void updateGroup(Integer groupId, String title, String description, String userId);

    void deleteGroup(Integer groupId, String userId);

    void addUser(Integer groupId, String userId, String ownerId);

    void kickUser(Integer groupId, String userId, String ownerId);

    void leaveGroup(Integer groupId, String userId);
}
