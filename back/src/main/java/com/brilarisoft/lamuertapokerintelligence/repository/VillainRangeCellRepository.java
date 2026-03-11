package com.brilarisoft.lamuertapokerintelligence.repository;

import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeCell;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VillainRangeCellRepository extends JpaRepository<VillainRangeCell, UUID> {
}
