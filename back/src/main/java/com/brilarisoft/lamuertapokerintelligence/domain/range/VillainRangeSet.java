package com.brilarisoft.lamuertapokerintelligence.domain.range;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
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
 * Represents an opponent range hypothesis for a given tactical context.
 * It enriches the decision engine but never replaces it.
 */
@Entity
@Table(name = "villain_range_sets")
public class VillainRangeSet extends BaseEntity {

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
    @Column(nullable = false, length = 16)
    private Position villainPosition;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Position heroPosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ScenarioType scenarioType;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ActionType triggerActionType;

    @Column(length = 128)
    private String lineSignature;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private Integer priority = 0;

    @Column(length = 2000)
    private String notes;

    @OneToMany(mappedBy = "villainRangeSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VillainRangeCell> cells = new ArrayList<>();

    @OneToMany(mappedBy = "villainRangeSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VillainRangeScope> scopes = new ArrayList<>();

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

    public Position getVillainPosition() {
        return villainPosition;
    }

    public void setVillainPosition(Position villainPosition) {
        this.villainPosition = villainPosition;
    }

    public Position getHeroPosition() {
        return heroPosition;
    }

    public void setHeroPosition(Position heroPosition) {
        this.heroPosition = heroPosition;
    }

    public ScenarioType getScenarioType() {
        return scenarioType;
    }

    public void setScenarioType(ScenarioType scenarioType) {
        this.scenarioType = scenarioType;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<VillainRangeCell> getCells() {
        return cells;
    }

    public void setCells(List<VillainRangeCell> cells) {
        this.cells = cells;
    }

    public List<VillainRangeScope> getScopes() {
        return scopes;
    }

    public void setScopes(List<VillainRangeScope> scopes) {
        this.scopes = scopes;
    }
}
