package com.brilarisoft.lamuertapokerintelligence.repository;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.DecisionRule;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DecisionRuleRepository extends JpaRepository<DecisionRule, UUID>, JpaSpecificationExecutor<DecisionRule> {

    List<DecisionRule> findByStrategyProfileIdAndStreetAndScenarioTypeAndActiveTrueOrderByPriorityAsc(
            UUID strategyProfileId,
            Street street,
            ScenarioType scenarioType
    );
}
