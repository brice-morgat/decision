package com.brilarisoft.lamuertapokerintelligence.service.impl;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.BoardState;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionInput;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionResult;
import com.brilarisoft.lamuertapokerintelligence.domain.history.HandOutcome;
import com.brilarisoft.lamuertapokerintelligence.domain.history.HandReview;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewCreateDto;
import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.history.HandReviewSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.exception.BusinessValidationException;
import com.brilarisoft.lamuertapokerintelligence.exception.NotFoundException;
import com.brilarisoft.lamuertapokerintelligence.mapper.HandReviewMapper;
import com.brilarisoft.lamuertapokerintelligence.repository.HandReviewRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.StrategyProfileRepository;
import com.brilarisoft.lamuertapokerintelligence.service.HandReviewService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HandReviewServiceImpl implements HandReviewService {

    private final HandReviewRepository handReviewRepository;
    private final StrategyProfileRepository strategyProfileRepository;
    private final HandReviewMapper handReviewMapper;

    public HandReviewServiceImpl(
            HandReviewRepository handReviewRepository,
            StrategyProfileRepository strategyProfileRepository,
            HandReviewMapper handReviewMapper
    ) {
        this.handReviewRepository = handReviewRepository;
        this.strategyProfileRepository = strategyProfileRepository;
        this.handReviewMapper = handReviewMapper;
    }

    @Override
    public List<HandReviewSummaryDto> listRecentReviews() {
        return handReviewRepository.findTop20ByOrderByUpdatedAtDesc()
                .stream()
                .map(handReviewMapper::toSummaryDto)
                .toList();
    }

    @Override
    public HandReviewDetailDto getReview(UUID reviewId) {
        return handReviewRepository.findById(reviewId)
                .map(handReviewMapper::toDetailDto)
                .orElseThrow(() -> new NotFoundException("Hand review not found: " + reviewId));
    }

    @Override
    @Transactional
    public HandReviewDetailDto createReview(HandReviewCreateDto request) {
        validateIssueConsistency(request);

        var strategyProfile = strategyProfileRepository.findById(request.strategyProfileId())
                .orElseThrow(() -> new NotFoundException("Strategy profile not found: " + request.strategyProfileId()));

        HandReview review = new HandReview();
        review.setStrategyProfile(strategyProfile);
        review.setReviewNote(request.reviewNote());

        DecisionInput decisionInput = new DecisionInput();
        decisionInput.setStrategyProfile(strategyProfile);
        decisionInput.setGameType(strategyProfile.getGameType());
        decisionInput.setHeroPosition(Position.BTN);
        decisionInput.setVillainPosition(Position.BB);
        decisionInput.setStreet(Street.PREFLOP);
        decisionInput.setScenarioType(ScenarioType.OPEN_FIRST_IN);
        decisionInput.setEffectiveStackInBigBlinds(BigDecimal.ZERO);
        decisionInput.setPotSizeInBigBlinds(BigDecimal.ZERO);
        decisionInput.setHeroHandCode(request.heroHandCode());
        decisionInput.setHeroCards(List.of(request.heroHandCode()));

        BoardState boardState = new BoardState();
        boardState.setCards(parseBoardCards(request.boardCards()));
        decisionInput.setBoardState(boardState);
        review.setDecisionInput(decisionInput);

        DecisionResult decisionResult = new DecisionResult();
        decisionResult.setStatus(request.decisionStatus());
        decisionResult.setRecommendedAction(request.recommendedAction());
        decisionResult.setExplanation(request.engineExplanation() == null ? "Review captured manually" : request.engineExplanation());
        review.setDecisionResult(decisionResult);

        HandOutcome handOutcome = new HandOutcome();
        handOutcome.setActualAction(request.actualAction());
        handOutcome.setNetResultInBigBlinds(request.netResultInBigBlinds());
        handOutcome.setIssue(request.issue());
        handOutcome.setShowdownReached(false);
        handOutcome.setNote(request.situationSummary());
        review.setHandOutcome(handOutcome);

        return handReviewMapper.toDetailDto(handReviewRepository.save(review));
    }

    private List<String> parseBoardCards(String boardCards) {
        if (boardCards == null || boardCards.isBlank()) {
            return List.of();
        }

        return java.util.Arrays.stream(boardCards.split(","))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .toList();
    }

    private void validateIssueConsistency(HandReviewCreateDto request) {
        int sign = request.netResultInBigBlinds().signum();
        switch (request.issue()) {
            case WIN -> {
                if (sign <= 0) {
                    throw new BusinessValidationException("Issue WIN requires a positive netResultInBigBlinds");
                }
            }
            case LOSS -> {
                if (sign >= 0) {
                    throw new BusinessValidationException("Issue LOSS requires a negative netResultInBigBlinds");
                }
            }
            case BREAKEVEN -> {
                if (sign != 0) {
                    throw new BusinessValidationException("Issue BREAKEVEN requires a zero netResultInBigBlinds");
                }
            }
            default -> throw new BusinessValidationException("Unsupported review issue");
        }
    }
}
