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
import io.experiment4j.exp.store.ExperimentStateStoreContract;
import io.experiment4j.exp.store.ExperimentStatus;
import io.experiment4j.exp.store.StateTransition;
import io.experiment4j.exp.store.TransitionReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdbcExperimentStateStoreTest extends ExperimentStateStoreContract {

  private DataSource dataSource;

  @Override
  protected ExperimentStateStore newStore() {
    dataSource = H2.withSchema(null);
    return new JdbcExperimentStateStore(dataSource);
  }

  /** The same contract against H2 in PostgreSQL compatibility mode. */
  @Nested
  class PostgreSqlMode extends ExperimentStateStoreContract {
    @Override
    protected ExperimentStateStore newStore() {
      return new JdbcExperimentStateStore(H2.withSchema("PostgreSQL"));
    }
  }

  @Test
  @DisplayName("instants are stored and returned with microsecond precision")
  void microsecondPrecision() {
    Instant withNanos = Instant.parse("2026-11-01T10:15:30.123456789Z");

    ExperimentState created = store.createIfAbsent(ExperimentState.initial("exp", 0, withNanos));

    Instant micros = Instant.parse("2026-11-01T10:15:30.123456Z");
    assertEquals(micros, created.updatedAt());
    assertEquals(micros, created.stepEnteredAt());
    assertEquals(created, store.find("exp").orElseThrow());
  }

  @Test
  @DisplayName("concurrent createIfAbsent calls all get the same stored state")
  void concurrentCreateIfAbsent() throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(8);
    List<Future<ExperimentState>> results = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      int percent = i;
      results.add(pool.submit(() -> store.createIfAbsent(ExperimentState.initial("exp", percent, T0))));
    }
    ExperimentState stored = store.find("exp").orElseThrow();
    for (Future<ExperimentState> result : results) {
      assertEquals(stored, result.get(30, TimeUnit.SECONDS));
    }
    pool.shutdown();
  }

  @Test
  @DisplayName("if the transition can't be written, the state update is rolled back")
  void updateIsAtomic() throws Exception {
    ExperimentState current = store.createIfAbsent(ExperimentState.initial("exp", 0, T0));
    try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
      statement.execute("DROP TABLE experiment_state_transition");
    }
    ExperimentState next = current.withStatus(ExperimentStatus.SHADOW, 5, T0.plusSeconds(60));

    JdbcStoreException thrown = assertThrows(JdbcStoreException.class, () -> store.update(current, next,
        StateTransition.between(current, next, TransitionReason.MANUAL, "alice", null)));

    assertTrue(thrown.getMessage().contains("'exp'"), thrown.getMessage());
    assertEquals(current, store.find("exp").orElseThrow());
  }

  @Test
  @DisplayName("database errors are reported as JdbcStoreException with the SQLException as cause")
  void databaseErrorsAreWrapped() {
    ExperimentStateStore noTables = new JdbcExperimentStateStore(H2.emptyDatabase(null));
    ExperimentState state = ExperimentState.initial("exp", 0, T0);
    ExperimentState next = state.withStatus(ExperimentStatus.SHADOW, 5, T0);
    StateTransition transition = StateTransition.between(state, next, TransitionReason.MANUAL, "alice", null);

    for (Runnable call : List.<Runnable>of(
        () -> noTables.find("exp"),
        () -> noTables.createIfAbsent(state),
        () -> noTables.update(state, next, transition),
        () -> noTables.transitions("exp"))) {
      JdbcStoreException thrown = assertThrows(JdbcStoreException.class, call::run);
      assertInstanceOf(java.sql.SQLException.class, thrown.getCause());
    }
  }

  @Test
  @DisplayName("a DataSource is required")
  void requiresDataSource() {
    assertThrows(IllegalArgumentException.class, () -> new JdbcExperimentStateStore(null));
  }
}
