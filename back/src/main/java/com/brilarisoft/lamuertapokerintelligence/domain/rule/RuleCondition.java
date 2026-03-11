package com.brilarisoft.lamuertapokerintelligence.domain.rule;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.RuleOperator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rule_conditions")
public class RuleCondition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decision_rule_id", nullable = false)
    private DecisionRule decisionRule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private RuleConditionType conditionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RuleOperator operator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ConditionValueType valueType;

    @Column(nullable = false, length = 128)
    private String expectedValue;

    @Column(length = 128)
    private String secondaryValue;

    @Column(nullable = false)
    private Integer conditionOrder = 0;

    public DecisionRule getDecisionRule() {
        return decisionRule;
    }

    public void setDecisionRule(DecisionRule decisionRule) {
        this.decisionRule = decisionRule;
    }

    public RuleConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(RuleConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public RuleOperator getOperator() {
        return operator;
    }

    public void setOperator(RuleOperator operator) {
        this.operator = operator;
    }

    public ConditionValueType getValueType() {
        return valueType;
    }

    public void setValueType(ConditionValueType valueType) {
        this.valueType = valueType;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    public String getSecondaryValue() {
        return secondaryValue;
    }

    public void setSecondaryValue(String secondaryValue) {
        this.secondaryValue = secondaryValue;
    }

    public Integer getConditionOrder() {
        return conditionOrder;
    }

    public void setConditionOrder(Integer conditionOrder) {
        this.conditionOrder = conditionOrder;
    }
}
