package com.brilarisoft.lamuertapokerintelligence.service.impl;

import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.RuleOperator;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.SizingType;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.ConditionValueType;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.DecisionRule;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleFilterDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleUpsertDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DuplicateDecisionRuleDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.RuleActionDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.RuleConditionDto;
import com.brilarisoft.lamuertapokerintelligence.exception.BusinessValidationException;
import com.brilarisoft.lamuertapokerintelligence.exception.NotFoundException;
import com.brilarisoft.lamuertapokerintelligence.mapper.DecisionRuleMapper;
import com.brilarisoft.lamuertapokerintelligence.repository.DecisionRuleRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.HeroRangeSetRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.StrategyProfileRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.VillainRangeSetRepository;
import com.brilarisoft.lamuertapokerintelligence.service.DecisionRuleService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DecisionRuleServiceImpl implements DecisionRuleService {

    private final DecisionRuleRepository decisionRuleRepository;
    private final StrategyProfileRepository strategyProfileRepository;
    private final HeroRangeSetRepository heroRangeSetRepository;
    private final VillainRangeSetRepository villainRangeSetRepository;
    private final DecisionRuleMapper decisionRuleMapper;

    public DecisionRuleServiceImpl(
            DecisionRuleRepository decisionRuleRepository,
            StrategyProfileRepository strategyProfileRepository,
            HeroRangeSetRepository heroRangeSetRepository,
            VillainRangeSetRepository villainRangeSetRepository,
            DecisionRuleMapper decisionRuleMapper
    ) {
        this.decisionRuleRepository = decisionRuleRepository;
        this.strategyProfileRepository = strategyProfileRepository;
        this.heroRangeSetRepository = heroRangeSetRepository;
        this.villainRangeSetRepository = villainRangeSetRepository;
        this.decisionRuleMapper = decisionRuleMapper;
    }

    @Override
    public DecisionRuleDetailDto createRule(DecisionRuleUpsertDto request) {
        DecisionRule rule = new DecisionRule();
        applyRequest(rule, request);
        return decisionRuleMapper.toDetailDto(decisionRuleRepository.save(rule));
    }

    @Override
    public DecisionRuleDetailDto updateRule(UUID ruleId, DecisionRuleUpsertDto request) {
        DecisionRule rule = getRequiredRule(ruleId);
        applyRequest(rule, request);
        return decisionRuleMapper.toDetailDto(decisionRuleRepository.save(rule));
    }

    @Override
    public void deleteRule(UUID ruleId) {
        decisionRuleRepository.delete(getRequiredRule(ruleId));
    }

    @Override
    @Transactional(readOnly = true)
    public DecisionRuleDetailDto getRule(UUID ruleId) {
        return decisionRuleMapper.toDetailDto(getRequiredRule(ruleId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DecisionRuleSummaryDto> listRules(DecisionRuleFilterDto filter) {
        return decisionRuleRepository.findAll(buildSpecification(filter)).stream()
                .map(decisionRuleMapper::toSummaryDto)
                .toList();
    }

    @Override
    public DecisionRuleDetailDto duplicateRule(UUID ruleId, DuplicateDecisionRuleDto request) {
        DecisionRule source = getRequiredRule(ruleId);
        DecisionRule copy = new DecisionRule();
        copy.setStrategyProfile(source.getStrategyProfile());
        copy.setName(request.name() == null || request.name().isBlank() ? source.getName() + " (copy)" : request.name().trim());
        copy.setDescription(source.getDescription());
        copy.setGameType(source.getGameType());
        copy.setStreet(source.getStreet());
        copy.setScenarioType(source.getScenarioType());
        copy.setHeroPosition(source.getHeroPosition());
        copy.setVillainPosition(source.getVillainPosition());
        copy.setHeroHandCode(source.getHeroHandCode());
        copy.setHeroStrategyLegend(source.getHeroStrategyLegend());
        copy.setHeroRangeSet(source.getHeroRangeSet());
        copy.setVillainRangeSet(source.getVillainRangeSet());
        copy.setFacingActionType(source.getFacingActionType());
        copy.setLineSignature(source.getLineSignature());
        copy.setSizingConditionCode(source.getSizingConditionCode());
        copy.setActive(source.isActive());
        copy.setStopOnMatch(source.isStopOnMatch());
        copy.setPriority(source.getPriority());
        copy.setNote(source.getNote());
        copy.setConditions(source.getConditions().stream().map(condition -> decisionRuleMapper.toConditionEntity(
                new RuleConditionDto(
                        condition.getConditionType(),
                        condition.getOperator(),
                        condition.getValueType(),
                        condition.getExpectedValue(),
                        condition.getSecondaryValue(),
                        condition.getConditionOrder()
                ),
                copy
        )).toList());
        copy.setActions(source.getActions().stream().map(action -> decisionRuleMapper.toActionEntity(
                new RuleActionDto(
                        action.getActionType(),
                        action.getSizingType(),
                        action.getSizingValue(),
                        action.getExecutionOrder(),
                        action.getMessageTemplate()
                ),
                copy
        )).toList());
        return decisionRuleMapper.toDetailDto(decisionRuleRepository.save(copy));
    }

    private void applyRequest(DecisionRule rule, DecisionRuleUpsertDto request) {
        validateRequest(request);
        var profile = strategyProfileRepository.findById(request.profileId())
                .orElseThrow(() -> new NotFoundException("Strategy profile not found: " + request.profileId()));
        HeroRangeSet heroRangeSet = resolveHeroRange(request.heroRangeSetId());
        VillainRangeSet villainRangeSet = resolveVillainRange(request.villainRangeSetId());
        rule.setStrategyProfile(profile);
        decisionRuleMapper.updateEntity(rule, request, heroRangeSet, villainRangeSet);
        rule.getConditions().clear();
        rule.getConditions().addAll(request.conditions() == null ? List.of() : request.conditions().stream()
                .map(condition -> decisionRuleMapper.toConditionEntity(condition, rule))
                .toList());
        rule.getActions().clear();
        rule.getActions().addAll(request.actions().stream()
                .map(action -> decisionRuleMapper.toActionEntity(action, rule))
                .toList());
    }

    private void validateRequest(DecisionRuleUpsertDto request) {
        validateConditionOrders(request.conditions());
        validateActionOrders(request.actions());
        if (request.conditions() != null) {
            request.conditions().forEach(this::validateCondition);
        }
        request.actions().forEach(this::validateAction);
    }

    private void validateConditionOrders(List<RuleConditionDto> conditions) {
        if (conditions == null) {
            return;
        }
        Set<Integer> seen = new HashSet<>();
        for (RuleConditionDto condition : conditions) {
            if (!seen.add(condition.conditionOrder())) {
                throw new BusinessValidationException("Condition order must be unique within a rule");
            }
        }
    }

    private void validateActionOrders(List<RuleActionDto> actions) {
        Set<Integer> seen = new HashSet<>();
        for (RuleActionDto action : actions) {
            if (!seen.add(action.executionOrder())) {
                throw new BusinessValidationException("Action execution order must be unique within a rule");
            }
        }
    }

    private void validateCondition(RuleConditionDto condition) {
        switch (condition.valueType()) {
            case BOOLEAN -> {
                if (condition.operator() != RuleOperator.EQUALS) {
                    throw new BusinessValidationException("Boolean conditions only support EQUALS");
                }
                if (!"true".equalsIgnoreCase(condition.expectedValue()) && !"false".equalsIgnoreCase(condition.expectedValue())) {
                    throw new BusinessValidationException("Boolean expectedValue must be true or false");
                }
            }
            case ENUM, STRING -> {
                if (!(condition.operator() == RuleOperator.EQUALS
                        || condition.operator() == RuleOperator.IN
                        || condition.operator() == RuleOperator.CONTAINS)) {
                    throw new BusinessValidationException("Enum/string conditions support EQUALS, IN and CONTAINS");
                }
            }
            case NUMBER -> {
                if (!(condition.operator() == RuleOperator.EQUALS
                        || condition.operator() == RuleOperator.GREATER_THAN
                        || condition.operator() == RuleOperator.GREATER_OR_EQUAL
                        || condition.operator() == RuleOperator.LESS_THAN
                        || condition.operator() == RuleOperator.LESS_OR_EQUAL
                        || condition.operator() == RuleOperator.BETWEEN)) {
                    throw new BusinessValidationException("Numeric conditions use numeric comparison operators");
                }
                parseNumber(condition.expectedValue());
                if (condition.operator() == RuleOperator.BETWEEN) {
                    if (condition.secondaryValue() == null || condition.secondaryValue().isBlank()) {
                        throw new BusinessValidationException("BETWEEN conditions require a secondary value");
                    }
                    parseNumber(condition.secondaryValue());
                }
            }
            case LIST -> {
                if (condition.operator() != RuleOperator.IN) {
                    throw new BusinessValidationException("List conditions only support IN");
                }
            }
        }
    }

    private void validateAction(RuleActionDto action) {
        if (requiresSizingValue(action.sizingType()) && action.sizingValue() == null) {
            throw new BusinessValidationException("Sizing value is required for " + action.sizingType());
        }
        if (!requiresSizingValue(action.sizingType()) && action.sizingValue() != null) {
            throw new BusinessValidationException("Sizing value must be null for " + action.sizingType());
        }
    }

    private boolean requiresSizingValue(SizingType sizingType) {
        return sizingType == SizingType.BB
                || sizingType == SizingType.POT_PERCENT
                || sizingType == SizingType.ABSOLUTE
                || sizingType == SizingType.MULTIPLIER;
    }

    private void parseNumber(String value) {
        try {
            new java.math.BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new BusinessValidationException("Numeric condition values must be decimal numbers");
        }
    }

    private HeroRangeSet resolveHeroRange(UUID heroRangeSetId) {
        if (heroRangeSetId == null) {
            return null;
        }
        return heroRangeSetRepository.findById(heroRangeSetId)
                .orElseThrow(() -> new NotFoundException("Hero range not found: " + heroRangeSetId));
    }

    private VillainRangeSet resolveVillainRange(UUID villainRangeSetId) {
        if (villainRangeSetId == null) {
            return null;
        }
        return villainRangeSetRepository.findById(villainRangeSetId)
                .orElseThrow(() -> new NotFoundException("Villain range not found: " + villainRangeSetId));
    }

    private DecisionRule getRequiredRule(UUID ruleId) {
        return decisionRuleRepository.findById(ruleId)
                .orElseThrow(() -> new NotFoundException("Decision rule not found: " + ruleId));
    }

    private Specification<DecisionRule> buildSpecification(DecisionRuleFilterDto filter) {
        return Specification.allOf(
                equalsSpec("strategyProfile.id", filter.profileId()),
                equalsSpec("gameType", filter.gameType()),
                equalsSpec("street", filter.street()),
                equalsSpec("scenarioType", filter.scenarioType()),
                equalsSpec("active", filter.enabled()),
                equalsSpec("heroPosition", filter.heroPosition()),
                equalsSpec("villainPosition", filter.villainPosition()),
                equalsSpec("priority", filter.priority())
        );
    }

    private Specification<DecisionRule> equalsSpec(String fieldPath, Object value) {
        if (value == null) {
            return null;
        }
        return (root, query, builder) -> {
            var path = root.get(fieldPath.split("\\.")[0]);
            if (fieldPath.contains(".")) {
                path = path.get(fieldPath.split("\\.")[1]);
            }
            return builder.equal(path, value);
        };
    }
}
