package com.brilarisoft.lamuertapokerintelligence.service.impl;

import com.brilarisoft.lamuertapokerintelligence.dto.settings.AppSettingsDto;
import com.brilarisoft.lamuertapokerintelligence.service.AppSettingsService;
import org.springframework.stereotype.Service;

@Service
public class AppSettingsServiceImpl implements AppSettingsService {

    private volatile AppSettingsDto currentSettings = new AppSettingsDto(null, true, false);

    @Override
    public AppSettingsDto getSettings() {
        return currentSettings;
    }

    @Override
    public AppSettingsDto updateSettings(AppSettingsDto request) {
        currentSettings = request;
        return currentSettings;
    }
}
