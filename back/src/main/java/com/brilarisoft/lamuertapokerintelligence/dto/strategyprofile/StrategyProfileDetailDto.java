package com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import java.time.Instant;
import java.util.UUID;

public record StrategyProfileDetailDto(
        UUID id,
        String name,
        String description,
        GameType gameType,
        boolean active,
        boolean archived,
        String versionLabel,
        Instant createdAt,
        Instant updatedAt
) {
}
