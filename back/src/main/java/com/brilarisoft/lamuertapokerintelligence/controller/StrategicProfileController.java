package com.brilarisoft.lamuertapokerintelligence.controller;

import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.DuplicateStrategyProfileDto;
import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.StrategyProfileDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.StrategyProfileSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.StrategyProfileUpsertDto;
import com.brilarisoft.lamuertapokerintelligence.service.StrategicProfileService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/profiles")
public class StrategicProfileController {

    private final StrategicProfileService strategicProfileService;

    public StrategicProfileController(StrategicProfileService strategicProfileService) {
        this.strategicProfileService = strategicProfileService;
    }

    @GetMapping
    public List<StrategyProfileSummaryDto> listProfiles() {
        return strategicProfileService.listProfiles();
    }

    @GetMapping("/active")
    public StrategyProfileDetailDto getActiveProfile() {
        return strategicProfileService.getActiveProfile();
    }

    @GetMapping("/{profileId}")
    public StrategyProfileDetailDto getProfile(@PathVariable UUID profileId) {
        return strategicProfileService.getProfile(profileId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StrategyProfileDetailDto createProfile(@Valid @RequestBody StrategyProfileUpsertDto request) {
        return strategicProfileService.createProfile(request);
    }

    @PutMapping("/{profileId}")
    public StrategyProfileDetailDto updateProfile(
            @PathVariable UUID profileId,
            @Valid @RequestBody StrategyProfileUpsertDto request
    ) {
        return strategicProfileService.updateProfile(profileId, request);
    }

    @PutMapping("/{profileId}/activate")
    public StrategyProfileDetailDto activateProfile(@PathVariable UUID profileId) {
        return strategicProfileService.activateProfile(profileId);
    }

    @PostMapping("/{profileId}/duplicate")
    @ResponseStatus(HttpStatus.CREATED)
    public StrategyProfileDetailDto duplicateProfile(
            @PathVariable UUID profileId,
            @Valid @RequestBody DuplicateStrategyProfileDto request
    ) {
        return strategicProfileService.duplicateProfile(profileId, request);
    }

    @DeleteMapping("/{profileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@PathVariable UUID profileId) {
        strategicProfileService.deleteProfile(profileId);
    }
}
