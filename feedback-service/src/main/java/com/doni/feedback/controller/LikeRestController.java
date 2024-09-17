package com.doni.feedback.controller;

import com.doni.feedback.dto.LikeReadDto;
import com.doni.feedback.entity.Like;
import com.doni.feedback.exception.UserIsNotOwnerException;
import com.doni.feedback.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/feedback-api/likes/by-publication-id/{publicationId:\\d+}/and-user-id/{userId}")
@SecurityRequirement(name = "keycloak")
public class LikeRestController {
    private final LikeService likeService;
    private final MessageSource messageSource;

    @GetMapping
    @Operation(
            summary = "Получение лайка",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Лайк найден"
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Лайк не найден",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Данный пользователь еще не ставил лайк данной публикаций\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public LikeReadDto getLikeByPublicationIdAndUserId(@PathVariable("publicationId") Integer publicationId,
                                                       @PathVariable("userId") String userId) {
        return likeService.findLikeByPublicationIdAndUserId(publicationId, userId);
    }

    @DeleteMapping
    @Operation(
            summary = "Удаление лайка",
            responses = {
                    @ApiResponse(
                            responseCode = "204", description = "Лайк удален"
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
                                                    "  \"instance\": \"/feedback-api/comments/1\",\n" +
                                                    "  \"errors\": [\n" +
                                                    "    \"Данный пользователь не является владельцем лайка\"" +
                                                    "  ]\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Лайк не найден",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Данный пользователь еще не ставил лайк данной публикаций\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Void> deleteLike(@PathVariable("publicationId") Integer publicationId,
                                           @PathVariable("userId") String userId,
                                           JwtAuthenticationToken jwtAuthenticationToken) {
        likeService.deleteLike(publicationId, userId, jwtAuthenticationToken.getToken().getSubject());
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
