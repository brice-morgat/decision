package com.brilarisoft.lamuertapokerintelligence.mapper;

import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeCell;
import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.range.PokerHandCodeCatalog;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeCellDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeUpsertDto;
import org.springframework.stereotype.Component;

/**
 * Maps hero range persistence entities to API DTOs and back.
 */
@Component
public class HeroRangeMapper {

    public HeroRangeSummaryDto toSummaryDto(HeroRangeSet rangeSet) {
        return new HeroRangeSummaryDto(
                rangeSet.getId(),
                rangeSet.getStrategyProfile().getId(),
                rangeSet.getName(),
                rangeSet.getDescription(),
                rangeSet.getGameType(),
                rangeSet.getStreet(),
                rangeSet.getHeroPosition(),
                rangeSet.getScenarioType(),
                rangeSet.getSubScenarioCode(),
                rangeSet.isEnabled(),
                rangeSet.getPriority(),
                rangeSet.getUpdatedAt()
        );
    }

    public HeroRangeDetailDto toDetailDto(HeroRangeSet rangeSet) {
        return new HeroRangeDetailDto(
                rangeSet.getId(),
                rangeSet.getStrategyProfile().getId(),
                rangeSet.getName(),
                rangeSet.getDescription(),
                rangeSet.getGameType(),
                rangeSet.getStreet(),
                rangeSet.getHeroPosition(),
                rangeSet.getScenarioType(),
                rangeSet.getSubScenarioCode(),
                rangeSet.isEnabled(),
                rangeSet.getPriority(),
                rangeSet.getNotes(),
                rangeSet.getCells().stream()
                        .map(this::toCellDto)
                        .sorted((left, right) -> left.handCode().compareTo(right.handCode()))
                        .toList(),
                rangeSet.getCreatedAt(),
                rangeSet.getUpdatedAt()
        );
    }

    public void updateEntity(HeroRangeSet rangeSet, HeroRangeUpsertDto dto) {
        rangeSet.setName(dto.name().trim());
        rangeSet.setDescription(dto.description() == null ? "" : dto.description().trim());
        rangeSet.setGameType(dto.gameType());
        rangeSet.setStreet(dto.street());
        rangeSet.setHeroPosition(dto.heroPosition());
        rangeSet.setScenarioType(dto.scenarioType());
        rangeSet.setSubScenarioCode(normalizeNullable(dto.subScenarioCode()));
        rangeSet.setEnabled(dto.enabled());
        rangeSet.setPriority(dto.priority());
        rangeSet.setNotes(normalizeNullable(dto.notes()));
    }

    public HeroRangeCell toCellEntity(HeroRangeCellDto dto, HeroRangeSet rangeSet) {
        HeroRangeCell cell = new HeroRangeCell();
        cell.setHeroRangeSet(rangeSet);
        cell.setHandCode(PokerHandCodeCatalog.normalize(dto.handCode()));
        cell.setEnabled(dto.enabled());
        cell.setStrategyLegend(dto.legendCode());
        cell.setColorCode(normalizeNullable(dto.colorCode()));
        cell.setNote(normalizeNullable(dto.note()));
        cell.setWeightPercent(dto.enabled() ? 100 : 0);
        return cell;
    }

    private HeroRangeCellDto toCellDto(HeroRangeCell cell) {
        return new HeroRangeCellDto(
                cell.getHandCode(),
                cell.isEnabled(),
                cell.getStrategyLegend(),
                cell.getColorCode(),
                cell.getNote()
        );
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
