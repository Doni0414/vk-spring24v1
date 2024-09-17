package com.doni.messenger.controller;

import com.doni.messenger.dto.GroupCreateDto;
import com.doni.messenger.dto.GroupReadDto;
import com.doni.messenger.entity.Group;
import com.doni.messenger.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/messenger-api/groups")
@SecurityRequirement(name = "keycloak")
public class GroupsRestController {
    private final GroupService groupService;

    @GetMapping
    @Operation(
            summary = "Получение списка групп у пользователя",
            responses = @ApiResponse(
                    responseCode = "200", description = "Список групп",
                    useReturnTypeSchema = true
            )
    )
    public List<GroupReadDto> getGroupsByUserId(JwtAuthenticationToken jwtAuthenticationToken) {
        return groupService.findAllByUserId(jwtAuthenticationToken.getToken().getSubject());
    }

    @PostMapping
    @Operation(
            summary = "Создание группы",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "Валидное тело запроса",
                                            value = "{\n" +
                                                    "   \"title\": \"Семья\",\n" +
                                                    "   \"description\": \"Невозможно описать словами мою семью!\"" +
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
                            responseCode = "201", description = "Группа успешно создана",
                            useReturnTypeSchema = true
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Плохой запрос",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            summary = "Тело запроса содержал ошибки",
                                            value = "{\n" +
                                                    "  \"type\": \"about:blank\",\n" +
                                                    "  \"title\": \"Bad Request\",\n" +
                                                    "  \"status\": 400,\n" +
                                                    "  \"detail\": \"Плохой запрос\",\n" +
                                                    "  \"instance\": \"/messenger-api/groups\",\n" +
                                                    "  \"errors\": [\n" +
                                                    "    \"Название группы должно быть указано\",\n" +
                                                    "    \"Название группы не должно быть пустым\"\n" +
                                                    "  ]\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<GroupReadDto> createGroup(@RequestBody @Valid GroupCreateDto payload,
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
            GroupReadDto group = groupService.createGroup(payload.title(), payload.description(), jwtAuthenticationToken.getToken().getSubject());
            return ResponseEntity.created(uriComponentsBuilder.replacePath("/messenger-api/groups/{groupId}")
                    .build(Map.of("groupId", group.id())))
                    .body(group);
        }
    }
}
