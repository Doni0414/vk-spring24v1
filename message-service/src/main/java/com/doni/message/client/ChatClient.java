package com.doni.message.client;

import com.doni.message.entity.Chat;

import java.util.Optional;

public interface ChatClient {
    Optional<Chat> findChat(Integer chatId);
}
