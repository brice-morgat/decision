package com.brilarisoft.lamuertapokerintelligence.service.ruleengine;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.DecisionRule;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.RuleCondition;
import com.brilarisoft.lamuertapokerintelligence.repository.DecisionRuleRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RuleMatcher {

    private final DecisionRuleRepository decisionRuleRepository;
    private final ConditionEvaluator conditionEvaluator;

    public RuleMatcher(DecisionRuleRepository decisionRuleRepository, ConditionEvaluator conditionEvaluator) {
        this.decisionRuleRepository = decisionRuleRepository;
        this.conditionEvaluator = conditionEvaluator;
    }

    public List<RuleMatchCandidate> match(DecisionContext context) {
        return decisionRuleRepository.findByStrategyProfileIdAndStreetAndScenarioTypeAndActiveTrueOrderByPriorityAsc(
                        context.getStrategyProfile().getId(),
                        context.getStreet(),
                        context.getScenarioType()
                ).stream()
                .filter(rule -> matchesStructure(rule, context))
                .map(rule -> evaluateRule(rule, context))
                .filter(candidate -> candidate != null)
                .toList();
    }

    private boolean matchesStructure(DecisionRule rule, DecisionContext context) {
        return (rule.getHeroPosition() == null || rule.getHeroPosition() == context.getHeroPosition())
                && (rule.getVillainPosition() == null || rule.getVillainPosition() == context.getVillainPosition())
                && (rule.getFacingActionType() == null || rule.getFacingActionType() == context.getFacingActionType())
                && (rule.getHeroStrategyLegend() == null || rule.getHeroStrategyLegend() == context.getHeroStrategyLegend())
                && (rule.getHeroHandCode() == null || rule.getHeroHandCode().equalsIgnoreCase(context.getHeroHandCode()))
                && (rule.getLineSignature() == null || rule.getLineSignature().equalsIgnoreCase(context.getLineSignature()))
                && (rule.getHeroRangeSet() == null || (context.getHeroRangeSet() != null && rule.getHeroRangeSet().getId().equals(context.getHeroRangeSet().getId())))
                && (rule.getVillainRangeSet() == null || (context.getVillainRangeSet() != null && rule.getVillainRangeSet().getId().equals(context.getVillainRangeSet().getId())));
    }

    private RuleMatchCandidate evaluateRule(DecisionRule rule, DecisionContext context) {
        List<String> trace = new ArrayList<>();
        List<RuleCondition> conditions = rule.getConditions().stream()
                .sorted(Comparator.comparing(RuleCondition::getConditionOrder))
                .toList();
        for (RuleCondition condition : conditions) {
            boolean matches = conditionEvaluator.evaluate(condition, context);
            trace.add(conditionEvaluator.describeEvaluation(condition, context) + " => " + matches);
            if (!matches) {
                return null;
            }
        }
        return new RuleMatchCandidate(rule, conditions.size(), computeStructuralSpecificity(rule), trace);
    }

    private int computeStructuralSpecificity(DecisionRule rule) {
        int score = 0;
        if (rule.getHeroPosition() != null) {
            score++;
        }
        if (rule.getVillainPosition() != null) {
            score++;
        }
        if (rule.getHeroHandCode() != null) {
            score++;
        }
        if (rule.getHeroStrategyLegend() != null) {
            score++;
        }
        if (rule.getFacingActionType() != null) {
            score++;
        }
        if (rule.getLineSignature() != null) {
            score++;
        }
        if (rule.getHeroRangeSet() != null) {
            score++;
        }
        if (rule.getVillainRangeSet() != null) {
            score++;
        }
        return score;
    }
}
