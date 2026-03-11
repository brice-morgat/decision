package com.brilarisoft.lamuertapokerintelligence.domain.rule;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.StrategyLegend;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.domain.strategyprofile.StrategyProfile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Configures a deterministic decision rule for a strategic profile.
 * The rule remains declarative; evaluation belongs to dedicated services.
 */
@Entity
@Table(name = "decision_rules")
public class DecisionRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "strategy_profile_id", nullable = false)
    private StrategyProfile strategyProfile;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private GameType gameType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Street street;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ScenarioType scenarioType;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Position heroPosition;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Position villainPosition;

    @Column(length = 8)
    private String heroHandCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private StrategyLegend heroStrategyLegend;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hero_range_set_id")
    private HeroRangeSet heroRangeSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "villain_range_set_id")
    private VillainRangeSet villainRangeSet;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ActionType facingActionType;

    @Column(length = 128)
    private String lineSignature;

    @Column(length = 128)
    private String sizingConditionCode;

    @Column(nullable = false)
    private Integer priority;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean stopOnMatch;

    @Column(length = 2000)
    private String note;

    @OneToMany(mappedBy = "decisionRule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RuleCondition> conditions = new ArrayList<>();

    @OneToMany(mappedBy = "decisionRule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RuleAction> actions = new ArrayList<>();

    public StrategyProfile getStrategyProfile() {
        return strategyProfile;
    }

    public void setStrategyProfile(StrategyProfile strategyProfile) {
        this.strategyProfile = strategyProfile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public Street getStreet() {
        return street;
    }

    public void setStreet(Street street) {
        this.street = street;
    }

    public ScenarioType getScenarioType() {
        return scenarioType;
    }

    public void setScenarioType(ScenarioType scenarioType) {
        this.scenarioType = scenarioType;
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

    public String getHeroHandCode() {
        return heroHandCode;
    }

    public void setHeroHandCode(String heroHandCode) {
        this.heroHandCode = heroHandCode;
    }

    public StrategyLegend getHeroStrategyLegend() {
        return heroStrategyLegend;
    }

    public void setHeroStrategyLegend(StrategyLegend heroStrategyLegend) {
        this.heroStrategyLegend = heroStrategyLegend;
    }

    public HeroRangeSet getHeroRangeSet() {
        return heroRangeSet;
    }

    public void setHeroRangeSet(HeroRangeSet heroRangeSet) {
        this.heroRangeSet = heroRangeSet;
    }

    public VillainRangeSet getVillainRangeSet() {
        return villainRangeSet;
    }

    public void setVillainRangeSet(VillainRangeSet villainRangeSet) {
        this.villainRangeSet = villainRangeSet;
    }

    public ActionType getFacingActionType() {
        return facingActionType;
    }

    public void setFacingActionType(ActionType facingActionType) {
        this.facingActionType = facingActionType;
    }

    public String getLineSignature() {
        return lineSignature;
    }

    public void setLineSignature(String lineSignature) {
        this.lineSignature = lineSignature;
    }

    public String getSizingConditionCode() {
        return sizingConditionCode;
    }

    public void setSizingConditionCode(String sizingConditionCode) {
        this.sizingConditionCode = sizingConditionCode;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isStopOnMatch() {
        return stopOnMatch;
    }

    public void setStopOnMatch(boolean stopOnMatch) {
        this.stopOnMatch = stopOnMatch;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<RuleCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<RuleCondition> conditions) {
        this.conditions = conditions;
    }

    public List<RuleAction> getActions() {
        return actions;
    }

    public void setActions(List<RuleAction> actions) {
        this.actions = actions;
    }
}
