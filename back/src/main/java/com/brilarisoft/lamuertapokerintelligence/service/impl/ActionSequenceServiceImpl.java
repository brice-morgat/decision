package com.brilarisoft.lamuertapokerintelligence.service.impl;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.ActionEvent;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.ActionSequence;
import com.brilarisoft.lamuertapokerintelligence.exception.BusinessValidationException;
import com.brilarisoft.lamuertapokerintelligence.exception.NotFoundException;
import com.brilarisoft.lamuertapokerintelligence.repository.ActionSequenceRepository;
import com.brilarisoft.lamuertapokerintelligence.service.ActionSequenceService;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Maintains ordered action sequences and validates event invariants.
 */
@Service
@Transactional
public class ActionSequenceServiceImpl implements ActionSequenceService {

    private final ActionSequenceRepository actionSequenceRepository;

    public ActionSequenceServiceImpl(ActionSequenceRepository actionSequenceRepository) {
        this.actionSequenceRepository = actionSequenceRepository;
    }

    @Override
    public ActionSequence createSequence(String name) {
        ActionSequence sequence = new ActionSequence();
        sequence.setName(name);
        return actionSequenceRepository.save(sequence);
    }

    @Override
    public ActionSequence replaceEvents(ActionSequence actionSequence, List<ActionEvent> events) {
        validateEvents(events);
        actionSequence.getEvents().clear();
        for (ActionEvent event : events.stream().sorted(Comparator.comparing(ActionEvent::getOrderIndex)).toList()) {
            event.setActionSequence(actionSequence);
            actionSequence.getEvents().add(event);
        }
        return actionSequenceRepository.save(actionSequence);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActionEvent> getOrderedEvents(ActionSequence actionSequence) {
        return actionSequence.getEvents().stream()
                .sorted(Comparator.comparing(ActionEvent::getOrderIndex))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ActionSequence getSequence(UUID sequenceId) {
        return actionSequenceRepository.findById(sequenceId)
                .orElseThrow(() -> new NotFoundException("Action sequence not found: " + sequenceId));
    }

    private void validateEvents(List<ActionEvent> events) {
        Set<Integer> orderIndexes = new HashSet<>();
        for (ActionEvent event : events) {
            if (event.getOrderIndex() == null || event.getStreet() == null
                    || event.getActorType() == null || event.getActionCode() == null) {
                throw new BusinessValidationException("Action events require orderIndex, street, actorType and actionCode");
            }
            if (!orderIndexes.add(event.getOrderIndex())) {
                throw new BusinessValidationException("Action event orderIndex must be unique within a sequence");
            }
            validateMonotonicAmounts(event.getPotSizeBefore(), event.getPotSizeAfter(), "pot sizes");
            validateMonotonicAmounts(event.getStackAfter(), event.getStackBefore(), "stacks");
        }
    }

    private void validateMonotonicAmounts(BigDecimal lowerBound, BigDecimal upperBound, String label) {
        if (lowerBound != null && upperBound != null && lowerBound.compareTo(upperBound) > 0) {
            throw new BusinessValidationException("Invalid " + label + " ordering");
        }
    }
}
