package com.brilarisoft.lamuertapokerintelligence.mapper;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.ActionEvent;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.ActionSequence;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.BoardState;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionInput;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionResult;
import com.brilarisoft.lamuertapokerintelligence.domain.range.PokerHandCodeCatalog;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.SizingType;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.ActionEventDto;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionAnalysisDto;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionEquityDto;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionRequestDto;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionResponseDto;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.MatchedRuleCandidateDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DecisionMapper {

    public DecisionInput toDecisionInput(DecisionRequestDto request) {
        ActionSequence actionSequence = new ActionSequence();
        actionSequence.setCurrentStreet(request.street());
        actionSequence.setName("decision-request-sequence");

        List<ActionEvent> actionEvents = request.actionEvents() == null
                ? List.of()
                : request.actionEvents().stream()
                        .map(dto -> toActionEvent(dto, actionSequence))
                        .toList();
        actionSequence.setEvents(actionEvents);

        BoardState boardState = new BoardState();
        boardState.setCards(request.boardCards() == null ? List.of() : request.boardCards());

        String heroHandCode = request.heroHandCode() == null || request.heroHandCode().isBlank()
                ? PokerHandCodeCatalog.fromCards(request.heroCards())
                : request.heroHandCode().trim().toUpperCase();
        List<String> heroCards = request.heroCards() == null || request.heroCards().isEmpty()
                ? toSyntheticCards(heroHandCode)
                : request.heroCards();

        DecisionInput decisionInput = new DecisionInput();
        decisionInput.setGameType(request.gameType());
        decisionInput.setHeroPosition(request.heroPosition());
        decisionInput.setVillainPosition(request.villainPosition());
        decisionInput.setStreet(request.street());
        decisionInput.setScenarioType(request.scenarioType());
        decisionInput.setEffectiveStackInBigBlinds(request.effectiveStackInBigBlinds());
        decisionInput.setPotSizeInBigBlinds(request.potSizeInBigBlinds());
        decisionInput.setHeroCards(heroCards);
        decisionInput.setExactHeroCards(request.heroCards() != null && request.heroCards().size() == 2);
        decisionInput.setHeroHandCode(heroHandCode);
        decisionInput.setBoardState(boardState);
        decisionInput.setActionSequence(actionSequence);
        return decisionInput;
    }

    public DecisionResponseDto toResponseDto(
            DecisionResult result,
            DecisionEquityDto equity,
            List<MatchedRuleCandidateDto> matchedCandidates
    ) {
        return new DecisionResponseDto(
                result.getStatus(),
                result.getRecommendedAction(),
                result.getRecommendedSizingValue(),
                result.getMatchedRule() == null ? null : result.getMatchedRule().getId(),
                result.getMatchedRule() == null ? null : result.getMatchedRule().getName(),
                result.getExplanation(),
                toAnalysisDto(result),
                equity,
                result.getTrace(),
                result.getWarnings(),
                matchedCandidates
        );
    }

    private DecisionAnalysisDto toAnalysisDto(DecisionResult result) {
        String decisionSource = null;
        Double villainCoverage = null;
        Integer handStrengthScore = null;
        List<String> insights = result.getTrace() == null ? List.of() : result.getTrace();

        for (String item : insights) {
            if (item == null) {
                continue;
            }
            if (item.startsWith("Decision source: ")) {
                decisionSource = item.substring("Decision source: ".length());
            } else if (item.startsWith("Villain coverage: ")) {
                String rawValue = item.substring("Villain coverage: ".length()).replace("%", "").trim();
                villainCoverage = Double.valueOf(rawValue);
            } else if (item.startsWith("Hero hand strength score: ")) {
                String rawValue = item.substring("Hero hand strength score: ".length()).trim();
                handStrengthScore = Integer.valueOf(rawValue);
            }
        }

        return new DecisionAnalysisDto(
                decisionSource,
                result.getDecisionContext() == null ? null : result.getDecisionContext().getHeroHandCode(),
                result.getDecisionContext() == null || result.getDecisionContext().getHeroStrategyLegend() == null
                        ? null
                        : result.getDecisionContext().getHeroStrategyLegend().name(),
                villainCoverage,
                handStrengthScore,
                insights
        );
    }

    private ActionEvent toActionEvent(ActionEventDto dto, ActionSequence actionSequence) {
        ActionEvent actionEvent = new ActionEvent();
        actionEvent.setActionSequence(actionSequence);
        actionEvent.setOrderIndex(dto.orderIndex());
        actionEvent.setActorType(dto.actorType());
        actionEvent.setActorPosition(dto.actorPosition());
        actionEvent.setStreet(dto.street());
        actionEvent.setActionCode(dto.actionCode());
        actionEvent.setSizingType(dto.sizingType() == null ? SizingType.CATEGORY : dto.sizingType());
        actionEvent.setSizingValue(dto.sizingValue());
        actionEvent.setPotSizeBefore(dto.potSizeBefore());
        actionEvent.setPotSizeAfter(dto.potSizeAfter());
        actionEvent.setStackBefore(dto.stackBefore());
        actionEvent.setStackAfter(dto.stackAfter());
        actionEvent.setNote(dto.note());
        return actionEvent;
    }

    private List<String> toSyntheticCards(String heroHandCode) {
        if (heroHandCode == null || heroHandCode.isBlank()) {
            return List.of();
        }

        String normalized = heroHandCode.trim().toUpperCase();
        char firstRank = normalized.charAt(0);
        char secondRank = normalized.charAt(1);
        if (normalized.length() == 2) {
            return List.of(firstRank + "s", secondRank + "h");
        }

        boolean suited = normalized.endsWith("S");
        return suited
                ? List.of(firstRank + "s", secondRank + "s")
                : List.of(firstRank + "s", secondRank + "d");
    }
}
