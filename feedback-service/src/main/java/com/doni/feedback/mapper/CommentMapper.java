package com.doni.feedback.mapper;

import com.doni.feedback.dto.CommentCreateDto;
import com.doni.feedback.dto.CommentReadDto;
import com.doni.feedback.entity.Comment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentReadDto commentToCommentReadDto(Comment comment);

    List<CommentReadDto> commentsToCommentReadDtos(List<Comment> comments);

    Comment commentCreateDtoToComment(CommentCreateDto dto);
}
