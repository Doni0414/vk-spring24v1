package com.doni.messenger.mapper;

import com.doni.messenger.dto.ChatReadDto;
import com.doni.messenger.entity.Chat;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    ChatReadDto entityToChatReadDto(Chat chat);
    List<ChatReadDto> entitiesToChatReadDtos(List<Chat> chats);
}
