package com.brilarisoft.lamuertapokerintelligence.domain.range;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "villain_range_scopes")
public class VillainRangeScope extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "villain_range_set_id", nullable = false)
    private VillainRangeSet villainRangeSet;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Position heroPosition;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Position villainPosition;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ActionType triggerActionType;

    @Column(length = 128)
    private String lineSignature;

    @Column(nullable = false)
    private Integer scopeWeight = 0;

    public VillainRangeSet getVillainRangeSet() {
        return villainRangeSet;
    }

    public void setVillainRangeSet(VillainRangeSet villainRangeSet) {
        this.villainRangeSet = villainRangeSet;
    }

    public Position getHeroPosition() {
        return heroPosition;
    }

    public void setHeroPosition(Position heroPosition) {
        this.heroPosition = heroPosition;
    }

    public Position getVillainPosition() {
        return villainPosition;
    }

    public void setVillainPosition(Position villainPosition) {
        this.villainPosition = villainPosition;
    }

    public ActionType getTriggerActionType() {
        return triggerActionType;
    }

    public void setTriggerActionType(ActionType triggerActionType) {
        this.triggerActionType = triggerActionType;
    }

    public String getLineSignature() {
        return lineSignature;
    }

    public void setLineSignature(String lineSignature) {
        this.lineSignature = lineSignature;
    }

    public Integer getScopeWeight() {
        return scopeWeight;
    }

    public void setScopeWeight(Integer scopeWeight) {
        this.scopeWeight = scopeWeight;
    }
}
