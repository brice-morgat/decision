package com.brilarisoft.lamuertapokerintelligence.repository;

import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VillainRangeSetRepository extends JpaRepository<VillainRangeSet, UUID> {

    List<VillainRangeSet> findByStrategyProfileIdOrderByUpdatedAtDesc(UUID strategyProfileId);

    Optional<VillainRangeSet> findByStrategyProfileIdAndGameTypeAndStreetAndVillainPositionAndHeroPositionAndScenarioTypeAndTriggerActionTypeAndLineSignature(
            UUID strategyProfileId,
            GameType gameType,
            Street street,
            Position villainPosition,
            Position heroPosition,
            ScenarioType scenarioType,
            ActionType triggerActionType,
            String lineSignature
    );
}
