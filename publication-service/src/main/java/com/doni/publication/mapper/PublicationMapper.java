package com.doni.publication.mapper;

import com.doni.publication.dto.PublicationReadDto;
import com.doni.publication.entity.Publication;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PublicationMapper {

    PublicationReadDto publicationToPublicationReadDto(Publication publication);

    List<PublicationReadDto> publicationsToPublicationReadDtos(List<Publication> publications);
}
