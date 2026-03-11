package com.brilarisoft.lamuertapokerintelligence.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.ActionEvent;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.ActionSequence;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.PlayerRole;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.exception.BusinessValidationException;
import com.brilarisoft.lamuertapokerintelligence.repository.ActionSequenceRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActionSequenceServiceImplTest {

    @Mock
    private ActionSequenceRepository actionSequenceRepository;

    private ActionSequenceServiceImpl actionSequenceService;

    @BeforeEach
    void setUp() {
        actionSequenceService = new ActionSequenceServiceImpl(actionSequenceRepository);
    }

    @Test
    void replaceEventsRejectsDuplicateOrderIndex() {
        ActionSequence sequence = new ActionSequence();

        assertThatThrownBy(() -> actionSequenceService.replaceEvents(sequence, List.of(
                event(1, ActionType.OPEN),
                event(1, ActionType.CALL)
        ))).isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void getOrderedEventsSortsByOrderIndex() {
        ActionSequence sequence = new ActionSequence();
        sequence.setEvents(List.of(event(2, ActionType.CALL), event(1, ActionType.OPEN)));

        assertThat(actionSequenceService.getOrderedEvents(sequence))
                .extracting(ActionEvent::getOrderIndex)
                .containsExactly(1, 2);
    }

    private ActionEvent event(int orderIndex, ActionType actionType) {
        ActionEvent event = new ActionEvent();
        event.setOrderIndex(orderIndex);
        event.setStreet(Street.PREFLOP);
        event.setActorType(PlayerRole.HERO);
        event.setActionCode(actionType);
        event.setPotSizeBefore(BigDecimal.ONE);
        event.setPotSizeAfter(BigDecimal.TEN);
        event.setStackBefore(BigDecimal.TEN);
        event.setStackAfter(BigDecimal.ONE);
        return event;
    }
}
