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

package io.experiment4j.exp.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Behaviour every {@link ExperimentDefinitionStore} must have. Extend this class
 * and implement {@link #newStore()} to run it against an implementation; each
 * test gets a new, empty store.
 */
public abstract class ExperimentDefinitionStoreContract {

  protected ExperimentDefinitionStore store;

  /** A new, empty store. */
  protected abstract ExperimentDefinitionStore newStore();

  @BeforeEach
  void createStore() {
    store = newStore();
  }

  @Test
  @DisplayName("an empty store finds nothing")
  void emptyStore() {
    assertTrue(store.find("unknown").isEmpty());
    assertTrue(store.find(null).isEmpty());
    assertEquals(List.of(), store.findAll());
  }

  @Test
  @DisplayName("a saved definition can be found by name with all its fields")
  void saveAndFind() {
    ExperimentDefinition definition = new ExperimentDefinition("checkout-pricing", "new pricing service",
        "payments-team", Instant.parse("2026-11-01T00:00:00Z"), Instant.parse("2026-12-01T00:00:00Z"), 1, 50);

    assertEquals(definition, store.save(definition));
    assertEquals(definition, store.find("checkout-pricing").orElseThrow());
  }

  @Test
  @DisplayName("saving a definition with an existing name replaces it")
  void saveReplaces() {
    store.save(ExperimentDefinition.named("exp"));
    ExperimentDefinition changed = new ExperimentDefinition("exp", "changed", null, null, null, 5, 25);

    store.save(changed);

    assertEquals(changed, store.find("exp").orElseThrow());
    assertEquals(List.of(changed), store.findAll());
  }

  @Test
  @DisplayName("findAll returns every definition ordered by name")
  void findAllOrderedByName() {
    store.save(ExperimentDefinition.named("charlie"));
    store.save(ExperimentDefinition.named("alpha"));
    store.save(ExperimentDefinition.named("bravo"));

    List<String> names = store.findAll().stream().map(ExperimentDefinition::name).toList();

    assertEquals(List.of("alpha", "bravo", "charlie"), names);
  }
}
