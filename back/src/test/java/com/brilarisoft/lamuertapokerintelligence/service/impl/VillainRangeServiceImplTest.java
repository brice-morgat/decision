package com.brilarisoft.lamuertapokerintelligence.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeCellDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeCellsUpdateDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeContextQueryDto;
import com.brilarisoft.lamuertapokerintelligence.exception.BusinessValidationException;
import com.brilarisoft.lamuertapokerintelligence.mapper.VillainRangeMapper;
import com.brilarisoft.lamuertapokerintelligence.repository.StrategyProfileRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.VillainRangeSetRepository;
import com.brilarisoft.lamuertapokerintelligence.service.range.VillainRangeContextResolver;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.brilarisoft.lamuertapokerintelligence.domain.strategyprofile.StrategyProfile;

@ExtendWith(MockitoExtension.class)
class VillainRangeServiceImplTest {

    @Mock
    private VillainRangeSetRepository villainRangeSetRepository;

    @Mock
    private StrategyProfileRepository strategyProfileRepository;

    @Mock
    private VillainRangeContextResolver villainRangeContextResolver;

    private VillainRangeServiceImpl villainRangeService;

    @BeforeEach
    void setUp() {
        villainRangeService = new VillainRangeServiceImpl(
                villainRangeSetRepository,
                strategyProfileRepository,
                new VillainRangeMapper(),
                villainRangeContextResolver
        );
    }

    @Test
    void updateCellsRejectsInvalidHandCode() {
        VillainRangeSet rangeSet = new VillainRangeSet();
        ReflectionTestUtils.setField(rangeSet, "id", UUID.randomUUID());
        when(villainRangeSetRepository.findById(rangeSet.getId())).thenReturn(Optional.of(rangeSet));

        assertThatThrownBy(() -> villainRangeService.updateCells(rangeSet.getId(), new VillainRangeCellsUpdateDto(List.of(
                new VillainRangeCellDto("AHKD", true, 100, null, null)
        )))).isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void getRangeByContextAcceptsMissingScenarioType() {
        VillainRangeSet rangeSet = new VillainRangeSet();
        ReflectionTestUtils.setField(rangeSet, "id", UUID.randomUUID());
        rangeSet.setStrategyProfile(profile());
        rangeSet.setName("BB defend");
        rangeSet.setDescription("");
        rangeSet.setGameType(GameType.CASH);
        rangeSet.setStreet(Street.PREFLOP);
        rangeSet.setVillainPosition(Position.BTN);

        when(villainRangeContextResolver.resolve(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(GameType.CASH),
                org.mockito.ArgumentMatchers.eq(Street.PREFLOP),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(Position.BTN),
                org.mockito.ArgumentMatchers.eq(Position.BB),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull()
        )).thenReturn(Optional.of(rangeSet));

        var result = villainRangeService.getRangeByContext(new VillainRangeContextQueryDto(
                UUID.randomUUID(),
                GameType.CASH,
                Street.PREFLOP,
                Position.BTN,
                Position.BB,
                null,
                null,
                null
        ));

        org.assertj.core.api.Assertions.assertThat(result.name()).isEqualTo("BB defend");
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
}
