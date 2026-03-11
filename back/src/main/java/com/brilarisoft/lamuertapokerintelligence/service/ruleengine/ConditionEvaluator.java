package com.brilarisoft.lamuertapokerintelligence.service.ruleengine;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.RuleOperator;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.ConditionValueType;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.RuleCondition;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ConditionEvaluator {

    public boolean evaluate(RuleCondition condition, DecisionContext context) {
        Object actualValue = extractActualValue(condition, context);
        return switch (condition.getValueType()) {
            case BOOLEAN -> evaluateBoolean(condition, actualValue);
            case NUMBER -> evaluateNumber(condition, actualValue);
            case ENUM, STRING -> evaluateStringLike(condition, actualValue == null ? null : actualValue.toString());
            case LIST -> evaluateList(condition, actualValue == null ? null : actualValue.toString());
        };
    }

    public String describeEvaluation(RuleCondition condition, DecisionContext context) {
        Object actualValue = extractActualValue(condition, context);
        return condition.getConditionType() + " actual=" + actualValue + " expected=" + condition.getExpectedValue();
    }

    private Object extractActualValue(RuleCondition condition, DecisionContext context) {
        return switch (condition.getConditionType()) {
            case HERO_WAS_PREFLOP_AGGRESSOR -> context.isHeroWasPreflopAggressor();
            case VILLAIN_WAS_PREFLOP_AGGRESSOR -> context.isVillainWasPreflopAggressor();
            case VILLAIN_CHECKED_TO_HERO -> context.isVillainCheckedToHero();
            case HERO_BET_CURRENT_STREET -> context.isHeroBetCurrentStreet();
            case VILLAIN_RAISED_HERO_BET -> context.isVillainRaisedHeroBet();
            case HERO_FACING_SECOND_BARREL -> context.isHeroFacingSecondBarrel();
            case HERO_FACING_THIRD_BARREL -> context.isHeroFacingThirdBarrel();
            case CURRENT_STREET_BET_COUNT -> context.getCurrentStreetBetCount();
            case CURRENT_STREET_RAISE_COUNT -> context.getCurrentStreetRaiseCount();
            case LINE_SIGNATURE -> context.getLineSignature();
            case HERO_STRATEGY_LEGEND -> context.getHeroStrategyLegend();
            case FACING_ACTION_TYPE -> context.getFacingActionType();
            case CURRENT_ACTOR -> context.getCurrentActor();
            case POT_SIZE_BB -> context.getPotSizeInBigBlinds();
            case EFFECTIVE_STACK_BB -> context.getEffectiveStackInBigBlinds();
            case HERO_POSITION -> context.getHeroPosition();
            case VILLAIN_POSITION -> context.getVillainPosition();
            case SCENARIO_TYPE -> context.getScenarioType();
            case STREET -> context.getStreet();
            case HERO_HAND_CODE -> context.getHeroHandCode();
        };
    }

    private boolean evaluateBoolean(RuleCondition condition, Object actualValue) {
        boolean actual = actualValue instanceof Boolean value && value;
        boolean expected = Boolean.parseBoolean(condition.getExpectedValue());
        return actual == expected;
    }

    private boolean evaluateNumber(RuleCondition condition, Object actualValue) {
        if (actualValue == null) {
            return false;
        }
        BigDecimal actual = new BigDecimal(actualValue.toString());
        BigDecimal expected = new BigDecimal(condition.getExpectedValue());
        return switch (condition.getOperator()) {
            case EQUALS -> actual.compareTo(expected) == 0;
            case GREATER_THAN -> actual.compareTo(expected) > 0;
            case GREATER_OR_EQUAL -> actual.compareTo(expected) >= 0;
            case LESS_THAN -> actual.compareTo(expected) < 0;
            case LESS_OR_EQUAL -> actual.compareTo(expected) <= 0;
            case BETWEEN -> {
                BigDecimal secondary = new BigDecimal(condition.getSecondaryValue());
                yield actual.compareTo(expected) >= 0 && actual.compareTo(secondary) <= 0;
            }
            default -> false;
        };
    }

    private boolean evaluateStringLike(RuleCondition condition, String actualValue) {
        if (actualValue == null) {
            return false;
        }
        return switch (condition.getOperator()) {
            case EQUALS -> actualValue.equalsIgnoreCase(condition.getExpectedValue());
            case CONTAINS -> actualValue.toUpperCase().contains(condition.getExpectedValue().toUpperCase());
            case IN -> splitValues(condition.getExpectedValue()).stream().anyMatch(value -> value.equalsIgnoreCase(actualValue));
            default -> false;
        };
    }

    private boolean evaluateList(RuleCondition condition, String actualValue) {
        if (actualValue == null) {
            return false;
        }
        return splitValues(condition.getExpectedValue()).stream().anyMatch(value -> value.equalsIgnoreCase(actualValue));
    }

    private List<String> splitValues(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .toList();
    }
}
