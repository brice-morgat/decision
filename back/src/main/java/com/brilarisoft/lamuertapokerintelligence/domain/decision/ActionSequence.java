package com.brilarisoft.lamuertapokerintelligence.domain.decision;

import com.brilarisoft.lamuertapokerintelligence.domain.common.BaseEntity;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
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
 * Stores the normalized chronological history of a hand.
 */
@Entity
@Table(name = "action_sequences")
public class ActionSequence extends BaseEntity {

    @Column(length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Street currentStreet;

    @OneToMany(mappedBy = "actionSequence", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActionEvent> events = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Street getCurrentStreet() {
        return currentStreet;
    }

    public void setCurrentStreet(Street currentStreet) {
        this.currentStreet = currentStreet;
    }

    public List<ActionEvent> getEvents() {
        return events;
    }

    public void setEvents(List<ActionEvent> events) {
        this.events = events;
    }
}
