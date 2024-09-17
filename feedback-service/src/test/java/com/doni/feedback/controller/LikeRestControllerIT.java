package com.doni.feedback.controller;

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
@Sql("/sql/likes.sql")
@ActiveProfiles("test")
class LikeRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getLikeByPublicationIdAndUserId_UserIsAuthorized_LikeExists_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/feedback-api/likes/by-publication-id/1/and-user-id/j.dewar")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                {"publicationId": 1, "userId": "j.dewar"}
                                """
                        )
                );
    }

    @Test
    void getLikeByPublicationIdAndUserId_UserIsNotAuthorized_LikeExists_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/feedback-api/likes/by-publication-id/1/and-user-id/j.dewar");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void getLikeByPublicationIdAndUserId_UserIsAuthorized_LikeDoesNotExists_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/feedback-api/likes/by-publication-id/1/and-user-id/jacob")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Данный пользователь еще не ставил лайк данной публикаций"}
                                """
                        )
                );
    }

    @Test
    void deleteLike_UserIsAuthorized_LikeExists_UserIsOwner_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/feedback-api/likes/by-publication-id/1/and-user-id/j.dewar")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );
    }

    @Test
    void deleteLike_UserIsNotAuthorized_LikeExists_UserIsOwner_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/feedback-api/likes/by-publication-id/1/and-user-id/j.dewar");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void deleteLike_UserIsAuthorized_LikeDoesNotExists_UserIsOwner_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/feedback-api/likes/by-publication-id/100/and-user-id/j.dewar")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Данный пользователь еще не ставил лайк данной публикаций"}
                                """
                        )
                );
    }

    @Test
    void deleteLike_UserIsAuthorized_LikeExists_UserIsNotOwner_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/feedback-api/likes/by-publication-id/1/and-user-id/j.dewar")
                .with(jwt().jwt(builder -> builder.subject("j.daniels")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Данный пользователь не является владельцем лайка"}
                                """
                        )
                );
    }
}