package com.doni.message.controller;

import com.doni.message.repository.GroupMessageRepository;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/sql/groupMessages.sql")
@WireMockTest(httpPort = 54321)
class GroupMessageRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    GroupMessageRepository groupMessageRepository;

    @Test
    void getGroupMessage_UserIsAuthorized_GroupMessageExists_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/message-api/group-messages/1")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "ownerId": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                        {"id": 1, "text": "Text 1", "authorId": "j.dewar", "groupId": 1}
                        """)
                );
    }

    @Test
    void getGroupMessage_UserIsAuthorized_GroupMessageExists_UserIsNotGroupParticipant_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/message-api/group-messages/1")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.badRequest().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Вы не можете получить сообщения этой группы, так как не являетесь участником группы"}
                        """)
                );
    }

    @Test
    void getGroupMessage_UserIsNotAuthorized_GroupMessageExists_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/message-api/group-messages/1");


        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "ownerId": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void getGroupMessage_UserIsAuthorized_GroupMessageDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/message-api/group-messages/100")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "ownerId": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Сообщение не найдено"}
                        """)
                );
    }

    @Test
    void updateGroupMessage_UserIsAuthorized_GroupMessageExists_PayloadIsValid_UserIsOwner_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/group-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Updated text"}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "ownerId": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );

        groupMessageRepository.findById(1L)
                .ifPresent(groupMessage -> assertAll(
                        () -> assertEquals("Updated text", groupMessage.getText()),
                        () -> assertEquals("j.dewar", groupMessage.getAuthorId()),
                        () -> assertEquals(1, groupMessage.getGroupId())
                ));
    }

    @Test
    void updateGroupMessage_UserIsAuthorized_GroupMessageExists_UserIsNotGroupParticipant_PayloadIsValid_UserIsOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/group-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Updated text"}
                """).with(jwt().jwt(builder -> builder.subject("d.susan")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.badRequest().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Вы не можете получить сообщения этой группы, так как не являетесь участником группы"}
                        """)
                );
    }

    @Test
    void updateGroupMessage_UserIsNotAuthorized_GroupMessageExists_PayloadIsValid_UserIsOwner_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/group-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Updated text"}
                """);

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "ownerId": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );

        groupMessageRepository.findById(1L)
                .ifPresent(groupMessage -> assertAll(
                        () -> assertEquals("Text 1", groupMessage.getText()),
                        () -> assertEquals("j.dewar", groupMessage.getAuthorId()),
                        () -> assertEquals(1, groupMessage.getGroupId())
                ));
    }

    @Test
    void updateGroupMessage_UserIsAuthorized_GroupMessageDoesNotExist_PayloadIsValid_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/group-messages/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Updated text"}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "ownerId": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Сообщение не найдено"}
                        """)
                );
    }

    @Test
    void updateGroupMessage_UserIsAuthorized_GroupMessageExists_PayloadIsValid_UserIsNotOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/group-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Updated text"}
                """).with(jwt().jwt(builder -> builder.subject("j.daniels")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "ownerId": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Вы не можете обновить сообщение, так как не являетесь его владельцем"}
                        """)
                );

        groupMessageRepository.findById(1L)
                .ifPresent(groupMessage -> assertAll(
                        () -> assertEquals("Text 1", groupMessage.getText()),
                        () -> assertEquals("j.dewar", groupMessage.getAuthorId()),
                        () -> assertEquals(1, groupMessage.getGroupId())
                ));
    }

    @Test
    void updateGroupMessage_UserIsAuthorized_GroupMessageExists_PayloadIsValid_TextIsNull_UserIsOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/group-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": null}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "ownerId": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {
                            "detail": "Плохой запрос",
                            "errors": [
                                "Текст сообщения не может быть пустым",
                                "Текст сообщения должен быть указан"
                            ]
                        }
                        """)
                );

        groupMessageRepository.findById(1L)
                .ifPresent(groupMessage -> assertAll(
                        () -> assertEquals("Text 1", groupMessage.getText()),
                        () -> assertEquals("j.dewar", groupMessage.getAuthorId()),
                        () -> assertEquals(1, groupMessage.getGroupId())
                ));
    }

    @Test
    void updateGroupMessage_UserIsAuthorized_GroupMessageExists_PayloadIsValid_TextIsBlank_UserIsOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/group-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "         "}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "ownerId": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {
                            "detail": "Плохой запрос",
                            "errors": [
                                "Текст сообщения не может быть пустым"
                            ]
                        }
                        """)
                );

        groupMessageRepository.findById(1L)
                .ifPresent(groupMessage -> assertAll(
                        () -> assertEquals("Text 1", groupMessage.getText()),
                        () -> assertEquals("j.dewar", groupMessage.getAuthorId()),
                        () -> assertEquals(1, groupMessage.getGroupId())
                ));
    }

    @Test
    void updateGroupMessage_UserIsAuthorized_GroupMessageExists_PayloadIsValid_TextIsLessThan1_UserIsOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/group-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": ""}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "ownerId": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {
                            "detail": "Плохой запрос",
                            "errors": [
                                "Текст сообщения не может быть пустым",
                                "Длина сообщения должа быть между 1 и 2000 символами"
                            ]
                        }
                        """)
                );

        groupMessageRepository.findById(1L)
                .ifPresent(groupMessage -> assertAll(
                        () -> assertEquals("Text 1", groupMessage.getText()),
                        () -> assertEquals("j.dewar", groupMessage.getAuthorId()),
                        () -> assertEquals(1, groupMessage.getGroupId())
                ));
    }

    @Test
    void updateGroupMessage_UserIsAuthorized_GroupMessageNotExists_PayloadIsValid_TextIsGreaterThan2000_UserIsOwner_ReturnsBadRequest() throws Exception {
        String text = "a".repeat(2001);
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/group-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "%s"}
                """.formatted(text)).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "ownerId": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {
                            "detail": "Плохой запрос",
                            "errors": [
                                "Длина сообщения должа быть между 1 и 2000 символами"
                            ]
                        }
                        """)
                );

        groupMessageRepository.findById(1L)
                .ifPresent(groupMessage -> assertAll(
                        () -> assertEquals("Text 1", groupMessage.getText()),
                        () -> assertEquals("j.dewar", groupMessage.getAuthorId()),
                        () -> assertEquals(1, groupMessage.getGroupId())
                ));
    }

    @Test
    void deleteGroupMessage_UserIsAuthorized_GroupMessageExists_UserIsOwner_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/message-api/group-messages/1")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "ownerId": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );

        assertFalse(groupMessageRepository.findById(1L).isPresent());
    }

    @Test
    void deleteGroupMessage_UserIsAuthorized_GroupMessageExists_UserIsNotGroupParticipant_UserIsOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/message-api/group-messages/1")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.badRequest().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Вы не можете получить сообщения этой группы, так как не являетесь участником группы"}
                        """)
                );

    }

    @Test
    void deleteGroupMessage_UserIsNotAuthorized_GroupMessageExists_UserIsOwner_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/message-api/group-messages/1");

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "ownerId": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );

        assertTrue(groupMessageRepository.findById(1L).isPresent());
    }

    @Test
    void deleteGroupMessage_UserIsAuthorized_GroupMessageDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/message-api/group-messages/100")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "ownerId": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Сообщение не найдено"}
                        """)
                );
    }

    @Test
    void deleteGroupMessage_UserIsAuthorized_GroupMessageExists_UserIsNotOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/message-api/group-messages/1")
                .with(jwt().jwt(builder -> builder.subject("j.daniels")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "ownerId": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Вы не можете удалить сообщение, так как не являетесь его владельцем"}
                        """)
                );

        assertTrue(groupMessageRepository.findById(1L).isPresent());
    }
}