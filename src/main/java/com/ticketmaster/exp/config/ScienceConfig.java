package com.ticketmaster.exp.config;

import lombok.Data;

import java.io.Serializable;

/**
 * ExperimentConfiguration + entry guard FQN
 * For ramp up steps, create a JSON file prefixed: science-config-<ramp-up-type>-<experiment-name>-rampup.json
 */
@Data
public class ScienceConfig implements Serializable {
    private String version;
    private String key;
    private ExperimentConfig experimentConfig;
    //private EntryGuard entryGuard;
}
