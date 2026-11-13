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

import io.experiment4j.exp.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

/** {@link ExperimentDefinitionStore} that keeps definitions in memory, for one JVM. */
public class InMemoryExperimentDefinitionStore implements ExperimentDefinitionStore {

  private final ConcurrentSkipListMap<String, ExperimentDefinition> definitions = new ConcurrentSkipListMap<>();

  @Override
  public ExperimentDefinition save(ExperimentDefinition definition) {
    Assert.notNull(definition, "definition must be non-null");
    definitions.put(definition.name(), definition);
    return definition;
  }

  @Override
  public Optional<ExperimentDefinition> find(String name) {
    return Optional.ofNullable(name == null ? null : definitions.get(name));
  }

  @Override
  public List<ExperimentDefinition> findAll() {
    return List.copyOf(definitions.values());
  }
}
