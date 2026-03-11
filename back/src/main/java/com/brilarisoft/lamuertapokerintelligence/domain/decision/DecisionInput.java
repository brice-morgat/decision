package com.brilarisoft.lamuertapokerintelligence.domain.decision;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.domain.strategyprofile.StrategyProfile;
import jakarta.persistence.CascadeType;
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
 * Captures the raw user-provided data before context reconstruction.
 */
@Entity
@Table(name = "decision_inputs")
public class DecisionInput extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "strategy_profile_id", nullable = false)
    private StrategyProfile strategyProfile;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "decision_input_hero_cards", joinColumns = @JoinColumn(name = "decision_input_id"))
    @Column(name = "card_code", nullable = false, length = 4)
    private List<String> heroCards = new ArrayList<>();

    public StrategyProfile getStrategyProfile() {
        return strategyProfile;
    }

    public void setStrategyProfile(StrategyProfile strategyProfile) {
        this.strategyProfile = strategyProfile;
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

    public List<String> getHeroCards() {
        return heroCards;
    }

    public void setHeroCards(List<String> heroCards) {
        this.heroCards = heroCards;
    }
}
