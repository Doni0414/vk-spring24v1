package com.doni.feedback.controller;

import com.doni.feedback.dto.CommentCreateDto;
import com.doni.feedback.dto.CommentReadDto;
import com.doni.feedback.entity.Comment;
import com.doni.feedback.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.message.Message;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/feedback-api/comments")
@SecurityRequirement(name = "keycloak")
public class CommentsRestController {
    private final CommentService commentService;
    private final MessageSource messageSource;

    @GetMapping("/by-publication-id/{publicationId:\\d+}")
    @Operation(
            summary = "Получение комментариев публикаций",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Список комментариев публикаций"
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Публикация не найдена",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Публикация не найдена",
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Публикация не найдена\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public List<CommentReadDto> findCommentsByPublicationId(@PathVariable("publicationId") Integer publicationId) {
        return commentService.findCommentsByPublicationId(publicationId);
    }

    @PostMapping
    @Operation(
            summary = "Создание комментария к публикаций",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(
                                    name = "Запрос 1", summary = "Валидный запрос", description = "Полезная нагрузка запроса валидна",
                                    value = "{\n" +
                                            "  \"text\": \"Love it!\",\n" +
                                            "  \"publicationId\": 1\n" +
                                            "}"
                            ),
                            @ExampleObject(
                                    name = "Запрос 2", summary = "Инвалидный запрос", description = "Полезная нагрузка содержит ошибки. Текст равен null",
                                    value = "{\n" +
                                            "  \"text\": null,\n" +
                                            "  \"publicationId\": 1\n" +
                                            "}"
                            )
                    })
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201", description = "Комментарий создан",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"id\": 1,\n" +
                                                    "  \"text\": \"Love it!\",\n" +
                                                    "  \"publicationId\": 1,\n" +
                                                    "  \"userId\": \"j.daniels\"\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Плохой запрос",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Bad Request\",\n" +
                                                    "  \"status\": 400,\n" +
                                                    "  \"detail\": \"Плохой запрос\",\n" +
                                                    "  \"instance\": \"/feedback-api/comments\",\n" +
                                                    "  \"errors\": [\n" +
                                                    "    \"Текст комментария должен быть указан\",\n" +
                                                    "    \"Комментарий не может быть пустым\"\n" +
                                                    "  ]\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Публикация не найдена",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Публикация не найдена",
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"При созданий комментария публикация не была найдена\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<CommentReadDto> createComment(@RequestBody @Valid CommentCreateDto payload,
                                                 BindingResult bindingResult,
                                                 JwtAuthenticationToken jwtAuthenticationToken,
                                                 UriComponentsBuilder uriComponentsBuilder) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException ex) {
                throw ex;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            CommentReadDto comment = commentService.createComment(payload.text(), payload.publicationId(), jwtAuthenticationToken.getToken().getSubject());
            return ResponseEntity.created(uriComponentsBuilder.replacePath("/feedback-api/comments/{commentId}")
                    .build(Map.of("commentId", comment.id())))
                    .body(comment);
        }
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNoSuchElementException(NoSuchElementException exception,
                                                                      Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                Objects.requireNonNull(messageSource
                        .getMessage(exception.getMessage(), new Object[0],
                        exception.getMessage(), locale))
                );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }
}
