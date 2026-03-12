package com.brilarisoft.lamuertapokerintelligence.service;

import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeCellsUpdateDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeContextQueryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.VillainRangeUpsertDto;
import java.util.List;
import java.util.UUID;

public interface VillainRangeService {

    VillainRangeDetailDto createRange(VillainRangeUpsertDto request);

    VillainRangeDetailDto bulkUpsert(VillainRangeUpsertDto request);

    VillainRangeDetailDto updateRange(UUID rangeId, VillainRangeUpsertDto request);

    void deleteRange(UUID rangeId);

    VillainRangeDetailDto getRange(UUID rangeId);

    VillainRangeDetailDto getRangeByContext(VillainRangeContextQueryDto query);

    List<VillainRangeSummaryDto> listByProfile(UUID profileId);

    VillainRangeDetailDto updateCells(UUID rangeId, VillainRangeCellsUpdateDto request);
}
