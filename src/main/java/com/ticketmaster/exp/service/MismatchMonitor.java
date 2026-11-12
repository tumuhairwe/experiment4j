package com.ticketmaster.exp.service;

import com.google.common.collect.Range;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.ticketmaster.exp.consumer.InfluxDbMismatchPublisher;
import com.ticketmaster.exp.influx.InfluxDBConfig;
import com.ticketmaster.exp.repository.ExperimentRepository;
import com.ticketmaster.exp.repository.InFluxDbExperimentRepository;

import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A background service that periodically monitors InfluxDB for mismatch
 * thresholds and triggers traffic reductions via the ExperimentControlService.
 */
public class MismatchMonitor {

    private final InfluxDBClient influxClient;
    private final InfluxDBConfig influxConfig;
    private final ExperimentControlService controlService;
    private final ScheduledExecutorService scheduler;
    private InFluxDbExperimentRepository repository;
    private ExperimentRepository experimentRepository;
    private InfluxDbMismatchPublisher mismatchListener;

    // A set of experiments we are actively monitoring
    private final Set<String> monitoredExperiments;

    // Configuration for the monitor
    private static final long CHECK_INTERVAL_SECONDS = 10;
    private static final String QUERY_TIME_RANGE = "1d"; // Check for mismatches in the past day
    private static final int MISMATCH_THRESHOLD = 10; // Trigger if > 10 mismatches
    private static final int TRAFFIC_REDUCTION_PERCENTAGE = 50; // Reduce traffic by 50%

    public MismatchMonitor(InfluxDBClient influxClient, InfluxDBConfig influxConfig, ExperimentControlService controlService) {
        this.influxClient = influxClient;
        this.influxConfig = influxConfig;
        this.controlService = controlService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.monitoredExperiments = new HashSet<>();
    }

    /**
     * Adds an experiment to the monitor' watch list.
     * @param experimentName The name of the experiment.
     */
    public void startMonitoring(String experimentName) {
        monitoredExperiments.add(experimentName);
        System.out.println("Started monitoring mismatches for: " + experimentName);
    }

    /**
     * Starts the background monitoring task.
     */
    public void start() {
        scheduler.scheduleAtFixedRate(this::checkMismatchRates,
                CHECK_INTERVAL_SECONDS,
                CHECK_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
        System.out.println("MismatchMonitor started. Checking every " + CHECK_INTERVAL_SECONDS + " seconds.");
    }

    /**
     * Stops the background monitoring task.
     */
    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        System.out.println("MismatchMonitor stopped.");
    }

    /**
    * The core monitoring logic that runs periodically.
    */
    private void checkMismatchRates() {
        if (monitoredExperiments.isEmpty()) {
            return;
        }

        // Create a query to check mismatch counts for all monitored experiments
        monitoredExperiments.stream()
                .map(exp -> repository.findMismatchesByExperiment(exp, ChronoUnit.DAYS, 1))
                .flatMap(records -> records.stream())
                .forEach(record -> {
                    String experimentName = (String) record.getValueByKey("experimentName");
                    long mismatchCount = getMismatchCount(record);
                });

        String fluxQuery = buildFluxQuery();

        //InfluxQLQueryApi queryApi = influxClient.getInfluxQLQueryApi();
        QueryApi queryApi = influxClient.getQueryApi();

        try {
            List<FluxTable> tables = queryApi.query(fluxQuery, influxConfig.getOrg());
            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    String experimentName = (String) record.getValueByKey("experimentName");
                    long mismatchCount = getMismatchCount(record);
                    Range<Integer> mismatchRange = experimentRepository.findMismatchThreshold(experimentName);

                    // Check if the threshold is breached
                    //if (mismatchCount > MISMATCH_THRESHOLD) {
                    if (!mismatchRange.contains((int) mismatchCount)){
                        // Threshold breached! Tell the control service to reduce traffic.
                        // may save to DB so rampUp or rampDown be aware
                        // may notify REST endpoint
                        // may log or alert via SMTP
                        mismatchListener.accept(experimentName, mismatchCount); // listener may be composite
                        controlService.reduceTraffic(experimentName, TRAFFIC_REDUCTION_PERCENTAGE);
                    } else {
                         // Optional: Log that the experiment is healthy
                         // System.out.printf("Health check OK for '%s': %d mismatches (Threshold: %d)%n",
                         //         experimentName, mismatchCount, MISMATCH_THRESHOLD);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error querying InfluxDB for mismatch rates: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private long getMismatchCount(FluxRecord record) {
        // The _value field can be of different numeric types,
        // so we handle it gracefully.
        Object value = record.getValue();
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0;
    }

    /**
     * Builds a Flux query to get the mismatch counts for all
     * monitored experiments within the defined time range.
     *
     * Example:
     * from(bucket: "experiment-bucket")
     * |> range(start: -1d)
     * |> filter(fn: (r) => r._measurement == "experiment_mismatch")
     * |> filter(fn: (r) => r.experimentName == "exp1" or r.experimentName == "exp2")
     * |> sum(column: "_value")
     * |> group(columns: ["experimentName"])
     * |> yield()
     *
     * @return A Flux query string.
     */
    private String buildFluxQuery() {
        StringBuilder filterExperiments = new StringBuilder();
        for (String expName : monitoredExperiments) {
            if (filterExperiments.length() > 0) {
                filterExperiments.append(" or ");
            }
            filterExperiments.append(String.format("r.experimentName == \"%s\"", expName));
        }

        return String.format(
                "from(bucket: \"%s\")\n" +
                "  |> range(start: -%s)\n" +
                "  |> filter(fn: (r) => r._measurement == \"experiment_mismatch\")\n" +
                "  |> filter(fn: (r) => %s)\n" +
                "  |> group(columns: [\"experimentName\"])\n" +
                "  |> sum(column: \"_value\")\n" +
                "  |> yield(name: \"mismatch_count\")",
                influxConfig.getBucket(),
                QUERY_TIME_RANGE,
                filterExperiments
        );
    }
}

