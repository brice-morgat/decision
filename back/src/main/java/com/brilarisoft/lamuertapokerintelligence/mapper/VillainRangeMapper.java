package com.brilarisoft.lamuertapokerintelligence.mapper;

import com.brilarisoft.lamuertapokerintelligence.domain.range.PokerHandCodeCatalog;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeCell;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeCellDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeUpsertDto;
import org.springframework.stereotype.Component;

/**
 * Maps villain range persistence entities to API DTOs and back.
 */
@Component
public class VillainRangeMapper {

    public VillainRangeSummaryDto toSummaryDto(VillainRangeSet rangeSet) {
        return new VillainRangeSummaryDto(
                rangeSet.getId(),
                rangeSet.getStrategyProfile().getId(),
                rangeSet.getName(),
                rangeSet.getDescription(),
                rangeSet.getGameType(),
                rangeSet.getStreet(),
                rangeSet.getVillainPosition(),
                rangeSet.getHeroPosition(),
                rangeSet.getScenarioType(),
                rangeSet.getTriggerActionType(),
                rangeSet.getLineSignature(),
                rangeSet.isEnabled(),
                rangeSet.getPriority(),
                rangeSet.getUpdatedAt()
        );
    }

    public VillainRangeDetailDto toDetailDto(VillainRangeSet rangeSet) {
        return new VillainRangeDetailDto(
                rangeSet.getId(),
                rangeSet.getStrategyProfile().getId(),
                rangeSet.getName(),
                rangeSet.getDescription(),
                rangeSet.getGameType(),
                rangeSet.getStreet(),
                rangeSet.getVillainPosition(),
                rangeSet.getHeroPosition(),
                rangeSet.getScenarioType(),
                rangeSet.getTriggerActionType(),
                rangeSet.getLineSignature(),
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

    public void updateEntity(VillainRangeSet rangeSet, VillainRangeUpsertDto dto) {
        rangeSet.setName(dto.name().trim());
        rangeSet.setDescription(dto.description() == null ? "" : dto.description().trim());
        rangeSet.setGameType(dto.gameType());
        rangeSet.setStreet(dto.street());
        rangeSet.setVillainPosition(dto.villainPosition());
        rangeSet.setHeroPosition(dto.heroPosition());
        rangeSet.setScenarioType(dto.scenarioType());
        rangeSet.setTriggerActionType(dto.triggerActionCode());
        rangeSet.setLineSignature(normalizeNullable(dto.lineSignature()));
        rangeSet.setEnabled(dto.enabled());
        rangeSet.setPriority(dto.priority());
        rangeSet.setNotes(normalizeNullable(dto.notes()));
    }

    public VillainRangeCell toCellEntity(VillainRangeCellDto dto, VillainRangeSet rangeSet) {
        VillainRangeCell cell = new VillainRangeCell();
        cell.setVillainRangeSet(rangeSet);
        cell.setHandCode(PokerHandCodeCatalog.normalize(dto.handCode()));
        cell.setEnabled(dto.enabled());
        cell.setWeightPercent(dto.weight());
        cell.setTagCode(normalizeNullable(dto.tagCode()));
        cell.setNote(normalizeNullable(dto.note()));
        return cell;
    }

    private VillainRangeCellDto toCellDto(VillainRangeCell cell) {
        return new VillainRangeCellDto(
                cell.getHandCode(),
                cell.isEnabled(),
                cell.getWeightPercent(),
                cell.getTagCode(),
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
