package com.ticketmaster.exp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import java.util.function.BiPredicate;

import lombok.Data;

/**
 * Represents the configuration for a single scientific experiment.
 */
@Data
@Entity
@Table(name = "experiment")
public class Experiment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "control_class_name", nullable = false)
    private String control;

    @Column(name = "candidate_class_name", nullable = false)
    private String candidate;

    /**
     * The fully qualified class name of a BiPredicate<T, T> that determines if
     * two non-exceptional results are considered a "match".
     */
    @Column(name = "same_when_class_name")
    private String sameWhen;

    /**
     * The fully qualified class name of a BiFunction<Exception, Exception, Boolean>
     * that determines if two exceptions are considered a "match".
     */
    @Column(name = "exception_when_class_name")
    private String exceptionWhen;

    /**
     * The fully qualified class name of a publisher that handles the results
     * of an experiment run.
     */
    @Column(name = "publisher_class_name")
    private String publisher;

    @Embedded
    private ReturnChoice returnChoice;
}
