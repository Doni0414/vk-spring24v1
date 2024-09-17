package com.doni.messenger.controller;

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
@Sql("/sql/chats.sql")
@ActiveProfiles("test")
class ChatsRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getChatsByUserId_UserIsAuthorized_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/messenger-api/chats")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                [
                                    {"userId1": "j.dewar", "userId2": "j.daniels"},
                                    {"userId1": "r.susan", "userId2": "j.dewar"}
                                ]
                                """
                        )
                );
    }

    @Test
    void getChatsByUserId_UserIsAuthorized_UserHasNoChats_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/messenger-api/chats")
                .with(jwt().jwt(builder -> builder.subject("j.black")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                []
                                """
                        )
                );
    }

    @Test
    void getChatsByUserId_UserIsNotAuthorized_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/messenger-api/chats");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void createChat_UserIsAuthorized_ReturnsCreated() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/messenger-api/chats")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"userId": "j.black"}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        header().string(HttpHeaders.LOCATION, "http://localhost/messenger-api/chats/1"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                {"id": 1, "userId1": "j.black", "userId2": "j.dewar"}
                                """
                        )
                );
    }

    @Test
    void createChat_UserIsNotAuthorized_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/messenger-api/chats")
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
    void createChat_UserIsAuthorized_PayloadIsInvalid_UserIdIsNull_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/messenger-api/chats")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"userId": null}
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
                                        "Пользователь должен быть указан"
                                    ]
                                }
                                """
                        )
                );
    }

    @Test
    void createChat_UserIsAuthorized_PayloadIsInvalid_ChatAlreadyExists1_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/messenger-api/chats")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"userId": "j.daniels"}
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
                                    "detail": "Чат с данным пользователем уже существует"
                                }
                                """
                        )
                );
    }

    @Test
    void createChat_UserIsAuthorized_PayloadIsInvalid_ChatAlreadyExists2_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/messenger-api/chats")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"userId": "j.dewar"}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.daniels")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {
                                    "detail": "Чат с данным пользователем уже существует"
                                }
                                """
                        )
                );
    }
}