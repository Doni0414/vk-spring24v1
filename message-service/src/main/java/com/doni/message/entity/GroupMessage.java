package com.doni.message.entity;

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
@Table(name = "group_message")
public class GroupMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text")
    @NotNull(message = "{message-api.group-messages.create.errors.text_is_null}")
    @NotBlank(message = "{message-api.group-messages.create.errors.text_is_blank}")
    @Size(min = 1, max = 2000, message = "{message-api.group-messages.create.errors.text_has_invalid_size}")
    private String text;

    @Column(name = "author_id", nullable = false)
    private String authorId;

    @Column(name = "group_id", nullable = false)
    private Integer groupId;
}
