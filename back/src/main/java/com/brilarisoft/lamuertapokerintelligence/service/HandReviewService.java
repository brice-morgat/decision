package com.brilarisoft.lamuertapokerintelligence.service;

import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewSummaryDto;
import java.util.List;

public interface HandReviewService {

    List<HandReviewSummaryDto> listRecentReviews();
}
