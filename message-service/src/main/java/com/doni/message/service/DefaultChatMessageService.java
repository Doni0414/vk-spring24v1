package com.doni.message.service;

import com.doni.message.client.ChatClient;
import com.doni.message.dto.ChatMessageReadDto;
import com.doni.message.entity.ChatMessage;
import com.doni.message.exception.UserIsNotChatParticipantException;
import com.doni.message.exception.UserIsNotOwnerException;
import com.doni.message.mapper.ChatMessageMapper;
import com.doni.message.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultChatMessageService implements ChatMessageService {
    private final ChatClient chatClient;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatMessageRepository chatMessageRepository;

    @Override
    public List<ChatMessageReadDto> findChatMessagesByChatId(Integer chatId) {
        try {
            chatClient.findChat(chatId)
                    .orElseThrow(() -> new NoSuchElementException("message-api.chat-messages.read.errors.chat_is_not_found"));
        } catch (HttpClientErrorException.BadRequest exception) {
            throw new UserIsNotChatParticipantException("message-api.chat-messages.read.errors.user_is_not_chat_participant");
        }
        List<ChatMessage> messages = chatMessageRepository.findAllByChatId(chatId);
        return chatMessageMapper.chatMessagesToChatMessageReadDtos(messages);
    }

    @Override
    @Transactional
    public ChatMessageReadDto createChatMessage(String text, String userId, Integer chatId) {
        try {
            chatClient.findChat(chatId)
                    .orElseThrow(() -> new NoSuchElementException("message-api.chat-messages.create.errors.chat_is_not_found"));
        } catch (HttpClientErrorException.BadRequest exception) {
            throw new UserIsNotChatParticipantException("message-api.chat-messages.read.errors.user_is_not_chat_participant");
        }
        ChatMessage chatMessage = ChatMessage.builder()
                .text(text)
                .authorId(userId)
                .chatId(chatId)
                .build();
        ChatMessage message = chatMessageRepository.save(chatMessage);
        return chatMessageMapper.chatMessageToChatMessageReadDto(message);
    }

    @Override
    public Optional<ChatMessageReadDto> findChatMessage(Long messageId) {
        Optional<ChatMessage> optionalChatMessage = chatMessageRepository.findById(messageId);
        optionalChatMessage.ifPresent(chatMessage -> {
            try {
                chatClient.findChat(chatMessage.getChatId());
            } catch (HttpClientErrorException.BadRequest exception) {
                throw new UserIsNotChatParticipantException("message-api.chat-messages.read.errors.user_is_not_chat_participant");
            }
        });
        return optionalChatMessage
                .map(chatMessageMapper::chatMessageToChatMessageReadDto);
    }

    @Override
    @Transactional
    public void updateChatMessage(Long messageId, String text, String userId) {
        chatMessageRepository.findById(messageId)
                .ifPresent(chatMessage -> {
                    if (!chatMessage.getAuthorId().equals(userId)) {
                        throw new UserIsNotOwnerException("message-api.chat-messages.update.errors.user_is_not_owner");
                    }
                    chatMessage.setText(text);
                });
    }

    @Override
    @Transactional
    public void deleteChatMessage(Long messageId, String userId) {
        chatMessageRepository.findById(messageId)
                .ifPresent(chatMessage -> {
                    if (!chatMessage.getAuthorId().equals(userId)) {
                        throw new UserIsNotOwnerException("message-api.chat-messages.delete.errors.user_is_not_owner");
                    }
                    chatMessageRepository.deleteById(messageId);
                });
    }
}
