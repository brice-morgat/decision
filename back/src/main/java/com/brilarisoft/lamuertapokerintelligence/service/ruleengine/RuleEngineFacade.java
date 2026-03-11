package com.brilarisoft.lamuertapokerintelligence.service.ruleengine;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import org.springframework.stereotype.Service;

@Service
public class RuleEngineFacade {

    private final RuleMatcher ruleMatcher;
    private final PriorityResolver priorityResolver;
    private final DecisionAssembler decisionAssembler;

    public RuleEngineFacade(
            RuleMatcher ruleMatcher,
            PriorityResolver priorityResolver,
            DecisionAssembler decisionAssembler
    ) {
        this.ruleMatcher = ruleMatcher;
        this.priorityResolver = priorityResolver;
        this.decisionAssembler = decisionAssembler;
    }

    public ResolvedRuleSelection evaluate(DecisionContext context) {
        return priorityResolver.resolve(ruleMatcher.match(context));
    }

    public DecisionAssembler decisionAssembler() {
        return decisionAssembler;
    }
}
