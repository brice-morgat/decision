package com.brilarisoft.lamuertapokerintelligence.controller;

import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewCreateDto;
import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewSummaryDto;
import jakarta.validation.Valid;
import com.brilarisoft.lamuertapokerintelligence.service.HandReviewService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

@Validated
@RestController
@RequestMapping("/history")
public class HandReviewController {

    private final HandReviewService handReviewService;

    public HandReviewController(HandReviewService handReviewService) {
        this.handReviewService = handReviewService;
    }

    @GetMapping
    public List<HandReviewSummaryDto> listRecentReviews() {
        return handReviewService.listRecentReviews();
    }

    @GetMapping("/{reviewId}")
    public HandReviewDetailDto getReview(@PathVariable UUID reviewId) {
        return handReviewService.getReview(reviewId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HandReviewDetailDto createReview(@Valid @RequestBody HandReviewCreateDto request) {
        return handReviewService.createReview(request);
    }
}
