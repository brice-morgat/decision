package com.brilarisoft.lamuertapokerintelligence.mapper;

import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.DecisionRule;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.RuleAction;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.RuleCondition;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleUpsertDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.RuleActionDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.RuleConditionDto;
import java.util.Comparator;
import org.springframework.stereotype.Component;

@Component
public class DecisionRuleMapper {

    public DecisionRuleSummaryDto toSummaryDto(DecisionRule rule) {
        return new DecisionRuleSummaryDto(
                rule.getId(),
                rule.getStrategyProfile().getId(),
                rule.getName(),
                rule.getDescription(),
                rule.getGameType(),
                rule.getStreet(),
                rule.getScenarioType(),
                rule.getHeroPosition(),
                rule.getVillainPosition(),
                rule.isActive(),
                rule.isStopOnMatch(),
                rule.getPriority(),
                rule.getConditions().size(),
                rule.getActions().size(),
                rule.getUpdatedAt()
        );
    }

    public DecisionRuleDetailDto toDetailDto(DecisionRule rule) {
        return new DecisionRuleDetailDto(
                rule.getId(),
                rule.getStrategyProfile().getId(),
                rule.getName(),
                rule.getDescription(),
                rule.getGameType(),
                rule.getStreet(),
                rule.getScenarioType(),
                rule.getHeroPosition(),
                rule.getVillainPosition(),
                rule.getHeroHandCode(),
                rule.getHeroStrategyLegend(),
                rule.getHeroRangeSet() == null ? null : rule.getHeroRangeSet().getId(),
                rule.getVillainRangeSet() == null ? null : rule.getVillainRangeSet().getId(),
                rule.getFacingActionType(),
                rule.getLineSignature(),
                rule.getSizingConditionCode(),
                rule.isActive(),
                rule.isStopOnMatch(),
                rule.getPriority(),
                rule.getNote(),
                rule.getConditions().stream()
                        .sorted(Comparator.comparing(RuleCondition::getConditionOrder))
                        .map(this::toConditionDto)
                        .toList(),
                rule.getActions().stream()
                        .sorted(Comparator.comparing(RuleAction::getExecutionOrder))
                        .map(this::toActionDto)
                        .toList(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }

    public void updateEntity(
            DecisionRule rule,
            DecisionRuleUpsertDto request,
            HeroRangeSet heroRangeSet,
            VillainRangeSet villainRangeSet
    ) {
        rule.setName(request.name().trim());
        rule.setDescription(request.description() == null ? "" : request.description().trim());
        rule.setGameType(request.gameType());
        rule.setStreet(request.street());
        rule.setScenarioType(request.scenarioType());
        rule.setHeroPosition(request.heroPosition());
        rule.setVillainPosition(request.villainPosition());
        rule.setHeroHandCode(normalizeNullable(request.heroHandCode()));
        rule.setHeroStrategyLegend(request.heroStrategyLegend());
        rule.setHeroRangeSet(heroRangeSet);
        rule.setVillainRangeSet(villainRangeSet);
        rule.setFacingActionType(request.facingActionType());
        rule.setLineSignature(normalizeNullable(request.lineSignature()));
        rule.setSizingConditionCode(normalizeNullable(request.sizingConditionCode()));
        rule.setActive(request.enabled());
        rule.setStopOnMatch(request.stopOnMatch());
        rule.setPriority(request.priority());
        rule.setNote(normalizeNullable(request.note()));
    }

    public RuleCondition toConditionEntity(RuleConditionDto dto, DecisionRule targetRule) {
        RuleCondition condition = new RuleCondition();
        condition.setDecisionRule(targetRule);
        condition.setConditionType(dto.conditionType());
        condition.setOperator(dto.operator());
        condition.setValueType(dto.valueType());
        condition.setExpectedValue(dto.expectedValue().trim());
        condition.setSecondaryValue(normalizeNullable(dto.secondaryValue()));
        condition.setConditionOrder(dto.conditionOrder());
        return condition;
    }

    public RuleAction toActionEntity(RuleActionDto dto, DecisionRule targetRule) {
        RuleAction action = new RuleAction();
        action.setDecisionRule(targetRule);
        action.setActionType(dto.actionType());
        action.setSizingType(dto.sizingType());
        action.setSizingValue(dto.sizingValue());
        action.setExecutionOrder(dto.executionOrder());
        action.setMessageTemplate(normalizeNullable(dto.messageTemplate()));
        return action;
    }

    private RuleConditionDto toConditionDto(RuleCondition condition) {
        return new RuleConditionDto(
                condition.getConditionType(),
                condition.getOperator(),
                condition.getValueType(),
                condition.getExpectedValue(),
                condition.getSecondaryValue(),
                condition.getConditionOrder()
        );
    }

    private RuleActionDto toActionDto(RuleAction action) {
        return new RuleActionDto(
                action.getActionType(),
                action.getSizingType(),
                action.getSizingValue(),
                action.getExecutionOrder(),
                action.getMessageTemplate()
        );
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
