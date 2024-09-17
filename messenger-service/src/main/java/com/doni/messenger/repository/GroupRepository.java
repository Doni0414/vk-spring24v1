package com.doni.messenger.repository;

import com.doni.messenger.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {
    @Query("select g from Group g " +
            "join g.groupMembers gm " +
            "where gm.userId = ?1")
    List<Group> findAllByUserId(String userId);
}
