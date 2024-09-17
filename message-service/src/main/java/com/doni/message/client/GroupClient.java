package com.doni.message.client;

import com.doni.message.entity.Group;

import java.util.Optional;

public interface GroupClient {
    Optional<Group> findGroup(Integer groupId);
}
