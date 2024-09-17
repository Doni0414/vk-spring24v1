package com.doni.messenger.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "t_group_member")
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "user_id", nullable = false)
    private String userId;

    public GroupMember(Integer id, Group group, String userId) {
        this.id = id;
        this.group = group;
        this.userId = userId;
    }

    public GroupMember() {
    }

    public static GroupMemberBuilder builder() {
        return new GroupMemberBuilder();
    }

    @Override
    public String toString() {
        return "GroupMember{" +
                "id=" + id +
                ", groupId=" + group.getId() +
                ", userId='" + userId + '\'' +
                '}';
    }

    public Integer getId() {
        return this.id;
    }

    public Group getGroup() {
        return this.group;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof GroupMember)) return false;
        final GroupMember other = (GroupMember) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$group = this.getGroup();
        final Object other$group = other.getGroup();
        if (this$group == null ? other$group != null : !this$group.equals(other$group)) return false;
        final Object this$userId = this.getUserId();
        final Object other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof GroupMember;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $group = this.getGroup();
        result = result * PRIME + ($group == null ? 43 : $group.hashCode());
        final Object $userId = this.getUserId();
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        return result;
    }

    public static class GroupMemberBuilder {
        private Integer id;
        private Group group;
        private String userId;

        GroupMemberBuilder() {
        }

        public GroupMemberBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        public GroupMemberBuilder group(Group group) {
            this.group = group;
            return this;
        }

        public GroupMemberBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public GroupMember build() {
            return new GroupMember(this.id, this.group, this.userId);
        }

        public String toString() {
            return "GroupMember.GroupMemberBuilder(id=" + this.id + ", group=" + this.group + ", userId=" + this.userId + ")";
        }
    }
}
