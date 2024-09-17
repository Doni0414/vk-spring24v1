package com.doni.message.mapper;

import com.doni.message.dto.ChatMessageReadDto;
import com.doni.message.entity.ChatMessage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    ChatMessageReadDto chatMessageToChatMessageReadDto(ChatMessage chatMessage);

    List<ChatMessageReadDto> chatMessagesToChatMessageReadDtos(List<ChatMessage> chatMessages);
}
