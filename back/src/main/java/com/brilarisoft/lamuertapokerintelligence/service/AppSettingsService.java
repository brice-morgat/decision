package com.brilarisoft.lamuertapokerintelligence.service;

import com.brilarisoft.lamuertapokerintelligence.dto.settings.AppSettingsDto;

public interface AppSettingsService {

    AppSettingsDto getSettings();

    AppSettingsDto updateSettings(AppSettingsDto request);
}
