package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.dto.SystemSettingsDTO;
import com.notekeeper.notekeeper.exception.BadRequestException;
import com.notekeeper.notekeeper.model.SystemSetting;
import com.notekeeper.notekeeper.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Reads and writes the admin-controlled switches. Missing rows fall back to
 * defaults that keep the app behaving exactly as it did before settings existed.
 */
@Service
public class SystemSettingService {

    public static final String ALLOW_REGISTRATION = "allowRegistration";
    public static final String DEFAULT_ROLE = "defaultRole";
    public static final String REQUIRE_TWO_FACTOR = "requireTwoFactor";
    public static final String EMAIL_NOTIFICATIONS = "emailNotifications";
    public static final String SMS_NOTIFICATIONS = "smsNotifications";

    /** Roles an admin may hand out to new sign-ups. ADMIN is never allowed. */
    static final Set<String> ASSIGNABLE_ROLES = Set.of("USER", "EDITOR");

    private final SystemSettingRepository repository;

    public SystemSettingService(SystemSettingRepository repository) {
        this.repository = repository;
    }

    public SystemSettingsDTO getSettings() {
        SystemSettingsDTO dto = new SystemSettingsDTO();
        dto.setAllowRegistration(getBoolean(ALLOW_REGISTRATION, true));
        dto.setDefaultRole(getString(DEFAULT_ROLE, "USER"));
        dto.setRequireTwoFactor(getBoolean(REQUIRE_TWO_FACTOR, true));
        dto.setEmailNotifications(getBoolean(EMAIL_NOTIFICATIONS, true));
        dto.setSmsNotifications(getBoolean(SMS_NOTIFICATIONS, true));
        return dto;
    }

    @Transactional
    public SystemSettingsDTO update(SystemSettingsDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Settings are required");
        }
        String role = dto.getDefaultRole() == null ? "" : dto.getDefaultRole().trim().toUpperCase();
        if (!ASSIGNABLE_ROLES.contains(role)) {
            throw new BadRequestException("Default role must be USER or EDITOR");
        }
        save(ALLOW_REGISTRATION, String.valueOf(dto.isAllowRegistration()));
        save(DEFAULT_ROLE, role);
        save(REQUIRE_TWO_FACTOR, String.valueOf(dto.isRequireTwoFactor()));
        save(EMAIL_NOTIFICATIONS, String.valueOf(dto.isEmailNotifications()));
        save(SMS_NOTIFICATIONS, String.valueOf(dto.isSmsNotifications()));
        return getSettings();
    }

    public boolean isRegistrationAllowed() {
        return getBoolean(ALLOW_REGISTRATION, true);
    }

    public String getDefaultRole() {
        String role = getString(DEFAULT_ROLE, "USER");
        return ASSIGNABLE_ROLES.contains(role) ? role : "USER";
    }

    public boolean isTwoFactorRequired() {
        return getBoolean(REQUIRE_TWO_FACTOR, true);
    }

    public boolean isEmailEnabled() {
        return getBoolean(EMAIL_NOTIFICATIONS, true);
    }

    public boolean isSmsEnabled() {
        return getBoolean(SMS_NOTIFICATIONS, true);
    }

    private boolean getBoolean(String key, boolean fallback) {
        return repository.findById(key)
                .map(s -> Boolean.parseBoolean(s.getValue()))
                .orElse(fallback);
    }

    private String getString(String key, String fallback) {
        return repository.findById(key).map(SystemSetting::getValue).orElse(fallback);
    }

    private void save(String key, String value) {
        repository.save(new SystemSetting(key, value));
    }
}
