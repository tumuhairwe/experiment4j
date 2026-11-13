/*
 * Copyright 2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.experiment4j.exp.jdbc;

import io.experiment4j.exp.store.ExperimentState;
import io.experiment4j.exp.store.ExperimentStateStore;
import io.experiment4j.exp.store.ExperimentStatus;
import io.experiment4j.exp.store.StaleStateException;
import io.experiment4j.exp.store.StateTransition;
import io.experiment4j.exp.store.TransitionReason;
import io.experiment4j.exp.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.experiment4j.exp.jdbc.JdbcSupport.getInstant;
import static io.experiment4j.exp.jdbc.JdbcSupport.isConstraintViolation;
import static io.experiment4j.exp.jdbc.JdbcSupport.rollbackQuietly;
import static io.experiment4j.exp.jdbc.JdbcSupport.setInstant;
import static io.experiment4j.exp.jdbc.JdbcSupport.toStoredPrecision;

/**
 * {@link ExperimentStateStore} backed by the {@code experiment_state} and
 * {@code experiment_state_transition} tables (see {@link JdbcSchema}).
 *
 * <p>{@link #update} runs in one transaction: a conditional
 * {@code UPDATE ... WHERE version = ?} (optimistic locking, so no row locks are
 * held between reading and writing) followed by the transition {@code INSERT}.
 * Uses only standard SQL; each call takes a connection from the
 * {@link DataSource} and returns it before returning.
 */
public class JdbcExperimentStateStore implements ExperimentStateStore {

  private static final String STATE_COLUMNS = "experiment_name, status, traffic_percent, current_step, "
      + "step_entered_at, killed, version, updated_at";
  private static final String SELECT_STATE =
      "SELECT " + STATE_COLUMNS + " FROM experiment_state WHERE experiment_name = ?";
  private static final String INSERT_STATE =
      "INSERT INTO experiment_state (" + STATE_COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
  private static final String UPDATE_STATE = "UPDATE experiment_state SET status = ?, traffic_percent = ?, "
      + "current_step = ?, step_entered_at = ?, killed = ?, version = ?, updated_at = ? "
      + "WHERE experiment_name = ? AND version = ?";
  private static final String TRANSITION_COLUMNS = "experiment_name, transitioned_at, from_status, to_status, "
      + "from_traffic_percent, to_traffic_percent, reason, actor, detail";
  private static final String INSERT_TRANSITION = "INSERT INTO experiment_state_transition ("
      + TRANSITION_COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
  private static final String SELECT_TRANSITIONS = "SELECT " + TRANSITION_COLUMNS
      + " FROM experiment_state_transition WHERE experiment_name = ? ORDER BY id";

  private final DataSource dataSource;

  public JdbcExperimentStateStore(DataSource dataSource) {
    Assert.notNull(dataSource, "dataSource must be non-null");
    this.dataSource = dataSource;
  }

  @Override
  public Optional<ExperimentState> find(String experimentName) {
    try (Connection connection = dataSource.getConnection()) {
      return select(connection, experimentName);
    } catch (SQLException e) {
      throw new JdbcStoreException("cannot read state of experiment '" + experimentName + "'", e);
    }
  }

  @Override
  public ExperimentState createIfAbsent(ExperimentState initial) {
    Assert.notNull(initial, "initial must be non-null");
    ExperimentState state = stored(initial, 0);
    try (Connection connection = dataSource.getConnection()) {
      try (PreparedStatement insert = connection.prepareStatement(INSERT_STATE)) {
        bindState(insert, state);
        insert.executeUpdate();
        return state;
      } catch (SQLException e) {
        if (!isConstraintViolation(e)) {
          throw e;
        }
        // another writer created it first: return what is stored
        return select(connection, initial.experimentName()).orElseThrow(() -> e);
      }
    } catch (SQLException e) {
      throw new JdbcStoreException("cannot create state of experiment '" + initial.experimentName() + "'", e);
    }
  }

  @Override
  public ExperimentState update(ExperimentState current, ExperimentState next, StateTransition transition) {
    Assert.notNull(current, "current must be non-null");
    Assert.notNull(next, "next must be non-null");
    Assert.notNull(transition, "transition must be non-null");
    String name = current.experimentName();
    if (!name.equals(next.experimentName()) || !name.equals(transition.experimentName())) {
      throw new IllegalArgumentException("current state, next state and transition must all be for experiment '"
          + name + "'");
    }
    ExperimentState updated = stored(next, current.version() + 1);
    try (Connection connection = dataSource.getConnection()) {
      boolean autoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      try {
        if (updateIfVersion(connection, updated, current.version()) == 0) {
          ExperimentState actual = select(connection, name).orElse(null);
          connection.rollback();
          if (actual == null) {
            throw new IllegalStateException("experiment '" + name + "' has no state; call createIfAbsent first");
          }
          throw new StaleStateException(name, current.version(), actual.version());
        }
        insertTransition(connection, transition);
        connection.commit();
        return updated;
      } catch (SQLException e) {
        rollbackQuietly(connection, e);
        throw e;
      } finally {
        connection.setAutoCommit(autoCommit);
      }
    } catch (SQLException e) {
      throw new JdbcStoreException("cannot update state of experiment '" + name + "'", e);
    }
  }

  @Override
  public List<StateTransition> transitions(String experimentName) {
    try (Connection connection = dataSource.getConnection();
         PreparedStatement select = connection.prepareStatement(SELECT_TRANSITIONS)) {
      select.setString(1, experimentName);
      try (ResultSet rows = select.executeQuery()) {
        List<StateTransition> transitions = new ArrayList<>();
        while (rows.next()) {
          transitions.add(readTransition(rows));
        }
        return List.copyOf(transitions);
      }
    } catch (SQLException e) {
      throw new JdbcStoreException("cannot read transitions of experiment '" + experimentName + "'", e);
    }
  }

  private static Optional<ExperimentState> select(Connection connection, String name) throws SQLException {
    try (PreparedStatement select = connection.prepareStatement(SELECT_STATE)) {
      select.setString(1, name);
      try (ResultSet rows = select.executeQuery()) {
        return rows.next() ? Optional.of(readState(rows)) : Optional.empty();
      }
    }
  }

  private static int updateIfVersion(Connection connection, ExperimentState s, long expectedVersion)
      throws SQLException {
    try (PreparedStatement update = connection.prepareStatement(UPDATE_STATE)) {
      update.setString(1, s.status().name());
      update.setInt(2, s.trafficPercent());
      update.setInt(3, s.currentStep());
      setInstant(update, 4, s.stepEnteredAt());
      update.setBoolean(5, s.killed());
      update.setLong(6, s.version());
      setInstant(update, 7, s.updatedAt());
      update.setString(8, s.experimentName());
      update.setLong(9, expectedVersion);
      return update.executeUpdate();
    }
  }

  private static void bindState(PreparedStatement insert, ExperimentState s) throws SQLException {
    insert.setString(1, s.experimentName());
    insert.setString(2, s.status().name());
    insert.setInt(3, s.trafficPercent());
    insert.setInt(4, s.currentStep());
    setInstant(insert, 5, s.stepEnteredAt());
    insert.setBoolean(6, s.killed());
    insert.setLong(7, s.version());
    setInstant(insert, 8, s.updatedAt());
  }

  private static void insertTransition(Connection connection, StateTransition t) throws SQLException {
    try (PreparedStatement insert = connection.prepareStatement(INSERT_TRANSITION)) {
      insert.setString(1, t.experimentName());
      setInstant(insert, 2, t.at());
      insert.setString(3, t.fromStatus().name());
      insert.setString(4, t.toStatus().name());
      insert.setInt(5, t.fromTrafficPercent());
      insert.setInt(6, t.toTrafficPercent());
      insert.setString(7, t.reason().name());
      insert.setString(8, t.actor());
      insert.setString(9, t.detail());
      insert.executeUpdate();
    }
  }

  private static ExperimentState readState(ResultSet row) throws SQLException {
    return new ExperimentState(
        row.getString("experiment_name"),
        ExperimentStatus.valueOf(row.getString("status")),
        row.getInt("traffic_percent"),
        row.getInt("current_step"),
        getInstant(row, "step_entered_at"),
        row.getBoolean("killed"),
        row.getLong("version"),
        getInstant(row, "updated_at"));
  }

  private static StateTransition readTransition(ResultSet row) throws SQLException {
    return new StateTransition(
        row.getString("experiment_name"),
        getInstant(row, "transitioned_at"),
        ExperimentStatus.valueOf(row.getString("from_status")),
        ExperimentStatus.valueOf(row.getString("to_status")),
        row.getInt("from_traffic_percent"),
        row.getInt("to_traffic_percent"),
        TransitionReason.valueOf(row.getString("reason")),
        row.getString("actor"),
        row.getString("detail"));
  }

  /** The state as the database stores it: the given version, instants at microsecond precision. */
  private static ExperimentState stored(ExperimentState s, long version) {
    return new ExperimentState(s.experimentName(), s.status(), s.trafficPercent(), s.currentStep(),
        toStoredPrecision(s.stepEnteredAt()), s.killed(), version, toStoredPrecision(s.updatedAt()));
  }
}
