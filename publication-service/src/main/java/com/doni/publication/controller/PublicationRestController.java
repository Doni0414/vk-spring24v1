package com.doni.publication.controller;

import com.doni.publication.dto.PublicationCreateDto;
import com.doni.publication.dto.PublicationReadDto;
import com.doni.publication.dto.PublicationUpdateDto;
import com.doni.publication.service.PublicationService;
import com.doni.publication.exception.UserIsNotOwnerException;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/publication-api/publications/{publicationId:\\d+}")
@SecurityRequirement(name = "keycloak")
public class PublicationRestController {
    private final MessageSource messageSource;
    private final PublicationService publicationService;

    @GetMapping
    @Operation(
            summary = "Получение публикаций",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Публикация найдена",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(
                                            type = "object", implementation = PublicationReadDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Публикация не найдена",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(
                                            type = "object", implementation = ProblemDetail.class
                            ))
                    )
            }
    )
    public PublicationReadDto getPublication(@PathVariable("publicationId") Integer publicationId, JwtAuthenticationToken jwtAuthenticationToken) {
        return publicationService.findPublication(publicationId);
    }

    @PatchMapping
    @Operation(
            summary = "Обновление публикаций",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(
                            type = "object", implementation = PublicationCreateDto.class
                    ))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "204", description = "Публикация обновлена"
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Публикация не найдена",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(
                                    type = "object", implementation = ProblemDetail.class
                            ))
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Плохой запрос",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(
                                    type = "object", implementation = ProblemDetail.class
                            ))
                    )
            }
    )
    public ResponseEntity<Void> updatePublication(@PathVariable("publicationId") Integer publicationId,
                                                  JwtAuthenticationToken jwtAuthenticationToken,
                                                  @RequestBody @Valid PublicationUpdateDto payload,
                                                  BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException ex) {
                throw ex;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            publicationService.updatePublication(publicationId, payload.title(), payload.description(), jwtAuthenticationToken.getToken().getSubject());
            return ResponseEntity.noContent()
                    .build();
        }
    }

    @DeleteMapping
    @Operation(
            summary = "Удаление публикаций",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Публикация удалена"),
                    @ApiResponse(
                            responseCode = "404", description = "Публикация не найдена",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(
                                    type = "object", implementation = ProblemDetail.class
                            ))
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Плохой запрос",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(
                                    type = "object", implementation = ProblemDetail.class
                            ))
                    )
            }
    )
    public ResponseEntity<Void> deletePublication(@PathVariable("publicationId") Integer publicationId,
                                                  JwtAuthenticationToken jwtAuthenticationToken) {
        publicationService.deletePublication(publicationId, jwtAuthenticationToken.getToken().getSubject());
        return ResponseEntity.noContent()
                .build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNoSuchElementException(NoSuchElementException exception, Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                Objects.requireNonNull(messageSource.getMessage(exception.getMessage(), new Object[0],
                        exception.getMessage(), locale)));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }

    @ExceptionHandler(UserIsNotOwnerException.class)
    public ResponseEntity<ProblemDetail> handleUserIsNotOwnerException(UserIsNotOwnerException exception, Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                Objects.requireNonNull(messageSource.getMessage(exception.getMessage(), new Object[0],
                        exception.getMessage(), locale)));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }
}
