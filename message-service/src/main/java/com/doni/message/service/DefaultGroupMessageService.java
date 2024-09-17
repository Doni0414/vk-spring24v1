package com.doni.message.service;

import com.doni.message.client.GroupClient;
import com.doni.message.dto.GroupMessageReadDto;
import com.doni.message.entity.GroupMessage;
import com.doni.message.exception.UserIsNotGroupParticipantException;
import com.doni.message.exception.UserIsNotOwnerException;
import com.doni.message.mapper.GroupMessageMapper;
import com.doni.message.repository.GroupMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultGroupMessageService implements GroupMessageService {
    private final GroupClient groupClient;
    private final GroupMessageMapper groupMessageMapper;
    private final GroupMessageRepository groupMessageRepository;

    @Override
    public List<GroupMessageReadDto> findGroupMessagesByGroupId(Integer groupId) {
        try {
            groupClient.findGroup(groupId)
                    .orElseThrow(() -> new NoSuchElementException("message-api.group-messages.read.errors.group_is_not_found"));
        } catch (HttpClientErrorException.BadRequest exception) {
            throw new UserIsNotGroupParticipantException("message-api.group-messages.read.errors.user_is_not_group_participant");
        }
        List<GroupMessage> messages = groupMessageRepository.findAllByGroupId(groupId);
        return groupMessageMapper.groupMessagesToGroupMessagesReadDtos(messages);
    }

    @Override
    @Transactional
    public GroupMessageReadDto createGroupMessage(String text, String userId, Integer groupId) {
        try {
            groupClient.findGroup(groupId)
                    .orElseThrow(() -> new NoSuchElementException("message-api.group-messages.create.errors.group_is_not_found"));
        } catch (HttpClientErrorException.BadRequest exception) {
            throw new UserIsNotGroupParticipantException("message-api.group-messages.create.errors.user_is_not_group_participant");
        }
        GroupMessage groupMessage = GroupMessage.builder()
                .text(text)
                .authorId(userId)
                .groupId(groupId)
                .build();
        GroupMessage message = groupMessageRepository.save(groupMessage);
        return groupMessageMapper.groupMessageToGroupMessageReadDto(message);
    }

    @Override
    public Optional<GroupMessageReadDto> findGroupMessage(Long messageId) {
        Optional<GroupMessage> optionalGroupMessage = groupMessageRepository.findById(messageId);
        optionalGroupMessage.ifPresent(groupMessage -> {
            try {
                groupClient.findGroup(groupMessage.getGroupId());
            } catch (HttpClientErrorException.BadRequest exception) {
                throw new UserIsNotOwnerException("message-api.group-messages.read.errors.user_is_not_group_participant");
            }
        });
        return optionalGroupMessage
                .map(groupMessageMapper::groupMessageToGroupMessageReadDto);
    }

    @Override
    @Transactional
    public void updateGroupMessage(Long messageId, String text, String userId) {
        groupMessageRepository.findById(messageId)
                .ifPresent(groupMessage -> {
                    if (!groupMessage.getAuthorId().equals(userId)) {
                        throw new UserIsNotOwnerException("message-api.group-messages.update.errors.user_is_not_owner");
                    }
                    groupMessage.setText(text);
                });
    }

    @Override
    public void deleteGroupMessage(Long messageId, String userId) {
        groupMessageRepository.findById(messageId)
                .ifPresent(groupMessage -> {
                    if (!groupMessage.getAuthorId().equals(userId)) {
                        throw new UserIsNotOwnerException("message-api.group-messages.delete.errors.user_is_not_owner");
                    }
                    groupMessageRepository.deleteById(messageId);
                });
    }
}
