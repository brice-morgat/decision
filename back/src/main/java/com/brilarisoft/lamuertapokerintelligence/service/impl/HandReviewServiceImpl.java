package com.brilarisoft.lamuertapokerintelligence.service.impl;

import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.mapper.HandReviewMapper;
import com.brilarisoft.lamuertapokerintelligence.repository.HandReviewRepository;
import com.brilarisoft.lamuertapokerintelligence.service.HandReviewService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HandReviewServiceImpl implements HandReviewService {

    private final HandReviewRepository handReviewRepository;
    private final HandReviewMapper handReviewMapper;

    public HandReviewServiceImpl(HandReviewRepository handReviewRepository, HandReviewMapper handReviewMapper) {
        this.handReviewRepository = handReviewRepository;
        this.handReviewMapper = handReviewMapper;
    }

    @Override
    public List<HandReviewSummaryDto> listRecentReviews() {
        return handReviewRepository.findTop20ByOrderByUpdatedAtDesc()
                .stream()
                .map(handReviewMapper::toSummaryDto)
                .toList();
    }
}
