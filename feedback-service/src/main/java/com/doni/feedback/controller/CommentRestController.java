package com.doni.feedback.controller;

import com.doni.feedback.dto.CommentReadDto;
import com.doni.feedback.dto.CommentUpdateDto;
import com.doni.feedback.exception.UserIsNotOwnerException;
import com.doni.feedback.service.CommentService;
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

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/feedback-api/comments/{commentId:\\d+}")
@SecurityRequirement(name = "keycloak")
public class CommentRestController {
    private final MessageSource messageSource;
    private final CommentService commentService;

    @GetMapping
    @Operation(
            summary = "Получение комментария",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Комментарий получен",
                            useReturnTypeSchema = true
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Комментарий не найден",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Комментарий не найден\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public CommentReadDto getComment(@PathVariable("commentId") Integer commentId) {
        return commentService.findComment(commentId);
    }

    @PatchMapping
    @Operation(
            summary = "Обновление комментария",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "Body 1", summary = "Валидный запрос",
                                            value = "{\n" +
                                                    "  \"text\": \"Updated text\"\n" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "Body 2", summary = "Инвалидный запрос",
                                            value = "{\n" +
                                                    "  \"text\": \"\"\n" +
                                                    "}"
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "204", description = "Комментарий обновлен"
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
                                                            "  \"instance\": \"/feedback-api/comments/1\",\n" +
                                                            "  \"errors\": [\n" +
                                                            "    \"Текст комментария должен быть указан\",\n" +
                                                            "    \"Комментарий не может быть пустым\"\n" +
                                                            "  ]\n" +
                                                            "}"
                                            ),
                                            @ExampleObject(
                                                    name = "Если пользователь не является автором комментария, то он не может обновить этот комментарий",
                                                    value = "{\n" +
                                                            "  \"type\": \"about:blank\",\n" +
                                                            "  \"title\": \"Bad Request\",\n" +
                                                            "  \"status\": 400,\n" +
                                                            "  \"detail\": \"Плохой запрос\",\n" +
                                                            "  \"instance\": \"/feedback-api/comments/1\",\n" +
                                                            "  \"errors\": [\n" +
                                                            "    \"Данный пользователь не является автором этого комментария\"" +
                                                            "  ]\n" +
                                                            "}"
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Комментарий не найден",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Комментарий не найден\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Void> updateComment(@PathVariable("commentId") Integer commentId,
                                              @RequestBody @Valid CommentUpdateDto payload,
                                              BindingResult bindingResult,
                                              JwtAuthenticationToken jwtAuthenticationToken) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException ex) {
                throw ex;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            commentService.updateComment(commentId, payload.text(), jwtAuthenticationToken.getToken().getSubject());
            return ResponseEntity.noContent()
                    .build();
        }
    }

    @DeleteMapping
    @Operation(
            summary = "Удаление комментария",
            responses = {
                    @ApiResponse(
                            responseCode = "204", description = "Комментарий удален"
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Пользователь не является автором комментария",
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
                                                    "    \"Данный пользователь не является автором этого комментария\"" +
                                                    "  ]\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Комментарий не найден",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Комментарий не найден\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Integer commentId,
                                              JwtAuthenticationToken jwtAuthenticationToken) {
        commentService.deleteComment(commentId, jwtAuthenticationToken.getToken().getSubject());
        return ResponseEntity.noContent()
                .build();
    }

    @ExceptionHandler(UserIsNotOwnerException.class)
    public ResponseEntity<ProblemDetail> handleUserIsNotOwnerException(UserIsNotOwnerException exception, Locale locale) {
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
