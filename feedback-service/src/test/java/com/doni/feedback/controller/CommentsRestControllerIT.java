package com.doni.feedback.controller;

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
class CommentsRestControllerIT {
    @Autowired
    MockMvc mockMvc;

    @Test
    @Sql("/sql/comments.sql")
    void getAllCommentsByPublicationId_UserIsAuthorized_PublicationExists_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/feedback-api/comments/by-publication-id/1")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/publication-api/publications/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "userId": "j.daniels"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                [
                                    {"id": 1, "text": "Text 1", "publicationId": 1, "userId": "j.dewar"},
                                    {"id": 2, "text": "Text 2", "publicationId": 1, "userId": "j.dewar"}
                                ]
                                """
                        )
                );
    }

    @Test
    @Sql("/sql/comments.sql")
    void getAllCommentsByPublicationId_UserIsNotAuthorized_PublicationExists_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/feedback-api/comments/by-publication-id/1");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    @Sql("/sql/comments.sql")
    void getAllCommentsByPublicationId_UserIsAuthorized_PublicationDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/feedback-api/comments/by-publication-id/100")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/publication-api/publications/%d".formatted(100)))
                .willReturn(WireMock.notFound()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {
                                    "detail": "Публикация не найдена"
                                }
                                """
                        )
                );
    }

    @Test
    void createComment_UserIsAuthorized_PayloadIsValid_ReturnsCreated() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/feedback-api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Text 1", "publicationId": 1}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/publication-api/publications/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "userId": "j.daniels"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        header().string(HttpHeaders.LOCATION, "http://localhost/feedback-api/comments/1"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                {"id": 1, "text": "Text 1", "publicationId": 1, "userId": "j.dewar"}
                                """
                        )
                );
    }

    @Test
    void createComment_UserIsAuthorized_PayloadIsValid_PublicationDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/feedback-api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Text 1", "publicationId": 100}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/publication-api/publications/100"))
                .willReturn(WireMock.notFound().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {
                                    "detail": "При созданий комментария публикация не была найдена"
                                }
                                """
                        )
                );
    }

    @Test
    void createComment_UserIsNotAuthorized_PayloadIsValid_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/feedback-api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "Text 1", "publicationId": 1}
                """);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void createComment_UserIsAuthorized_PayloadIsInvalid_TextIsNull_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/feedback-api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": null, "publicationId": 1}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/publication-api/publications/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "userId": "j.daniels"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        header().doesNotExist(HttpHeaders.LOCATION),
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
    void createComment_UserIsAuthorized_PayloadIsInvalid_TextIsBlank_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/feedback-api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "        ", "publicationId": 1}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/publication-api/publications/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "userId": "j.daniels"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        header().doesNotExist(HttpHeaders.LOCATION),
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
    void createComment_UserIsAuthorized_PayloadIsInvalid_TextIsLesserThan1_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/feedback-api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "", "publicationId": 1}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/publication-api/publications/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "userId": "j.daniels"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        header().doesNotExist(HttpHeaders.LOCATION),
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
    void createComment_UserIsAuthorized_PayloadIsInvalid_TextIsGreaterThan2000_ReturnsBadRequest() throws Exception {
        String text = "a".repeat(2001);
        var requestBuilder = MockMvcRequestBuilders.post("/feedback-api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"text": "%s", "publicationId": 1}
                """.formatted(text))
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/publication-api/publications/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Title",
                            "description": "Description",
                            "userId": "j.daniels"
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        header().doesNotExist(HttpHeaders.LOCATION),
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
}