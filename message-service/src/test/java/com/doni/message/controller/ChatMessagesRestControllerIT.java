package com.doni.message.controller;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WireMockTest(httpPort = 54321)
class ChatMessagesRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    @Sql("/sql/chatMessages.sql")
    void getChatMessagesByChatId_UserIsAuthorized_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/message-api/chat-messages/by-chat-id/1")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/chats/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "userId1": "j.daniels",
                            "userId2": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                        [
                            {"id": 1, "text": "Text 1", "authorId": "j.dewar", "chatId": 1},
                            {"id": 2, "text": "Text 2", "authorId": "j.dewar", "chatId": 1},
                            {"id": 3, "text": "Text 3", "authorId": "j.daniels", "chatId": 1}
                        ]
                        """)
                );
    }

    @Test
    @Sql("/sql/chatMessages.sql")
    void getChatMessagesByChatId_UserIsAuthorized_UserIsNotChatParticipant_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/message-api/chat-messages/by-chat-id/1")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/chats/1"))
                .willReturn(WireMock.badRequest().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Вы не имеете доступ к сообщению этого чата, так как не являетесь участником чата"}
                        """)
                );
    }

    @Test
    @Sql("/sql/chatMessages.sql")
    void getChatMessagesByChatId_UserIsAuthorized_ChatDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/message-api/chat-messages/by-chat-id/1")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/chats/1"))
                .willReturn(WireMock.notFound().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Чат не найден"}
                        """)
                );
    }

    @Test
    void getChatMessagesByChatId_UserIsNotAuthorized_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/message-api/chat-messages/by-chat-id/1");

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/chats/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "userId1": "j.daniels",
                            "userId2": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void createChatMessage_UserIsAuthorized_PayloadIsValid_ReturnsCreated() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/chat-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Text", "chatId": 1}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/chats/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "userId1": "j.daniels",
                            "userId2": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        header().string(HttpHeaders.LOCATION, "http://localhost/message-api/chat-messages/1"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                        {"id": 1, "text": "Text", "authorId": "j.dewar", "chatId": 1}
                        """)
                );
    }

    @Test
    void createChatMessage_UserIsAuthorized_UserIsNotChatParticipant_PayloadIsValid_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/chat-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Text", "chatId": 1}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/chats/1"))
                .willReturn(WireMock.badRequest().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Вы не имеете доступ к сообщению этого чата, так как не являетесь участником чата"}
                        """)
                );
    }

    @Test
    void createChatMessage_UserIsAuthorized_PayloadIsValid_ChatDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/chat-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Text", "chatId": 1}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/chats/1"))
                .willReturn(WireMock.notFound().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "При созданий сообщения произошла ошибка: Чат не существует"}
                        """)
                );
    }

    @Test
    void createChatMessage_UserIsNotAuthorized_PayloadIsValid_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/chat-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Text", "chatId": 1}
                """);

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/chats/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "userId1": "j.daniels",
                            "userId2": "j.dewar"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void createChatMessage_UserIsAuthorized_PayloadIsInvalid_TextIsNull_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/chat-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": null, "chatId": 1}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/chats/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "userId1": "j.daniels",
                            "userId2": "j.dewar"
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
                                "Текст сообщения не должен быть пустым",
                                "Текст сообщения должен быть указан"
                            ]
                        }
                        """)
                );
    }

    @Test
    void createChatMessage_UserIsAuthorized_PayloadIsInvalid_TextIsBlank_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/chat-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "          ", "chatId": 1}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/chats/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "userId1": "j.daniels",
                            "userId2": "j.dewar"
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
                                "Текст сообщения не должен быть пустым"
                            ]
                        }
                        """)
                );
    }

    @Test
    void createChatMessage_UserIsAuthorized_PayloadIsInvalid_TextIsLessThan1_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/chat-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "", "chatId": 1}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/chats/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "userId1": "j.daniels",
                            "userId2": "j.dewar"
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
                                "Текст сообщения не должен быть пустым",
                                "Длина текста сообщения должна быть между 1 и 2000 символами"
                            ]
                        }
                        """)
                );
    }

    @Test
    void createChatMessage_UserIsAuthorized_PayloadIsInvalid_TextIsGreaterThan2000_ReturnsBadRequest() throws Exception {
        String text = "a".repeat(2001);
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/chat-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "%s", "chatId": 1}
                """.formatted(text)).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/chats/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "userId1": "j.daniels",
                            "userId2": "j.dewar"
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
                                "Длина текста сообщения должна быть между 1 и 2000 символами"
                            ]
                        }
                        """)
                );
    }

    @Test
    void createChatMessage_UserIsAuthorized_PayloadIsInvalid_ChatIdIsNull_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/message-api/chat-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Text", "chatId": null}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/messenger-api/chats/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "userId1": "j.daniels",
                            "userId2": "j.dewar"
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
                                "Чат для сообщения должен быть указан"
                            ]
                        }
                        """)
                );
    }
}