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
@Table(name = "chat_message")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text", nullable = false)
    @NotNull(message = "{message-api.chat-messages.create.errors.text_is_null}")
    @NotBlank(message = "{message-api.chat-messages.create.errors.text_is_blank}")
    @Size(min = 1, max = 2000, message = "{message-api.chat-messages.create.errors.text_has_invalid_size}")
    private String text;

    @Column(name = "author_id", nullable = false)
    private String authorId;

    @Column(name = "chat_id", nullable = false)
    private Integer chatId;
}
