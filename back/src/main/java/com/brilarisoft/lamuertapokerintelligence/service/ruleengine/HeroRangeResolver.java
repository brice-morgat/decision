package com.brilarisoft.lamuertapokerintelligence.service.ruleengine;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeCell;
import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.repository.HeroRangeSetRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class HeroRangeResolver {

    private final HeroRangeSetRepository heroRangeSetRepository;

    public HeroRangeResolver(HeroRangeSetRepository heroRangeSetRepository) {
        this.heroRangeSetRepository = heroRangeSetRepository;
    }

    public Optional<HeroRangeSet> resolve(DecisionContext context) {
        Optional<HeroRangeSet> optionalRange = heroRangeSetRepository
                .findByStrategyProfileIdAndGameTypeAndStreetAndHeroPositionAndScenarioTypeAndSubScenarioCode(
                        context.getStrategyProfile().getId(),
                        context.getGameType(),
                        context.getStreet(),
                        context.getHeroPosition(),
                        context.getScenarioType(),
                        null
                );
        optionalRange.ifPresent(range -> {
            context.setHeroRangeSet(range);
            range.getCells().stream()
                    .filter(HeroRangeCell::isEnabled)
                    .filter(cell -> cell.getHandCode().equals(context.getHeroHandCode()))
                    .findFirst()
                    .ifPresent(cell -> context.setHeroStrategyLegend(cell.getStrategyLegend()));
        });
        return optionalRange;
    }
}
