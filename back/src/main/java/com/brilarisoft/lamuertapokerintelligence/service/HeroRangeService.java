package com.brilarisoft.lamuertapokerintelligence.service;

import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeCellsUpdateDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeContextQueryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeSummaryDto;
import com.brilarisoft.lamuertapokerintelligence.dto.range.HeroRangeUpsertDto;
import java.util.List;
import java.util.UUID;

public interface HeroRangeService {

    HeroRangeDetailDto createRange(HeroRangeUpsertDto request);

    HeroRangeDetailDto updateRange(UUID rangeId, HeroRangeUpsertDto request);

    void deleteRange(UUID rangeId);

    HeroRangeDetailDto getRange(UUID rangeId);

    HeroRangeDetailDto getRangeByContext(HeroRangeContextQueryDto query);

    List<HeroRangeSummaryDto> listByProfile(UUID profileId);

    HeroRangeDetailDto updateCells(UUID rangeId, HeroRangeCellsUpdateDto request);
}
