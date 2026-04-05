package com.brilarisoft.lamuertapokerintelligence.service.range;

import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeScope;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.repository.VillainRangeSetRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class VillainRangeContextResolver {

    private final VillainRangeSetRepository villainRangeSetRepository;

    public VillainRangeContextResolver(VillainRangeSetRepository villainRangeSetRepository) {
        this.villainRangeSetRepository = villainRangeSetRepository;
    }

    public Optional<VillainRangeSet> resolve(
            UUID profileId,
            GameType gameType,
            Street street,
            ScenarioType scenarioType,
            Position villainPosition,
            Position heroPosition,
            ActionType triggerActionType,
            String lineSignature
    ) {
        return orderedCandidates(profileId, gameType, street, scenarioType, villainPosition, heroPosition, triggerActionType, lineSignature)
                .stream()
                .findFirst();
    }

    public Optional<VillainRangeSet> resolveByTargetWidth(
            UUID profileId,
            GameType gameType,
            Street street,
            ScenarioType scenarioType,
            Position villainPosition,
            Position heroPosition,
            ActionType triggerActionType,
            String lineSignature,
            double targetRangePercent
    ) {
        return orderedCandidates(profileId, gameType, street, scenarioType, villainPosition, heroPosition, triggerActionType, lineSignature)
                .stream()
                .min(Comparator
                        .comparingDouble((VillainRangeSet rangeSet) -> Math.abs(computeEnabledPercent(rangeSet) - targetRangePercent))
                        .thenComparing(VillainRangeSet::getPriority, Comparator.naturalOrder())
                        .thenComparing(rangeSet -> safeInstant(rangeSet.getUpdatedAt()), Comparator.reverseOrder())
                        .thenComparing(VillainRangeSet::getId));
    }

    private List<VillainRangeSet> orderedCandidates(
            UUID profileId,
            GameType gameType,
            Street street,
            ScenarioType scenarioType,
            Position villainPosition,
            Position heroPosition,
            ActionType triggerActionType,
            String lineSignature
    ) {
        List<VillainRangeSet> candidates = villainRangeSetRepository.findCandidatesWithScopes(
                profileId,
                gameType,
                street,
                scenarioType
        );
        if (scenarioType == null) {
            candidates = villainRangeSetRepository.findCandidatesWithScopesWithoutScenario(
                    profileId,
                    gameType,
                    street
            );
        }

        List<VillainRangeSet> strict = candidates.stream()
                .map(rangeSet -> scoreRange(rangeSet, villainPosition, heroPosition, triggerActionType, normalize(lineSignature), false, false))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator
                        .comparingInt(ScoredVillainRange::score).reversed()
                        .thenComparing(scored -> scored.rangeSet().getPriority(), Comparator.naturalOrder())
                        .thenComparing(scored -> safeInstant(scored.rangeSet().getUpdatedAt()), Comparator.reverseOrder())
                        .thenComparing(scored -> scored.rangeSet().getId()))
                .map(ScoredVillainRange::rangeSet)
                .toList();
        if (!strict.isEmpty()) {
            return strict;
        }

        List<VillainRangeSet> ignoreTrigger = candidates.stream()
                        .map(rangeSet -> scoreRange(rangeSet, villainPosition, heroPosition, triggerActionType, normalize(lineSignature), true, false))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .sorted(Comparator
                                .comparingInt(ScoredVillainRange::score).reversed()
                                .thenComparing(scored -> scored.rangeSet().getPriority(), Comparator.naturalOrder())
                                .thenComparing(scored -> safeInstant(scored.rangeSet().getUpdatedAt()), Comparator.reverseOrder())
                                .thenComparing(scored -> scored.rangeSet().getId()))
                        .map(ScoredVillainRange::rangeSet)
                        .toList();
        if (!ignoreTrigger.isEmpty()) {
            return ignoreTrigger;
        }

        return candidates.stream()
                        .map(rangeSet -> scoreRange(rangeSet, villainPosition, heroPosition, triggerActionType, normalize(lineSignature), true, true))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .sorted(Comparator
                                .comparingInt(ScoredVillainRange::score).reversed()
                                .thenComparing(scored -> scored.rangeSet().getPriority(), Comparator.naturalOrder())
                                .thenComparing(scored -> safeInstant(scored.rangeSet().getUpdatedAt()), Comparator.reverseOrder())
                                .thenComparing(scored -> scored.rangeSet().getId()))
                        .map(ScoredVillainRange::rangeSet)
                        .toList();
    }

    private Optional<ScoredVillainRange> scoreRange(
            VillainRangeSet rangeSet,
            Position villainPosition,
            Position heroPosition,
            ActionType triggerActionType,
            String lineSignature,
            boolean ignoreTriggerAction,
            boolean ignoreLineSignature
    ) {
        if (rangeSet.getScopes() == null || rangeSet.getScopes().isEmpty()) {
            if (!legacyMatches(rangeSet, villainPosition, heroPosition, triggerActionType, lineSignature, ignoreTriggerAction, ignoreLineSignature)) {
                return Optional.empty();
            }
            return Optional.of(new ScoredVillainRange(rangeSet, legacyScore(rangeSet, villainPosition, heroPosition, triggerActionType, lineSignature, ignoreTriggerAction, ignoreLineSignature)));
        }

        return rangeSet.getScopes().stream()
                .filter(scope -> scopeMatches(scope, villainPosition, heroPosition, triggerActionType, lineSignature, ignoreTriggerAction, ignoreLineSignature))
                .map(scope -> new ScoredVillainRange(rangeSet, scoreScope(scope, villainPosition, heroPosition, triggerActionType, lineSignature, ignoreTriggerAction, ignoreLineSignature)))
                .max(Comparator.comparingInt(ScoredVillainRange::score));
    }

    private int legacyScore(
            VillainRangeSet rangeSet,
            Position villainPosition,
            Position heroPosition,
            ActionType triggerActionType,
            String lineSignature,
            boolean ignoreTriggerAction,
            boolean ignoreLineSignature
    ) {
        int score = 0;
        if (rangeSet.getVillainPosition() == villainPosition) {
            score += 8;
        }
        if (rangeSet.getHeroPosition() != null && rangeSet.getHeroPosition() == heroPosition) {
            score += 8;
        }
        if (!ignoreTriggerAction && rangeSet.getTriggerActionType() != null && rangeSet.getTriggerActionType() == triggerActionType) {
            score += 6;
        }
        if (!ignoreLineSignature && normalize(rangeSet.getLineSignature()) != null && normalize(rangeSet.getLineSignature()).equals(lineSignature)) {
            score += 4;
        }
        if (ignoreTriggerAction) {
            score -= 1;
        }
        if (ignoreLineSignature) {
            score -= 1;
        }
        return score;
    }

    private int scoreScope(
            VillainRangeScope scope,
            Position villainPosition,
            Position heroPosition,
            ActionType triggerActionType,
            String lineSignature,
            boolean ignoreTriggerAction,
            boolean ignoreLineSignature
    ) {
        int score = 0;
        if (scope.getVillainPosition() != null && scope.getVillainPosition() == villainPosition) {
            score += 8;
        }
        if (scope.getHeroPosition() != null && scope.getHeroPosition() == heroPosition) {
            score += 8;
        }
        if (!ignoreTriggerAction && scope.getTriggerActionType() != null && scope.getTriggerActionType() == triggerActionType) {
            score += 6;
        }
        if (!ignoreLineSignature && normalize(scope.getLineSignature()) != null && normalize(scope.getLineSignature()).equals(lineSignature)) {
            score += 4;
        }
        score += scope.getScopeWeight() == null ? 0 : scope.getScopeWeight();
        if (ignoreTriggerAction) {
            score -= 1;
        }
        if (ignoreLineSignature) {
            score -= 1;
        }
        return score;
    }

    private boolean legacyMatches(
            VillainRangeSet rangeSet,
            Position villainPosition,
            Position heroPosition,
            ActionType triggerActionType,
            String lineSignature,
            boolean ignoreTriggerAction,
            boolean ignoreLineSignature
    ) {
        if (rangeSet.getVillainPosition() != villainPosition) {
            return false;
        }
        if (rangeSet.getHeroPosition() != null && rangeSet.getHeroPosition() != heroPosition) {
            return false;
        }
        if (!ignoreTriggerAction && rangeSet.getTriggerActionType() != null && rangeSet.getTriggerActionType() != triggerActionType) {
            return false;
        }
        return ignoreLineSignature || normalize(rangeSet.getLineSignature()) == null || normalize(rangeSet.getLineSignature()).equals(lineSignature);
    }

    private boolean scopeMatches(
            VillainRangeScope scope,
            Position villainPosition,
            Position heroPosition,
            ActionType triggerActionType,
            String lineSignature,
            boolean ignoreTriggerAction,
            boolean ignoreLineSignature
    ) {
        if (scope.getVillainPosition() != null && scope.getVillainPosition() != villainPosition) {
            return false;
        }
        if (scope.getHeroPosition() != null && scope.getHeroPosition() != heroPosition) {
            return false;
        }
        if (!ignoreTriggerAction && scope.getTriggerActionType() != null && scope.getTriggerActionType() != triggerActionType) {
            return false;
        }
        return ignoreLineSignature || normalize(scope.getLineSignature()) == null || normalize(scope.getLineSignature()).equals(lineSignature);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Instant safeInstant(Instant value) {
        return value == null ? Instant.EPOCH : value;
    }

    private double computeEnabledPercent(VillainRangeSet rangeSet) {
        if (rangeSet.getCells() == null || rangeSet.getCells().isEmpty()) {
            return 0d;
        }
        long enabled = rangeSet.getCells().stream()
                .filter(cell -> cell != null && cell.isEnabled())
                .count();
        return (enabled * 100d) / 169d;
    }

    private record ScoredVillainRange(VillainRangeSet rangeSet, int score) {
    }
}
