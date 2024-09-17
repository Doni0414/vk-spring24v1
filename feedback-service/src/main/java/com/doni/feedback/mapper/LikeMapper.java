package com.doni.feedback.mapper;

import com.doni.feedback.dto.LikeCreateDto;
import com.doni.feedback.dto.LikeReadDto;
import com.doni.feedback.entity.Like;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LikeMapper {

    LikeReadDto likeToLikeReadDto(Like like);

    List<LikeReadDto> likesToLikeReadDtos(List<Like> likes);

    Like likeCreateDtoToLike(LikeCreateDto dto);
}
