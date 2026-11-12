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

/**
 * Thrown by {@link ExperimentStateStore#update} when the stored state is no
 * longer the version the caller read, because another writer (for example
 * another application instance) changed it first. Re-read the state and
 * decide again.
 */
public class StaleStateException extends RuntimeException {

  public StaleStateException(String experimentName, long expectedVersion, long actualVersion) {
    super("state of experiment '" + experimentName + "' is at version " + actualVersion
        + ", not the expected version " + expectedVersion);
  }
}
