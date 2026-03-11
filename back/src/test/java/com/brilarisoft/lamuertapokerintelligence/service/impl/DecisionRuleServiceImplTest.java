package com.brilarisoft.lamuertapokerintelligence.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.RuleOperator;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.SizingType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.ConditionValueType;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.DecisionRule;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.RuleConditionType;
import com.brilarisoft.lamuertapokerintelligence.domain.strategyprofile.StrategyProfile;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleUpsertDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DuplicateDecisionRuleDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.RuleActionDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.RuleConditionDto;
import com.brilarisoft.lamuertapokerintelligence.exception.BusinessValidationException;
import com.brilarisoft.lamuertapokerintelligence.mapper.DecisionRuleMapper;
import com.brilarisoft.lamuertapokerintelligence.repository.DecisionRuleRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.HeroRangeSetRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.StrategyProfileRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.VillainRangeSetRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DecisionRuleServiceImplTest {

    @Mock
    private DecisionRuleRepository decisionRuleRepository;

    @Mock
    private StrategyProfileRepository strategyProfileRepository;

    @Mock
    private HeroRangeSetRepository heroRangeSetRepository;

    @Mock
    private VillainRangeSetRepository villainRangeSetRepository;

    private DecisionRuleServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DecisionRuleServiceImpl(
                decisionRuleRepository,
                strategyProfileRepository,
                heroRangeSetRepository,
                villainRangeSetRepository,
                new DecisionRuleMapper()
        );
    }

    @Test
    void createRuleRejectsDuplicateConditionOrder() {
        assertThatThrownBy(() -> service.createRule(request(List.of(
                condition(0, RuleConditionType.LINE_SIGNATURE, ConditionValueType.STRING, RuleOperator.EQUALS, "X"),
                condition(0, RuleConditionType.HERO_WAS_PREFLOP_AGGRESSOR, ConditionValueType.BOOLEAN, RuleOperator.EQUALS, "true")
        )))).isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void createRuleRejectsMissingSizingValue() {
        assertThatThrownBy(() -> service.createRule(request(
                List.of(condition(0, RuleConditionType.HERO_WAS_PREFLOP_AGGRESSOR, ConditionValueType.BOOLEAN, RuleOperator.EQUALS, "true")),
                List.of(new RuleActionDto(ActionType.BET, SizingType.POT_PERCENT, null, 0, null))
        ))).isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void createRulePersistsConditionsAndActions() {
        UUID profileId = UUID.randomUUID();
        when(strategyProfileRepository.findById(profileId)).thenReturn(Optional.of(profile(profileId)));
        when(decisionRuleRepository.save(any(DecisionRule.class))).thenAnswer(invocation -> {
            DecisionRule rule = invocation.getArgument(0);
            ReflectionTestUtils.setField(rule, "id", UUID.randomUUID());
            return rule;
        });

        var result = service.createRule(request(profileId));

        assertThat(result.name()).isEqualTo("Flop c-bet");
        assertThat(result.conditions()).hasSize(1);
        assertThat(result.actions()).hasSize(1);
    }

    @Test
    void duplicateRuleCopiesConditionsAndActions() {
        DecisionRule source = new DecisionRule();
        ReflectionTestUtils.setField(source, "id", UUID.randomUUID());
        source.setStrategyProfile(profile(UUID.randomUUID()));
        source.setName("Base rule");
        source.setDescription("");
        source.setGameType(GameType.CASH);
        source.setStreet(Street.FLOP);
        source.setScenarioType(ScenarioType.FLOP_CBET_SPOT);
        source.setPriority(10);
        source.setActive(true);
        source.setConditions(List.of(new DecisionRuleMapper().toConditionEntity(
                condition(0, RuleConditionType.LINE_SIGNATURE, ConditionValueType.STRING, RuleOperator.EQUALS, "VILLAIN_CHECK__HERO_BET"),
                source
        )));
        source.setActions(List.of(new DecisionRuleMapper().toActionEntity(
                new RuleActionDto(ActionType.BET, SizingType.POT_PERCENT, BigDecimal.valueOf(50), 0, "Bet flop"),
                source
        )));
        when(decisionRuleRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(decisionRuleRepository.save(any(DecisionRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.duplicateRule(source.getId(), new DuplicateDecisionRuleDto(null));

        assertThat(result.name()).isEqualTo("Base rule (copy)");
        assertThat(result.conditions()).hasSize(1);
        assertThat(result.actions()).hasSize(1);
    }

    private DecisionRuleUpsertDto request(UUID profileId) {
        return request(profileId, List.of(condition(0, RuleConditionType.HERO_WAS_PREFLOP_AGGRESSOR, ConditionValueType.BOOLEAN, RuleOperator.EQUALS, "true")),
                List.of(new RuleActionDto(ActionType.BET, SizingType.POT_PERCENT, BigDecimal.valueOf(50), 0, "Bet flop")));
    }

    private DecisionRuleUpsertDto request(List<RuleConditionDto> conditions) {
        return request(UUID.randomUUID(), conditions, List.of(new RuleActionDto(ActionType.CALL, SizingType.CATEGORY, null, 0, null)));
    }

    private DecisionRuleUpsertDto request(List<RuleConditionDto> conditions, List<RuleActionDto> actions) {
        return request(UUID.randomUUID(), conditions, actions);
    }

    private DecisionRuleUpsertDto request(UUID profileId, List<RuleConditionDto> conditions, List<RuleActionDto> actions) {
        return new DecisionRuleUpsertDto(
                profileId,
                "Flop c-bet",
                "",
                GameType.CASH,
                Street.FLOP,
                ScenarioType.FLOP_CBET_SPOT,
                Position.BTN,
                Position.BB,
                null,
                null,
                null,
                null,
                null,
                "VILLAIN_CHECK__HERO_BET",
                null,
                true,
                false,
                10,
                null,
                conditions,
                actions
        );
    }

    private RuleConditionDto condition(int order, RuleConditionType type, ConditionValueType valueType, RuleOperator operator, String expectedValue) {
        return new RuleConditionDto(type, operator, valueType, expectedValue, null, order);
    }

    private StrategyProfile profile(UUID id) {
        StrategyProfile profile = new StrategyProfile();
        ReflectionTestUtils.setField(profile, "id", id);
        profile.setName("Cash");
        profile.setDescription("");
        profile.setGameType(GameType.CASH);
        profile.setVersionLabel("v1");
        return profile;
    }
}
