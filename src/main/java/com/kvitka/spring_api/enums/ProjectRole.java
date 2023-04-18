package com.kvitka.spring_api.enums;

import java.util.ArrayList;
import java.util.List;

public enum ProjectRole {

    CREATOR(new Permission[]{
            Permission.PROJECT_SETTINGS,
            Permission.SET_ADMIN_ROLE,
            Permission.SET_ROLES,
            Permission.ADD_AND_DELETE_USERS,
            Permission.EDIT,
            Permission.COMMENT}),

    PROJECT_ADMIN(new Permission[]{
            Permission.SET_ROLES,
            Permission.ADD_AND_DELETE_USERS,
            Permission.EDIT,
            Permission.COMMENT}),

    EDITOR(new Permission[]{
            Permission.EDIT,
            Permission.COMMENT}),

    COMMENTER(new Permission[]{
            Permission.COMMENT}),

    WATCHER(new Permission[]{});

    private final List<Permission> permissions;

    ProjectRole(Permission[] permissions) {
        this.permissions = new ArrayList<>(List.of(permissions));
        this.permissions.add(Permission.BE_PARTICIPANT);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public List<Permission> getPermissions() {
        return new ArrayList<>(permissions);
    }

    public enum Permission {
        PROJECT_SETTINGS,
        SET_ADMIN_ROLE,
        SET_ROLES,
        ADD_AND_DELETE_USERS,
        EDIT,
        COMMENT,
        BE_PARTICIPANT;

        public boolean accept(ProjectRole projectRole) {
            if (projectRole == null) return false;
            return projectRole.hasPermission(this);
        }
    }
}
