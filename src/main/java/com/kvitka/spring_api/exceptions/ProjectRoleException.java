package com.kvitka.spring_api.exceptions;

public class ProjectRoleException extends RuntimeException {
    public ProjectRoleException() {
    }

    public ProjectRoleException(String message) {
        super(message);
    }

    public ProjectRoleException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProjectRoleException(Throwable cause) {
        super(cause);
    }
}
