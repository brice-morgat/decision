package com.brilarisoft.lamuertapokerintelligence.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionInput;
import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeCell;
import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeCell;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.DecisionStatus;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.StrategyLegend;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.domain.strategyprofile.StrategyProfile;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionEquityDto;
import com.brilarisoft.lamuertapokerintelligence.dto.decision.DecisionRequestDto;
import com.brilarisoft.lamuertapokerintelligence.mapper.DecisionMapper;
import com.brilarisoft.lamuertapokerintelligence.repository.HeroRangeSetRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.StrategyProfileRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.VillainRangeSetRepository;
import com.brilarisoft.lamuertapokerintelligence.service.StateReconstructionService;
import com.brilarisoft.lamuertapokerintelligence.service.decision.DecisionAnalysisAssembler;
import com.brilarisoft.lamuertapokerintelligence.service.decision.DecisionDiagnosticsService;
import com.brilarisoft.lamuertapokerintelligence.service.decision.HeroRangeDecisionResolver;
import com.brilarisoft.lamuertapokerintelligence.service.decision.VillainRangeAdjustmentService;
import com.brilarisoft.lamuertapokerintelligence.service.equity.EquityAnalysisService;
import com.brilarisoft.lamuertapokerintelligence.service.range.SyntheticVillainRangeFactory;
import com.brilarisoft.lamuertapokerintelligence.service.ruleengine.DecisionInputValidator;
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
    private HeroRangeSetRepository heroRangeSetRepository;
    @Mock
    private VillainRangeSetRepository villainRangeSetRepository;
    @Mock
    private StateReconstructionService stateReconstructionService;
    @Mock
    private EquityAnalysisService equityAnalysisService;
    @Mock
    private DecisionDiagnosticsService decisionDiagnosticsService;

    private DecisionEngineServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DecisionEngineServiceImpl(
                new DecisionMapper(),
                strategyProfileRepository,
                heroRangeSetRepository,
                villainRangeSetRepository,
                new DecisionInputValidator(),
                stateReconstructionService,
                new HeroRangeDecisionResolver(),
                new VillainRangeAdjustmentService(),
                new DecisionAnalysisAssembler(),
                decisionDiagnosticsService,
                equityAnalysisService,
                new SyntheticVillainRangeFactory()
        );

        when(decisionDiagnosticsService.enrich(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(DecisionDiagnosticsService.DecisionDiagnostics.empty());
    }

    @Test
    void returnsInvalidInputWhenHeroCardsCannotBeNormalized() {
        UUID profileId = UUID.randomUUID();
        when(strategyProfileRepository.findById(profileId)).thenReturn(Optional.of(profile()));
        when(heroRangeSetRepository.findById(HERO_RANGE_ID)).thenReturn(Optional.of(heroRangeSet(StrategyLegend.OPEN)));

        var response = service.decide(request(profileId, List.of("As")));

        assertThat(response.status()).isEqualTo(DecisionStatus.INVALID_INPUT);
    }

    @Test
    void returnsFoldWhenHeroHandIsNotActiveInSelectedRange() {
        DecisionContext context = context();
        UUID profileId = UUID.randomUUID();
        when(strategyProfileRepository.findById(profileId)).thenReturn(Optional.of(profile()));
        when(heroRangeSetRepository.findById(HERO_RANGE_ID)).thenReturn(Optional.of(heroRangeSet(StrategyLegend.FOLD)));
        when(stateReconstructionService.reconstruct(org.mockito.ArgumentMatchers.any(DecisionInput.class))).thenReturn(context);

        var response = service.decide(request(profileId, List.of("As", "Ks")));

        assertThat(response.status()).isEqualTo(DecisionStatus.SUCCESS);
        assertThat(response.recommendedAction()).isEqualTo(ActionType.FOLD);
    }

    @Test
    void returnsHeroRangeDecisionBeforeRuleEngineWhenHeroRangeIsUsable() {
        DecisionContext context = context();
        HeroRangeSet heroRangeSet = heroRangeSet(StrategyLegend.OPEN);
        UUID profileId = UUID.randomUUID();

        when(strategyProfileRepository.findById(profileId)).thenReturn(Optional.of(profile()));
        when(heroRangeSetRepository.findById(HERO_RANGE_ID)).thenReturn(Optional.of(heroRangeSet));
        when(stateReconstructionService.reconstruct(org.mockito.ArgumentMatchers.any(DecisionInput.class))).thenReturn(context);

        var response = service.decide(request(profileId, List.of("As", "Ks")));

        assertThat(response.status()).isEqualTo(DecisionStatus.SUCCESS);
        assertThat(response.recommendedAction()).isEqualTo(ActionType.OPEN);
        assertThat(response.equity()).isNull();
    }

    @Test
    void returnsRangeFallbackWhenNoRulesButHeroLegendIsAvailable() {
        DecisionContext context = context();
        HeroRangeSet heroRangeSet = heroRangeSet(StrategyLegend.THREE_BET);
        UUID profileId = UUID.randomUUID();

        when(strategyProfileRepository.findById(profileId)).thenReturn(Optional.of(profile()));
        when(heroRangeSetRepository.findById(HERO_RANGE_ID)).thenReturn(Optional.of(heroRangeSet));
        when(stateReconstructionService.reconstruct(org.mockito.ArgumentMatchers.any(DecisionInput.class))).thenReturn(context);

        var response = service.decide(request(profileId, List.of("As", "Ks")));

        assertThat(response.status()).isEqualTo(DecisionStatus.SUCCESS);
        assertThat(response.recommendedAction()).isEqualTo(ActionType.THREE_BET);
        assertThat(response.explanation()).contains("range hero");
    }

    @Test
    void returnsFoldWhenNoRulesAndHeroLegendIsFold() {
        DecisionContext context = context();
        HeroRangeSet heroRangeSet = heroRangeSet(StrategyLegend.FOLD);
        UUID profileId = UUID.randomUUID();

        when(strategyProfileRepository.findById(profileId)).thenReturn(Optional.of(profile()));
        when(heroRangeSetRepository.findById(HERO_RANGE_ID)).thenReturn(Optional.of(heroRangeSet));
        when(stateReconstructionService.reconstruct(org.mockito.ArgumentMatchers.any(DecisionInput.class))).thenReturn(context);

        var response = service.decide(request(profileId, List.of("As", "Ks")));

        assertThat(response.status()).isEqualTo(DecisionStatus.SUCCESS);
        assertThat(response.recommendedAction()).isEqualTo(ActionType.FOLD);
    }

    @Test
    void returnsHeroRangeDecisionWhenRuleEngineIsBypassed() {
        UUID profileId = UUID.randomUUID();
        HeroRangeSet heroRangeSet = heroRangeSet(StrategyLegend.OPEN);
        when(strategyProfileRepository.findById(profileId)).thenReturn(Optional.of(profile()));
        when(heroRangeSetRepository.findById(HERO_RANGE_ID)).thenReturn(Optional.of(heroRangeSet));
        when(stateReconstructionService.reconstruct(org.mockito.ArgumentMatchers.any(DecisionInput.class))).thenReturn(context());

        var response = service.decide(request(profileId, List.of("As", "Ks")));

        assertThat(response.status()).isEqualTo(DecisionStatus.SUCCESS);
        assertThat(response.recommendedAction()).isEqualTo(ActionType.OPEN);
        assertThat(response.explanation()).contains("range hero");
    }

    @Test
    void adjustsDecisionWhenVillainRangeIsSelected() {
        UUID profileId = UUID.randomUUID();
        UUID villainRangeId = UUID.fromString("30000000-0000-0000-0000-000000000001");
        HeroRangeSet heroRangeSet = heroRangeSet(StrategyLegend.CALL);
        VillainRangeSet villainRangeSet = villainRangeSet(villainRangeId, 6);

        when(strategyProfileRepository.findById(profileId)).thenReturn(Optional.of(profile()));
        when(heroRangeSetRepository.findById(HERO_RANGE_ID)).thenReturn(Optional.of(heroRangeSet));
        when(villainRangeSetRepository.findById(villainRangeId)).thenReturn(Optional.of(villainRangeSet));
        when(stateReconstructionService.reconstruct(org.mockito.ArgumentMatchers.any(DecisionInput.class))).thenReturn(context());
        when(equityAnalysisService.analyze(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new DecisionEquityDto(BigDecimal.valueOf(55), BigDecimal.valueOf(43), BigDecimal.valueOf(2)));

        var response = service.decide(request(profileId, villainRangeId, List.of("As", "Ks")));

        assertThat(response.status()).isEqualTo(DecisionStatus.SUCCESS);
        assertThat(response.recommendedAction()).isEqualTo(ActionType.CALL);
        assertThat(response.explanation()).contains("range vilain");
        assertThat(response.equity()).isNotNull();
    }

    private static final UUID HERO_RANGE_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID PROFILE_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    private StrategyProfile profile() {
        StrategyProfile profile = new StrategyProfile();
        ReflectionTestUtils.setField(profile, "id", PROFILE_ID);
        profile.setName("Cash");
        profile.setDescription("");
        profile.setGameType(GameType.CASH);
        profile.setVersionLabel("v1");
        return profile;
    }

    private HeroRangeSet heroRangeSet(StrategyLegend legend) {
        HeroRangeSet heroRangeSet = new HeroRangeSet();
        ReflectionTestUtils.setField(heroRangeSet, "id", HERO_RANGE_ID);
        heroRangeSet.setStrategyProfile(profile());
        heroRangeSet.setHeroPosition(Position.BTN);
        heroRangeSet.setStreet(Street.FLOP);
        heroRangeSet.setScenarioType(ScenarioType.FLOP_CBET_SPOT);
        HeroRangeCell cell = new HeroRangeCell();
        cell.setHandCode("AKS");
        cell.setEnabled(legend != StrategyLegend.FOLD);
        cell.setStrategyLegend(legend);
        cell.setHeroRangeSet(heroRangeSet);
        heroRangeSet.setCells(List.of(cell));
        return heroRangeSet;
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
        return request(profileId, null, heroCards);
    }

    private DecisionRequestDto request(UUID profileId, UUID villainRangeId, List<String> heroCards) {
        return new DecisionRequestDto(
                profileId,
                HERO_RANGE_ID,
                villainRangeId,
                null,
                null,
                null,
                null,
                null,
                null,
                BigDecimal.valueOf(100),
                BigDecimal.TEN,
                "AKS",
                heroCards,
                List.of(),
                List.of()
        );
    }

    private VillainRangeSet villainRangeSet(UUID villainRangeId, int enabledCellCount) {
        VillainRangeSet villainRangeSet = new VillainRangeSet();
        ReflectionTestUtils.setField(villainRangeSet, "id", villainRangeId);
        villainRangeSet.setStrategyProfile(profile());
        villainRangeSet.setVillainPosition(Position.BB);
        villainRangeSet.setHeroPosition(Position.BTN);
        villainRangeSet.setStreet(Street.FLOP);
        villainRangeSet.setScenarioType(ScenarioType.FLOP_CBET_SPOT);
        villainRangeSet.setCells(java.util.stream.IntStream.range(0, enabledCellCount)
                .mapToObj(index -> villainCell(villainRangeSet, "AA"))
                .toList());
        return villainRangeSet;
    }

    private VillainRangeCell villainCell(VillainRangeSet villainRangeSet, String handCode) {
        VillainRangeCell cell = new VillainRangeCell();
        cell.setVillainRangeSet(villainRangeSet);
        cell.setHandCode(handCode);
        cell.setEnabled(true);
        cell.setWeightPercent(100);
        return cell;
    }
}
