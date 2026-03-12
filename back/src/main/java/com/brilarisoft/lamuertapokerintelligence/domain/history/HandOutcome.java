package com.brilarisoft.lamuertapokerintelligence.domain.history;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ReviewIssue;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Stores the observed outcome of a reviewed hand independently from the engine.
 */
@Entity
@Table(name = "hand_outcomes")
public class HandOutcome extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ActionType actualAction;

    @Column(precision = 10, scale = 2)
    private BigDecimal netResultInBigBlinds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReviewIssue issue;

    @Column(nullable = false)
    private boolean showdownReached;

    @Column(length = 1000)
    private String note;

    public ActionType getActualAction() {
        return actualAction;
    }

    public void setActualAction(ActionType actualAction) {
        this.actualAction = actualAction;
    }

    public BigDecimal getNetResultInBigBlinds() {
        return netResultInBigBlinds;
    }

    public void setNetResultInBigBlinds(BigDecimal netResultInBigBlinds) {
        this.netResultInBigBlinds = netResultInBigBlinds;
    }

    public ReviewIssue getIssue() {
        return issue;
    }

    public void setIssue(ReviewIssue issue) {
        this.issue = issue;
    }

    public boolean isShowdownReached() {
        return showdownReached;
    }

    public void setShowdownReached(boolean showdownReached) {
        this.showdownReached = showdownReached;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
