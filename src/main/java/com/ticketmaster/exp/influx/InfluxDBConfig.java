package com.ticketmaster.exp.influx;

import lombok.Data;

@Data
public class InfluxDBConfig {

    public static final String EXPERIMENT_MISMATCH_MEASUREMENT = "experiment_mismatch";
    public static final String EXPERIMENT_MATCH_MEASUREMENT = "experiment_match";
    private String org = "ticketmaster";
    private String host = "http://localhost:8086";
    private String token = System.getenv("INFLUXDB_TOKEN");
//    private String org = "my-org"; // Your InfluxDB Organization
    private String database = "experimentdb"; // The bucket to write to
    private String bucket = "experimentdb"; // The bucket to write to

    private String password = System.getenv("INFLUXDB_PASSWORD");
    private String username = "experimenter";
}