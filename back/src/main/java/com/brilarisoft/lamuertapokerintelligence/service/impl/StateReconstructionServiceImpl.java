package com.brilarisoft.lamuertapokerintelligence.service.impl;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.ActionEvent;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.ActionSequence;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionInput;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.PlayerRole;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.service.StateReconstructionService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Derives stable state flags from the raw decision input and ordered action history.
 */
@Service
public class StateReconstructionServiceImpl implements StateReconstructionService {

    @Override
    public DecisionContext reconstruct(DecisionInput decisionInput) {
        List<ActionEvent> events = orderedEvents(decisionInput.getActionSequence());
        List<ActionEvent> preflopEvents = events.stream().filter(event -> event.getStreet() == Street.PREFLOP).toList();
        List<ActionEvent> currentStreetEvents = events.stream()
                .filter(event -> event.getStreet() == decisionInput.getStreet())
                .toList();

        DecisionContext context = new DecisionContext();
        context.setDecisionInput(decisionInput);
        context.setStrategyProfile(decisionInput.getStrategyProfile());
        context.setGameType(decisionInput.getGameType());
        context.setHeroPosition(decisionInput.getHeroPosition());
        context.setVillainPosition(decisionInput.getVillainPosition());
        context.setStreet(decisionInput.getStreet());
        context.setScenarioType(decisionInput.getScenarioType());
        context.setEffectiveStackInBigBlinds(decisionInput.getEffectiveStackInBigBlinds());
        context.setPotSizeInBigBlinds(decisionInput.getPotSizeInBigBlinds());
        context.setHeroHandCode(decisionInput.getHeroHandCode());
        context.setBoardState(decisionInput.getBoardState());
        context.setActionSequence(decisionInput.getActionSequence());

        ActionEvent preflopAggressor = lastAggressiveAction(preflopEvents);
        context.setHeroWasPreflopAggressor(preflopAggressor != null && preflopAggressor.getActorType() == PlayerRole.HERO);
        context.setVillainWasPreflopAggressor(preflopAggressor != null && preflopAggressor.getActorType() == PlayerRole.VILLAIN);

        context.setVillainCheckedToHero(currentStreetEvents.stream()
                .anyMatch(event -> event.getActorType() == PlayerRole.VILLAIN && event.getActionCode() == ActionType.CHECK));
        context.setHeroBetCurrentStreet(currentStreetEvents.stream()
                .anyMatch(event -> event.getActorType() == PlayerRole.HERO && isBetLike(event.getActionCode())));
        context.setVillainRaisedHeroBet(hasVillainRaisedHeroBet(currentStreetEvents));

        int betCount = (int) currentStreetEvents.stream().filter(event -> event.getActionCode() == ActionType.BET).count();
        int raiseCount = (int) currentStreetEvents.stream()
                .filter(event -> event.getActionCode() == ActionType.RAISE
                        || event.getActionCode() == ActionType.THREE_BET
                        || event.getActionCode() == ActionType.FOUR_BET)
                .count();

        context.setCurrentStreetBetCount(betCount);
        context.setCurrentStreetRaiseCount(raiseCount);
        context.setHeroFacingSecondBarrel(isFacingNthBarrel(events, decisionInput.getStreet(), 2));
        context.setHeroFacingThirdBarrel(isFacingNthBarrel(events, decisionInput.getStreet(), 3));
        context.setCurrentActor(lastActor(events));
        context.setFacingActionType(lastFacingAction(currentStreetEvents));
        context.setLineSignature(buildLineSignature(currentStreetEvents));
        return context;
    }

    private List<ActionEvent> orderedEvents(ActionSequence actionSequence) {
        if (actionSequence == null || actionSequence.getEvents() == null) {
            return List.of();
        }
        return actionSequence.getEvents().stream()
                .sorted(java.util.Comparator.comparing(ActionEvent::getOrderIndex))
                .toList();
    }

    private ActionEvent lastAggressiveAction(List<ActionEvent> events) {
        return events.stream()
                .filter(event -> isAggressive(event.getActionCode()))
                .reduce((left, right) -> right)
                .orElse(null);
    }

    private boolean isAggressive(ActionType actionType) {
        return actionType == ActionType.OPEN
                || actionType == ActionType.BET
                || actionType == ActionType.RAISE
                || actionType == ActionType.THREE_BET
                || actionType == ActionType.FOUR_BET
                || actionType == ActionType.SHOVE;
    }

    private boolean isBetLike(ActionType actionType) {
        return actionType == ActionType.BET
                || actionType == ActionType.OPEN
                || actionType == ActionType.RAISE
                || actionType == ActionType.THREE_BET
                || actionType == ActionType.FOUR_BET
                || actionType == ActionType.SHOVE;
    }

    private boolean hasVillainRaisedHeroBet(List<ActionEvent> currentStreetEvents) {
        boolean heroBetSeen = false;
        for (ActionEvent event : currentStreetEvents) {
            if (event.getActorType() == PlayerRole.HERO && isBetLike(event.getActionCode())) {
                heroBetSeen = true;
            }
            if (heroBetSeen && event.getActorType() == PlayerRole.VILLAIN
                    && (event.getActionCode() == ActionType.RAISE
                    || event.getActionCode() == ActionType.THREE_BET
                    || event.getActionCode() == ActionType.FOUR_BET)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFacingNthBarrel(List<ActionEvent> events, Street currentStreet, int nthBarrel) {
        if (currentStreet == Street.PREFLOP) {
            return false;
        }

        long heroFacingBets = events.stream()
                .filter(event -> event.getActorType() == PlayerRole.VILLAIN)
                .filter(event -> event.getStreet().ordinal() <= currentStreet.ordinal())
                .filter(event -> event.getActionCode() == ActionType.BET)
                .count();
        return heroFacingBets >= nthBarrel;
    }

    private PlayerRole lastActor(List<ActionEvent> events) {
        return events.isEmpty() ? null : events.get(events.size() - 1).getActorType();
    }

    private ActionType lastFacingAction(List<ActionEvent> currentStreetEvents) {
        if (currentStreetEvents.isEmpty()) {
            return null;
        }
        return currentStreetEvents.get(currentStreetEvents.size() - 1).getActionCode();
    }

    private String buildLineSignature(List<ActionEvent> currentStreetEvents) {
        if (currentStreetEvents.isEmpty()) {
            return null;
        }
        return currentStreetEvents.stream()
                .map(event -> event.getActorType().name() + "_" + event.getActionCode().name())
                .collect(Collectors.joining("__"));
    }
}
