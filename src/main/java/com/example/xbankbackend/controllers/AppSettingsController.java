package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.UpdateAppSettingRequest;
import com.example.xbankbackend.models.AppSetting;
import com.example.xbankbackend.services.AppSettingsService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/settings")
public class AppSettingsController {

    private AppSettingsService appSettingsService;

    // ADMIN-only

    @GetMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppSetting> getByKey(@PathVariable String key) {
        log.info("Getting setting with key {}", key);

        AppSetting setting = appSettingsService.getByKey(key);

        return ResponseEntity.status(HttpStatus.OK).body(setting);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppSetting>> getAllSettings() {
        log.info("Getting all app settings");

        List<AppSetting> settings = appSettingsService.getAllSettings();

        return ResponseEntity.status(HttpStatus.OK).body(settings);
    }

    @PatchMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppSetting> updateSetting(@PathVariable String key, @RequestBody UpdateAppSettingRequest request) {
        log.info("Updating setting with key {}: {}", key, request);

        AppSetting setting = appSettingsService.update(key, request);

        return ResponseEntity.status(HttpStatus.OK).body(setting);
    }
}
