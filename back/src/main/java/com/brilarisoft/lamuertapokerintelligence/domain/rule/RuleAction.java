package com.brilarisoft.lamuertapokerintelligence.domain.rule;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.SizingType;
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
@Table(name = "rule_actions")
public class RuleAction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decision_rule_id", nullable = false)
    private DecisionRule decisionRule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SizingType sizingType;

    @Column(precision = 10, scale = 2)
    private BigDecimal sizingValue;

    @Column(nullable = false)
    private Integer executionOrder;

    @Column(length = 512)
    private String messageTemplate;

    public DecisionRule getDecisionRule() {
        return decisionRule;
    }

    public void setDecisionRule(DecisionRule decisionRule) {
        this.decisionRule = decisionRule;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
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

    public Integer getExecutionOrder() {
        return executionOrder;
    }

    public void setExecutionOrder(Integer executionOrder) {
        this.executionOrder = executionOrder;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }
}
