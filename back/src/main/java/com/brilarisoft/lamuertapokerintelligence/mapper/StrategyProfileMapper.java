package com.brilarisoft.lamuertapokerintelligence.mapper;

import com.brilarisoft.lamuertapokerintelligence.domain.strategyprofile.StrategyProfile;
import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.StrategyProfileUpsertDto;
import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.StrategyProfileDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.StrategyProfileSummaryDto;
import org.springframework.stereotype.Component;

@Component
public class StrategyProfileMapper {

    public StrategyProfileSummaryDto toSummaryDto(StrategyProfile profile) {
        return new StrategyProfileSummaryDto(
                profile.getId(),
                profile.getName(),
                profile.getDescription(),
                profile.getGameType(),
                profile.isActive(),
                profile.isArchived(),
                profile.getVersionLabel(),
                profile.getUpdatedAt()
        );
    }

    public StrategyProfileDetailDto toDetailDto(StrategyProfile profile) {
        return new StrategyProfileDetailDto(
                profile.getId(),
                profile.getName(),
                profile.getDescription(),
                profile.getGameType(),
                profile.isActive(),
                profile.isArchived(),
                profile.getVersionLabel(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    public StrategyProfile toNewEntity(StrategyProfileUpsertDto dto) {
        StrategyProfile profile = new StrategyProfile();
        updateEntity(profile, dto);
        return profile;
    }

    public void updateEntity(StrategyProfile profile, StrategyProfileUpsertDto dto) {
        profile.setName(dto.name().trim());
        profile.setDescription(dto.description() == null ? "" : dto.description().trim());
        profile.setGameType(dto.gameType());
        profile.setActive(dto.active());
        profile.setArchived(dto.archived());
        profile.setVersionLabel(dto.versionLabel().trim());
    }
}
