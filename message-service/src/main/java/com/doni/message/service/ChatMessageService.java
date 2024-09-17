package com.doni.message.service;

import com.doni.message.dto.ChatMessageReadDto;
import com.doni.message.entity.ChatMessage;

import java.util.List;
import java.util.Optional;

public interface ChatMessageService {
    List<ChatMessageReadDto> findChatMessagesByChatId(Integer chatId);

    ChatMessageReadDto createChatMessage(String text, String userId, Integer chatId);

    Optional<ChatMessageReadDto> findChatMessage(Long messageId);

    void updateChatMessage(Long messageId, String text, String userId);

    void deleteChatMessage(Long messageId, String userId);
}
