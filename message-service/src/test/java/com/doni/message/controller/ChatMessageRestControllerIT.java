package com.doni.message.controller;

import com.doni.message.repository.ChatMessageRepository;
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
@Sql("/sql/chatMessages.sql")
@WireMockTest(httpPort = 54321)
class ChatMessageRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Test
    void getChatMessage_UserIsAuthorized_ChatMessageExists_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/message-api/chat-messages/1")
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
                        {"id": 1, "text": "Text 1", "authorId": "j.dewar", "chatId": 1}
                        """)
                );
    }

    @Test
    void getChatMessage_UserIsAuthorized_UserIsNotChatParticipant_ChatMessageExists_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/message-api/chat-messages/1")
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
    void getChatMessage_UserIsNotAuthorized_ChatMessageExists_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/message-api/chat-messages/1");

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
    void getChatMessage_UserIsAuthorized_ChatMessageDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/message-api/chat-messages/100")
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
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {
                            "detail": "Сообщение не найдено"
                        }
                        """)
                );
    }

    @Test
    void updateChatMessage_UserIsAuthorized_ChatMessageExists_PayloadIsValid_UserIsOwner_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/chat-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Updated text"}
                """)
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
                        status().isNoContent()
                );

        chatMessageRepository.findById(1L)
                .ifPresent(chatMessage -> assertEquals("Updated text", chatMessage.getText()));
    }

    @Test
    void updateChatMessage_UserIsAuthorized_ChatMessageExists_UserIsNotChatParticipant_PayloadIsValid_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/chat-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Updated text"}
                """)
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
    void updateChatMessage_UserIsNotAuthorized_ChatMessageExists_PayloadIsValid_UserIsOwner_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/chat-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Updated text"}
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
    void updateChatMessage_UserIsAuthorized_ChatMessageDoesNotExist_PayloadIsValid_UserIsOwner_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/chat-messages/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Updated text"}
                """)
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
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Сообщение не найдено"}
                        """)
                );
    }

    @Test
    void updateChatMessage_UserIsAuthorized_ChatMessageExist_PayloadIsValid_UserIsNotOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/chat-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Updated text"}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.daniels")));

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
                        {"detail": "Пользователь не является автором сообщения"}
                        """)
                );

        chatMessageRepository.findById(1L)
                .ifPresent(chatMessage -> assertEquals("Text 1", chatMessage.getText()));
    }

    @Test
    void updateChatMessage_UserIsAuthorized_ChatMessageExist_PayloadIsInvalid_TextIsNull_UserIsOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/chat-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": null}
                """)
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

        chatMessageRepository.findById(1L)
                .ifPresent(chatMessage -> assertEquals("Text 1", chatMessage.getText()));
    }

    @Test
    void updateChatMessage_UserIsAuthorized_ChatMessageExist_PayloadIsInvalid_TextIsBlank_UserIsOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/chat-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "     "}
                """)
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

        chatMessageRepository.findById(1L)
                .ifPresent(chatMessage -> assertEquals("Text 1", chatMessage.getText()));
    }

    @Test
    void updateChatMessage_UserIsAuthorized_ChatMessageExist_PayloadIsInvalid_TextIsLessThan1_UserIsOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/chat-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": ""}
                """)
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

        chatMessageRepository.findById(1L)
                .ifPresent(chatMessage -> assertEquals("Text 1", chatMessage.getText()));
    }

    @Test
    void updateChatMessage_UserIsAuthorized_ChatMessageExist_PayloadIsInvalid_TextIsGreaterThan2000_UserIsOwner_ReturnsBadRequest() throws Exception {
        String text = "a".repeat(2001);
        var requestBuilder = MockMvcRequestBuilders.patch("/message-api/chat-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "%s"}
                """.formatted(text))
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

        chatMessageRepository.findById(1L)
                .ifPresent(chatMessage -> assertEquals("Text 1", chatMessage.getText()));
    }

    @Test
    void deleteChatMessage_UserIsAuthorized_ChatMessageExists_UserIsOwner_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/message-api/chat-messages/1")
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
                        status().isNoContent()
                );

        assertFalse(chatMessageRepository.existsById(1L));
    }

    @Test
    void deleteChatMessage_UserIsAuthorized_ChatMessageExists_UserIsNotChatParticipant_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/message-api/chat-messages/1")
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
    void deleteChatMessage_UserIsNotAuthorized_ChatMessageExists_UserIsOwner_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/message-api/chat-messages/1");

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

        assertTrue(chatMessageRepository.existsById(1L));
    }

    @Test
    void deleteChatMessage_UserIsAuthorized_ChatMessageDoesNotExists_UserIsOwner_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/message-api/chat-messages/100")
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
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "Сообщение не найдено"}
                        """)
                );
    }

    @Test
    void deleteChatMessage_UserIsAuthorized_ChatMessageExists_UserIsNotOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/message-api/chat-messages/1")
                .with(jwt().jwt(builder -> builder.subject("j.daniels")));

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
                        {"detail": "Пользователь не является автором сообщения"}
                        """)
                );

        assertTrue(chatMessageRepository.existsById(1L));
    }
}