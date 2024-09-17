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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/sql/comments.sql")
class CommentRestControllerIT {
    @Autowired
    MockMvc mockMvc;
    
    @Test
    void getComment_UserIsAuthorized_CommentExists_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/feedback-api/comments/1")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                {"id": 1, "text": "Text 1", "publicationId": 1, "userId": "j.dewar"}
                                """
                        )
                );
    }

    @Test
    void getComment_UserIsNotAuthorized_CommentExists_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/feedback-api/comments/1");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void getComment_UserIsAuthorized_CommentDoesNotExists_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/feedback-api/comments/100")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Комментарий не найден"}
                                """
                        )
                );
    }

    @Test
    void updateComment_UserIsAuthorized_UserIsOwner_PayloadIsValid_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/feedback-api/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Updated text"}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );
    }

    @Test
    void updateComment_UserIsNotAuthorized_PayloadIsValid_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/feedback-api/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Updated text"}
                """);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void updateComment_UserIsAuthorized_CommentDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/feedback-api/comments/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Updated text"}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Комментарий не найден"}
                                """
                        )
                );
    }

    @Test
    void updateComment_UserIsAuthorized_UserIsNotOwner_PayloadIsValid_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/feedback-api/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Updated text"}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.daniels")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Данный пользователь не является автором этого комментария"}
                                """
                        )
                );
    }

    @Test
    void updateComment_UserIsAuthorized_PayloadIsInvalid_TextIsNull_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/feedback-api/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": null}
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
                                        "Комментарий не может быть пустым",
                                        "Текст комментария должен быть указан"
                                    ]
                                }
                                """
                        )
                );
    }

    @Test
    void updateComment_UserIsAuthorized_PayloadIsInvalid_TextIsBlank_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/feedback-api/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "      "}
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
                                        "Комментарий не может быть пустым"
                                    ]
                                }
                                """
                        )
                );
    }

    @Test
    void updateComment_UserIsAuthorized_PayloadIsInvalid_TextIsLesserThan1_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/feedback-api/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": ""}
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
                                        "Комментарий не может быть пустым",
                                        "Длина комментария должна быть между 1 и 2000 символами"
                                    ]
                                }
                                """
                        )
                );
    }

    @Test
    void updateComment_UserIsAuthorized_PayloadIsInvalid_TextIsGreaterThan2000_ReturnsBadRequest() throws Exception {
        String text = "a".repeat(2001);
        var requestBuilder = MockMvcRequestBuilders.patch("/feedback-api/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "%s"}
                """.formatted(text))
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
                                        "Длина комментария должна быть между 1 и 2000 символами"
                                    ]
                                }
                                """
                        )
                );
    }

    @Test
    void deleteComment_UserIsAuthorized_UserIsOwner_CommentExist_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/feedback-api/comments/1")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );
    }

    @Test
    void deleteComment_UserIsNotAuthorized_UserIsOwner_CommentExist_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/feedback-api/comments/1");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void deleteComment_UserIsAuthorized_UserIsNotOwner_CommentExist_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/feedback-api/comments/1")
                .with(jwt().jwt(builder -> builder.subject("j.daniels")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Данный пользователь не является автором этого комментария"}
                                """
                        )
                );
    }

    @Test
    void deleteComment_UserIsAuthorized_UserIsOwner_CommentDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/feedback-api/comments/100")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Комментарий не найден"}
                                """
                        )
                );
    }
}