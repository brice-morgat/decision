package com.brilarisoft.lamuertapokerintelligence.domain.history;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionInput;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionResult;
import com.brilarisoft.lamuertapokerintelligence.domain.strategyprofile.StrategyProfile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Historical aggregate tying a played hand to the engine input, normalized
 * context, computed result and observed outcome.
 */
@Entity
@Table(name = "hand_reviews")
public class HandReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "strategy_profile_id", nullable = false)
    private StrategyProfile strategyProfile;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "decision_input_id")
    private DecisionInput decisionInput;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "decision_context_id")
    private DecisionContext decisionContext;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "decision_result_id")
    private DecisionResult decisionResult;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "hand_outcome_id")
    private HandOutcome handOutcome;

    @Column(length = 1000)
    private String reviewNote;

    public StrategyProfile getStrategyProfile() {
        return strategyProfile;
    }

    public void setStrategyProfile(StrategyProfile strategyProfile) {
        this.strategyProfile = strategyProfile;
    }

    public DecisionInput getDecisionInput() {
        return decisionInput;
    }

    public void setDecisionInput(DecisionInput decisionInput) {
        this.decisionInput = decisionInput;
    }

    public DecisionContext getDecisionContext() {
        return decisionContext;
    }

    public void setDecisionContext(DecisionContext decisionContext) {
        this.decisionContext = decisionContext;
    }

    public DecisionResult getDecisionResult() {
        return decisionResult;
    }

    public void setDecisionResult(DecisionResult decisionResult) {
        this.decisionResult = decisionResult;
    }

    public HandOutcome getHandOutcome() {
        return handOutcome;
    }

    public void setHandOutcome(HandOutcome handOutcome) {
        this.handOutcome = handOutcome;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }
}
