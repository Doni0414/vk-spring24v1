package com.doni.messenger.entity;

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
@Table(name = "chat")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id_1")
    @NotNull
    private String userId1;

    @Column(name = "user_id_2")
    @NotNull(message = "{messenger-api.chats.create.errors.user_id_is_null}")
    private String userId2;
}
