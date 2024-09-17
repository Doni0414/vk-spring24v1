package com.doni.feedback.repository;

import com.doni.feedback.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Integer> {
    List<Like> findAllByPublicationId(Integer publicationId);

    Optional<Like> findByPublicationIdAndUserId(Integer publicationId, String userId);

    void deleteByPublicationIdAndUserId(Integer publicationId, String userId);
}
