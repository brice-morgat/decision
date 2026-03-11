package com.brilarisoft.lamuertapokerintelligence.service;

import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionRequestDto;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionResponseDto;

/**
 * Orchestrates decision input normalization, rule lookup and response mapping.
 */
public interface DecisionEngineService {

    DecisionResponseDto decide(DecisionRequestDto request);
}
