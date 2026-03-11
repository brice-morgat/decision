package com.brilarisoft.lamuertapokerintelligence.domain.range;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.StrategyLegend;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "hero_range_cells",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hero_range_set_id", "hand_code"})
)
public class HeroRangeCell extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hero_range_set_id", nullable = false)
    private HeroRangeSet heroRangeSet;

    @Column(name = "hand_code", nullable = false, length = 8)
    private String handCode;

    @Column(nullable = false)
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private StrategyLegend strategyLegend;

    @Column(length = 16)
    private String colorCode;

    @Column(length = 1000)
    private String note;

    @Column
    private Integer weightPercent;

    public HeroRangeSet getHeroRangeSet() {
        return heroRangeSet;
    }

    public void setHeroRangeSet(HeroRangeSet heroRangeSet) {
        this.heroRangeSet = heroRangeSet;
    }

    public String getHandCode() {
        return handCode;
    }

    public void setHandCode(String handCode) {
        this.handCode = handCode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public StrategyLegend getStrategyLegend() {
        return strategyLegend;
    }

    public void setStrategyLegend(StrategyLegend strategyLegend) {
        this.strategyLegend = strategyLegend;
    }

    public Integer getWeightPercent() {
        return weightPercent;
    }

    public void setWeightPercent(Integer weightPercent) {
        this.weightPercent = weightPercent;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
