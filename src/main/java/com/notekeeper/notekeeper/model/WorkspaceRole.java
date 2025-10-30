package com.notekeeper.notekeeper.model;

public enum WorkspaceRole {
    OWNER,
    EDITOR,
    VIEWER;

    public static boolean isValid(String role) {
        try {
            WorkspaceRole.valueOf(role.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}