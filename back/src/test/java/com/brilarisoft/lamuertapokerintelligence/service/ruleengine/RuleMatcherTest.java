package com.brilarisoft.lamuertapokerintelligence.service.ruleengine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.RuleOperator;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.ConditionValueType;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.DecisionRule;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.RuleCondition;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.RuleConditionType;
import com.brilarisoft.lamuertapokerintelligence.domain.strategyprofile.StrategyProfile;
import com.brilarisoft.lamuertapokerintelligence.repository.DecisionRuleRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RuleMatcherTest {

    @Mock
    private DecisionRuleRepository decisionRuleRepository;

    private RuleMatcher ruleMatcher;

    @BeforeEach
    void setUp() {
        ruleMatcher = new RuleMatcher(decisionRuleRepository, new ConditionEvaluator());
    }

    @Test
    void matchesCompatibleRulesOnly() {
        DecisionContext context = context();
        DecisionRule matchingRule = rule(ActionType.CHECK, condition(RuleConditionType.HERO_WAS_PREFLOP_AGGRESSOR, "true"));
        DecisionRule nonMatchingRule = rule(ActionType.BET, condition(RuleConditionType.LINE_SIGNATURE, "OTHER_LINE"));
        when(decisionRuleRepository.findByStrategyProfileIdAndStreetAndScenarioTypeAndActiveTrueOrderByPriorityAsc(
                context.getStrategyProfile().getId(), Street.FLOP, ScenarioType.FLOP_CBET_SPOT
        )).thenReturn(List.of(matchingRule, nonMatchingRule));

        List<RuleMatchCandidate> candidates = ruleMatcher.match(context);

        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).rule()).isEqualTo(matchingRule);
    }

    private DecisionContext context() {
        DecisionContext context = new DecisionContext();
        context.setStrategyProfile(profile());
        context.setGameType(GameType.CASH);
        context.setStreet(Street.FLOP);
        context.setScenarioType(ScenarioType.FLOP_CBET_SPOT);
        context.setHeroPosition(Position.BTN);
        context.setVillainPosition(Position.BB);
        context.setHeroWasPreflopAggressor(true);
        context.setLineSignature("VILLAIN_CHECK__HERO_BET");
        context.setFacingActionType(ActionType.CHECK);
        return context;
    }

    private DecisionRule rule(ActionType facingActionType, RuleCondition condition) {
        DecisionRule rule = new DecisionRule();
        rule.setName("rule");
        rule.setPriority(1);
        rule.setActive(true);
        rule.setStreet(Street.FLOP);
        rule.setScenarioType(ScenarioType.FLOP_CBET_SPOT);
        rule.setFacingActionType(facingActionType);
        condition.setDecisionRule(rule);
        rule.setConditions(List.of(condition));
        return rule;
    }

    private RuleCondition condition(RuleConditionType type, String expectedValue) {
        RuleCondition condition = new RuleCondition();
        condition.setConditionType(type);
        condition.setValueType(type == RuleConditionType.HERO_WAS_PREFLOP_AGGRESSOR ? ConditionValueType.BOOLEAN : ConditionValueType.STRING);
        condition.setOperator(RuleOperator.EQUALS);
        condition.setExpectedValue(expectedValue);
        condition.setConditionOrder(0);
        return condition;
    }

    private StrategyProfile profile() {
        StrategyProfile profile = new StrategyProfile();
        ReflectionTestUtils.setField(profile, "id", UUID.randomUUID());
        profile.setName("Cash");
        profile.setDescription("");
        profile.setGameType(GameType.CASH);
        profile.setVersionLabel("v1");
        return profile;
    }
}
