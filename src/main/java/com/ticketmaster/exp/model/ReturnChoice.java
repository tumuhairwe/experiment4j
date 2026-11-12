package com.ticketmaster.exp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

/**
 * An embeddable object that defines the strategy for returning a value from an experiment.
 */
@Data
@Embeddable
public class ReturnChoice {

    @Enumerated(EnumType.STRING)
    @Column(name = "return_choice_strategy")
    private ReturnChoiceStrategy name;

    @Column(name = "return_choice_metadata")
    private String metadata;
}
