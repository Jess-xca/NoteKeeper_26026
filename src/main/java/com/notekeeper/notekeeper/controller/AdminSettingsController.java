package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.SystemSettingsDTO;
import com.notekeeper.notekeeper.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only system switches. The ADMIN restriction for /api/admin/** lives in SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/settings")
@CrossOrigin(origins = "*")
public class AdminSettingsController {

    @Autowired
    private SystemSettingService systemSettingService;

    @GetMapping
    public ResponseEntity<SystemSettingsDTO> getSettings() {
        return ResponseEntity.ok(systemSettingService.getSettings());
    }

    @PutMapping
    public ResponseEntity<SystemSettingsDTO> updateSettings(@RequestBody SystemSettingsDTO settings) {
        return ResponseEntity.ok(systemSettingService.update(settings));
    }
}
