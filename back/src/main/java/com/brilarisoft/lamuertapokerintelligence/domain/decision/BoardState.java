package com.brilarisoft.lamuertapokerintelligence.domain.decision;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "board_states")
public class BoardState extends BaseEntity {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "board_state_cards", joinColumns = @JoinColumn(name = "board_state_id"))
    @Column(name = "card_code", nullable = false, length = 4)
    private List<String> cards = new ArrayList<>();

    public List<String> getCards() {
        return cards;
    }

    public void setCards(List<String> cards) {
        this.cards = cards;
    }
}
