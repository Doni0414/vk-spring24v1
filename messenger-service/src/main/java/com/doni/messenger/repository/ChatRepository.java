package com.doni.messenger.repository;

import com.doni.messenger.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {

    List<Chat> findAllByUserId1OrUserId2(String userId1, String userId2);

    @Query("select c from Chat c where c.userId1 = ?1 and c.userId2 = ?2 or c.userId1 = ?2 and c.userId2 = ?1")
    Optional<Chat> findByUserId1AndUserId2OrUserId2AndAndUserId1(String userId1, String userId2);
}
