package com.brilarisoft.lamuertapokerintelligence.mapper;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.ActionEvent;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.ActionSequence;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.BoardState;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionInput;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionResult;
import com.brilarisoft.lamuertapokerintelligence.domain.range.PokerHandCodeCatalog;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.SizingType;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.ActionEventDto;
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

        DecisionInput decisionInput = new DecisionInput();
        decisionInput.setGameType(request.gameType());
        decisionInput.setHeroPosition(request.heroPosition());
        decisionInput.setVillainPosition(request.villainPosition());
        decisionInput.setStreet(request.street());
        decisionInput.setScenarioType(request.scenarioType());
        decisionInput.setEffectiveStackInBigBlinds(request.effectiveStackInBigBlinds());
        decisionInput.setPotSizeInBigBlinds(request.potSizeInBigBlinds());
        decisionInput.setHeroCards(request.heroCards());
        decisionInput.setHeroHandCode(PokerHandCodeCatalog.fromCards(request.heroCards()));
        decisionInput.setBoardState(boardState);
        decisionInput.setActionSequence(actionSequence);
        return decisionInput;
    }

    public DecisionResponseDto toResponseDto(DecisionResult result, List<MatchedRuleCandidateDto> matchedCandidates) {
        return new DecisionResponseDto(
                result.getStatus(),
                result.getRecommendedAction(),
                result.getRecommendedSizingValue(),
                result.getMatchedRule() == null ? null : result.getMatchedRule().getId(),
                result.getMatchedRule() == null ? null : result.getMatchedRule().getName(),
                result.getExplanation(),
                result.getTrace(),
                result.getWarnings(),
                matchedCandidates
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
}
