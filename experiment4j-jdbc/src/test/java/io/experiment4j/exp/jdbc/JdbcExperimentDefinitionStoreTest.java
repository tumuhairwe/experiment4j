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

import io.experiment4j.exp.store.ExperimentDefinition;
import io.experiment4j.exp.store.ExperimentDefinitionStore;
import io.experiment4j.exp.store.ExperimentDefinitionStoreContract;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

public class JdbcExperimentDefinitionStoreTest extends ExperimentDefinitionStoreContract {

  @Override
  protected ExperimentDefinitionStore newStore() {
    return new JdbcExperimentDefinitionStore(H2.withSchema(null));
  }

  /** The same contract against H2 in PostgreSQL compatibility mode. */
  @Nested
  class PostgreSqlMode extends ExperimentDefinitionStoreContract {
    @Override
    protected ExperimentDefinitionStore newStore() {
      return new JdbcExperimentDefinitionStore(H2.withSchema("PostgreSQL"));
    }
  }

  @Test
  @DisplayName("window instants are stored with microsecond precision")
  void microsecondPrecision() {
    ExperimentDefinition definition = new ExperimentDefinition("exp", null, null,
        Instant.parse("2026-11-01T00:00:00.000000999Z"), null, 0, 100);

    ExperimentDefinition saved = store.save(definition);

    assertEquals(Instant.parse("2026-11-01T00:00:00Z"), saved.activeFrom());
    assertEquals(saved, store.find("exp").orElseThrow());
  }

  @Test
  @DisplayName("concurrent saves of a new name all succeed and leave one definition")
  void concurrentSavesOfNewName() throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(8);
    List<Future<ExperimentDefinition>> saves = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      int percent = i;
      saves.add(pool.submit(() -> store.save(new ExperimentDefinition("exp", null, null, null, null, percent, 100))));
    }
    for (Future<ExperimentDefinition> save : saves) {
      save.get(30, TimeUnit.SECONDS);
    }
    pool.shutdown();

    assertEquals(1, store.findAll().size());
  }

  @Test
  @DisplayName("database errors are reported as JdbcStoreException with the SQLException as cause")
  void databaseErrorsAreWrapped() {
    ExperimentDefinitionStore noTables = new JdbcExperimentDefinitionStore(H2.emptyDatabase(null));

    for (Runnable call : List.<Runnable>of(
        () -> noTables.save(ExperimentDefinition.named("exp")),
        () -> noTables.find("exp"),
        noTables::findAll)) {
      JdbcStoreException thrown = assertThrows(JdbcStoreException.class, call::run);
      assertInstanceOf(java.sql.SQLException.class, thrown.getCause());
    }
  }

  @Test
  @DisplayName("a DataSource is required")
  void requiresDataSource() {
    assertThrows(IllegalArgumentException.class, () -> new JdbcExperimentDefinitionStore(null));
  }
}
