package com.brilarisoft.lamuertapokerintelligence.service;

import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewCreateDto;
import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewSummaryDto;
import java.util.List;
import java.util.UUID;

public interface HandReviewService {

    List<HandReviewSummaryDto> listRecentReviews();

    HandReviewDetailDto getReview(UUID reviewId);

    HandReviewDetailDto createReview(HandReviewCreateDto request);
}
