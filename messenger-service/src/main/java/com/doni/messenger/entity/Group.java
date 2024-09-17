package com.doni.messenger.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_group")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title")
    @NotNull(message = "{messenger-api.groups.create.errors.title_is_null}")
    @NotBlank(message = "{messenger-api.groups.create.errors.title_is_blank}")
    @Size(min = 1, max = 100, message = "{messenger-api.groups.create.errors.title_has_invalid_size}")
    private String title;

    @Column(name = "description")
    @Size(max = 2000, message = "{messenger-api.groups.create.errors.description_has_invalid_size}")
    private String description;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @OneToMany(mappedBy = "group", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JsonIgnore
    private Set<GroupMember> groupMembers;

    public Group(Integer id, @NotNull(message = "{messenger-api.groups.create.errors.title_is_null}") @NotBlank(message = "{messenger-api.groups.create.errors.title_is_blank}") @Size(min = 1, max = 100, message = "{messenger-api.groups.create.errors.title_has_invalid_size}") String title, @Size(max = 2000, message = "{messenger-api.groups.create.errors.description_has_invalid_size}") String description, String ownerId, Set<GroupMember> groupMembers) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ownerId = ownerId;
        this.groupMembers = groupMembers;
    }

    public Group() {
    }

    public static GroupBuilder builder() {
        return new GroupBuilder();
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", ownerId='" + ownerId + '\'' +
                '}';
    }

    public Integer getId() {
        return this.id;
    }

    public @NotNull(message = "{messenger-api.groups.create.errors.title_is_null}") @NotBlank(message = "{messenger-api.groups.create.errors.title_is_blank}") @Size(min = 1, max = 100, message = "{messenger-api.groups.create.errors.title_has_invalid_size}") String getTitle() {
        return this.title;
    }

    public @Size(max = 2000, message = "{messenger-api.groups.create.errors.description_has_invalid_size}") String getDescription() {
        return this.description;
    }

    public String getOwnerId() {
        return this.ownerId;
    }

    public Set<GroupMember> getGroupMembers() {
        return this.groupMembers;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(@NotNull(message = "{messenger-api.groups.create.errors.title_is_null}") @NotBlank(message = "{messenger-api.groups.create.errors.title_is_blank}") @Size(min = 1, max = 100, message = "{messenger-api.groups.create.errors.title_has_invalid_size}") String title) {
        this.title = title;
    }

    public void setDescription(@Size(max = 2000, message = "{messenger-api.groups.create.errors.description_has_invalid_size}") String description) {
        this.description = description;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @JsonIgnore
    public void setGroupMembers(Set<GroupMember> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Group)) return false;
        final Group other = (Group) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) return false;
        final Object this$description = this.getDescription();
        final Object other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description))
            return false;
        final Object this$ownerId = this.getOwnerId();
        final Object other$ownerId = other.getOwnerId();
        if (this$ownerId == null ? other$ownerId != null : !this$ownerId.equals(other$ownerId)) return false;
        final Object this$groupMembers = this.getGroupMembers();
        final Object other$groupMembers = other.getGroupMembers();
        if (this$groupMembers == null ? other$groupMembers != null : !this$groupMembers.equals(other$groupMembers))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Group;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        final Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final Object $ownerId = this.getOwnerId();
        result = result * PRIME + ($ownerId == null ? 43 : $ownerId.hashCode());
        return result;
    }

    public void addGroupMember(GroupMember groupMember) {
        if (groupMembers == null) {
            groupMembers = new HashSet<>();
        }
        groupMember.setGroup(this);
        groupMembers.add(groupMember);
    }

    public static class GroupBuilder {
        private Integer id;
        private @NotNull(message = "{messenger-api.groups.create.errors.title_is_null}") @NotBlank(message = "{messenger-api.groups.create.errors.title_is_blank}") @Size(min = 1, max = 100, message = "{messenger-api.groups.create.errors.title_has_invalid_size}") String title;
        private @Size(max = 2000, message = "{messenger-api.groups.create.errors.description_has_invalid_size}") String description;
        private String ownerId;
        private Set<GroupMember> groupMembers;

        GroupBuilder() {
        }

        public GroupBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        public GroupBuilder title(@NotNull(message = "{messenger-api.groups.create.errors.title_is_null}") @NotBlank(message = "{messenger-api.groups.create.errors.title_is_blank}") @Size(min = 1, max = 100, message = "{messenger-api.groups.create.errors.title_has_invalid_size}") String title) {
            this.title = title;
            return this;
        }

        public GroupBuilder description(@Size(max = 2000, message = "{messenger-api.groups.create.errors.description_has_invalid_size}") String description) {
            this.description = description;
            return this;
        }

        public GroupBuilder ownerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        @JsonIgnore
        public GroupBuilder groupMembers(Set<GroupMember> groupMembers) {
            this.groupMembers = groupMembers;
            return this;
        }

        public Group build() {
            return new Group(this.id, this.title, this.description, this.ownerId, this.groupMembers);
        }

        public String toString() {
            return "Group.GroupBuilder(id=" + this.id + ", title=" + this.title + ", description=" + this.description + ", ownerId=" + this.ownerId + ", groupMembers=" + this.groupMembers + ")";
        }
    }
}
