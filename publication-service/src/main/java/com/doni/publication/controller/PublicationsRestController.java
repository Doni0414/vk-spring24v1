package com.doni.publication.controller;

import com.doni.publication.dto.PublicationCreateDto;
import com.doni.publication.dto.PublicationReadDto;
import com.doni.publication.entity.Publication;
import com.doni.publication.service.PublicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/publication-api/publications")
@SecurityRequirement(name = "keycloak")
public class  PublicationsRestController {
    private final PublicationService publicationService;

    @GetMapping
    @Operation(
            summary = "Получение всех существующих публикации",
            responses = @ApiResponse(responseCode = "200", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = PublicationReadDto.class))
            ))
    )
    public List<PublicationReadDto> getAllPublications() {
        return publicationService.findAllPublications();
    }

    @GetMapping("/by-user-id/{userId}")
    @Operation(
            summary = "Получение публикации пользователя",
            responses = @ApiResponse(responseCode = "200", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = PublicationReadDto.class))
            ))
    )
    public List<PublicationReadDto> getAllPublicationsByUser(@PathVariable("userId") String userId) {
        return publicationService.findAllPublicationsByUserId(userId);
    }

    @PostMapping
    @Operation(
            summary = "Создание публикаций",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    type = "object",
                                    implementation = PublicationCreateDto.class
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201", description = "Успешное создание публикаций",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(type = "object", implementation = PublicationReadDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Плохой запрос",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(type = "object", implementation = ProblemDetail.class)
                            )
                    )
            }
    )
    public ResponseEntity<PublicationReadDto> createPublication(JwtAuthenticationToken jwtAuthenticationToken,
                                                         @RequestBody @Valid PublicationCreateDto payload,
                                                         BindingResult bindingResult,
                                                         UriComponentsBuilder uriComponentsBuilder) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException ex) {
                throw ex;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            PublicationReadDto publicationReadDto = publicationService.createPublication(payload.title(), payload.description(), jwtAuthenticationToken.getToken().getSubject());
            return ResponseEntity.created(uriComponentsBuilder
                    .replacePath("/publication-api/publications/{publicationId}")
                    .build(Map.of("publicationId", publicationReadDto.id())))
                    .body(publicationReadDto);
        }
    }
}
