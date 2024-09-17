package com.doni.message.controller;

import com.doni.message.dto.ChatMessageReadDto;
import com.doni.message.dto.ChatMessageUpdateDto;
import com.doni.message.exception.UserIsNotChatParticipantException;
import com.doni.message.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
@RequestMapping("/message-api/chat-messages/{messageId:\\d+}")
@SecurityRequirement(name = "keycloak")
public class ChatMessageRestController {
    private final MessageSource messageSource;
    private final ChatMessageService chatMessageService;

    @ModelAttribute("message")
    public ChatMessageReadDto loadChatMessage(@PathVariable("messageId") Long messageId) {
        return chatMessageService.findChatMessage(messageId)
                .orElseThrow(() -> new NoSuchElementException("message-api.chat-messages.errors.not_found"));
    }

    @GetMapping
    @Operation(
            summary = "Получение сообщения в чате",
            parameters = @Parameter(name = "messageId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class)),
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Сообщение найдено",
                            useReturnTypeSchema = true
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Сообщение не найдено",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Сообщение не найдено\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ChatMessageReadDto getChatMessage(@Parameter(hidden = true) @ModelAttribute("message") ChatMessageReadDto chatMessage) {
        return chatMessage;
    }

    @PatchMapping
    @Operation(
            summary = "Обновление сообщения в чате",
            parameters = @Parameter(name = "messageId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class)),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "Валидное тело запроса",
                                            value = "{\n" +
                                                    "  \"text\": \"Updated text\"\n" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "Тело запроса содержит ошибки",
                                            value = "{\n" +
                                                    "  \"text\": \"\"\n" +
                                                    "}"
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "204", description = "Сообщение успешно обновлено"
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
                                                            "  \"instance\": \"/message-api/chat-messages\",\n" +
                                                            "  \"errors\": [\n" +
                                                            "    \"Текст сообщения не должен быть пустым\"\n" +
                                                            "  ]\n" +
                                                            "}"
                                            ),
                                            @ExampleObject(
                                                    name = "Пользователь не является владельцем сообщения",
                                                    value = "{\n" +
                                                            "  \"type\": \"about:blank\",\n" +
                                                            "  \"title\": \"Bad Request\",\n" +
                                                            "  \"status\": 400,\n" +
                                                            "  \"detail\": \"Плохой запрос\",\n" +
                                                            "  \"instance\": \"/message-api/chat-messages\",\n" +
                                                            "  \"errors\": [\n" +
                                                            "    \"Пользователь не является автором сообщения\"\n" +
                                                            "  ]\n" +
                                                            "}"
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Сообщение не найдено",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Сообщение не найдено\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Void> updateChatMessage(@Parameter(hidden = true) @ModelAttribute("message") ChatMessageReadDto chatMessage,
                                                  @RequestBody @Valid ChatMessageUpdateDto payload,
                                                  BindingResult bindingResult,
                                                  JwtAuthenticationToken jwtAuthenticationToken) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException ex) {
                throw ex;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            chatMessageService.updateChatMessage(chatMessage.id(), payload.text(), jwtAuthenticationToken.getToken().getSubject());
            return ResponseEntity.noContent()
                    .build();
        }
    }

    @DeleteMapping
    @Operation(
            summary = "Удаление сообщения в чате",
            parameters = @Parameter(name = "messageId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class)),
            responses = {
                    @ApiResponse(
                            responseCode = "204", description = "Сообщение удалено"
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Плохой запрос",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Пользователь не является владельцем сообщения",
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Bad Request\",\n" +
                                                    "  \"status\": 400,\n" +
                                                    "  \"detail\": \"Плохой запрос\",\n" +
                                                    "  \"instance\": \"/message-api/group-messages\",\n" +
                                                    "  \"errors\": [\n" +
                                                    "    \"Пользователь не является автором сообщения\"\n" +
                                                    "  ]\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Сообщение не найдено",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Сообщение не найдено\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Void> deleteChatMessage(@Parameter(hidden = true) @ModelAttribute("message") ChatMessageReadDto chatMessage,
                                                  JwtAuthenticationToken jwtAuthenticationToken) {
        chatMessageService.deleteChatMessage(chatMessage.id(), jwtAuthenticationToken.getToken().getSubject());
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
