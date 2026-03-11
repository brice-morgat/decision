package com.brilarisoft.lamuertapokerintelligence.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionInput;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionResult;
import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.DecisionStatus;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.domain.strategyprofile.StrategyProfile;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionRequestDto;
import com.brilarisoft.lamuertapokerintelligence.mapper.DecisionMapper;
import com.brilarisoft.lamuertapokerintelligence.repository.DecisionRuleRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.StrategyProfileRepository;
import com.brilarisoft.lamuertapokerintelligence.service.StateReconstructionService;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.DecisionAssembler;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.DecisionInputValidator;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.HeroRangeResolver;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.ResolvedRuleSelection;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.RuleEngineFacade;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.RuleMatchCandidate;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.VillainRangeResolver;
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
class DecisionEngineServiceImplTest {

    @Mock
    private StrategyProfileRepository strategyProfileRepository;
    @Mock
    private DecisionRuleRepository decisionRuleRepository;
    @Mock
    private StateReconstructionService stateReconstructionService;
    @Mock
    private HeroRangeResolver heroRangeResolver;
    @Mock
    private VillainRangeResolver villainRangeResolver;
    @Mock
    private RuleEngineFacade ruleEngineFacade;
    @Mock
    private DecisionAssembler decisionAssembler;

    private DecisionEngineServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DecisionEngineServiceImpl(
                new DecisionMapper(),
                strategyProfileRepository,
                decisionRuleRepository,
                new DecisionInputValidator(),
                stateReconstructionService,
                heroRangeResolver,
                villainRangeResolver,
                ruleEngineFacade,
                decisionAssembler
        );
    }

    @Test
    void returnsInvalidInputWhenHeroCardsCannotBeNormalized() {
        UUID profileId = UUID.randomUUID();
        when(strategyProfileRepository.findById(profileId)).thenReturn(Optional.of(profile()));

        var response = service.decide(request(profileId, List.of("As")));

        assertThat(response.status()).isEqualTo(DecisionStatus.INVALID_INPUT);
    }

    @Test
    void returnsIncompleteConfigurationWhenHeroRangeIsMissing() {
        DecisionContext context = context();
        UUID profileId = UUID.randomUUID();
        when(strategyProfileRepository.findById(profileId)).thenReturn(Optional.of(profile()));
        when(stateReconstructionService.reconstruct(org.mockito.ArgumentMatchers.any(DecisionInput.class))).thenReturn(context);
        when(heroRangeResolver.resolve(context)).thenReturn(Optional.empty());
        when(villainRangeResolver.resolve(context)).thenReturn(Optional.empty());
        when(decisionRuleRepository.findByStrategyProfileIdAndStreetAndScenarioTypeAndActiveTrueOrderByPriorityAsc(
                context.getStrategyProfile().getId(), context.getStreet(), context.getScenarioType()
        )).thenReturn(List.of());

        var response = service.decide(request(profileId, List.of("As", "Ks")));

        assertThat(response.status()).isEqualTo(DecisionStatus.INCOMPLETE_CONFIGURATION);
    }

    @Test
    void returnsSuccessWhenRuleEngineResolvesRule() {
        DecisionContext context = context();
        HeroRangeSet heroRangeSet = new HeroRangeSet();
        DecisionResult success = new DecisionResult();
        success.setStatus(DecisionStatus.SUCCESS);
        success.setRecommendedAction(ActionType.CALL);
        success.setExplanation("ok");
        RuleMatchCandidate candidate = new RuleMatchCandidate(new com.brilarisoft.lamuertapokerintelligence.domain.rule.DecisionRule(), 1, 1, List.of("ok"));
        ResolvedRuleSelection selection = new ResolvedRuleSelection(candidate, List.of(candidate), false);
        UUID profileId = UUID.randomUUID();

        when(strategyProfileRepository.findById(profileId)).thenReturn(Optional.of(profile()));
        when(stateReconstructionService.reconstruct(org.mockito.ArgumentMatchers.any(DecisionInput.class))).thenReturn(context);
        when(heroRangeResolver.resolve(context)).thenReturn(Optional.of(heroRangeSet));
        when(villainRangeResolver.resolve(context)).thenReturn(Optional.empty());
        when(decisionRuleRepository.findByStrategyProfileIdAndStreetAndScenarioTypeAndActiveTrueOrderByPriorityAsc(
                context.getStrategyProfile().getId(), context.getStreet(), context.getScenarioType()
        )).thenReturn(List.of(new com.brilarisoft.lamuertapokerintelligence.domain.rule.DecisionRule()));
        when(ruleEngineFacade.evaluate(context)).thenReturn(selection);
        when(decisionAssembler.assemble(context, selection)).thenReturn(success);
        when(decisionAssembler.toMatchedCandidates(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        var response = service.decide(request(profileId, List.of("As", "Ks")));

        assertThat(response.status()).isEqualTo(DecisionStatus.SUCCESS);
        assertThat(response.recommendedAction()).isEqualTo(ActionType.CALL);
    }

    private StrategyProfile profile() {
        StrategyProfile profile = new StrategyProfile();
        ReflectionTestUtils.setField(profile, "id", UUID.randomUUID());
        profile.setName("Cash");
        profile.setDescription("");
        profile.setGameType(GameType.CASH);
        profile.setVersionLabel("v1");
        return profile;
    }

    private DecisionContext context() {
        DecisionContext context = new DecisionContext();
        context.setStrategyProfile(profile());
        context.setGameType(GameType.CASH);
        context.setHeroPosition(Position.BTN);
        context.setVillainPosition(Position.BB);
        context.setStreet(Street.FLOP);
        context.setScenarioType(ScenarioType.FLOP_CBET_SPOT);
        context.setHeroHandCode("AKS");
        context.setPotSizeInBigBlinds(BigDecimal.TEN);
        context.setEffectiveStackInBigBlinds(BigDecimal.valueOf(100));
        return context;
    }

    private DecisionRequestDto request(UUID profileId, List<String> heroCards) {
        return new DecisionRequestDto(
                profileId,
                Position.BTN,
                Position.BB,
                Street.FLOP,
                ScenarioType.FLOP_CBET_SPOT,
                BigDecimal.valueOf(100),
                BigDecimal.TEN,
                heroCards,
                List.of(),
                List.of()
        );
    }
}
