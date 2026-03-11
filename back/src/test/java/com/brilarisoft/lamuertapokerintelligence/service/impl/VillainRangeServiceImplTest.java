package com.brilarisoft.lamuertapokerintelligence.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeCellDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeCellsUpdateDto;
import com.brilarisoft.lamuertapokerintelligence.exception.BusinessValidationException;
import com.brilarisoft.lamuertapokerintelligence.mapper.VillainRangeMapper;
import com.brilarisoft.lamuertapokerintelligence.repository.StrategyProfileRepository;
import com.brilarisoft.lamuertapokerintelligence.repository.VillainRangeSetRepository;
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
class VillainRangeServiceImplTest {

    @Mock
    private VillainRangeSetRepository villainRangeSetRepository;

    @Mock
    private StrategyProfileRepository strategyProfileRepository;

    private VillainRangeServiceImpl villainRangeService;

    @BeforeEach
    void setUp() {
        villainRangeService = new VillainRangeServiceImpl(
                villainRangeSetRepository,
                strategyProfileRepository,
                new VillainRangeMapper()
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
}
