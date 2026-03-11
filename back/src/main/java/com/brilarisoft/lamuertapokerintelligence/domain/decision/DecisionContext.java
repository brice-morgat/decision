package com.brilarisoft.lamuertapokerintelligence.domain.decision;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
import com.brilarisoft.lamuertapokerintelligence.domain.range.HeroRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.range.VillainRangeSet;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.PlayerRole;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Normalized decision context consumed by the rule engine.
 */
@Entity
@Table(name = "decision_contexts")
public class DecisionContext extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "strategy_profile_id", nullable = false)
    private StrategyProfile strategyProfile;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "decision_input_id")
    private DecisionInput decisionInput;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private GameType gameType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Position heroPosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Position villainPosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Street street;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ScenarioType scenarioType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal effectiveStackInBigBlinds;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal potSizeInBigBlinds;

    @Column(nullable = false, length = 16)
    private String heroHandCode;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "board_state_id")
    private BoardState boardState;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "action_sequence_id")
    private ActionSequence actionSequence;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ActionType facingActionType;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private StrategyLegend heroStrategyLegend;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hero_range_set_id")
    private HeroRangeSet heroRangeSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "villain_range_set_id")
    private VillainRangeSet villainRangeSet;

    @Column(nullable = false)
    private boolean heroWasPreflopAggressor;

    @Column(nullable = false)
    private boolean villainWasPreflopAggressor;

    @Column(nullable = false)
    private boolean villainCheckedToHero;

    @Column(nullable = false)
    private boolean heroBetCurrentStreet;

    @Column(nullable = false)
    private boolean villainRaisedHeroBet;

    @Column(nullable = false)
    private boolean heroFacingSecondBarrel;

    @Column(nullable = false)
    private boolean heroFacingThirdBarrel;

    @Column(nullable = false)
    private Integer currentStreetBetCount = 0;

    @Column(nullable = false)
    private Integer currentStreetRaiseCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private PlayerRole currentActor;

    @Column(length = 256)
    private String lineSignature;

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

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
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

    public BigDecimal getEffectiveStackInBigBlinds() {
        return effectiveStackInBigBlinds;
    }

    public void setEffectiveStackInBigBlinds(BigDecimal effectiveStackInBigBlinds) {
        this.effectiveStackInBigBlinds = effectiveStackInBigBlinds;
    }

    public BigDecimal getPotSizeInBigBlinds() {
        return potSizeInBigBlinds;
    }

    public void setPotSizeInBigBlinds(BigDecimal potSizeInBigBlinds) {
        this.potSizeInBigBlinds = potSizeInBigBlinds;
    }

    public String getHeroHandCode() {
        return heroHandCode;
    }

    public void setHeroHandCode(String heroHandCode) {
        this.heroHandCode = heroHandCode;
    }

    public BoardState getBoardState() {
        return boardState;
    }

    public void setBoardState(BoardState boardState) {
        this.boardState = boardState;
    }

    public ActionSequence getActionSequence() {
        return actionSequence;
    }

    public void setActionSequence(ActionSequence actionSequence) {
        this.actionSequence = actionSequence;
    }

    public ActionType getFacingActionType() {
        return facingActionType;
    }

    public void setFacingActionType(ActionType facingActionType) {
        this.facingActionType = facingActionType;
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

    public boolean isHeroWasPreflopAggressor() {
        return heroWasPreflopAggressor;
    }

    public void setHeroWasPreflopAggressor(boolean heroWasPreflopAggressor) {
        this.heroWasPreflopAggressor = heroWasPreflopAggressor;
    }

    public boolean isVillainCheckedToHero() {
        return villainCheckedToHero;
    }

    public void setVillainCheckedToHero(boolean villainCheckedToHero) {
        this.villainCheckedToHero = villainCheckedToHero;
    }

    public boolean isVillainWasPreflopAggressor() {
        return villainWasPreflopAggressor;
    }

    public void setVillainWasPreflopAggressor(boolean villainWasPreflopAggressor) {
        this.villainWasPreflopAggressor = villainWasPreflopAggressor;
    }

    public boolean isHeroBetCurrentStreet() {
        return heroBetCurrentStreet;
    }

    public void setHeroBetCurrentStreet(boolean heroBetCurrentStreet) {
        this.heroBetCurrentStreet = heroBetCurrentStreet;
    }

    public boolean isVillainRaisedHeroBet() {
        return villainRaisedHeroBet;
    }

    public void setVillainRaisedHeroBet(boolean villainRaisedHeroBet) {
        this.villainRaisedHeroBet = villainRaisedHeroBet;
    }

    public boolean isHeroFacingSecondBarrel() {
        return heroFacingSecondBarrel;
    }

    public void setHeroFacingSecondBarrel(boolean heroFacingSecondBarrel) {
        this.heroFacingSecondBarrel = heroFacingSecondBarrel;
    }

    public boolean isHeroFacingThirdBarrel() {
        return heroFacingThirdBarrel;
    }

    public void setHeroFacingThirdBarrel(boolean heroFacingThirdBarrel) {
        this.heroFacingThirdBarrel = heroFacingThirdBarrel;
    }

    public Integer getCurrentStreetBetCount() {
        return currentStreetBetCount;
    }

    public void setCurrentStreetBetCount(Integer currentStreetBetCount) {
        this.currentStreetBetCount = currentStreetBetCount;
    }

    public Integer getCurrentStreetRaiseCount() {
        return currentStreetRaiseCount;
    }

    public void setCurrentStreetRaiseCount(Integer currentStreetRaiseCount) {
        this.currentStreetRaiseCount = currentStreetRaiseCount;
    }

    public PlayerRole getCurrentActor() {
        return currentActor;
    }

    public void setCurrentActor(PlayerRole currentActor) {
        this.currentActor = currentActor;
    }

    public String getLineSignature() {
        return lineSignature;
    }

    public void setLineSignature(String lineSignature) {
        this.lineSignature = lineSignature;
    }
}
