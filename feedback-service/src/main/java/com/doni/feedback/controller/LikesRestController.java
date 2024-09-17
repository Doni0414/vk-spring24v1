package com.doni.feedback.controller;

import com.doni.feedback.dto.LikeCreateDto;
import com.doni.feedback.dto.LikeReadDto;
import com.doni.feedback.entity.Like;
import com.doni.feedback.exception.LikeExistsException;
import com.doni.feedback.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/feedback-api/likes")
@SecurityRequirement(name = "keycloak")
public class LikesRestController {
    private final LikeService likeService;
    private final MessageSource messageSource;

    @GetMapping("/by-publication-id/{publicationId:\\d+}")
    @Operation(
            summary = "Получение лайков публикаций",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Список лайков",
                            useReturnTypeSchema = true
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
    public List<LikeReadDto> getLikesByPublicationId(@PathVariable("publicationId") Integer publicationId) {
        return likeService.findLikesByPublicationId(publicationId);
    }

    @PostMapping
    @Operation(
            summary = "Создание лайка",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "Valid request body", summary = "Валидное тело запроса",
                                            value = "{\n" +
                                                    "  \"publicationId\": 1\n" +
                                                    "}"
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201", description = "Лайк создан",
                            useReturnTypeSchema = true
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Плохой запрос",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = {
                                            @ExampleObject(
                                                    name = "Тело запроса содержит ошибки",
                                                    value = "{\n" +
                                                            "  \"type\": \"about:blank\",\n" +
                                                            "  \"title\": \"Bad Request\",\n" +
                                                            "  \"status\": 400,\n" +
                                                            "  \"detail\": \"Плохой запрос\",\n" +
                                                            "  \"instance\": \"/feedback-api/likes\",\n" +
                                                            "  \"errors\": [\n" +
                                                            "    \"Публикация лайка должна быть указана\"" +
                                                            "  ]\n" +
                                                            "}"
                                            ),
                                            @ExampleObject(
                                                    name = "Пользователь повторно не может ставить лайк",
                                                    value = "{\n" +
                                                            "  \"type\": \"about:blank\",\n" +
                                                            "  \"title\": \"Bad Request\",\n" +
                                                            "  \"status\": 400,\n" +
                                                            "  \"detail\": \"Плохой запрос\",\n" +
                                                            "  \"instance\": \"/feedback-api/likes\",\n" +
                                                            "  \"errors\": [\n" +
                                                            "    \"Пользователь уже ставил лайк данной публикаций\"" +
                                                            "  ]\n" +
                                                            "}"
                                            )
                                    }

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
                                                    "  \"detail\": \"При созданий лайка публикация не была найдена\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<LikeReadDto> createLike(@RequestBody @Valid LikeCreateDto payload,
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
            LikeReadDto like = likeService.createLike(payload.publicationId(), jwtAuthenticationToken.getToken().getSubject());
            return ResponseEntity.created(uriComponentsBuilder.replacePath("/feedback-api/likes/{likeId}")
                            .build(Map.of("likeId", like.id())))
                    .body(like);
        }
    }

    @ExceptionHandler(LikeExistsException.class)
    public ResponseEntity<ProblemDetail> handleLikeExistsException(LikeExistsException exception, Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                Objects.requireNonNull(messageSource.getMessage(exception.getMessage(), new Object[0],
                        exception.getMessage(), locale)));
        return ResponseEntity.badRequest()
                .body(problemDetail);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNoSuchElementException(NoSuchElementException exception, Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                Objects.requireNonNull(messageSource.getMessage(exception.getMessage(), new Object[0],
                        exception.getMessage(), locale)));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }
}
