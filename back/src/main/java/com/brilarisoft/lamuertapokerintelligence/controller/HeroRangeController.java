package com.brilarisoft.lamuertapokerintelligence.controller;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeCellsUpdateDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeContextQueryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeUpsertDto;
import com.brilarisoft.lamuertapokerintelligence.service.HeroRangeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/ranges/hero")
public class HeroRangeController {

    private final HeroRangeService heroRangeService;

    public HeroRangeController(HeroRangeService heroRangeService) {
        this.heroRangeService = heroRangeService;
    }

    @GetMapping
    public List<HeroRangeSummaryDto> listByProfile(@RequestParam @NotNull UUID profileId) {
        return heroRangeService.listByProfile(profileId);
    }

    @GetMapping("/{rangeId}")
    public HeroRangeDetailDto getRange(@PathVariable UUID rangeId) {
        return heroRangeService.getRange(rangeId);
    }

    @GetMapping("/context")
    public HeroRangeDetailDto getByContext(
            @RequestParam @NotNull UUID profileId,
            @RequestParam GameType gameType,
            @RequestParam Street street,
            @RequestParam Position heroPosition,
            @RequestParam ScenarioType scenarioType,
            @RequestParam(required = false) String subScenarioCode
    ) {
        return heroRangeService.getRangeByContext(new HeroRangeContextQueryDto(
                profileId,
                gameType,
                street,
                heroPosition,
                scenarioType,
                subScenarioCode
        ));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HeroRangeDetailDto createRange(@Valid @RequestBody HeroRangeUpsertDto request) {
        return heroRangeService.createRange(request);
    }

    @PutMapping("/{rangeId}")
    public HeroRangeDetailDto updateRange(@PathVariable UUID rangeId, @Valid @RequestBody HeroRangeUpsertDto request) {
        return heroRangeService.updateRange(rangeId, request);
    }

    @PutMapping("/{rangeId}/cells")
    public HeroRangeDetailDto updateCells(@PathVariable UUID rangeId, @Valid @RequestBody HeroRangeCellsUpdateDto request) {
        return heroRangeService.updateCells(rangeId, request);
    }

    @DeleteMapping("/{rangeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRange(@PathVariable UUID rangeId) {
        heroRangeService.deleteRange(rangeId);
    }
}
