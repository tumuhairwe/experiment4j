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

    @Column(name = "experiment_key")
    private String experimentKey;
    @Column(name = "start_time")
    private Instant startTime;
    @Column(name = "control_duration_ms")
    private long controlDurationMs;
    @Column(name = "candidate_duration_ms")
    private long candidateDurationMs;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type")
    private MatchType matchType;

    @Lob
    @Column(name = "control_value")
    private String controlValue;
    @Lob
    @Column(name = "candidate_value")
    private String candidateValue;
    @Lob
    @Column(name = "control_exception_details")
    private String controlExceptionDetails;
    @Lob
    @Column(name = "candidate_exception_details")
    private String candidateExceptionDetails;
}
