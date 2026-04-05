package com.brilarisoft.lamuertapokerintelligence.repository;

import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeroRangeSetRepository extends JpaRepository<HeroRangeSet, UUID> {

    List<HeroRangeSet> findByStrategyProfileIdOrderByUpdatedAtDesc(UUID strategyProfileId);

    Optional<HeroRangeSet> findByStrategyProfileIdAndGameTypeAndStreetAndHeroPositionAndScenarioTypeAndSubScenarioCode(
            UUID strategyProfileId,
            GameType gameType,
            Street street,
            Position heroPosition,
            ScenarioType scenarioType,
            String subScenarioCode
    );

    List<HeroRangeSet> findByStrategyProfileIdAndGameTypeAndStreetAndHeroPositionOrderByPriorityAscUpdatedAtDesc(
            UUID strategyProfileId,
            GameType gameType,
            Street street,
            Position heroPosition
    );
}
