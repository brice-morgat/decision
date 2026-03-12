package com.brilarisoft.lamuertapokerintelligence.controller;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeCellsUpdateDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeContextQueryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeUpsertDto;
import com.brilarisoft.lamuertapokerintelligence.service.VillainRangeService;
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
@RequestMapping("/ranges/villain")
public class VillainRangeController {

    private final VillainRangeService villainRangeService;

    public VillainRangeController(VillainRangeService villainRangeService) {
        this.villainRangeService = villainRangeService;
    }

    @GetMapping
    public List<VillainRangeSummaryDto> listByProfile(@RequestParam @NotNull UUID profileId) {
        return villainRangeService.listByProfile(profileId);
    }

    @GetMapping("/{rangeId}")
    public VillainRangeDetailDto getRange(@PathVariable UUID rangeId) {
        return villainRangeService.getRange(rangeId);
    }

    @GetMapping("/context")
    public VillainRangeDetailDto getByContext(
            @RequestParam @NotNull UUID profileId,
            @RequestParam GameType gameType,
            @RequestParam Street street,
            @RequestParam Position villainPosition,
            @RequestParam(required = false) Position heroPosition,
            @RequestParam ScenarioType scenarioType,
            @RequestParam(required = false) ActionType triggerActionCode,
            @RequestParam(required = false) String lineSignature
    ) {
        return villainRangeService.getRangeByContext(new VillainRangeContextQueryDto(
                profileId,
                gameType,
                street,
                villainPosition,
                heroPosition,
                scenarioType,
                triggerActionCode,
                lineSignature
        ));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VillainRangeDetailDto createRange(@Valid @RequestBody VillainRangeUpsertDto request) {
        return villainRangeService.createRange(request);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public VillainRangeDetailDto bulkUpsert(@Valid @RequestBody VillainRangeUpsertDto request) {
        return villainRangeService.bulkUpsert(request);
    }

    @PutMapping("/{rangeId}")
    public VillainRangeDetailDto updateRange(@PathVariable UUID rangeId, @Valid @RequestBody VillainRangeUpsertDto request) {
        return villainRangeService.updateRange(rangeId, request);
    }

    @PutMapping("/{rangeId}/cells")
    public VillainRangeDetailDto updateCells(@PathVariable UUID rangeId, @Valid @RequestBody VillainRangeCellsUpdateDto request) {
        return villainRangeService.updateCells(rangeId, request);
    }

    @DeleteMapping("/{rangeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRange(@PathVariable UUID rangeId) {
        villainRangeService.deleteRange(rangeId);
    }
}
