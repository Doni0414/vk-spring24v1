package com.doni.messenger.service;

import com.doni.messenger.dto.GroupReadDto;
import com.doni.messenger.entity.Group;
import com.doni.messenger.entity.GroupMember;
import com.doni.messenger.exception.UserIsAlreadyGroupMemberException;
import com.doni.messenger.exception.UserIsNotGroupOwnerException;
import com.doni.messenger.exception.UserIsNotGroupParticipantException;
import com.doni.messenger.mapper.GroupMapper;
import com.doni.messenger.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DefaultGroupService implements GroupService {
    private final GroupMapper groupMapper;
    private final GroupRepository groupRepository;

    @Override
    public List<GroupReadDto> findAllByUserId(String userId) {
        List<Group> groups = groupRepository.findAllByUserId(userId);
        return groupMapper.entitiesToGroupReadDtos(groups);
    }

    @Override
    @Transactional
    public GroupReadDto createGroup(String title, String description, String userId) {
        GroupMember groupMember = GroupMember.builder()
                .userId(userId)
                .build();
        Group group = Group.builder()
                .title(title)
                .description(description)
                .ownerId(userId)
                .build();
        group.addGroupMember(groupMember);
        Group saved = groupRepository.save(group);
        return groupMapper.entityToGroupReadDto(saved);
    }

    @Override
    public Optional<GroupReadDto> findGroup(Integer groupId, String userId) {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        optionalGroup.ifPresent(group -> {
            System.out.println("UserId: " + userId);
            System.out.println(group.getGroupMembers());
            int size = group.getGroupMembers().stream()
                    .filter(groupMember -> groupMember.getUserId().equals(userId))
                    .toList()
                    .size();
            if (size == 0) {
                throw new UserIsNotGroupParticipantException("messenger-api.groups.errors.user_is_not_participant");
            }
        });
        return optionalGroup
                .map(groupMapper::entityToGroupReadDto);
    }

    @Override
    @Transactional
    public void updateGroup(Integer groupId, String title, String description, String userId) {
        groupRepository.findById(groupId)
                .ifPresent(group -> {
                    if (!group.getOwnerId().equals(userId)) {
                        throw new UserIsNotGroupOwnerException("messenger-api.groups.update.errors.user_is_not_owner");
                    } else {
                        group.setTitle(title);
                        group.setDescription(description);
                    }
                });
    }

    @Override
    @Transactional
    public void deleteGroup(Integer groupId, String userId) {
        groupRepository.findById(groupId)
                .ifPresent(group -> {
                    if (!group.getOwnerId().equals(userId)) {
                        throw new UserIsNotGroupOwnerException("messenger-api.groups.delete.errors.user_is_not_owner");
                    }
                    groupRepository.deleteById(groupId);
                });
    }

    @Override
    @Transactional
    public void addUser(Integer groupId, String userId, String ownerId) {
        groupRepository.findById(groupId)
                .ifPresent(group -> {
                    if (!group.getOwnerId().equals(ownerId)) {
                        throw new UserIsNotGroupOwnerException("messenger-api.groups.add-user.errors.user_is_not_owner");
                    }

                    int size = group.getGroupMembers().stream()
                            .filter(groupMember -> groupMember.getUserId().equals(userId))
                            .toList()
                            .size();

                    if (size != 0) {
                        throw new UserIsAlreadyGroupMemberException("messenger-api.groups.add-user.errors.user_is_already_in_group");
                    }
                    group.addGroupMember(GroupMember.builder()
                            .userId(userId)
                            .build());
                });
    }

    @Override
    @Transactional
    public void kickUser(Integer groupId, String userId, String ownerId) {
        groupRepository.findById(groupId)
                .ifPresent(group -> {
                    if (!group.getOwnerId().equals(ownerId)) {
                        throw new UserIsNotGroupOwnerException("messenger-api.groups.kick-user.errors.user_is_not_owner");
                    }

                    int size = group.getGroupMembers().stream()
                            .filter(groupMember -> groupMember.getUserId().equals(userId))
                            .toList()
                            .size();

                    if (size == 0) {
                        throw new UserIsNotGroupParticipantException("messenger-api.groups.kick-user.errors.user_is_not_participant");
                    }
                    group.getGroupMembers().removeIf(groupMember -> groupMember.getUserId().equals(userId));
                });
    }

    @Override
    @Transactional
    public void leaveGroup(Integer groupId, String userId) {
        groupRepository.findById(groupId)
                .ifPresent(group -> {
                    int size = group.getGroupMembers().stream()
                            .filter(groupMember -> groupMember.getUserId().equals(userId))
                            .toList()
                            .size();

                    if (size == 0) {
                        throw new UserIsNotGroupParticipantException("messenger-api.groups.leave-group.errors.user_is_not_participant");
                    }
                    group.getGroupMembers().removeIf(groupMember -> groupMember.getUserId().equals(userId));
                });
    }
}
