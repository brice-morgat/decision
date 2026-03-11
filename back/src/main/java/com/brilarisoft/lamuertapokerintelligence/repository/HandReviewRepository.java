package com.brilarisoft.lamuertapokerintelligence.repository;

import com.brilarisoft.lamuertapokerintelligence.domain.history.HandReview;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HandReviewRepository extends JpaRepository<HandReview, UUID> {

    List<HandReview> findTop20ByOrderByUpdatedAtDesc();
}
