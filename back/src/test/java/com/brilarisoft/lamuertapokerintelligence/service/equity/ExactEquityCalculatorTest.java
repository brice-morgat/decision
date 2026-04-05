package com.brilarisoft.lamuertapokerintelligence.service.equity;

import static org.assertj.core.api.Assertions.assertThat;

import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeCell;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionEquityDto;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExactEquityCalculatorTest {

    private ExactEquityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ExactEquityCalculator(new PokerHandEvaluator());
    }

    @Test
    void calculatesExactTurnEquityForSpecificHands() {
        DecisionEquityDto equity = calculator.calculate(
                "AA",
                List.of("Ah", "Ac"),
                true,
                List.of("Qh", "Jd", "8s", "2c"),
                villainRange("KK", 100)
        );

        assertThat(equity).isNotNull();
        assertThat(equity.heroEquityPercent()).isEqualByComparingTo(new BigDecimal("95.45"));
        assertThat(equity.villainEquityPercent()).isEqualByComparingTo(new BigDecimal("4.55"));
        assertThat(equity.tiePercent()).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    void expandsHeroHandCodeWhenExactCardsAreNotProvided() {
        DecisionEquityDto equity = calculator.calculate(
                "AKS",
                List.of(),
                false,
                List.of("Qh", "Jd", "8s"),
                villainRange("QQ", 100)
        );

        assertThat(equity).isNotNull();
        assertThat(equity.heroEquityPercent()).isBetween(new BigDecimal("0.00"), new BigDecimal("100.00"));
        assertThat(equity.villainEquityPercent()).isBetween(new BigDecimal("0.00"), new BigDecimal("100.00"));
        assertThat(equity.tiePercent()).isBetween(new BigDecimal("0.00"), new BigDecimal("100.00"));
    }

    @Test
    void matchesPublishedPreflopEquityForPocketAcesVersusPocketKings() {
        DecisionEquityDto equity = calculator.calculate(
                "AA",
                List.of(),
                false,
                List.of(),
                villainRange("KK", 100)
        );

        assertThat(equity).isNotNull();
        assertThat(equity.heroEquityPercent()).isEqualByComparingTo(new BigDecimal("81.95"));
        assertThat(equity.villainEquityPercent()).isEqualByComparingTo(new BigDecimal("18.05"));
        assertThat(equity.tiePercent()).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    void matchesPublishedPreflopEquityForPocketAcesVersusAceKingOffsuit() {
        DecisionEquityDto equity = calculator.calculate(
                "AA",
                List.of(),
                false,
                List.of(),
                villainRange("AKO", 100)
        );

        assertThat(equity).isNotNull();
        assertThat(equity.heroEquityPercent()).isEqualByComparingTo(new BigDecimal("93.17"));
        assertThat(equity.villainEquityPercent()).isEqualByComparingTo(new BigDecimal("6.83"));
        assertThat(equity.tiePercent()).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    private VillainRangeSet villainRange(String handCode, int weight) {
        VillainRangeSet rangeSet = new VillainRangeSet();
        VillainRangeCell cell = new VillainRangeCell();
        cell.setVillainRangeSet(rangeSet);
        cell.setHandCode(handCode);
        cell.setEnabled(true);
        cell.setWeightPercent(weight);
        rangeSet.setCells(List.of(cell));
        return rangeSet;
    }
}
