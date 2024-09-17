package com.doni.messenger.service;

import com.doni.messenger.dto.ChatReadDto;
import com.doni.messenger.entity.Chat;
import com.doni.messenger.exception.ChatExistsException;
import com.doni.messenger.exception.UserIsNotChatParticipantException;
import com.doni.messenger.mapper.ChatMapper;
import com.doni.messenger.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultChatService implements ChatService {
    private final ChatMapper chatMapper;
    private final ChatRepository chatRepository;

    @Override
    public List<ChatReadDto> findAllChatsByUserId(String userId) {
        List<Chat> chats = chatRepository.findAllByUserId1OrUserId2(userId, userId);
        return chatMapper.entitiesToChatReadDtos(chats);
    }

    @Override
    public ChatReadDto createChat(String userId1, String userId2) {
        chatRepository.findByUserId1AndUserId2OrUserId2AndAndUserId1(userId1, userId2)
                .ifPresent(chat -> {
                    throw new ChatExistsException("messenger-api.chats.create.errors.chat_already_exists");
                });

        Chat chat = Chat.builder()
                .userId1(userId1)
                .userId2(userId2)
                .build();

        Chat saved = chatRepository.save(chat);
        return chatMapper.entityToChatReadDto(saved);
    }

    @Override
    public Optional<ChatReadDto> findChat(Integer chatId, String userId) {
        Optional<Chat> optionalChat = chatRepository.findById(chatId);
        optionalChat.ifPresent(chat -> {
            if (!chat.getUserId1().equals(userId) && !chat.getUserId2().equals(userId)) {
                throw new UserIsNotChatParticipantException("messenger-api.chats.errors.user_is_not_chat_participant");
            }
        });
        return optionalChat
                .map(chatMapper::entityToChatReadDto);
    }

    @Override
    public void deleteChat(Integer chatId, String userId) {
        chatRepository.findById(chatId)
                .ifPresent(chat -> {
                    if (!chat.getUserId1().equals(userId) && !chat.getUserId2().equals(userId)) {
                        throw new UserIsNotChatParticipantException("messenger-api.chats.errors.user_is_not_chat_participant");
                    }
                    chatRepository.deleteById(chatId);
                });
    }
}
