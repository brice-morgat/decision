package com.brilarisoft.lamuertapokerintelligence.mapper;

import com.brilarisoft.lamuertapokerintelligence.domain.history.HandReview;
import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewSummaryDto;
import org.springframework.stereotype.Component;

@Component
public class HandReviewMapper {

    public HandReviewSummaryDto toSummaryDto(HandReview handReview) {
        String boardCards = handReview.getDecisionInput() != null
                && handReview.getDecisionInput().getBoardState() != null
                && handReview.getDecisionInput().getBoardState().getCards() != null
                ? String.join(",", handReview.getDecisionInput().getBoardState().getCards())
                : null;

        return new HandReviewSummaryDto(
                handReview.getId(),
                handReview.getStrategyProfile().getId(),
                handReview.getStrategyProfile().getName(),
                handReview.getDecisionInput() == null ? null : handReview.getDecisionInput().getHeroHandCode(),
                boardCards,
                handReview.getDecisionResult() == null ? null : handReview.getDecisionResult().getStatus(),
                handReview.getDecisionResult() == null ? null : handReview.getDecisionResult().getRecommendedAction(),
                handReview.getDecisionResult() == null ? null : handReview.getDecisionResult().getExplanation(),
                handReview.getUpdatedAt()
        );
    }
}
