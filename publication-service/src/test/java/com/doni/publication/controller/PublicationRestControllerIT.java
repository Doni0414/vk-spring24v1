package com.doni.publication.controller;

import com.doni.publication.service.PublicationService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/sql/publications.sql")
class PublicationRestControllerIT {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    PublicationService publicationService;

    @Test
    void getProduct_UserIsAuthorized_PublicationExists_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/publication-api/publications/1")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                {"id": 1, "title": "Title 1", "description": "Description 1", "userId": "j.dewar"}
                                """
                        )
                );
    }

    @Test
    void getProduct_UserIsAuthorized_PublicationDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/publication-api/publications/100")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

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
    void getProduct_UserIsNotAuthorized_PublicationExists_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/publication-api/publications/1");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void getProduct_UserIsNotAuthorized_PublicationDoesNotExist_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/publication-api/publications/100");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void updatePublication_UserIsAuthorized_UserIsOwner_PayloadIsValid_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/publication-api/publications/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "Updated title", "description": "Updated description"}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );
    }

    @Test
    void updatePublication_UserIsNotAuthorized_UserIsOwner_PayloadIsValid_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/publication-api/publications/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "Updated title", "description": "Updated description"}
                """);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void updatePublication_UserIsAuthorized_PublicationDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/publication-api/publications/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "Updated title", "description": "Updated description"}
                """).with(jwt().jwt(builder -> builder.subject("j.dewar")));

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
    void updatePublication_UserIsAuthorized_UserIsNotOwner_PayloadIsValid_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/publication-api/publications/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "Updated title", "description": "Updated description"}
                """).with(jwt().jwt(builder -> builder.subject("j.daniels")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {
                                    "detail": "Данный пользователь не является автором этого поста"
                                }
                                """
                        )
                );
    }

    @Test
    void createPublication_UserIsAuthorized_UserIsOwner_PayloadIsInvalid_TitleIsNull_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/publication-api/publications/1")
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
    void createPublication_UserIsAuthorized_UserIsOwner_PayloadIsInvalid_TitleIsBlank_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/publication-api/publications/1")
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
    void createPublication_UserIsAuthorized_UserIsOwner_PayloadIsInvalid_TitleIsLesserThan3_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/publication-api/publications/1")
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
    void createPublication_UserIsAuthorized_UserIsOwner_PayloadIsInvalid_TitleIsGreaterThan200_ReturnsBadRequest() throws Exception {
        String title = "a".repeat(201);
        var requestBuilder = MockMvcRequestBuilders.patch("/publication-api/publications/1")
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
    void createPublication_UserIsAuthorized_UserIsOwner_PayloadIsInvalid_DescriptionIsGreaterThan2000_ReturnsBadRequest() throws Exception {
        String description = "a".repeat(2001);
        var requestBuilder = MockMvcRequestBuilders.patch("/publication-api/publications/1")
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

    @Test
    void deletePublication_UserIsAuthorized_UserIsOwner_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/publication-api/publications/1")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );

        assertEquals(2, publicationService.findAllPublications().size());
    }

    @Test
    void deletePublication_UserIsAuthorized_PublicationDoesNotExist_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/publication-api/publications/100")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

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

        assertEquals(3, publicationService.findAllPublications().size());
    }

    @Test
    void deletePublication_UserIsNotAuthorized_UserIsOwner_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/publication-api/publications/1");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );

        assertEquals(3, publicationService.findAllPublications().size());
    }

    @Test
    void deletePublication_UserIsAuthorized_UserIsNotOwner_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/publication-api/publications/1")
                .with(jwt().jwt(builder -> builder.subject("j.daniels")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json(
                                """
                                {"detail": "Данный пользователь не является автором этого поста"}
                                """
                        )
                );

        assertEquals(3, publicationService.findAllPublications().size());
    }
}