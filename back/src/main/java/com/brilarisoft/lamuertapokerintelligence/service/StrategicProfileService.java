package com.brilarisoft.lamuertapokerintelligence.service;

import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.DuplicateStrategyProfileDto;
import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.StrategyProfileDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.StrategyProfileSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.StrategyProfileUpsertDto;
import java.util.List;
import java.util.UUID;

/**
 * Application service exposing strategic profile read and selection use cases.
 */
public interface StrategicProfileService {

    List<StrategyProfileSummaryDto> listProfiles();

    StrategyProfileDetailDto getProfile(UUID profileId);

    StrategyProfileDetailDto getActiveProfile();

    StrategyProfileDetailDto createProfile(StrategyProfileUpsertDto request);

    StrategyProfileDetailDto updateProfile(UUID profileId, StrategyProfileUpsertDto request);

    void deleteProfile(UUID profileId);

    StrategyProfileDetailDto activateProfile(UUID profileId);

    StrategyProfileDetailDto duplicateProfile(UUID profileId, DuplicateStrategyProfileDto request);
}
