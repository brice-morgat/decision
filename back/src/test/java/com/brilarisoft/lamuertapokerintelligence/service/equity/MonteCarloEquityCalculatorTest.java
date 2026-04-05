package com.brilarisoft.lamuertapokerintelligence.service.equity;

import static org.assertj.core.api.Assertions.assertThat;

import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeCell;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionEquityDto;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MonteCarloEquityCalculatorTest {

    private MonteCarloEquityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new MonteCarloEquityCalculator(new PokerHandEvaluator());
    }

    @Test
    void calculatesEquityOnCompleteBoard() {
        DecisionEquityDto equity = calculator.calculate(
                List.of("As", "Ah"),
                List.of("2c", "3d", "4h", "5s", "7c"),
                villainRange("KK", 100),
                500
        );

        assertThat(equity).isNotNull();
        assertThat(equity.heroEquityPercent()).isNotNull();
        assertThat(equity.villainEquityPercent()).isNotNull();
        assertThat(equity.tiePercent()).isNotNull();
    }

    @Test
    void returnsNullWhenVillainRangeHasNoPlayableCombo() {
        DecisionEquityDto equity = calculator.calculate(
                List.of("As", "Ah"),
                List.of("Ks", "Kh", "Kd"),
                villainRange("KK", 100),
                200
        );

        assertThat(equity).isNull();
    }

    @Test
    void supportsWeightedVillainRangeWithPartialBoard() {
        DecisionEquityDto equity = calculator.calculate(
                List.of("As", "Kh"),
                List.of("2c", "7d", "Th"),
                villainRange("QJO", 35),
                400
        );

        assertThat(equity).isNotNull();
        assertThat(equity.heroEquityPercent().doubleValue()).isBetween(0.0, 100.0);
        assertThat(equity.villainEquityPercent().doubleValue()).isBetween(0.0, 100.0);
        assertThat(equity.tiePercent().doubleValue()).isBetween(0.0, 100.0);
    }

    @Test
    void staysCloseToExactCalculatorOnKnownPreflopMatchup() {
        ExactEquityCalculator exactCalculator = new ExactEquityCalculator(new PokerHandEvaluator());
        DecisionEquityDto exact = exactCalculator.calculate(
                "AKS",
                List.of("As", "Ks"),
                true,
                List.of(),
                villainRange("QQ", 100)
        );
        DecisionEquityDto monteCarlo = calculator.calculate(
                List.of("As", "Ks"),
                List.of(),
                villainRange("QQ", 100),
                15000
        );

        assertThat(exact).isNotNull();
        assertThat(monteCarlo).isNotNull();
        assertThat(monteCarlo.heroEquityPercent())
                .isBetween(
                        exact.heroEquityPercent().subtract(new BigDecimal("1.50")),
                        exact.heroEquityPercent().add(new BigDecimal("1.50"))
                );
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
