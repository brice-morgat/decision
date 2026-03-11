package com.brilarisoft.lamuertapokerintelligence.domain.strategyprofile;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
import com.brilarisoft.lamuertapokerintelligence.domain.history.HandReview;
import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.DecisionRule;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Root aggregate for a user's strategic configuration.
 * It groups ranges, rules and reviews under one explicit poker context.
 */
@Entity
@Table(name = "strategy_profiles")
public class StrategyProfile extends BaseEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private GameType gameType;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean archived;

    @Column(nullable = false, length = 32)
    private String versionLabel;

    @OneToMany(mappedBy = "strategyProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HeroRangeSet> heroRangeSets = new ArrayList<>();

    @OneToMany(mappedBy = "strategyProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VillainRangeSet> villainRangeSets = new ArrayList<>();

    @OneToMany(mappedBy = "strategyProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DecisionRule> decisionRules = new ArrayList<>();

    @OneToMany(mappedBy = "strategyProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HandReview> handReviews = new ArrayList<>();

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

    public List<HeroRangeSet> getHeroRangeSets() {
        return heroRangeSets;
    }

    public void setHeroRangeSets(List<HeroRangeSet> heroRangeSets) {
        this.heroRangeSets = heroRangeSets;
    }

    public List<VillainRangeSet> getVillainRangeSets() {
        return villainRangeSets;
    }

    public void setVillainRangeSets(List<VillainRangeSet> villainRangeSets) {
        this.villainRangeSets = villainRangeSets;
    }

    public List<DecisionRule> getDecisionRules() {
        return decisionRules;
    }

    public void setDecisionRules(List<DecisionRule> decisionRules) {
        this.decisionRules = decisionRules;
    }

    public List<HandReview> getHandReviews() {
        return handReviews;
    }

    public void setHandReviews(List<HandReview> handReviews) {
        this.handReviews = handReviews;
    }
}
