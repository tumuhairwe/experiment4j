package com.ticketmaster.exp.publish;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.ticketmaster.exp.MatchType;
import com.ticketmaster.exp.Publisher;
import com.ticketmaster.exp.Result;
import com.ticketmaster.exp.influx.InfluxDBConfig;

public class InfluxDbPublisher<K> implements Publisher<K> {
    private final InfluxDBClient client;
    private final InfluxDBConfig config;

    public InfluxDbPublisher(InfluxDBClient client, InfluxDBConfig config) {
        this.client = client;
        this.config = config;
    }

    @Override
    public void accept(MatchType matchType, Result<K> kResult) {
        publish(matchType, kResult);
    }

    /**
     * Publishes experiment results to InfluxDB asynchronously.
     * @param matchType The type of match (e.g., MATCH, MISMATCH).
     * Schema: table = matchType [MATCH, MISMATCH, etc.]
     *         tag:
     *            experiment_name: String
     *         fields:
     *            is_match: Boolean
     *            <candidate_duration_key>: Long (duration of candidate trial)
     *            <control_duration_key>: Long (duration of control trial)
     *            timestamp: Instant
     */
    @Override
    public void publish(MatchType matchType, Result<K> payload) {
        String candidateDurationKey = PatternDurationNamer.DEFAULT.name(payload.getExperimentName(), payload.getCandidateResult().getTrialType());
        String controlDurationKey = PatternDurationNamer.DEFAULT.name(payload.getExperimentName(), payload.getControlResult().getTrialType());

        //String tableName = matchType.name().toLowerCase();
        String tableName = matchType.isMismatch() ? InfluxDBConfig.EXPERIMENT_MISMATCH_MEASUREMENT : InfluxDBConfig.EXPERIMENT_MATCH_MEASUREMENT;
        Point point = Point.measurement(tableName)
                .addTag("experiment_name", payload.getExperimentName())
                .addField("is_match", matchType.isMismatch())
                .addField(candidateDurationKey, payload.getCandidateResult().getDuration().toMillis())
                .addField(controlDurationKey, payload.getControlResult().getDuration().toMillis())
                .time(payload.getTimestamp(), WritePrecision.MS);

        WriteApiBlocking writeApi = client.getWriteApiBlocking();
        writeApi.writePoint(config.getBucket(), config.getOrg(), point);
    }
    /**
     * Publishes a single mismatch event to InfluxDB asynchronously.
     *
     * @param experimentName The name of the experiment that mismatched.
     */
//    public void publishMismatch(String experimentName) {
//        try {
//            // Create a data point
//            Point point = Point
//                    .measurement(InfluxDBConfig.EXPERIMENT_MISMATCH_MEASUREMENT) // The InfluxDB "table" or measurement name
//                    .addTag("experimentName", experimentName) // A tag for filtering (e.g., WHERE experimentName = '...')
//                    .addField("count", 1)
//                    .time(Instant.now(), WritePrecision.MS);
//
//        } catch (Exception e) {
//            System.err.println("Failed to create InfluxDB data point: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
}
