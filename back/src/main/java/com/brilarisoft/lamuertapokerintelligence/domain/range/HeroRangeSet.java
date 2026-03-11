package com.brilarisoft.lamuertapokerintelligence.domain.range;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
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
 * Defines the hero range used for a precise strategic context.
 * The entity stores only configuration and no decision logic.
 */
@Entity
@Table(name = "hero_range_sets")
public class HeroRangeSet extends BaseEntity {

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
    private Position heroPosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ScenarioType scenarioType;

    @Column(length = 64)
    private String subScenarioCode;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private Integer priority = 0;

    @Column(length = 2000)
    private String notes;

    @OneToMany(mappedBy = "heroRangeSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HeroRangeCell> cells = new ArrayList<>();

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

    public String getSubScenarioCode() {
        return subScenarioCode;
    }

    public void setSubScenarioCode(String subScenarioCode) {
        this.subScenarioCode = subScenarioCode;
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

    public List<HeroRangeCell> getCells() {
        return cells;
    }

    public void setCells(List<HeroRangeCell> cells) {
        this.cells = cells;
    }
}
