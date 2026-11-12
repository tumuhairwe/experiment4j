package com.ticketmaster.exp.influx;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;

/**
 * Creates and manages a singleton instance of the InfluxDB client.
 * This ensures we don't create multiple connection pools.
 */
public class InfluxClientFactory implements AutoCloseable {

    private static final String HOST_URL = "http://localhost:8086"; // Or your cloud URL
    private static final String DATABASE = "my-database"; // InfluxDB v3 uses 'database' instead of 'bucket' and 'org'
    private static final String TOKEN = "my-access-token";

    private static InfluxDBClient client;

    /**
     * Gets a singleton instance of the InfluxDB client.
     *
     * @param config The configuration to use for initialization.
     * @return A thread-safe InfluxDBClient.
     */
    public static InfluxDBClient getClient(InfluxDBConfig config) throws Exception {
        if (client != null) {
            return client;
        }
        synchronized (InfluxClientFactory.class) {
            if (client == null) {
                client = InfluxDBClientFactory.create(config.getHost(), config.getToken().toCharArray(), config.getOrg());
            }
        }
        return client;
    }

    /**
     * Closes the client connection.
     * Should be called on application shutdown.
     */
    public void close() throws Exception {
        if (client != null) {
            client.close();
        }
    }
}