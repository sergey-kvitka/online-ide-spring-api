package com.kvitka.spring_api.enums;

public enum ProjectType {
    PUBLIC_EDIT(ProjectRole.EDITOR),
    PUBLIC_WATCH(ProjectRole.WATCHER),
    PRIVATE(null);

    private final ProjectRole defaultProjectRole;

    ProjectType(ProjectRole projectRole) {
        defaultProjectRole = projectRole;
    }

    public ProjectRole getDefaultProjectRole() {
        return defaultProjectRole;
    }
}
