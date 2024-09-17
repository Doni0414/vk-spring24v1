package com.doni.feedback.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "text")
    @NotNull(message = "{feedback-api.comments.create.errors.text_is_null}")
    @NotBlank(message = "{feedback-api.comments.create.errors.text_is_blank}")
    @Size(min = 1, max = 2000, message = "{feedback-api.comments.create.errors.text_has_invalid_size}")
    private String text;

    @Column(name = "publication_id", nullable = false)
    @NotNull(message = "{feedback-api.comments.create.errors.publication_id_is_null}")
    private Integer publicationId;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "{feedback-api.comments.create.errors.user_id_is_null}")
    private String userId;
}
