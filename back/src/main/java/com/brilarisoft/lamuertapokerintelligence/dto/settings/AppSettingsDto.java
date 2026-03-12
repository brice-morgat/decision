package com.brilarisoft.lamuertapokerintelligence.dto.settings;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AppSettingsDto(
        UUID defaultProfileId,
        @NotNull Boolean autoSave,
        @NotNull Boolean telemetryEnabled
) {
}
