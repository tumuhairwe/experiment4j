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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Behaviour every {@link ExperimentStateStore} must have. Extend this class and
 * implement {@link #newStore()} to run it against an implementation; each test
 * gets a new, empty store.
 */
public abstract class ExperimentStateStoreContract {

  protected static final Instant T0 = Instant.parse("2026-11-01T00:00:00Z");

  protected ExperimentStateStore store;

  /** A new, empty store. */
  protected abstract ExperimentStateStore newStore();

  @BeforeEach
  void createStore() {
    store = newStore();
  }

  private ExperimentState created(String name) {
    return store.createIfAbsent(ExperimentState.initial(name, 0, T0));
  }

  private ExperimentState rampTo(ExperimentState current, int percent) {
    ExperimentState next = current.withStatus(ExperimentStatus.RAMPING, percent, current.updatedAt().plusSeconds(60));
    return store.update(current, next,
        StateTransition.between(current, next, TransitionReason.AUTO_RAMP_UP, "ramp-up", null));
  }

  @Test
  @DisplayName("an unknown experiment has no state and no transitions")
  void unknownExperiment() {
    assertTrue(store.find("unknown").isEmpty());
    assertEquals(List.of(), store.transitions("unknown"));
  }

  @Test
  @DisplayName("createIfAbsent stores the initial state at version 0")
  void createIfAbsentStoresVersionZero() {
    ExperimentState initial = new ExperimentState("exp", ExperimentStatus.SHADOW, 5, 0, T0, false, 7, T0);

    ExperimentState stored = store.createIfAbsent(initial);

    assertEquals(0, stored.version());
    assertEquals(ExperimentStatus.SHADOW, stored.status());
    assertEquals(5, stored.trafficPercent());
    assertEquals(stored, store.find("exp").orElseThrow());
  }

  @Test
  @DisplayName("createIfAbsent leaves an existing state unchanged")
  void createIfAbsentKeepsExisting() {
    ExperimentState ramped = rampTo(created("exp"), 10);

    ExperimentState again = store.createIfAbsent(ExperimentState.initial("exp", 0, T0.plusSeconds(3600)));

    assertEquals(ramped, again);
    assertEquals(ramped, store.find("exp").orElseThrow());
  }

  @Test
  @DisplayName("update stores the next state at the next version and records the transition")
  void updateStoresNextVersionAndTransition() {
    ExperimentState current = created("exp");
    ExperimentState next = current.withStatus(ExperimentStatus.SHADOW, 5, T0.plusSeconds(60));
    StateTransition transition = StateTransition.between(current, next, TransitionReason.MANUAL, "alice", "start");

    ExperimentState stored = store.update(current, next, transition);

    assertEquals(1, stored.version());
    assertEquals(ExperimentStatus.SHADOW, stored.status());
    assertEquals(5, stored.trafficPercent());
    assertEquals(T0.plusSeconds(60), stored.updatedAt());
    assertEquals(stored, store.find("exp").orElseThrow());
    assertEquals(List.of(transition), store.transitions("exp"));
  }

  @Test
  @DisplayName("transitions are returned oldest first")
  void transitionsInOrder() {
    ExperimentState v1 = rampTo(created("exp"), 10);
    ExperimentState v2 = rampTo(v1, 20);
    rampTo(v2, 40);

    List<Integer> percents = store.transitions("exp").stream().map(StateTransition::toTrafficPercent).toList();

    assertEquals(List.of(10, 20, 40), percents);
    assertEquals(3, store.find("exp").orElseThrow().version());
  }

  @Test
  @DisplayName("update from a stale version fails and changes nothing")
  void staleUpdateFails() {
    ExperimentState v0 = created("exp");
    ExperimentState v1 = rampTo(v0, 10);

    assertThrows(StaleStateException.class, () -> rampTo(v0, 50));

    assertEquals(v1, store.find("exp").orElseThrow());
    assertEquals(1, store.transitions("exp").size());
  }

  @Test
  @DisplayName("update of an experiment without state fails")
  void updateUnknownFails() {
    ExperimentState current = ExperimentState.initial("unknown", 0, T0);

    assertThrows(IllegalStateException.class, () -> rampTo(current, 10));
    assertTrue(store.find("unknown").isEmpty());
  }

  @Test
  @DisplayName("update rejects states and transitions for different experiments")
  void updateRejectsMixedExperiments() {
    ExperimentState a = created("a");
    ExperimentState b = created("b");
    ExperimentState nextA = a.withStatus(ExperimentStatus.SHADOW, 5, T0.plusSeconds(60));
    ExperimentState nextB = b.withStatus(ExperimentStatus.SHADOW, 5, T0.plusSeconds(60));

    assertThrows(IllegalArgumentException.class, () -> store.update(a, nextB,
        StateTransition.between(b, nextB, TransitionReason.MANUAL, "alice", null)));
    assertThrows(IllegalArgumentException.class, () -> store.update(a, nextA,
        StateTransition.between(b, nextB, TransitionReason.MANUAL, "alice", null)));
    assertEquals(a, store.find("a").orElseThrow());
    assertEquals(List.of(), store.transitions("a"));
  }

  @Test
  @DisplayName("experiments are stored independently")
  void experimentsAreIndependent() {
    rampTo(created("a"), 10);
    ExperimentState b = created("b");

    assertEquals(0, store.find("b").orElseThrow().version());
    assertEquals(b, store.find("b").orElseThrow());
    assertEquals(List.of(), store.transitions("b"));
    assertEquals(1, store.transitions("a").size());
  }

  @Test
  @DisplayName("concurrent writers never lose an update")
  void concurrentUpdatesAreNotLost() throws Exception {
    created("exp");
    int writers = 8;
    int updatesEach = 25;
    ExecutorService pool = Executors.newFixedThreadPool(writers);
    List<Future<?>> futures = new ArrayList<>();
    for (int w = 0; w < writers; w++) {
      futures.add(pool.submit(() -> {
        for (int done = 0; done < updatesEach; ) {
          ExperimentState current = store.find("exp").orElseThrow();
          ExperimentState next = current.withStep(current.currentStep() + 1, current.trafficPercent(),
              current.updatedAt().plusMillis(1));
          try {
            store.update(current, next,
                StateTransition.between(current, next, TransitionReason.AUTO_RAMP_UP, "writer", null));
            done++;
          } catch (StaleStateException retry) {
            // another writer won: re-read and try again
          }
        }
        return null;
      }));
    }
    for (Future<?> f : futures) {
      f.get(30, TimeUnit.SECONDS);
    }
    pool.shutdown();

    ExperimentState last = store.find("exp").orElseThrow();
    assertEquals(writers * updatesEach, last.version());
    assertEquals(writers * updatesEach, last.currentStep());
    assertEquals(writers * updatesEach, store.transitions("exp").size());
  }
}
