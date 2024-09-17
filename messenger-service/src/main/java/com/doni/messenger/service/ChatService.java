package com.doni.messenger.service;

import com.doni.messenger.dto.ChatReadDto;
import com.doni.messenger.entity.Chat;

import java.util.List;
import java.util.Optional;

public interface ChatService {
    List<ChatReadDto> findAllChatsByUserId(String userId);

    ChatReadDto createChat(String userId1, String userId2);

    Optional<ChatReadDto> findChat(Integer chatId, String userId);

    void deleteChat(Integer chatId, String userId);
}
