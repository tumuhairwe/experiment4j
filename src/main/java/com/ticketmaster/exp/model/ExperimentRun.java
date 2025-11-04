package com.ticketmaster.exp.model;

import com.ticketmaster.exp.MatchType;
import lombok.Data;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * JPA Entity representing a single execution of a science experiment.
 * This record is saved to the database to capture the outcome.
 */
@Data
@Entity
public class ExperimentRun implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String experimentKey;
    private Instant startTime;
    private long controlDurationMs;
    private long candidateDurationMs;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MatchType matchType;

    @Lob
    private String controlValue;
    @Lob
    private String candidateValue;
    @Lob
    private String controlExceptionDetails;
    @Lob
    private String candidateExceptionDetails;
}
