package com.notekeeper.notekeeper.dto;

/**
 * All admin-controlled switches in one object.
 */
public class SystemSettingsDTO {
    private boolean allowRegistration = true;
    private String defaultRole = "USER";
    private boolean requireTwoFactor = true;
    private boolean emailNotifications = true;
    private boolean smsNotifications = true;

    public SystemSettingsDTO() {
    }

    public boolean isAllowRegistration() {
        return allowRegistration;
    }

    public void setAllowRegistration(boolean allowRegistration) {
        this.allowRegistration = allowRegistration;
    }

    public String getDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }

    public boolean isRequireTwoFactor() {
        return requireTwoFactor;
    }

    public void setRequireTwoFactor(boolean requireTwoFactor) {
        this.requireTwoFactor = requireTwoFactor;
    }

    public boolean isEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public boolean isSmsNotifications() {
        return smsNotifications;
    }

    public void setSmsNotifications(boolean smsNotifications) {
        this.smsNotifications = smsNotifications;
    }
}
