package com.brilarisoft.lamuertapokerintelligence.domain.range;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "villain_range_cells",
        uniqueConstraints = @UniqueConstraint(columnNames = {"villain_range_set_id", "hand_code"})
)
public class VillainRangeCell extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "villain_range_set_id", nullable = false)
    private VillainRangeSet villainRangeSet;

    @Column(name = "hand_code", nullable = false, length = 8)
    private String handCode;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column
    private Integer weightPercent;

    @Column(length = 64)
    private String tagCode;

    @Column(length = 1000)
    private String note;

    public VillainRangeSet getVillainRangeSet() {
        return villainRangeSet;
    }

    public void setVillainRangeSet(VillainRangeSet villainRangeSet) {
        this.villainRangeSet = villainRangeSet;
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

    public Integer getWeightPercent() {
        return weightPercent;
    }

    public void setWeightPercent(Integer weightPercent) {
        this.weightPercent = weightPercent;
    }

    public String getTagCode() {
        return tagCode;
    }

    public void setTagCode(String tagCode) {
        this.tagCode = tagCode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
