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
import wiremock.org.apache.hc.client5.http.impl.Wire;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WireMockTest(httpPort = 54321)
class LikesRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    @Sql("/sql/likes.sql")
    void getLikesByPublicationId_UserIsAuthorized_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/feedback-api/likes/by-publication-id/1")
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
                                    {"publicationId": 1, "userId": "j.dewar"},
                                    {"publicationId": 1, "userId": "j.daniels"}
                                ]
                                """
                        )
                );
    }

    @Test
    @Sql("/sql/likes.sql")
    void getLikesByPublicationId_UserIsAuthorized_PublicationDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/feedback-api/likes/by-publication-id/1")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/publication-api/publications/1"))
                .willReturn(WireMock.notFound().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Публикация не найдена"}
                                """
                        )
                );
    }

    @Test
    void getLikesByPublicationId_UserIsNotAuthorized_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/feedback-api/likes/by-publication-id/1");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void createLike_UserIsAuthorized_PayloadIsValid_ReturnsCreated() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/feedback-api/likes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"publicationId": 1}
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
                        header().string(HttpHeaders.LOCATION, "http://localhost/feedback-api/likes/1"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                {"publicationId": 1, "userId": "j.dewar"}
                                """
                        )
                );
    }

    @Test
    void createLike_UserIsAuthorized_PayloadIsValid_PublicationDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/feedback-api/likes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "publicationId": 100
                }
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/publication-api/publications/1"))
                .willReturn(WireMock.notFound().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                        {"detail": "При созданий лайка публикация не была найдена"}
                        """)
                );
    }

    @Test
    void createLike_UserIsNotAuthorized_PayloadIsValid_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/feedback-api/likes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"publicationId": 1}
                        """);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void createLike_UserIsAuthorized_PayloadIsInvalid_PublicationIsNull_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/feedback-api/likes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"publicationId": null}
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
                                        "Публикация лайка должна быть указана"
                                    ]
                                }
                                """
                        )
                );
    }

    @Test
    @Sql("/sql/likes.sql")
    void createLike_UserIsAuthorized_PayloadValid_LikeExists_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/feedback-api/likes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"publicationId": 1}
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
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {
                                    "detail": "Пользователь уже ставил лайк данной публикаций"
                                }
                                """
                        )
                );
    }
}