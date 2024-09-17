package com.doni.messenger.controller;

import com.doni.messenger.repository.GroupRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GroupsRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    GroupRepository groupRepository;

    @Test
    @Sql("/sql/groups.sql")
    void getGroupsByUserId_UserIsAuthorized_ReturnsOk() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/messenger-api/groups")
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                [
                                    {"id": 1, "title": "Title 1", "description": "Description 1"},
                                    {"id": 3, "title": "Title 3", "description": "Description 3"}
                                ]
                                """
                        )
                );
    }

    @Test
    void getGroupsByUserId_UserIsNotAuthorized_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/messenger-api/groups");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void createGroup_UserIsAuthorized_PayloadIsValid_ReturnsCreated() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/messenger-api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "Title", "description": "Description"}
                """)
                .with(jwt().jwt(builder -> builder.subject("j.dewar")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        header().string(HttpHeaders.LOCATION, "http://localhost/messenger-api/groups/1"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                {"id": 1, "title": "Title", "description": "Description", "ownerId": "j.dewar"}
                                """
                        )
                );

        groupRepository.findById(1)
                .ifPresent(group -> {
                    System.out.println(group.getGroupMembers());
                    assertEquals(1, group.getGroupMembers().size());
                });
    }

    @Test
    void createGroup_UserIsNotAuthorized_PayloadIsValid_ReturnsUnauthorized() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/messenger-api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "Title", "description": "Description"}
                """);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void createGroup_UserIsAuthorized_PayloadIsInvalid_TitleIsNull_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/messenger-api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": null, "description": "Description"}
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
                                        "Название группы не должно быть пустым",
                                        "Название группы должно быть указано"
                                    ]
                                }
                                """
                        )
                );
    }

    @Test
    void createGroup_UserIsAuthorized_PayloadIsInvalid_TitleIsBlank_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/messenger-api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "     ", "description": "Description"}
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
                                        "Название группы не должно быть пустым"
                                    ]
                                }
                                """
                        )
                );
    }

    @Test
    void createGroup_UserIsAuthorized_PayloadIsInvalid_TitleIsLessThan1_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/messenger-api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "", "description": "Description"}
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
                                        "Название группы не должно быть пустым",
                                        "Название группы должно быть между 1 и 100 символами"
                                    ]
                                }
                                """
                        )
                );
    }

    @Test
    void createGroup_UserIsAuthorized_PayloadIsInvalid_TitleIsGreaterThan100_ReturnsBadRequest() throws Exception {
        String title = "a".repeat(101);
        var requestBuilder = MockMvcRequestBuilders.post("/messenger-api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "%s", "description": "Description"}
                """.formatted(title))
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
                                        "Название группы должно быть между 1 и 100 символами"
                                    ]
                                }
                                """
                        )
                );
    }

    @Test
    void createGroup_UserIsAuthorized_PayloadIsInvalid_DescriptionIsGreaterThan2000_ReturnsBadRequest() throws Exception {
        String description = "a".repeat(2001);
        var requestBuilder = MockMvcRequestBuilders.post("/messenger-api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"title": "Title", "description": "%s"}
                """.formatted(description))
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
                                        "Описание группы должно быть между 0 и 2000 символами"
                                    ]
                                }
                                """
                        )
                );
    }
}