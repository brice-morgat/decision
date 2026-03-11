package com.brilarisoft.lamuertapokerintelligence.service.ruleengine;

import static org.assertj.core.api.Assertions.assertThat;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.RuleOperator;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.StrategyLegend;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.ConditionValueType;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.RuleCondition;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.RuleConditionType;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConditionEvaluatorTest {

    private ConditionEvaluator evaluator;
    private DecisionContext context;

    @BeforeEach
    void setUp() {
        evaluator = new ConditionEvaluator();
        context = new DecisionContext();
        context.setHeroWasPreflopAggressor(true);
        context.setCurrentStreetBetCount(2);
        context.setHeroStrategyLegend(StrategyLegend.CHECK_FOLD);
        context.setLineSignature("VILLAIN_CHECK__HERO_BET");
        context.setPotSizeInBigBlinds(BigDecimal.valueOf(22));
    }

    @Test
    void evaluatesBooleanCondition() {
        assertThat(evaluator.evaluate(condition(RuleConditionType.HERO_WAS_PREFLOP_AGGRESSOR, ConditionValueType.BOOLEAN, RuleOperator.EQUALS, "true"), context))
                .isTrue();
    }

    @Test
    void evaluatesNumericBetweenCondition() {
        RuleCondition condition = condition(RuleConditionType.POT_SIZE_BB, ConditionValueType.NUMBER, RuleOperator.BETWEEN, "20");
        condition.setSecondaryValue("25");
        assertThat(evaluator.evaluate(condition, context)).isTrue();
    }

    @Test
    void evaluatesEnumAndStringConditions() {
        assertThat(evaluator.evaluate(condition(RuleConditionType.HERO_STRATEGY_LEGEND, ConditionValueType.ENUM, RuleOperator.EQUALS, "CHECK_FOLD"), context))
                .isTrue();
        assertThat(evaluator.evaluate(condition(RuleConditionType.LINE_SIGNATURE, ConditionValueType.STRING, RuleOperator.CONTAINS, "HERO_BET"), context))
                .isTrue();
    }

    private RuleCondition condition(RuleConditionType type, ConditionValueType valueType, RuleOperator operator, String expected) {
        RuleCondition condition = new RuleCondition();
        condition.setConditionType(type);
        condition.setValueType(valueType);
        condition.setOperator(operator);
        condition.setExpectedValue(expected);
        return condition;
    }
}
