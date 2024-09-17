package com.doni.message.mapper;

import com.doni.message.dto.GroupMessageReadDto;
import com.doni.message.entity.GroupMessage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroupMessageMapper {

    GroupMessageReadDto groupMessageToGroupMessageReadDto(GroupMessage groupMessage);

    List<GroupMessageReadDto> groupMessagesToGroupMessagesReadDtos(List<GroupMessage> groupMessages);
}
