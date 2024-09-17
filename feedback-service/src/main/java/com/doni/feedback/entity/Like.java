package com.doni.feedback.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_like")
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "publication_id", nullable = false)
    @NotNull(message = "{feedback-api.likes.create.errors.publication_id_is_null}")
    private Integer publicationId;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "{feedback-api.likes.create.errors.user_id_is_null}")
    private String userId;
}
