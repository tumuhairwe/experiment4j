package com.ticketmaster.exp.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.ticketmaster.exp.influx.InfluxDBConfig;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

public class InFluxDbExperimentRepository {

    public static final String EXPERIMENT_MISMATCH = "experiment_mismatch";
    private InfluxDBClient influxClient;
    private InfluxDBConfig influxConfig;
    private final Set<String> monitoredExperiments = Set.of("exp1", "exp2");
    private static final String QUERY_TIME_RANGE = "1d"; // Check for mismatches in the past day
    public InFluxDbExperimentRepository(InfluxDBClient influxClient) {
        this.influxClient = influxClient;
    }
    public List<FluxTable> getMismatchCounts(String experimentName, ChronoUnit unit, int timeAmount) {
        String fluxQuery = buildFluxQuery(experimentName, EXPERIMENT_MISMATCH, influxConfig.getBucket(), unit, timeAmount);

        return influxClient.getQueryApi().query(fluxQuery);
    }
    public List<FluxRecord> findMismatchesByExperiment(String exp, ChronoUnit unit, int timeAmount) {
        return List.of();
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
    private String buildFluxQuery(String experimentName, String experimentMetric, String bucketName, ChronoUnit timeUnit, int timeAmount) {
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
                        "  |> filter(fn: (r) => r._measurement == \"" + experimentMetric + "\")\n" +
                        "  |> filter(fn: (r) => %s)\n" +
                        "  |> group(columns: [\""+ experimentName +"\"])\n" +
                        "  |> sum(column: \"_value\")\n" +
                        "  |> yield(name: \"mismatch_count\")",
                bucketName,
                formatDuration(timeUnit, timeAmount),
                filterExperiments
        );
    }

    String formatDuration(ChronoUnit unit, int amount) {
        char prefix = 'd';
        if (unit == ChronoUnit.DAYS) {
            prefix = 'd';
        }
        else if (unit == ChronoUnit.HOURS) {
            prefix = 'h';
        }
        else if(unit == ChronoUnit.MINUTES) {
            prefix = 'm';
        } else if(unit == ChronoUnit.SECONDS) {
            prefix = 's';
        } else {
                throw new IllegalArgumentException("Unsupported ChronoUnit: " + unit);
        }

        return String.valueOf(prefix) + amount;
    }
}
