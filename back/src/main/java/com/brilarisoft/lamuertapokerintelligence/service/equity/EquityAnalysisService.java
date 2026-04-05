package com.brilarisoft.lamuertapokerintelligence.service.equity;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionEquityDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EquityAnalysisService {

    private static final long EXACT_OUTCOME_THRESHOLD = 2_500_000L;

    private final ExactEquityCalculator exactEquityCalculator;
    private final MonteCarloEquityCalculator monteCarloEquityCalculator;

    public EquityAnalysisService(
            ExactEquityCalculator exactEquityCalculator,
            MonteCarloEquityCalculator monteCarloEquityCalculator
    ) {
        this.exactEquityCalculator = exactEquityCalculator;
        this.monteCarloEquityCalculator = monteCarloEquityCalculator;
    }

    public DecisionEquityDto analyze(DecisionContext context, VillainRangeSet villainRangeSet) {
        if (context == null || context.getDecisionInput() == null || villainRangeSet == null) {
            return null;
        }

        String heroHandCode = context.getDecisionInput().getHeroHandCode();
        List<String> heroCards = context.getDecisionInput().getHeroCards();
        boolean exactHeroCards = context.getDecisionInput().isExactHeroCards();
        List<String> boardCards = context.getBoardState() == null ? List.of() : context.getBoardState().getCards();
        try {
            long outcomeCount = exactEquityCalculator.estimateOutcomeCount(
                    heroHandCode,
                    heroCards,
                    exactHeroCards,
                    boardCards,
                    villainRangeSet
            );
            if (outcomeCount > 0 && outcomeCount <= EXACT_OUTCOME_THRESHOLD) {
                return exactEquityCalculator.calculate(
                        heroHandCode,
                        heroCards,
                        exactHeroCards,
                        boardCards,
                        villainRangeSet
                );
            }
            return monteCarloEquityCalculator.calculate(
                    heroHandCode,
                    heroCards,
                    exactHeroCards,
                    boardCards,
                    villainRangeSet
            );
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
