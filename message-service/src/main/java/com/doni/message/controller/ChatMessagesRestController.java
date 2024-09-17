package com.doni.message.controller;

import com.doni.message.dto.ChatMessageCreateDto;
import com.doni.message.dto.ChatMessageReadDto;
import com.doni.message.entity.ChatMessage;
import com.doni.message.exception.UserIsNotChatParticipantException;
import com.doni.message.service.ChatMessageService;
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
@RequestMapping("/message-api/chat-messages")
@SecurityRequirement(name = "keycloak")
public class ChatMessagesRestController {
    private final MessageSource messageSource;
    private final ChatMessageService chatMessageService;

    @GetMapping("/by-chat-id/{chatId:\\d+}")
    @Operation(
            summary = "Получение сообщении по идентификатору чата",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Список сообщении в чате",
                            useReturnTypeSchema = true
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Группа не найдена",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Группа не найдена",
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Чат не найден\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public List<ChatMessageReadDto> getChatMessagesByChatId(@PathVariable("chatId") Integer chatId) {
        return chatMessageService.findChatMessagesByChatId(chatId);
    }

    @PostMapping
    @Operation(
            summary = "Создание сообщения в чате",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "Валидное тело запроса",
                                            value = "{\n" +
                                                    "  \"text\": \"Добрый день!\",\n" +
                                                    "  \"chatId\": 1\n" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "Тело запроса содержит ошибки",
                                            value = "{\n" +
                                                    "  \"text\": \"\",\n" +
                                                    "  \"chatId\": 1\n" +
                                                    "}"
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201", description = "Сообщение успешно создано",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"id\": 1,\n" +
                                                    "  \"text\": \"Добрый день!\",\n" +
                                                    "  \"chatId\": 1,\n" +
                                                    "  \"authorId\": \"j.daniels\"\n" +
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
                                                    "  \"instance\": \"/message-api/chat-messages\",\n" +
                                                    "  \"errors\": [\n" +
                                                    "    \"Текст сообщения не должен быть пустым\"\n" +
                                                    "  ]\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Группа не найдена",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Группа не найдена",
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"При созданий сообщения произошла ошибка: Чат не существует\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<ChatMessageReadDto> createChatMessage(@RequestBody @Valid ChatMessageCreateDto payload,
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
            ChatMessageReadDto chatMessage = chatMessageService.createChatMessage(payload.text(), jwtAuthenticationToken.getToken().getSubject(), payload.chatId());
            return ResponseEntity.created(uriComponentsBuilder.replacePath("/message-api/chat-messages/{messageId}")
                    .build(Map.of("messageId", chatMessage.id())))
                    .body(chatMessage);
        }
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
