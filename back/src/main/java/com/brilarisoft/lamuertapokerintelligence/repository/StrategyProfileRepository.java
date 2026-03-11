package com.brilarisoft.lamuertapokerintelligence.repository;

import com.brilarisoft.lamuertapokerintelligence.domain.strategyprofile.StrategyProfile;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StrategyProfileRepository extends JpaRepository<StrategyProfile, UUID> {

    List<StrategyProfile> findAllByOrderByUpdatedAtDesc();

    List<StrategyProfile> findAllByGameTypeAndActiveTrueAndArchivedFalse(GameType gameType);

    Optional<StrategyProfile> findByActiveTrueAndArchivedFalse();

    Optional<StrategyProfile> findByNameIgnoreCase(String name);
}
