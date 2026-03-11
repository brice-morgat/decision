package com.brilarisoft.lamuertapokerintelligence.service;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionInput;

public interface StateReconstructionService {

    DecisionContext reconstruct(DecisionInput decisionInput);
}
