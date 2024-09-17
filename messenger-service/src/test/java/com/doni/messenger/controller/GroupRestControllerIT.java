package com.doni.messenger.controller;

import com.doni.messenger.entity.Group;
import com.doni.messenger.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@Sql("/sql/groups.sql")
@ActiveProfiles("test")
class GroupRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    GroupRepository groupRepository;

    @Test
    void getGroup_UserIsAuthorized_GroupExists_UserIsParticipant_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/messenger-api/groups/1")
                .with(jwt().jwt(builder -> builder.subject("j.daniels")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                {"id": 1, "title": "Title 1", "description": "Description 1", "ownerId": "j.dewar"}
                                """
                        )
                );
    }

    @Test
    void getGroup_UserIsNotAuthorized_GroupExists_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/messenger-api/groups/1");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void getGroup_UserIsAuthorized_GroupDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/messenger-api/groups/100")
                .with(jwt().jwt(builder -> builder.subject("j.daniels")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Такой группы не существует"}
                                """
                        )
                );
    }

    @Test
    void getGroup_UserIsAuthorized_GroupExists_UserIsNotParticipant_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/messenger-api/groups/1")
                .with(jwt().jwt(builder -> builder.subject("j.black")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Пользователь не является участником группы"}
                                """
                        )
                );
    }

    @Test
    void updateGroup_UserIsAuthorized_GroupExists_PayloadIsValid_UserIsOwner_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/messenger-api/groups/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "Updated title", "description": "Updated description"}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );

        groupRepository.findById(1)
                .ifPresent(group -> assertAll(
                        () -> assertEquals("Updated title", group.getTitle()),
                        () -> assertEquals("Updated description", group.getDescription())
                ));
    }

    @Test
    void updateGroup_UserIsNotAuthorized_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/messenger-api/groups/1");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }


    @Test
    void updateGroup_UserIsAuthorized_GroupDoesNotExists_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/messenger-api/groups/100")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Такой группы не существует"}
                                """
                        )
                );

        groupRepository.findById(1)
                .ifPresent(group -> assertAll(
                        () -> assertEquals("Title 1", group.getTitle()),
                        () -> assertEquals("Description 1", group.getDescription())
                ));
    }

    @Test
    void updateGroup_UserIsAuthorized_GroupExists_PayloadIsValid_UserIsNotOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/messenger-api/groups/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "Updated title", "description": "Updated description"}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.daniels")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Пользователь не является владельцем группы"}
                                """
                        )
                );

        groupRepository.findById(1)
                .ifPresent(group -> assertAll(
                        () -> assertEquals("Title 1", group.getTitle()),
                        () -> assertEquals("Description 1", group.getDescription())
                ));
    }

    @Test
    void updateGroup_UserIsAuthorized_GroupExists_PayloadIsInvalid_TitleIsNull_UserIsOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/messenger-api/groups/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": null, "description": "Description 1"}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {
                                    "detail": "Плохой запрос",
                                    "errors": [
                                        "Название группы не должно быть пустым",
                                        "Название группы должно быть указано"
                                    ]
                                }
                                """
                        )
                );

        groupRepository.findById(1)
                .ifPresent(group -> assertAll(
                        () -> assertEquals("Title 1", group.getTitle()),
                        () -> assertEquals("Description 1", group.getDescription())
                ));
    }

    @Test
    void updateGroup_UserIsAuthorized_GroupExists_PayloadIsInvalid_TitleIsBlank_UserIsOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/messenger-api/groups/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "     ", "description": "Description 1"}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {
                                    "detail": "Плохой запрос",
                                    "errors": [
                                        "Название группы не должно быть пустым"
                                    ]
                                }
                                """
                        )
                );

        groupRepository.findById(1)
                .ifPresent(group -> assertAll(
                        () -> assertEquals("Title 1", group.getTitle()),
                        () -> assertEquals("Description 1", group.getDescription())
                ));
    }

    @Test
    void updateGroup_UserIsAuthorized_GroupExists_PayloadIsInvalid_TitleIsLessThan1_UserIsOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/messenger-api/groups/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "", "description": "Description 1"}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {
                                    "detail": "Плохой запрос",
                                    "errors": [
                                        "Название группы не должно быть пустым",
                                        "Название группы должно быть между 1 и 100 символами"
                                    ]
                                }
                                """
                        )
                );

        groupRepository.findById(1)
                .ifPresent(group -> assertAll(
                        () -> assertEquals("Title 1", group.getTitle()),
                        () -> assertEquals("Description 1", group.getDescription())
                ));
    }

    @Test
    void updateGroup_UserIsAuthorized_GroupExists_PayloadIsInvalid_TitleIsGreaterThan100_UserIsOwner_ReturnsBadRequest() throws Exception {
        String title = "a".repeat(101);
        var requestBuilder = MockMvcRequestBuilders.patch("/messenger-api/groups/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "%s", "description": "Description 1"}
                """.formatted(title))
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {
                                    "detail": "Плохой запрос",
                                    "errors": [
                                        "Название группы должно быть между 1 и 100 символами"
                                    ]
                                }
                                """
                        )
                );

        groupRepository.findById(1)
                .ifPresent(group -> assertAll(
                        () -> assertEquals("Title 1", group.getTitle()),
                        () -> assertEquals("Description 1", group.getDescription())
                ));
    }

    @Test
    void updateGroup_UserIsAuthorized_GroupExists_PayloadIsInvalid_DescriptionIsGreaterThan2000_UserIsOwner_ReturnsBadRequest() throws Exception {
        String description = "a".repeat(2001);
        var requestBuilder = MockMvcRequestBuilders.patch("/messenger-api/groups/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "Updated title", "description": "%s"}
                """.formatted(description))
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {
                                    "detail": "Плохой запрос",
                                    "errors": [
                                        "Описание группы должно быть между 0 и 2000 символами"
                                    ]
                                }
                                """
                        )
                );

        groupRepository.findById(1)
                .ifPresent(group -> assertAll(
                        () -> assertEquals("Title 1", group.getTitle()),
                        () -> assertEquals("Description 1", group.getDescription())
                ));
    }

    @Test
    void deleteGroup_UserIsAuthorized_GroupExists_UserIsOwner_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/groups/1")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );

        assertTrue(groupRepository.findById(1).isEmpty());
    }

    @Test
    void deleteGroup_UserIsNotAuthorized_GroupExists_UserIsOwner_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/groups/1");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );

        assertTrue(groupRepository.findById(1).isPresent());
    }

    @Test
    void deleteGroup_UserIsAuthorized_GroupDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/groups/100")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Такой группы не существует"}
                                """
                        )
                );
    }

    @Test
    void deleteGroup_UserIsAuthorized_GroupExists_UserIsNotOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/groups/1")
                .with(jwt().jwt(builder -> builder.subject("j.daniels")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Пользователь не является владельцем группы"}
                                """
                        )
                );

        assertTrue(groupRepository.findById(1).isPresent());
    }

    @Test
    void addUser_UserIsAuthorized_GroupExists_UserIsOwner_PayloadIsValid_NewUserIsNotParticipant_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/messenger-api/groups/1/add-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"userId": "j.black"}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );

        List<Group> groups = groupRepository.findAllByUserId("j.black");
        assertEquals(1, groups.size());
        assertFalse(groups.stream()
                .filter(group -> group.getId().equals(1))
                .toList()
                .isEmpty());
    }

    @Test
    void addUser_UserIsNotAuthorized_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/messenger-api/groups/1/add-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"userId": "j.black"}
                """);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void addUser_UserIsAuthorized_GroupDoesNotExist_UserIsOwner_PayloadIsValid_NewUserIsNotParticipant_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/messenger-api/groups/100/add-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"userId": "j.black"}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Такой группы не существует"}
                        """)
                );
    }

    @Test
    void addUser_UserIsAuthorized_GroupExist_UserIsNotOwner_PayloadIsValid_NewUserIsNotParticipant_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/messenger-api/groups/1/add-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"userId": "j.black"}
                """).with(jwt().jwt(builder -> builder.subject("j.daniels")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Вы не можете добавить пользователя так, как не являетесь владельцем группы"}
                        """)
                );
    }

    @Test
    void addUser_UserIsAuthorized_GroupExist_UserOwner_PayloadIsValid_NewUserIsParticipant_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/messenger-api/groups/1/add-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"userId": "j.daniels"}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Пользователь уже в группе"}
                        """)
                );
    }

    @Test
    void addUser_UserIsAuthorized_GroupExist_UserOwner_PayloadIsInvalid_NewUserIsNotParticipant_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/messenger-api/groups/1/add-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"userId": null}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {
                            "detail": "Плохой запрос",
                            "errors": [
                                "Пользователь должен быть указан"
                            ]
                        }
                        """)
                );
    }

    @Test
    void kickUser_UserIsAuthorized_GroupExists_UserIsOwner_KickedUserIsParticipant_PayloadIsValid_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/groups/1/kick-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"userId": "j.daniels"}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );

        assertEquals(0, groupRepository.findAllByUserId("j.daniels").stream()
                .filter(group -> group.getId().equals(1))
                .toList()
                .size());
    }

    @Test
    void kickUser_UserIsNotAuthorized_GroupExists_UserIsOwner_KickedUserIsParticipant_PayloadIsValid_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/groups/1/kick-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"userId": "j.daniels"}
                """);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );

        assertEquals(1, groupRepository.findAllByUserId("j.daniels").stream()
                .filter(group -> group.getId().equals(1))
                .toList()
                .size());
    }

    @Test
    void kickUser_UserIsAuthorized_GroupDoesNotExists_UserIsOwner_KickedUserIsParticipant_PayloadIsValid_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/groups/100/kick-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"userId": "j.daniels"}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Такой группы не существует"}
                        """)
                );
    }

    @Test
    void kickUser_UserIsAuthorized_GroupExists_UserIsNotOwner_KickedUserIsParticipant_PayloadIsValid_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/groups/1/kick-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"userId": "j.dewar"}
                """).with(jwt().jwt(builder -> builder.subject("j.daniels")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Вы не можете кикнуть пользователя так, как не являетесь владельцем группы"}
                        """)
                );

        assertEquals(1, groupRepository.findAllByUserId("j.dewar").stream()
                .filter(group -> group.getId().equals(1))
                .toList()
                .size());
    }

    @Test
    void kickUser_UserIsAuthorized_GroupExists_UserIsOwner_KickedUserIsNotParticipant_PayloadIsValid_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/groups/1/kick-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"userId": "j.black"}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Данный пользователь не состоит в группе"}
                        """)
                );
    }

    @Test
    void kickUser_UserIsAuthorized_GroupExists_UserIsOwner_KickedUserIsParticipant_PayloadIsInvalid_UserIdIsNull_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/groups/1/kick-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"userId": null}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {
                            "detail": "Плохой запрос",
                            "errors": [
                                "Пользователь должен быть указан"
                            ]
                        }
                        """)
                );
    }

    @Test
    void leaveGroup_UserIsAuthorized_GroupExists_UserIsParticipant_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/groups/1/leave-group")
                .with(jwt().jwt(builder -> builder.subject("j.daniels")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );

        assertEquals(0, groupRepository.findAllByUserId("j.daniels").stream()
                .filter(group -> group.getId().equals(1))
                .toList()
                .size());
    }

    @Test
    void leaveGroup_UserIsNotAuthorized_GroupExists_UserIsParticipant_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/groups/1/leave-group");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void leaveGroup_UserIsAuthorized_GroupDoesNotExists_UserIsParticipant_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/groups/100/leave-group")
                .with(jwt().jwt(builder -> builder.subject("j.daniels")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Такой группы не существует"}
                        """)
                );
    }

    @Test
    void leaveGroup_UserIsAuthorized_GroupExists_UserIsNotParticipant_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/groups/1/leave-group")
                .with(jwt().jwt(builder -> builder.subject("j.black")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Пользователь не является участником группы"}
                        """)
                );
    }
}