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

/**
 * Persistence interfaces (SPI) for experiment state, with in-memory defaults.
 *
 * <p>Three kinds of state are kept apart because they change at different rates:
 * <ul>
 *   <li>{@link io.experiment4j.exp.store.ExperimentDefinition}: how an experiment is
 *       configured; changes rarely.</li>
 *   <li>{@link io.experiment4j.exp.store.ExperimentState}: where its rollout stands
 *       (status, traffic percentage, ramp step, kill switch); changes only on a
 *       transition, each recorded as a {@link io.experiment4j.exp.store.StateTransition}.</li>
 *   <li>Run results and metrics: added in a later release.</li>
 * </ul>
 *
 * <p>The in-memory stores keep state for one JVM only. Implementations in
 * {@code experiment4j-jpa} and {@code experiment4j-jdbc} share it across instances
 * and restarts.
 */
package io.experiment4j.exp.store;
