package com.doni.messenger.controller;

import com.doni.messenger.dto.ChatCreateDto;
import com.doni.messenger.dto.ChatReadDto;
import com.doni.messenger.entity.Chat;
import com.doni.messenger.exception.ChatExistsException;
import com.doni.messenger.service.ChatService;
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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/messenger-api/chats")
@SecurityRequirement(name = "keycloak")
public class ChatsRestController {
    private final ChatService chatService;
    private final MessageSource messageSource;

    @GetMapping
    @Operation(
            summary = "Получение чатов пользователя",
            responses = @ApiResponse(
                    responseCode = "200", description = "Список чатов",
                    useReturnTypeSchema = true
            )
    )
    public List<ChatReadDto> getChatsByCurrentUserId(JwtAuthenticationToken jwtAuthenticationToken) {
        return chatService.findAllChatsByUserId(jwtAuthenticationToken.getToken().getSubject());
    }

    @PostMapping
    @Operation(
            summary = "Создание чата",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "Валидное тело запроса",
                                            value = "{\n" +
                                                    "   \"userId\": \"string\"\n" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "Тело запроса содержит ошибки",
                                            value = "{\n" +
                                                    "   \"userId\": null\n" +
                                                    "}"
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201", description = "Чат успешно создан",
                            useReturnTypeSchema = true
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Плохой запрос",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = {
                                            @ExampleObject(
                                                    name = "Тело запроса содержал ошибки",
                                                    value = "{\n" +
                                                            "  \"type\": \"about:blank\",\n" +
                                                            "  \"title\": \"Bad Request\",\n" +
                                                            "  \"status\": 400,\n" +
                                                            "  \"detail\": \"Плохой запрос\",\n" +
                                                            "  \"instance\": \"/messenger-api/chats\",\n" +
                                                            "  \"errors\": [\n" +
                                                            "    \"Пользователь должен быть указан\"\n" +
                                                            "  ]\n" +
                                                            "}"
                                            ),
                                            @ExampleObject(
                                                    name = "Чат с пользователем уже существует",
                                                    value = "{\n" +
                                                            "  \"type\": \"about:blank\",\n" +
                                                            "  \"title\": \"Bad Request\",\n" +
                                                            "  \"status\": 400,\n" +
                                                            "  \"detail\": \"Чат с данным пользователем уже существует\",\n" +
                                                            "  \"instance\": \"/messenger-api/chats\"\n" +
                                                            "}"
                                            )
                                    }
                            )
                    )
            }
    )
    public ResponseEntity<ChatReadDto> createChat(@RequestBody @Valid ChatCreateDto payload,
                                           BindingResult bindingResult,
                                           JwtAuthenticationToken jwtAuthenticationToken,
                                           UriComponentsBuilder uriComponentsBuilder) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            ChatReadDto chat = chatService.createChat(payload.userId(), jwtAuthenticationToken.getToken().getSubject());
            return ResponseEntity.created(uriComponentsBuilder.replacePath("/messenger-api/chats/{chatId}")
                    .build(Map.of("chatId", chat.id())))
                    .body(chat);
        }
    }

    @ExceptionHandler(ChatExistsException.class)
    public ResponseEntity<ProblemDetail> handleChatExistsException(ChatExistsException exception, Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                Objects.requireNonNull(messageSource.getMessage(exception.getMessage(), new Object[0],
                        exception.getMessage(), locale)));
        return ResponseEntity.badRequest()
                .body(problemDetail);
    }
}
