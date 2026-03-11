package com.brilarisoft.lamuertapokerintelligence.service.ruleengine;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.repository.VillainRangeSetRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class VillainRangeResolver {

    private final VillainRangeSetRepository villainRangeSetRepository;

    public VillainRangeResolver(VillainRangeSetRepository villainRangeSetRepository) {
        this.villainRangeSetRepository = villainRangeSetRepository;
    }

    public Optional<VillainRangeSet> resolve(DecisionContext context) {
        Optional<VillainRangeSet> optionalRange = villainRangeSetRepository
                .findByStrategyProfileIdAndGameTypeAndStreetAndVillainPositionAndHeroPositionAndScenarioTypeAndTriggerActionTypeAndLineSignature(
                        context.getStrategyProfile().getId(),
                        context.getGameType(),
                        context.getStreet(),
                        context.getVillainPosition(),
                        context.getHeroPosition(),
                        context.getScenarioType(),
                        context.getFacingActionType(),
                        context.getLineSignature()
                );
        optionalRange.ifPresent(context::setVillainRangeSet);
        return optionalRange;
    }
}
