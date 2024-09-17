package com.doni.message.service;

import com.doni.message.dto.GroupMessageReadDto;
import com.doni.message.entity.GroupMessage;

import java.util.List;
import java.util.Optional;

public interface GroupMessageService {
    List<GroupMessageReadDto> findGroupMessagesByGroupId(Integer groupId);

    GroupMessageReadDto createGroupMessage(String text, String userId, Integer groupId);

    Optional<GroupMessageReadDto> findGroupMessage(Long messageId);

    void updateGroupMessage(Long messageId, String text, String userId);

    void deleteGroupMessage(Long messageId, String userId);
}
