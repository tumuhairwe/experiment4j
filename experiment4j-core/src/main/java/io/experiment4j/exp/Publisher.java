/*
 * Copyright 2015 the original author or authors.
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

package io.experiment4j.exp;

import java.util.function.BiConsumer;

/**
 * Receives the outcome of every trial in which both control and candidate ran.
 *
 * <p>Implement {@link #publish}; {@link #accept} is the same operation, provided
 * so a publisher can be used wherever a {@link BiConsumer} is expected.
 */
@FunctionalInterface
public interface Publisher<T> extends BiConsumer<MatchType, Result<T>> {
  void publish(MatchType matchType, Result<T> payload);

  @Override
  default void accept(MatchType matchType, Result<T> payload) {
    publish(matchType, payload);
  }
}
