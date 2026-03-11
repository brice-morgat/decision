package com.brilarisoft.lamuertapokerintelligence.domain.decision;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.PlayerRole;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.SizingType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "action_events")
public class ActionEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_sequence_id")
    private ActionSequence actionSequence;

    @Column(nullable = false)
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PlayerRole actorType;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Position actorPosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Street street;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ActionType actionCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private SizingType sizingType;

    @Column(precision = 10, scale = 2)
    private BigDecimal sizingValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal potSizeBefore;

    @Column(precision = 10, scale = 2)
    private BigDecimal potSizeAfter;

    @Column(precision = 10, scale = 2)
    private BigDecimal stackBefore;

    @Column(precision = 10, scale = 2)
    private BigDecimal stackAfter;

    @Column(length = 1000)
    private String note;

    public ActionSequence getActionSequence() {
        return actionSequence;
    }

    public void setActionSequence(ActionSequence actionSequence) {
        this.actionSequence = actionSequence;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public PlayerRole getActorType() {
        return actorType;
    }

    public void setActorType(PlayerRole actorType) {
        this.actorType = actorType;
    }

    public Position getActorPosition() {
        return actorPosition;
    }

    public void setActorPosition(Position actorPosition) {
        this.actorPosition = actorPosition;
    }

    public Street getStreet() {
        return street;
    }

    public void setStreet(Street street) {
        this.street = street;
    }

    public ActionType getActionCode() {
        return actionCode;
    }

    public void setActionCode(ActionType actionCode) {
        this.actionCode = actionCode;
    }

    public SizingType getSizingType() {
        return sizingType;
    }

    public void setSizingType(SizingType sizingType) {
        this.sizingType = sizingType;
    }

    public BigDecimal getSizingValue() {
        return sizingValue;
    }

    public void setSizingValue(BigDecimal sizingValue) {
        this.sizingValue = sizingValue;
    }

    public BigDecimal getPotSizeBefore() {
        return potSizeBefore;
    }

    public void setPotSizeBefore(BigDecimal potSizeBefore) {
        this.potSizeBefore = potSizeBefore;
    }

    public BigDecimal getPotSizeAfter() {
        return potSizeAfter;
    }

    public void setPotSizeAfter(BigDecimal potSizeAfter) {
        this.potSizeAfter = potSizeAfter;
    }

    public BigDecimal getStackBefore() {
        return stackBefore;
    }

    public void setStackBefore(BigDecimal stackBefore) {
        this.stackBefore = stackBefore;
    }

    public BigDecimal getStackAfter() {
        return stackAfter;
    }

    public void setStackAfter(BigDecimal stackAfter) {
        this.stackAfter = stackAfter;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
