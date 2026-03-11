package com.brilarisoft.lamuertapokerintelligence.service;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.ActionEvent;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.ActionSequence;
import java.util.List;
import java.util.UUID;

public interface ActionSequenceService {

    ActionSequence createSequence(String name);

    ActionSequence replaceEvents(ActionSequence actionSequence, List<ActionEvent> events);

    List<ActionEvent> getOrderedEvents(ActionSequence actionSequence);

    ActionSequence getSequence(UUID sequenceId);
}
