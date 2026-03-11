package com.brilarisoft.lamuertapokerintelligence.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeCell;
import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.domain.strategyprofile.StrategyProfile;
import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.DuplicateStrategyProfileDto;
import com.brilarisoft.lamuertapokerintelligence.dto.strategyprofile.StrategyProfileUpsertDto;
import com.brilarisoft.lamuertapokerintelligence.exception.BusinessValidationException;
import com.brilarisoft.lamuertapokerintelligence.mapper.StrategyProfileMapper;
import com.brilarisoft.lamuertapokerintelligence.repository.StrategyProfileRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StrategicProfileServiceImplTest {

    @Mock
    private StrategyProfileRepository strategyProfileRepository;

    private StrategicProfileServiceImpl strategicProfileService;

    @BeforeEach
    void setUp() {
        strategicProfileService = new StrategicProfileServiceImpl(
                strategyProfileRepository,
                new StrategyProfileMapper()
        );
    }

    @Test
    void createProfileDeactivatesOtherActiveProfilesForSameGameType() {
        StrategyProfile existing = profile("Existing", GameType.CASH, true);
        when(strategyProfileRepository.findByNameIgnoreCase("New profile")).thenReturn(Optional.empty());
        when(strategyProfileRepository.findAllByGameTypeAndActiveTrueAndArchivedFalse(GameType.CASH))
                .thenReturn(List.of(existing));
        when(strategyProfileRepository.save(any(StrategyProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        strategicProfileService.createProfile(new StrategyProfileUpsertDto(
                "New profile",
                "Description",
                GameType.CASH,
                true,
                false,
                "v1"
        ));

        assertThat(existing.isActive()).isFalse();
        verify(strategyProfileRepository).save(any(StrategyProfile.class));
    }

    @Test
    void updateProfileRejectsArchivedActiveCombination() {
        assertThatThrownBy(() -> strategicProfileService.updateProfile(
                UUID.randomUUID(),
                new StrategyProfileUpsertDto("Profile", "", GameType.CASH, true, true, "v1")
        )).isInstanceOf(BusinessValidationException.class);

        verify(strategyProfileRepository, never()).findById(any());
    }

    @Test
    void activateProfileDeactivatesOtherProfiles() {
        StrategyProfile target = profile("Target", GameType.CASH, false);
        StrategyProfile other = profile("Other", GameType.CASH, true);
        UUID targetId = target.getId();

        when(strategyProfileRepository.findById(targetId)).thenReturn(Optional.of(target));
        when(strategyProfileRepository.findAllByGameTypeAndActiveTrueAndArchivedFalse(GameType.CASH))
                .thenReturn(List.of(other));
        when(strategyProfileRepository.save(any(StrategyProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        strategicProfileService.activateProfile(targetId);

        assertThat(target.isActive()).isTrue();
        assertThat(other.isActive()).isFalse();
    }

    @Test
    void duplicateProfileCopiesNestedRanges() {
        StrategyProfile source = profile("Source", GameType.CASH, true);
        HeroRangeSet rangeSet = new HeroRangeSet();
        rangeSet.setStrategyProfile(source);
        rangeSet.setName("BTN open");
        rangeSet.setDescription("desc");
        rangeSet.setGameType(GameType.CASH);
        rangeSet.setStreet(Street.PREFLOP);
        rangeSet.setScenarioType(ScenarioType.OPEN_FIRST_IN);
        HeroRangeCell cell = new HeroRangeCell();
        cell.setHeroRangeSet(rangeSet);
        cell.setHandCode("AJo");
        rangeSet.setCells(List.of(cell));
        source.setHeroRangeSets(List.of(rangeSet));

        when(strategyProfileRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(strategyProfileRepository.findByNameIgnoreCase("Source copy")).thenReturn(Optional.empty());
        when(strategyProfileRepository.save(any(StrategyProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        strategicProfileService.duplicateProfile(source.getId(), new DuplicateStrategyProfileDto("Source copy", false));

        ArgumentCaptor<StrategyProfile> captor = ArgumentCaptor.forClass(StrategyProfile.class);
        verify(strategyProfileRepository).save(captor.capture());
        StrategyProfile duplicated = captor.getValue();
        assertThat(duplicated.getName()).isEqualTo("Source copy");
        assertThat(duplicated.getHeroRangeSets()).hasSize(1);
        assertThat(duplicated.getHeroRangeSets().get(0).getCells()).hasSize(1);
        assertThat(duplicated.getHeroRangeSets().get(0).getCells().get(0).getHandCode()).isEqualTo("AJo");
    }

    private StrategyProfile profile(String name, GameType gameType, boolean active) {
        StrategyProfile profile = new StrategyProfile();
        ReflectionTestUtils.setField(profile, "id", UUID.randomUUID());
        profile.setName(name);
        profile.setDescription("desc");
        profile.setGameType(gameType);
        profile.setActive(active);
        profile.setArchived(false);
        profile.setVersionLabel("v1");
        profile.setHeroRangeSets(List.of());
        profile.setVillainRangeSets(List.of());
        profile.setDecisionRules(List.of());
        profile.setHandReviews(List.of());
        return profile;
    }
}
