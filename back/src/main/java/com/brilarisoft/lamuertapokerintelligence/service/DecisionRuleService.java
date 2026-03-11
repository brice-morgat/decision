package com.brilarisoft.lamuertapokerintelligence.service;

import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleFilterDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleUpsertDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DuplicateDecisionRuleDto;
import java.util.List;
import java.util.UUID;

public interface DecisionRuleService {

    DecisionRuleDetailDto createRule(DecisionRuleUpsertDto request);

    DecisionRuleDetailDto updateRule(UUID ruleId, DecisionRuleUpsertDto request);

    void deleteRule(UUID ruleId);

    DecisionRuleDetailDto getRule(UUID ruleId);

    List<DecisionRuleSummaryDto> listRules(DecisionRuleFilterDto filter);

    DecisionRuleDetailDto duplicateRule(UUID ruleId, DuplicateDecisionRuleDto request);
}
