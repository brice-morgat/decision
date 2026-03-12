package com.brilarisoft.lamuertapokerintelligence.mapper;

import com.brilarisoft.lamuertapokerintelligence.domain.history.HandReview;
import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewSummaryDto;
import org.springframework.stereotype.Component;

@Component
public class HandReviewMapper {

    public HandReviewSummaryDto toSummaryDto(HandReview handReview) {
        return new HandReviewSummaryDto(
                handReview.getId(),
                handReview.getStrategyProfile().getId(),
                handReview.getStrategyProfile().getName(),
                handReview.getDecisionInput() == null ? null : handReview.getDecisionInput().getHeroHandCode(),
                extractBoardCards(handReview),
                handReview.getDecisionResult() == null ? null : handReview.getDecisionResult().getStatus(),
                handReview.getDecisionResult() == null ? null : handReview.getDecisionResult().getRecommendedAction(),
                handReview.getHandOutcome() == null ? null : handReview.getHandOutcome().getActualAction(),
                handReview.getHandOutcome() == null ? null : handReview.getHandOutcome().getNetResultInBigBlinds(),
                handReview.getHandOutcome() == null ? null : handReview.getHandOutcome().getIssue(),
                handReview.getDecisionResult() == null ? null : handReview.getDecisionResult().getExplanation(),
                handReview.getUpdatedAt()
        );
    }

    public HandReviewDetailDto toDetailDto(HandReview handReview) {
        return new HandReviewDetailDto(
                handReview.getId(),
                handReview.getStrategyProfile().getId(),
                handReview.getStrategyProfile().getName(),
                handReview.getDecisionInput() == null ? null : handReview.getDecisionInput().getHeroHandCode(),
                extractBoardCards(handReview),
                handReview.getDecisionResult() == null ? null : handReview.getDecisionResult().getStatus(),
                handReview.getDecisionResult() == null ? null : handReview.getDecisionResult().getRecommendedAction(),
                handReview.getHandOutcome() == null ? null : handReview.getHandOutcome().getActualAction(),
                handReview.getHandOutcome() == null ? null : handReview.getHandOutcome().getNetResultInBigBlinds(),
                handReview.getHandOutcome() == null ? null : handReview.getHandOutcome().getIssue(),
                handReview.getDecisionResult() == null ? null : handReview.getDecisionResult().getExplanation(),
                handReview.getHandOutcome() == null ? null : handReview.getHandOutcome().getNote(),
                handReview.getReviewNote(),
                handReview.getCreatedAt(),
                handReview.getUpdatedAt()
        );
    }

    private String extractBoardCards(HandReview handReview) {
        return handReview.getDecisionInput() != null
                && handReview.getDecisionInput().getBoardState() != null
                && handReview.getDecisionInput().getBoardState().getCards() != null
                ? String.join(",", handReview.getDecisionInput().getBoardState().getCards())
                : null;
    }
}
