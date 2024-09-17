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
@WireMockTest(httpPort = 54321)
class GroupMessagesRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    GroupMessageRepository groupMessageRepository;

    @Test
    @Sql("/sql/groupMessages.sql")
    void getGroupMessagesByGroupId_UserIsAuthorized_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/message-api/group-messages/by-group-id/1")
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
                        [
                            {"id": 1, "text": "Text 1", "authorId": "j.dewar", "groupId": 1},
                            {"id": 2, "text": "Text 2", "authorId": "j.dewar", "groupId": 1},
                            {"id": 3, "text": "Text 3", "authorId": "j.daniels", "groupId": 1}
                        ]
                        """)
                );
    }

    @Test
    @Sql("/sql/groupMessages.sql")
    void getGroupMessagesByGroupId_UserIsAuthorized_UserIsNotGroupParticipant_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/message-api/group-messages/by-group-id/1")
                .with(jwt().jwt(builder -> builder.subject("d.susan")));

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
    @Sql("/sql/groupMessages.sql")
    void getGroupMessagesByGroupId_UserIsAuthorized_GroupDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/message-api/group-messages/by-group-id/1")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.notFound().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Группа не найдена"}
                        """)
                );

    }

    @Test
    @Sql("/sql/groupMessages.sql")
    void getGroupMessagesByGroupId_UserIsNotAuthorized_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/message-api/group-messages/by-group-id/1");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void createGroupMessage_UserIsAuthorized_PayloadIsValid_ReturnsCreated() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/group-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Text", "groupId": 1}
                """)
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
                        status().isCreated(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                        {"id": 1, "text": "Text", "authorId": "j.dewar", "groupId": 1}
                        """)
                );

        assertTrue(groupMessageRepository.findById(1L).isPresent());
    }

    @Test
    void createGroupMessage_UserIsAuthorized_PayloadIsValid_UserIsNotGroupParticipant_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/group-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Text", "groupId": 1}
                """)
                .with(jwt().jwt(builder -> builder.subject("d.susan")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.badRequest().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Вы не можете посылать сообщения в эту группу, так как не являетесь участником группы"}
                        """)
                );
    }

    @Test
    void createGroupMessage_UserIsAuthorized_PayloadIsValid_GroupDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/group-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Text", "groupId": 1}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/groups/1"))
                .willReturn(WireMock.notFound().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "При созданий сообщения произошла ошибка: Группа не существует"}
                        """)
                );
    }

    @Test
    void createGroupMessage_UserIsNotAuthorized_PayloadIsValid_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/group-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Text", "groupId": 1}
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

        assertFalse(groupMessageRepository.findById(1L).isPresent());
    }

    @Test
    void createGroupMessage_UserIsAuthorized_PayloadIsInvalid_TextIsNull_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/group-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": null, "groupId": 1}
                """)
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

        assertFalse(groupMessageRepository.findById(1L).isPresent());
    }

    @Test
    void createGroupMessage_UserIsAuthorized_PayloadIsInvalid_TextIsBlank_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/group-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "         ", "groupId": 1}
                """)
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

        assertFalse(groupMessageRepository.findById(1L).isPresent());
    }

    @Test
    void createGroupMessage_UserIsAuthorized_PayloadIsInvalid_TextIsLessThan1_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/group-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "", "groupId": 1}
                """)
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
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {
                            "detail": "Плохой запрос",
                            "errors": [
                                "Текст сообщения не может быть пустым",
                                "Длина сообщения должна быть между 1 и 2000 символами"
                            ]
                        }
                        """)
                );

        assertFalse(groupMessageRepository.findById(1L).isPresent());
    }

    @Test
    void createGroupMessage_UserIsAuthorized_PayloadIsInvalid_TextIsGreaterThan2000_ReturnsBadRequest() throws Exception {
        String text = "a".repeat(2001);
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/group-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "%s", "groupId": 1}
                """.formatted(text))
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
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {
                            "detail": "Плохой запрос",
                            "errors": [
                                "Длина сообщения должна быть между 1 и 2000 символами"
                            ]
                        }
                        """)
                );

        assertFalse(groupMessageRepository.findById(1L).isPresent());
    }

    @Test
    void createGroupMessage_UserIsAuthorized_PayloadIsInvalid_GroupIdIsNull_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/group-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Text", "groupId": null}
                """)
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
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {
                            "detail": "Плохой запрос",
                            "errors": [
                                "Группа сообщения должна быть указана"
                            ]
                        }
                        """)
                );

        assertFalse(groupMessageRepository.findById(1L).isPresent());
    }
}