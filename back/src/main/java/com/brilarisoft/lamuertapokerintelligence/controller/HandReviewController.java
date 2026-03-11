package com.brilarisoft.lamuertapokerintelligence.controller;

import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.service.HandReviewService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
public class HandReviewController {

    private final HandReviewService handReviewService;

    public HandReviewController(HandReviewService handReviewService) {
        this.handReviewService = handReviewService;
    }

    @GetMapping
    public List<HandReviewSummaryDto> listRecentReviews() {
        return handReviewService.listRecentReviews();
    }
}
