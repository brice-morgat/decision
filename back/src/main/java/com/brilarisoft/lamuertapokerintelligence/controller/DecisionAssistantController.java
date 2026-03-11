package com.brilarisoft.lamuertapokerintelligence.controller;

import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionRequestDto;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionResponseDto;
import com.brilarisoft.lamuertapokerintelligence.service.DecisionEngineService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/decision-assistant")
public class DecisionAssistantController {

    private final DecisionEngineService decisionEngineService;

    public DecisionAssistantController(DecisionEngineService decisionEngineService) {
        this.decisionEngineService = decisionEngineService;
    }

    @PostMapping("/decide")
    public DecisionResponseDto decide(@Valid @RequestBody DecisionRequestDto request) {
        return decisionEngineService.decide(request);
    }
}
