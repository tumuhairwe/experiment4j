package com.ticketmaster.exp.service;

import com.ticketmaster.exp.config.ExperimentConfig;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the dynamic state of running experiments, specifically the
 * current traffic percentage. This service is thread-safe and allows
 * other services (like a MismatchMonitor) to adjust traffic on the fly.
 */
//@Slf4j
public class ExperimentControlService {

    // A thread-safe map to hold the *current* runtime percentage for each experiment.
    // Key: Experiment Name, Value: Current Percentage (0-100)
    private final Map<String, Integer> experimentTrafficState;
    private final Random random;

    public ExperimentControlService() {
        this.experimentTrafficState = new ConcurrentHashMap<>();
        this.random = new Random();
    }

    /**
     * Initializes the state for a new experiment, setting its traffic
     * percentage to the configured initial value.
     *
     * @param experimentName The name of the experiment.
     * @param config The static configuration for the experiment.
     */
    public void initializeExperiment(String experimentName, ExperimentConfig config) {
        // Set the initial traffic percentage from the config.
        // If not present, default to 0.
        int initialTraffic = config.getInitialTrafficPercentage();
        experimentTrafficState.put(experimentName, initialTraffic);
//        log.info("Initialized experiment {} with traffic at {}%",
//                experimentName, initialTraffic);
    }

    /**
     * Attempts to reduce the traffic for a given experiment.
     * This is called by the MismatchMonitor when a threshold is breached.
     *
     * @param experimentName The name of the experiment.
     * @param reductionPercentage The percentage to reduce traffic by (e.g., 50 for 50%).
     */
    public synchronized void reduceTraffic(String experimentName, int reductionPercentage) {
        int currentPercentage = experimentTrafficState.getOrDefault(experimentName, 0);
        if (currentPercentage == 0) {
//            log.info("Traffic for {} is already 0%, no reduction needed.", experimentName);
            return;
        }

        // Calculate the new percentage
        int reductionAmount = (int) (currentPercentage * (reductionPercentage / 100.0));
        int newPercentage = Math.max(0, currentPercentage - reductionAmount); // Don't go below 0

        if (newPercentage < currentPercentage) {
            experimentTrafficState.put(experimentName, newPercentage);
//            log.warn("!!! CIRCUIT BREAKER: Mismatch threshold breached for {}. " +
//                            "Reducing traffic from {}% to {}%. !!!%n",
//                    experimentName, currentPercentage, newPercentage);
        }
    }

    /**
     * Checks if the candidate for a specific experiment should run based on the
     * *current* dynamic traffic percentage.
     *
     * @param experimentName The name of the experiment.
     * @return true if the candidate should run, false otherwise.
     */
    public boolean shouldRunCandidate(String experimentName) {
        int currentPercentage = experimentTrafficState.getOrDefault(experimentName, 0);

        if (currentPercentage == 0) {
            return false; // No traffic
        }
        if (currentPercentage == 100) {
            return true; // Full traffic
        }

        // Check if a random number (0-99) falls within the current percentage
        return random.nextInt(100) < currentPercentage;
    }

    /**
     * Gets the current traffic percentage for a specific experiment.
     *
     * @param experimentName The name of the experiment.
     * @return The current traffic percentage (0-100).
     */
    public int getCurrentTrafficPercentage(String experimentName) {
        return experimentTrafficState.getOrDefault(experimentName, 0);
    }
}

