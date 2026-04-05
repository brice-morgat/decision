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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select distinct rangeSet
            from VillainRangeSet rangeSet
            left join fetch rangeSet.scopes scopes
            where rangeSet.strategyProfile.id = :strategyProfileId
              and rangeSet.gameType = :gameType
              and rangeSet.street = :street
              and rangeSet.scenarioType = :scenarioType
              and rangeSet.enabled = true
            """)
    List<VillainRangeSet> findCandidatesWithScopes(
            @Param("strategyProfileId") UUID strategyProfileId,
            @Param("gameType") GameType gameType,
            @Param("street") Street street,
            @Param("scenarioType") ScenarioType scenarioType
    );

    @Query("""
            select distinct rangeSet
            from VillainRangeSet rangeSet
            left join fetch rangeSet.scopes scopes
            where rangeSet.strategyProfile.id = :strategyProfileId
              and rangeSet.gameType = :gameType
              and rangeSet.street = :street
              and rangeSet.enabled = true
            """)
    List<VillainRangeSet> findCandidatesWithScopesWithoutScenario(
            @Param("strategyProfileId") UUID strategyProfileId,
            @Param("gameType") GameType gameType,
            @Param("street") Street street
    );
}
