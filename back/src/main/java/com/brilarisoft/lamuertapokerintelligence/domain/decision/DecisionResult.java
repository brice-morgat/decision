package com.brilarisoft.lamuertapokerintelligence.domain.decision;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.DecisionStatus;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.DecisionRule;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistent output of the decision engine, including traceability metadata.
 */
@Entity
@Table(name = "decision_results")
public class DecisionResult extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "decision_context_id")
    private DecisionContext decisionContext;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DecisionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ActionType recommendedAction;

    @Column(precision = 10, scale = 2)
    private BigDecimal recommendedSizingValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_rule_id")
    private DecisionRule matchedRule;

    @Column(nullable = false, length = 2000)
    private String explanation;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "decision_result_trace", joinColumns = @JoinColumn(name = "decision_result_id"))
    @Column(name = "trace_entry", nullable = false, length = 512)
    private List<String> trace = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "decision_result_warnings", joinColumns = @JoinColumn(name = "decision_result_id"))
    @Column(name = "warning_entry", nullable = false, length = 512)
    private List<String> warnings = new ArrayList<>();

    public DecisionContext getDecisionContext() {
        return decisionContext;
    }

    public void setDecisionContext(DecisionContext decisionContext) {
        this.decisionContext = decisionContext;
    }

    public DecisionStatus getStatus() {
        return status;
    }

    public void setStatus(DecisionStatus status) {
        this.status = status;
    }

    public ActionType getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(ActionType recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public BigDecimal getRecommendedSizingValue() {
        return recommendedSizingValue;
    }

    public void setRecommendedSizingValue(BigDecimal recommendedSizingValue) {
        this.recommendedSizingValue = recommendedSizingValue;
    }

    public DecisionRule getMatchedRule() {
        return matchedRule;
    }

    public void setMatchedRule(DecisionRule matchedRule) {
        this.matchedRule = matchedRule;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<String> getTrace() {
        return trace;
    }

    public void setTrace(List<String> trace) {
        this.trace = trace;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
