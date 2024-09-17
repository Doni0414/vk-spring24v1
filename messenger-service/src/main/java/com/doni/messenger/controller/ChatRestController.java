package com.doni.messenger.controller;

import com.doni.messenger.dto.ChatReadDto;
import com.doni.messenger.entity.Chat;
import com.doni.messenger.exception.UserIsNotChatParticipantException;
import com.doni.messenger.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/messenger-api/chats/{chatId:\\d+}")
@SecurityRequirement(name = "keycloak")
public class ChatRestController {
    private final ChatService chatService;
    private final MessageSource messageSource;

    @ModelAttribute("chat")
    public ChatReadDto loadChat(@PathVariable("chatId") Integer chatId, JwtAuthenticationToken jwtAuthenticationToken) {
        return chatService.findChat(chatId, jwtAuthenticationToken.getToken().getSubject())
                .orElseThrow(() -> new NoSuchElementException("messenger-api.chats.errors.not_found"));
    }

    @GetMapping
    @Operation(
            summary = "Получение чата",
            parameters = @Parameter(name = "chatId", in = ParameterIn.PATH, schema = @Schema(implementation = Integer.class)),
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Чат получен",
                            useReturnTypeSchema = true
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Чат не существует",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Такой чат не существует\"\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Плохой запрос",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Чат не может быть удален, так как пользователь не является участником чата",
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Bad Request\",\n" +
                                                    "  \"status\": 400,\n" +
                                                    "  \"detail\": \"Вы не являетесь участником данного чата\",\n" +
                                                    "  \"instance\": \"/messenger-api/chats/1\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ChatReadDto getChat(@Parameter(hidden = true) @ModelAttribute("chat") ChatReadDto chat) {
        return chat;
    }

    @DeleteMapping
    @Operation(
            summary = "Удаление чата",
            parameters = @Parameter(name = "chatId", in = ParameterIn.PATH, schema = @Schema(implementation = Integer.class)),
            responses = {
                    @ApiResponse(
                            responseCode = "204", description = "Чат удален"
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Плохой запрос",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Чат не может быть удален, так как пользователь не является участником чата",
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Bad Request\",\n" +
                                                    "  \"status\": 400,\n" +
                                                    "  \"detail\": \"Вы не являетесь участником данного чата\",\n" +
                                                    "  \"instance\": \"/messenger-api/chats/1\"\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Чат не существует",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Такой чат не существует\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Void> deleteChat(@Parameter(hidden = true) @ModelAttribute("chat") ChatReadDto chat, JwtAuthenticationToken jwtAuthenticationToken) {
        chatService.deleteChat(chat.id(), jwtAuthenticationToken.getToken().getSubject());
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

    @ExceptionHandler(UserIsNotChatParticipantException.class)
    public ResponseEntity<ProblemDetail> handleUserIsNotChatParticipantException(UserIsNotChatParticipantException exception, Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                Objects.requireNonNull(messageSource.getMessage(exception.getMessage(), new Object[0],
                        exception.getMessage(), locale)));
        return ResponseEntity.badRequest()
                .body(problemDetail);
    }
}
