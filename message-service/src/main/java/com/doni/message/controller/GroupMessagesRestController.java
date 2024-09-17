package com.doni.message.controller;

import com.doni.message.dto.GroupMessageCreateDto;
import com.doni.message.dto.GroupMessageReadDto;
import com.doni.message.entity.GroupMessage;
import com.doni.message.exception.UserIsNotGroupParticipantException;
import com.doni.message.service.GroupMessageService;
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
@RequestMapping("/message-api/group-messages")
@SecurityRequirement(name = "keycloak")
public class GroupMessagesRestController {
    private final GroupMessageService groupMessageService;
    private final MessageSource messageSource;

    @GetMapping("/by-group-id/{groupId:\\d+}")
    @Operation(
            summary = "Получение списка сообщений в группе",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Список сообщении в группе",
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
                                                    "  \"detail\": \"Группа не найдена\"\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Плохой запрос",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Пользователь не является участником группы",
                                            value = "{\n" +
                                                            "  \"type\": \"about:blank\",\n" +
                                                            "  \"title\": \"Bad Request\",\n" +
                                                            "  \"status\": 400,\n" +
                                                            "  \"detail\": \"Вы не можете получить сообщения этой группы, так как не являетесь участником группы\",\n" +
                                                            "  \"instance\": \"/message-api/group-messages\"\n" +
                                                            "}"
                                            )

                            )
                    )
            }
    )
    public List<GroupMessageReadDto> getGroupMessagesByGroupId(@PathVariable("groupId") Integer groupId) {
        return groupMessageService.findGroupMessagesByGroupId(groupId);
    }

    @PostMapping
    @Operation(
            summary = "Создание сообщения в группе",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "Валидный запрос",
                                            value = "{\n" +
                                                    "  \"text\": \"Привет, всем!\",\n" +
                                                    "  \"groupId\": 1\n" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "Тело запроса содержит ошибки. Текст равен null",
                                            value = "{\n" +
                                                    "  \"text\": null,\n" +
                                                    "  \"groupId\": 1\n" +
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
                                                    "  \"text\": \"Привет, всем!\",\n" +
                                                    "  \"groupId\": 1,\n" +
                                                    "  \"authorId\": \"j.daniels\"\n" +
                                                    "}"
                                    )
                            )
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
                                                            "  \"instance\": \"/message-api/group-messages\",\n" +
                                                            "  \"errors\": [\n" +
                                                            "    \"Текст сообщения должен быть указан\",\n" +
                                                            "    \"Текст сообщения не может быть пустым\"\n" +
                                                            "  ]\n" +
                                                            "}"
                                            ),
                                            @ExampleObject(
                                                    name = "Пользователь не является участником группы",
                                                    value = "{\n" +
                                                            "  \"type\": \"about:blank\",\n" +
                                                            "  \"title\": \"Bad Request\",\n" +
                                                            "  \"status\": 400,\n" +
                                                            "  \"detail\": \"Вы не можете посылать сообщения в эту группу, так как не являетесь участником группы\",\n" +
                                                            "  \"instance\": \"/message-api/group-messages\"\n" +
                                                            "}"
                                            )
                                    }
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
                                                    "  \"detail\": \"При созданий сообщения произошла ошибка: Группа не существует\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<GroupMessageReadDto> createGroupMessage(@RequestBody @Valid GroupMessageCreateDto payload,
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
            GroupMessageReadDto groupMessage = groupMessageService.createGroupMessage(payload.text(), jwtAuthenticationToken.getToken().getSubject(), payload.groupId());
            return ResponseEntity.created(uriComponentsBuilder.replacePath("/message-api/group-messages/{messageId}")
                    .build(Map.of("messageId", groupMessage.id())))
                    .body(groupMessage);
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

    @ExceptionHandler(UserIsNotGroupParticipantException.class)
    public ResponseEntity<ProblemDetail> handleUserIsNotGroupParticipantException(UserIsNotGroupParticipantException exception, Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                Objects.requireNonNull(messageSource.getMessage(exception.getMessage(), new Object[0],
                        exception.getMessage(), locale)));
        return ResponseEntity.badRequest()
                .body(problemDetail);
    }
}
