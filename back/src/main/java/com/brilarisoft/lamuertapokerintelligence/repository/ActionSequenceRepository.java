package com.brilarisoft.lamuertapokerintelligence.repository;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.ActionSequence;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionSequenceRepository extends JpaRepository<ActionSequence, UUID> {
}
