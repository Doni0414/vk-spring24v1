package com.doni.messenger.controller;

import com.doni.messenger.dto.GroupAddUserDto;
import com.doni.messenger.dto.GroupKickUserDto;
import com.doni.messenger.dto.GroupReadDto;
import com.doni.messenger.dto.GroupUpdateDto;
import com.doni.messenger.entity.Group;
import com.doni.messenger.exception.UserIsAlreadyGroupMemberException;
import com.doni.messenger.exception.UserIsNotGroupOwnerException;
import com.doni.messenger.exception.UserIsNotGroupParticipantException;
import com.doni.messenger.service.GroupService;
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
@RequestMapping("/messenger-api/groups/{groupId:\\d+}")
@SecurityRequirement(name = "keycloak")
public class GroupRestController {
    private final GroupService groupService;
    private final MessageSource messageSource;

    @ModelAttribute("group")
    public GroupReadDto loadGroup(@PathVariable("groupId") Integer groupId,
                                  JwtAuthenticationToken jwtAuthenticationToken) {
        return groupService.findGroup(groupId, jwtAuthenticationToken.getToken().getSubject())
                .orElseThrow(() -> new NoSuchElementException("messenger-api.groups.errors.not_found"));
    }

    @GetMapping
    @Operation(
            summary = "Получение группы",
            parameters = @Parameter(name = "groupId", schema = @Schema(implementation = Integer.class), in = ParameterIn.PATH),
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Группа получена",
                            useReturnTypeSchema = true
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Плохой запрос",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "У данного пользователя нету доступа к группе, так как он не является его участником",
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Bad Request\",\n" +
                                                    "  \"status\": 400,\n" +
                                                    "  \"detail\": \"Пользователь не является участником группы\",\n" +
                                                    "  \"instance\": \"/messenger-api/groups/1\"\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Группа не найдена",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Такой группы не существует\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public GroupReadDto getGroup(@Parameter(hidden = true) @ModelAttribute("group") GroupReadDto group) {
        return group;
    }

    @PatchMapping
    @Operation(
            summary = "Обновление группы",
            parameters = @Parameter(name = "groupId", schema = @Schema(implementation = Integer.class), in = ParameterIn.PATH),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "Валидное тело запроса",
                                            value = "{\n" +
                                                    "   \"title\": \"Семья (Обновлено)\",\n" +
                                                    "   \"description\": \"Невозможно описать словами мою семью! (Обновлено)\"" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "Тело запроса содержит ошибки",
                                            value = "{\n" +
                                                    "   \"title\": null,\n" +
                                                    "   \"description\": \"Невозможно описать словами мою семью!\"" +
                                                    "}"
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "204", description = "Данные группы успешно обновлены"
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
                                                            "  \"instance\": \"/messenger-api/groups/1\",\n" +
                                                            "  \"errors\": [\n" +
                                                            "    \"Название группы должно быть указано\",\n" +
                                                            "    \"Название группы не должно быть пустым\"\n" +
                                                            "  ]\n" +
                                                            "}"
                                            ),
                                            @ExampleObject(
                                                    name = "У пользователя нету прав обновлять данные группы, так как он не является его владельцем",
                                                    value = "{\n" +
                                                            "  \"type\": \"about:blank\",\n" +
                                                            "  \"title\": \"Bad Request\",\n" +
                                                            "  \"status\": 400,\n" +
                                                            "  \"detail\": \"Пользователь не является владельцем группы\",\n" +
                                                            "  \"instance\": \"/messenger-api/groups/1\"\n" +
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
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Такой группы не существует\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Void> updateGroup(@Parameter(hidden = true) @ModelAttribute("group") GroupReadDto group,
                                            @RequestBody @Valid GroupUpdateDto payload,
                                            BindingResult bindingResult,
                                            JwtAuthenticationToken jwtAuthenticationToken) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException ex) {
                throw ex;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            groupService.updateGroup(group.id(), payload.title(), payload.description(), jwtAuthenticationToken.getToken().getSubject());
            return ResponseEntity.noContent()
                    .build();
        }
    }

    @DeleteMapping
    @Operation(
            summary = "Удаление группы",
            parameters = @Parameter(name = "groupId", schema = @Schema(implementation = Integer.class), in = ParameterIn.PATH),
            responses = {
                    @ApiResponse(
                            responseCode = "204", description = "Группа удалена"
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Плохой запрос",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Пользователь не может удалить группу, так как не является его владельцем",
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Bad Request\",\n" +
                                                    "  \"status\": 400,\n" +
                                                    "  \"detail\": \"Пользователь не является владельцем группы\",\n" +
                                                    "  \"instance\": \"/messenger-api/groups/1\"\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Группа не найдена",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Такой группы не существует\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Void> deleteGroup(@Parameter(hidden = true) @ModelAttribute("group") GroupReadDto group,
                                            JwtAuthenticationToken jwtAuthenticationToken) {
        groupService.deleteGroup(group.id(), jwtAuthenticationToken.getToken().getSubject());
        return ResponseEntity.noContent()
                .build();
    }

    @PatchMapping("/add-user")
    @Operation(
            summary = "Добавление участника в группу",
            parameters = @Parameter(name = "groupId", schema = @Schema(implementation = Integer.class), in = ParameterIn.PATH),
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
                            responseCode = "204", description = "Пользователь успешно добавлен"
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
                                                            "  \"instance\": \"/messenger-api/groups/1\",\n" +
                                                            "  \"errors\": [\n" +
                                                            "    \"Пользователь должен быть указан\"\n" +
                                                            "  ]\n" +
                                                            "}"
                                            ),
                                            @ExampleObject(
                                                    name = "Пользователь не может добавить участника в группу, так как не является его владельцем",
                                                    value = "{\n" +
                                                            "  \"type\": \"about:blank\",\n" +
                                                            "  \"title\": \"Bad Request\",\n" +
                                                            "  \"status\": 400,\n" +
                                                            "  \"detail\": \"Вы не можете добавить пользователя так, как не являетесь владельцем группы\",\n" +
                                                            "  \"instance\": \"/messenger-api/groups/1\"\n" +
                                                            "}"
                                            ),
                                            @ExampleObject(
                                                    name = "Пользователь не может быть добавлен в группу, так как он уже состоит в группе",
                                                    value = "{\n" +
                                                            "  \"type\": \"about:blank\",\n" +
                                                            "  \"title\": \"Bad Request\",\n" +
                                                            "  \"status\": 400,\n" +
                                                            "  \"detail\": \"Пользователь уже в группе\",\n" +
                                                            "  \"instance\": \"/messenger-api/groups/1\"\n" +
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
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Такой группы не существует\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Void> addUser(@Parameter(hidden = true) @ModelAttribute("group") GroupReadDto group,
                                        @RequestBody @Valid GroupAddUserDto payload,
                                        BindingResult bindingResult,
                                        JwtAuthenticationToken jwtAuthenticationToken) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException ex) {
                throw ex;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            groupService.addUser(group.id(), payload.userId(), jwtAuthenticationToken.getToken().getSubject());
            return ResponseEntity.noContent()
                    .build();
        }
    }

    @DeleteMapping("/kick-user")
    @Operation(
            summary = "Кик участника из группы",
            parameters = @Parameter(name = "groupId", schema = @Schema(implementation = Integer.class), in = ParameterIn.PATH),
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
                            responseCode = "204", description = "Участник успешно кикнут с группы"
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
                                                            "  \"instance\": \"/messenger-api/groups/1\",\n" +
                                                            "  \"errors\": [\n" +
                                                            "    \"Пользователь должен быть указан\"\n" +
                                                            "  ]\n" +
                                                            "}"
                                            ),
                                            @ExampleObject(
                                                    name = "Пользователь не может кикнуть участника из группы, так как не является его владельцем",
                                                    value = "{\n" +
                                                            "  \"type\": \"about:blank\",\n" +
                                                            "  \"title\": \"Bad Request\",\n" +
                                                            "  \"status\": 400,\n" +
                                                            "  \"detail\": \"Вы не можете кикнуть пользователя так, как не являетесь владельцем группы\",\n" +
                                                            "  \"instance\": \"/messenger-api/groups/1\"\n" +
                                                            "}"
                                            ),
                                            @ExampleObject(
                                                    name = "Нельзя кикнуть пользователя, так как он не состоит в группе",
                                                    value = "{\n" +
                                                            "  \"type\": \"about:blank\",\n" +
                                                            "  \"title\": \"Bad Request\",\n" +
                                                            "  \"status\": 400,\n" +
                                                            "  \"detail\": \"Данный пользователь не состоит в группе\",\n" +
                                                            "  \"instance\": \"/messenger-api/groups/1\"\n" +
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
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Такой группы не существует\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Void> kickUser(@Parameter(hidden = true) @ModelAttribute("group") GroupReadDto group,
                                         @RequestBody @Valid GroupKickUserDto payload,
                                         BindingResult bindingResult,
                                         JwtAuthenticationToken jwtAuthenticationToken) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException ex) {
                throw ex;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            groupService.kickUser(group.id(), payload.userId(), jwtAuthenticationToken.getToken().getSubject());
            return ResponseEntity.noContent()
                    .build();
        }
    }

    @DeleteMapping("/leave-group")
    @Operation(
            summary = "Покинуть группу",
            parameters = @Parameter(name = "groupId", schema = @Schema(implementation = Integer.class), in = ParameterIn.PATH),
            responses = {
                    @ApiResponse(
                            responseCode = "204", description = "Пользователь успешно покинул группу"
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Плохой запрос",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Пользователь не может покинуть группу, так как не является его участником",
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Bad Request\",\n" +
                                                    "  \"status\": 400,\n" +
                                                    "  \"detail\": \"Данный пользователь не состоит в группе\",\n" +
                                                    "  \"instance\": \"/messenger-api/groups/1\"\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "Группа не найдена",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Not Found\",\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"detail\": \"Такой группы не существует\"\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Void> leaveGroup(@Parameter(hidden = true) @ModelAttribute("group") GroupReadDto group,
                                           JwtAuthenticationToken jwtAuthenticationToken) {
        groupService.leaveGroup(group.id(), jwtAuthenticationToken.getToken().getSubject());
        return ResponseEntity.noContent()
                .build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNoSuchElementException(NoSuchElementException exception,
                                                                      Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                Objects.requireNonNull(messageSource.getMessage(exception.getMessage(), new Object[0],
                        exception.getMessage(), locale)));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }

    @ExceptionHandler(UserIsNotGroupParticipantException.class)
    public ResponseEntity<ProblemDetail> handleUserIsNotGroupParticipantException(UserIsNotGroupParticipantException exception,
                                                                                  Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                Objects.requireNonNull(messageSource.getMessage(exception.getMessage(), new Object[0],
                        exception.getMessage(), locale)));
        return ResponseEntity.badRequest()
                .body(problemDetail);
    }

    @ExceptionHandler(UserIsNotGroupOwnerException.class)
    public ResponseEntity<ProblemDetail> handleUserIsNotGroupOwnerException(UserIsNotGroupOwnerException exception,
                                                                            Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                Objects.requireNonNull(messageSource.getMessage(exception.getMessage(), new Object[0],
                        exception.getMessage(), locale)));
        return ResponseEntity.badRequest()
                .body(problemDetail);
    }

    @ExceptionHandler(UserIsAlreadyGroupMemberException.class)
    public ResponseEntity<ProblemDetail> handleUserIsAlreadyGroupMemberException(UserIsAlreadyGroupMemberException exception,
                                                                            Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                Objects.requireNonNull(messageSource.getMessage(exception.getMessage(), new Object[0],
                        exception.getMessage(), locale)));
        return ResponseEntity.badRequest()
                .body(problemDetail);
    }
}
