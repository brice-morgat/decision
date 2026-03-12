package com.brilarisoft.lamuertapokerintelligence.controller;

import com.brilarisoft.lamuertapokerintelligence.dto.settings.AppSettingsDto;
import com.brilarisoft.lamuertapokerintelligence.service.AppSettingsService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/settings")
public class SettingsController {

    private final AppSettingsService appSettingsService;

    public SettingsController(AppSettingsService appSettingsService) {
        this.appSettingsService = appSettingsService;
    }

    @GetMapping
    public AppSettingsDto getSettings() {
        return appSettingsService.getSettings();
    }

    @PutMapping
    public AppSettingsDto updateSettings(@Valid @RequestBody AppSettingsDto request) {
        return appSettingsService.updateSettings(request);
    }
}
