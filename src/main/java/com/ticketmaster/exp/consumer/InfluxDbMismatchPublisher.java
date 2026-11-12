package com.ticketmaster.exp.consumer;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.ticketmaster.exp.influx.InfluxDBConfig;

import java.time.Instant;
import java.util.function.BiConsumer;

public class InfluxDbMismatchPublisher implements BiConsumer<String, Long> {
    private final InfluxDBClient influxClient;
    private final InfluxDBConfig influxConfig;
    public InfluxDbMismatchPublisher(InfluxDBClient influxClient, InfluxDBConfig influxConfig) {
        this.influxClient = influxClient;
        this.influxConfig = influxConfig;
    }
    @Override
    public void accept(String experiment, Long mismatchCount) {
        Point point = Point
                .measurement(experiment)
                .addTag("host", "host1")
                .addField("used_percent", mismatchCount)
                .time(Instant.now(), WritePrecision.NS);
        WriteApiBlocking writeApi = influxClient.getWriteApiBlocking();
        writeApi.writePoint(influxConfig.getBucket(), influxConfig.getOrg(), point);
    }
}
