package com.brilarisoft.lamuertapokerintelligence.service.ruleengine;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionInput;
import com.brilarisoft.lamuertapokerintelligence.domain.range.PokerHandCodeCatalog;
import com.brilarisoft.lamuertapokerintelligence.exception.BusinessValidationException;
import org.springframework.stereotype.Service;

@Service
public class DecisionInputValidator {

    public void validate(DecisionInput decisionInput) {
        if (decisionInput.getHeroCards() == null || decisionInput.getHeroCards().size() != 2) {
            throw new BusinessValidationException("Decision input requires exactly two hero cards");
        }
        if (decisionInput.getHeroHandCode() == null || !PokerHandCodeCatalog.isValid(decisionInput.getHeroHandCode())) {
            throw new BusinessValidationException("Hero hand cannot be normalized to a valid hand code");
        }
        if (decisionInput.getHeroPosition() == null || decisionInput.getVillainPosition() == null) {
            throw new BusinessValidationException("Hero and villain positions are required");
        }
        if (decisionInput.getHeroPosition() == decisionInput.getVillainPosition()) {
            throw new BusinessValidationException("Hero and villain positions must differ");
        }
    }
}
