package com.brilarisoft.lamuertapokerintelligence.service.ruleengine;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PriorityResolver {

    public ResolvedRuleSelection resolve(List<RuleMatchCandidate> candidates) {
        if (candidates.isEmpty()) {
            return new ResolvedRuleSelection(null, List.of(), false);
        }

        List<RuleMatchCandidate> sorted = candidates.stream()
                .sorted(comparator())
                .toList();

        if (sorted.size() > 1 && samePriorityBand(sorted.get(0), sorted.get(1))) {
            return new ResolvedRuleSelection(null, sorted, true);
        }
        return new ResolvedRuleSelection(sorted.get(0), sorted, false);
    }

    private Comparator<RuleMatchCandidate> comparator() {
        return Comparator
                .comparing((RuleMatchCandidate candidate) -> candidate.rule().getPriority())
                .thenComparing(RuleMatchCandidate::matchedConditionCount, Comparator.reverseOrder())
                .thenComparing(RuleMatchCandidate::structuralSpecificityScore, Comparator.reverseOrder())
                .thenComparing(candidate -> candidate.rule().getUpdatedAt(), Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(candidate -> candidate.rule().getId());
    }

    private boolean samePriorityBand(RuleMatchCandidate left, RuleMatchCandidate right) {
        return left.rule().getPriority().equals(right.rule().getPriority())
                && left.matchedConditionCount() == right.matchedConditionCount()
                && left.structuralSpecificityScore() == right.structuralSpecificityScore()
                && java.util.Objects.equals(left.rule().getUpdatedAt(), right.rule().getUpdatedAt());
    }
}
