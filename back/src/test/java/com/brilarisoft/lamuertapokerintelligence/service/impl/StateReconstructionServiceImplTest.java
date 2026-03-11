package com.brilarisoft.lamuertapokerintelligence.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.ActionEvent;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.ActionSequence;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.BoardState;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionInput;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.PlayerRole;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StateReconstructionServiceImplTest {

    private StateReconstructionServiceImpl stateReconstructionService;

    @BeforeEach
    void setUp() {
        stateReconstructionService = new StateReconstructionServiceImpl();
    }

    @Test
    void reconstructDetectsPreflopAggressorAndRaiseFlags() {
        DecisionContext context = stateReconstructionService.reconstruct(input(List.of(
                event(1, Street.PREFLOP, PlayerRole.HERO, ActionType.OPEN),
                event(2, Street.PREFLOP, PlayerRole.VILLAIN, ActionType.CALL),
                event(3, Street.FLOP, PlayerRole.VILLAIN, ActionType.CHECK),
                event(4, Street.FLOP, PlayerRole.HERO, ActionType.BET),
                event(5, Street.FLOP, PlayerRole.VILLAIN, ActionType.RAISE)
        ), Street.FLOP));

        assertThat(context.isHeroWasPreflopAggressor()).isTrue();
        assertThat(context.isVillainCheckedToHero()).isTrue();
        assertThat(context.isHeroBetCurrentStreet()).isTrue();
        assertThat(context.isVillainRaisedHeroBet()).isTrue();
        assertThat(context.getCurrentStreetRaiseCount()).isEqualTo(1);
        assertThat(context.getLineSignature()).isEqualTo("VILLAIN_CHECK__HERO_BET__VILLAIN_RAISE");
    }

    @Test
    void reconstructDetectsSecondAndThirdBarrels() {
        DecisionContext turnContext = stateReconstructionService.reconstruct(input(List.of(
                event(1, Street.FLOP, PlayerRole.VILLAIN, ActionType.BET),
                event(2, Street.TURN, PlayerRole.VILLAIN, ActionType.BET)
        ), Street.TURN));
        DecisionContext riverContext = stateReconstructionService.reconstruct(input(List.of(
                event(1, Street.FLOP, PlayerRole.VILLAIN, ActionType.BET),
                event(2, Street.TURN, PlayerRole.VILLAIN, ActionType.BET),
                event(3, Street.RIVER, PlayerRole.VILLAIN, ActionType.BET)
        ), Street.RIVER));

        assertThat(turnContext.isHeroFacingSecondBarrel()).isTrue();
        assertThat(turnContext.isHeroFacingThirdBarrel()).isFalse();
        assertThat(riverContext.isHeroFacingThirdBarrel()).isTrue();
    }

    private DecisionInput input(List<ActionEvent> events, Street street) {
        ActionSequence actionSequence = new ActionSequence();
        actionSequence.setEvents(events);
        DecisionInput input = new DecisionInput();
        input.setGameType(GameType.CASH);
        input.setHeroPosition(Position.BTN);
        input.setVillainPosition(Position.BB);
        input.setStreet(street);
        input.setScenarioType(ScenarioType.OPEN_FIRST_IN);
        input.setEffectiveStackInBigBlinds(BigDecimal.valueOf(100));
        input.setPotSizeInBigBlinds(BigDecimal.valueOf(10));
        input.setHeroHandCode("AKS");
        input.setHeroCards(List.of("As", "Ks"));
        BoardState boardState = new BoardState();
        boardState.setCards(List.of());
        input.setBoardState(boardState);
        input.setActionSequence(actionSequence);
        return input;
    }

    private ActionEvent event(int orderIndex, Street street, PlayerRole actorType, ActionType actionCode) {
        ActionEvent event = new ActionEvent();
        event.setOrderIndex(orderIndex);
        event.setStreet(street);
        event.setActorType(actorType);
        event.setActionCode(actionCode);
        return event;
    }
}
