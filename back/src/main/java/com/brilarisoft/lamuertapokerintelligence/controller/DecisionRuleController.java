package com.brilarisoft.lamuertapokerintelligence.controller;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleFilterDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleUpsertDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DuplicateDecisionRuleDto;
import com.brilarisoft.lamuertapokerintelligence.service.DecisionRuleService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/rules")
public class DecisionRuleController {

    private final DecisionRuleService decisionRuleService;

    public DecisionRuleController(DecisionRuleService decisionRuleService) {
        this.decisionRuleService = decisionRuleService;
    }

    @GetMapping
    public List<DecisionRuleSummaryDto> listRules(
            @RequestParam(required = false) UUID profileId,
            @RequestParam(required = false) GameType gameType,
            @RequestParam(required = false) Street street,
            @RequestParam(required = false) ScenarioType scenarioType,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Position heroPosition,
            @RequestParam(required = false) Position villainPosition,
            @RequestParam(required = false) Integer priority
    ) {
        return decisionRuleService.listRules(new DecisionRuleFilterDto(
                profileId,
                gameType,
                street,
                scenarioType,
                enabled,
                heroPosition,
                villainPosition,
                priority
        ));
    }

    @GetMapping("/{ruleId}")
    public DecisionRuleDetailDto getRule(@PathVariable UUID ruleId) {
        return decisionRuleService.getRule(ruleId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DecisionRuleDetailDto createRule(@Valid @RequestBody DecisionRuleUpsertDto request) {
        return decisionRuleService.createRule(request);
    }

    @PutMapping("/{ruleId}")
    public DecisionRuleDetailDto updateRule(@PathVariable UUID ruleId, @Valid @RequestBody DecisionRuleUpsertDto request) {
        return decisionRuleService.updateRule(ruleId, request);
    }

    @PostMapping("/{ruleId}/duplicate")
    @ResponseStatus(HttpStatus.CREATED)
    public DecisionRuleDetailDto duplicateRule(@PathVariable UUID ruleId, @Valid @RequestBody DuplicateDecisionRuleDto request) {
        return decisionRuleService.duplicateRule(ruleId, request);
    }

    @DeleteMapping("/{ruleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRule(@PathVariable UUID ruleId) {
        decisionRuleService.deleteRule(ruleId);
    }
}
