package com.doni.publication.controller;

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
class PublicationsRestControllerIT {
    @Autowired
    MockMvc mockMvc;

    @Test
    @Sql("/sql/publications.sql")
    void getAllPublications_UserIsAuthorized_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/publication-api/publications")
                .with(jwt().jwt(builder -> builder
                        .subject("j.dewar")
                        .claim("foo", "bar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                [
                                {"id": 1, "title": "Title 1", "description": "Description 1"},
                                {"id": 2, "title": "Title 2", "description": "Description 2"},
                                {"id": 3, "title": "Title 3", "description": "Description 3"}
                                ]
                                """
                        )
                );
    }

    @Test
    @Sql("/sql/publications.sql")
    void getAllPublications_UserIsNotAuthorized_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/publication-api/publications");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    @Sql("/sql/publications.sql")
    void getAllPublicationsByUser_UserIsAuthorized_UserExists_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/publication-api/publications/by-user-id/j.dewar")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                [
                                    {"id": 1, "title": "Title 1", "description": "Description 1", userId: "j.dewar"},
                                    {"id": 2, "title": "Title 2", "description": "Description 2", userId: "j.dewar"}
                                ]
                                """
                        )
                );
    }

    @Test
    @Sql("/sql/publications.sql")
    void getAllPublicationsByUser_UserIsNotAuthorized_UserExists_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/publication-api/publications/by-user-id/j.dewar");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    @Sql("/sql/publications.sql")
    void getAllPublicationsByUser_UserIsAuthorized_UserDoesNotExist_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/publication-api/publications/by-user-id/johnny")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

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
    void createPublication_UserIsAuthorized_PayloadIsValid_ReturnsCreated() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/publication-api/publications")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "Title 1", "description": "Description 1"}
                """)
                .with(jwt().jwt(builder -> builder
                        .subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        header().string(HttpHeaders.LOCATION, "http://localhost/publication-api/publications/1"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                {"id": 1, "title": "Title 1", "description": "Description 1", "userId": "j.dewar"}
                                """
                        )
                );
    }

    @Test
    void createPublication_UserIsNotAuthorized_PayloadIsValid_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/publication-api/publications")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "Title 1", "description": "Description 1"}
                """);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void createPublication_UserIsAuthorized_PayloadIsInvalid_TitleIsNull_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/publication-api/publications")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": null, "description": "Description 1"}
                """)
                .with(jwt().jwt(builder -> builder
                        .subject("j.dewar")
                        .claim("foo", "bar")));

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
                                        "Название публикации не должно быть пустой",
                                        "Название публикации должно быть указано"
                                    ]
                                }
                                """
                        )
                );
    }

    @Test
    void createPublication_UserIsAuthorized_PayloadIsInvalid_TitleIsBlank_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/publication-api/publications")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "       ", "description": "Description 1"}
                """)
                .with(jwt().jwt(builder -> builder
                        .subject("j.dewar")
                        .claim("foo", "bar")));

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
                                        "Название публикации не должно быть пустой"
                                    ]
                                }
                                """
                        )
                );
    }

    @Test
    void createPublication_UserIsAuthorized_PayloadIsInvalid_TitleIsLesserThan3_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/publication-api/publications")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "ok", "description": "Description 1"}
                """)
                .with(jwt().jwt(builder -> builder
                        .subject("j.dewar")
                        .claim("foo", "bar")));

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
                                        "Название публикации должно быть между 3 и 200 символами"
                                    ]
                                }
                                """
                        )
                );
    }

    @Test
    void createPublication_UserIsAuthorized_PayloadIsInvalid_TitleIsGreaterThan200_ReturnsBadRequest() throws Exception {
        String title = "a".repeat(201);
        var requestBuilder = MockMvcRequestBuilders.post("/publication-api/publications")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "%s", "description": "Description 1"}
                """.formatted(title))
                .with(jwt().jwt(builder -> builder
                        .subject("j.dewar")
                        .claim("foo", "bar")));

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
                                        "Название публикации должно быть между 3 и 200 символами"
                                    ]
                                }
                                """
                        )
                );
    }

    @Test
    void createPublication_UserIsAuthorized_PayloadIsInvalid_DescriptionIsGreaterThan2000_ReturnsBadRequest() throws Exception {
        String description = "a".repeat(2001);
        var requestBuilder = MockMvcRequestBuilders.post("/publication-api/publications")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "Title 1", "description": "%s"}
                """.formatted(description))
                .with(jwt().jwt(builder -> builder
                        .subject("j.dewar")
                        .claim("foo", "bar")));

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
                                        "Описание публикации должно быть между 0 и 2000 символами"
                                    ]
                                }
                                """
                        )
                );
    }
}