package com.brilarisoft.lamuertapokerintelligence.service.ruleengine;

import static org.assertj.core.api.Assertions.assertThat;

import com.brilarisoft.lamuertapokerintelligence.domain.rule.DecisionRule;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PriorityResolverTest {

    private PriorityResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new PriorityResolver();
    }

    @Test
    void resolvesByPriorityThenSpecificity() {
        RuleMatchCandidate lowPriority = candidate(5, 1, 1, Instant.parse("2026-03-10T10:00:00Z"));
        RuleMatchCandidate highPriority = candidate(1, 1, 1, Instant.parse("2026-03-10T10:00:00Z"));
        RuleMatchCandidate moreSpecific = candidate(1, 3, 2, Instant.parse("2026-03-10T10:00:00Z"));

        ResolvedRuleSelection selection = resolver.resolve(List.of(lowPriority, highPriority, moreSpecific));

        assertThat(selection.conflicting()).isFalse();
        assertThat(selection.winningCandidate()).isEqualTo(moreSpecific);
    }

    @Test
    void flagsConflictWhenTopCandidatesAreEquivalent() {
        Instant updatedAt = Instant.parse("2026-03-10T10:00:00Z");
        ResolvedRuleSelection selection = resolver.resolve(List.of(
                candidate(1, 2, 1, updatedAt),
                candidate(1, 2, 1, updatedAt)
        ));

        assertThat(selection.conflicting()).isTrue();
        assertThat(selection.winningCandidate()).isNull();
    }

    private RuleMatchCandidate candidate(int priority, int matchedConditions, int specificity, Instant updatedAt) {
        DecisionRule rule = new DecisionRule();
        ReflectionTestUtils.setField(rule, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(rule, "updatedAt", updatedAt);
        rule.setPriority(priority);
        rule.setName("rule-" + priority + "-" + matchedConditions);
        return new RuleMatchCandidate(rule, matchedConditions, specificity, List.of());
    }
}
