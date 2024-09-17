package com.doni.messenger.mapper;

import com.doni.messenger.dto.GroupReadDto;
import com.doni.messenger.entity.Group;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    GroupReadDto entityToGroupReadDto(Group group);
    List<GroupReadDto> entitiesToGroupReadDtos(List<Group> groups);
}
