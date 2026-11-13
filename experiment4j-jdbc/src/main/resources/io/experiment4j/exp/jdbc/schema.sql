-- experiment4j JDBC schema.
-- Portable SQL, tested with H2 (default and PostgreSQL modes); intended for
-- PostgreSQL. Copy it into your migrations (Flyway, Liquibase, ...) or run it
-- with io.experiment4j.exp.jdbc.JdbcSchema.createTables(dataSource).
-- Timestamps are stored in UTC with microsecond precision.

CREATE TABLE IF NOT EXISTS experiment_definition (
    name                    VARCHAR(255)  NOT NULL PRIMARY KEY,
    description             VARCHAR(2000),
    owner_name              VARCHAR(255),
    active_from             TIMESTAMP(6),
    active_until            TIMESTAMP(6),
    initial_traffic_percent INTEGER       NOT NULL,
    max_traffic_percent     INTEGER       NOT NULL
);

CREATE TABLE IF NOT EXISTS experiment_state (
    experiment_name VARCHAR(255) NOT NULL PRIMARY KEY,
    status          VARCHAR(32)  NOT NULL,
    traffic_percent INTEGER      NOT NULL,
    current_step    INTEGER      NOT NULL,
    step_entered_at TIMESTAMP(6) NOT NULL,
    killed          BOOLEAN      NOT NULL,
    version         BIGINT       NOT NULL,
    updated_at      TIMESTAMP(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS experiment_state_transition (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    experiment_name      VARCHAR(255)  NOT NULL,
    transitioned_at      TIMESTAMP(6)  NOT NULL,
    from_status          VARCHAR(32)   NOT NULL,
    to_status            VARCHAR(32)   NOT NULL,
    from_traffic_percent INTEGER       NOT NULL,
    to_traffic_percent   INTEGER       NOT NULL,
    reason               VARCHAR(32)   NOT NULL,
    actor                VARCHAR(255)  NOT NULL,
    detail               VARCHAR(2000)
);

CREATE INDEX IF NOT EXISTS ix_state_transition_experiment
    ON experiment_state_transition (experiment_name, id);
