package com.doni.messenger.controller;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@Sql("/sql/chats.sql")
@ActiveProfiles("test")
class ChatRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getChat_UserIsAuthorized_ChatExists_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/messenger-api/chats/2")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                {"id": 2, "userId1": "j.dewar", "userId2": "j.daniels"}
                                """
                        )
                );
    }

    @Test
    void getChat_UserIsNotAuthorized_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/messenger-api/chats/2");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void getChat_UserIsAuthorized_ChatDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/messenger-api/chats/100")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Такой чат не существует"}
                                """
                        )
                );
    }

    @Test
    void getChat_UserIsAuthorized_UserIsNotChatParticipant_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/messenger-api/chats/2")
                .with(jwt().jwt(builder -> builder.subject("j.black")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Вы не являетесь участником данного чата"}
                                """
                        )
                );
    }

    @Test
    void deleteChat_UserIsAuthorized_UserIsParticipant_ChatExists_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/chats/2")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );
    }

    @Test
    void deleteChat_UserIsNotAuthorized_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/chats/2");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void deleteChat_UserIsAuthorized_UserIsParticipant_ChatDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/chats/100")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Такой чат не существует"}
                                """
                        )
                );
    }

    @Test
    void deleteChat_UserIsAuthorized_UserIsNotChatParticipant_ChatExists_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/messenger-api/chats/2")
                .with(jwt().jwt(builder -> builder.subject("j.black")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Вы не являетесь участником данного чата"}
                                """
                        )
                );
    }
}