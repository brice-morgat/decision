package com.brilarisoft.lamuertapokerintelligence.service.ruleengine;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.service.range.VillainRangeContextResolver;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class VillainRangeResolver {

    private final VillainRangeContextResolver villainRangeContextResolver;

    public VillainRangeResolver(VillainRangeContextResolver villainRangeContextResolver) {
        this.villainRangeContextResolver = villainRangeContextResolver;
    }

    public Optional<VillainRangeSet> resolve(DecisionContext context) {
        Optional<VillainRangeSet> optionalRange = villainRangeContextResolver.resolve(
                        context.getStrategyProfile().getId(),
                        context.getGameType(),
                        context.getStreet(),
                        context.getScenarioType(),
                        context.getVillainPosition(),
                        context.getHeroPosition(),
                        context.getFacingActionType(),
                        context.getLineSignature()
                );
        optionalRange.ifPresent(context::setVillainRangeSet);
        return optionalRange;
    }

    public Optional<VillainRangeSet> resolveByTargetWidth(DecisionContext context, double targetRangePercent) {
        Optional<VillainRangeSet> optionalRange = villainRangeContextResolver.resolveByTargetWidth(
                context.getStrategyProfile().getId(),
                context.getGameType(),
                context.getStreet(),
                context.getScenarioType(),
                context.getVillainPosition(),
                context.getHeroPosition(),
                context.getFacingActionType(),
                context.getLineSignature(),
                targetRangePercent
        );
        optionalRange.ifPresent(context::setVillainRangeSet);
        return optionalRange;
    }
}
