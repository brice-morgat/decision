package com.brilarisoft.lamuertapokerintelligence.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.StrategyLegend;
import com.brilarisoft.lamuertapokerintelligence.domain.strategyprofile.StrategyProfile;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeCellDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeCellsUpdateDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeContextQueryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeUpsertDto;
import com.brilarisoft.lamuertapokerintelligence.exception.BusinessValidationException;
import com.brilarisoft.lamuertapokerintelligence.mapper.HeroRangeMapper;
import com.brilarisoft.lamuertapokerintelligence.repository.HeroRangeSetRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.StrategyProfileRepository;
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
class HeroRangeServiceImplTest {

    @Mock
    private HeroRangeSetRepository heroRangeSetRepository;

    @Mock
    private StrategyProfileRepository strategyProfileRepository;

    private HeroRangeServiceImpl heroRangeService;

    @BeforeEach
    void setUp() {
        heroRangeService = new HeroRangeServiceImpl(
                heroRangeSetRepository,
                strategyProfileRepository,
                new HeroRangeMapper()
        );
    }

    @Test
    void createRangeRejectsContextCollision() {
        UUID profileId = UUID.randomUUID();
        when(heroRangeSetRepository.findByStrategyProfileIdAndGameTypeAndStreetAndHeroPositionAndScenarioTypeAndSubScenarioCode(
                profileId, GameType.CASH, Street.PREFLOP, Position.BTN, ScenarioType.OPEN_FIRST_IN, null
        )).thenReturn(Optional.of(new HeroRangeSet()));
        when(strategyProfileRepository.findById(profileId)).thenReturn(Optional.of(profile(profileId)));

        assertThatThrownBy(() -> heroRangeService.createRange(new HeroRangeUpsertDto(
                profileId, "BTN open", "", GameType.CASH, Street.PREFLOP, Position.BTN,
                ScenarioType.OPEN_FIRST_IN, null, true, 0, null
        ))).isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void updateCellsRejectsDuplicateHandCodes() {
        HeroRangeSet rangeSet = new HeroRangeSet();
        ReflectionTestUtils.setField(rangeSet, "id", UUID.randomUUID());
        when(heroRangeSetRepository.findById(rangeSet.getId())).thenReturn(Optional.of(rangeSet));

        assertThatThrownBy(() -> heroRangeService.updateCells(rangeSet.getId(), new HeroRangeCellsUpdateDto(List.of(
                new HeroRangeCellDto("AKs", true, StrategyLegend.OPEN, "#fff", null),
                new HeroRangeCellDto("aks", true, StrategyLegend.CALL, "#000", null)
        )))).isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void getRangeByContextReturnsMappedRange() {
        UUID profileId = UUID.randomUUID();
        HeroRangeSet rangeSet = new HeroRangeSet();
        rangeSet.setStrategyProfile(profile(profileId));
        rangeSet.setName("BTN open");
        rangeSet.setDescription("");
        rangeSet.setGameType(GameType.CASH);
        rangeSet.setStreet(Street.PREFLOP);
        rangeSet.setHeroPosition(Position.BTN);
        rangeSet.setScenarioType(ScenarioType.OPEN_FIRST_IN);

        when(heroRangeSetRepository.findByStrategyProfileIdAndGameTypeAndStreetAndHeroPositionAndScenarioTypeAndSubScenarioCode(
                profileId, GameType.CASH, Street.PREFLOP, Position.BTN, ScenarioType.OPEN_FIRST_IN, null
        )).thenReturn(Optional.of(rangeSet));

        assertThat(heroRangeService.getRangeByContext(new HeroRangeContextQueryDto(
                profileId, GameType.CASH, Street.PREFLOP, Position.BTN, ScenarioType.OPEN_FIRST_IN, null
        )).name()).isEqualTo("BTN open");
    }

    @Test
    void updateCellsReplacesAllCellsTransactionally() {
        HeroRangeSet rangeSet = new HeroRangeSet();
        rangeSet.setStrategyProfile(profile(UUID.randomUUID()));
        ReflectionTestUtils.setField(rangeSet, "id", UUID.randomUUID());
        when(heroRangeSetRepository.findById(rangeSet.getId())).thenReturn(Optional.of(rangeSet));
        when(heroRangeSetRepository.save(any(HeroRangeSet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        heroRangeService.updateCells(rangeSet.getId(), new HeroRangeCellsUpdateDto(List.of(
                new HeroRangeCellDto("AKS", true, StrategyLegend.OPEN, "#22AA99", "open"),
                new HeroRangeCellDto("QQ", true, StrategyLegend.OPEN, "#22AA99", null)
        )));

        assertThat(rangeSet.getCells()).hasSize(2);
        assertThat(rangeSet.getCells().get(0).getHandCode()).isEqualTo("AKS");
        verify(heroRangeSetRepository).save(rangeSet);
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
