package com.brilarisoft.lamuertapokerintelligence.repository;

import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeCell;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeroRangeCellRepository extends JpaRepository<HeroRangeCell, UUID> {
}
