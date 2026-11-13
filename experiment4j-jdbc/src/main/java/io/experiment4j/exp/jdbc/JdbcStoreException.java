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

import java.sql.SQLException;

/** A database error from one of the JDBC stores; the {@link SQLException} is the cause. */
public class JdbcStoreException extends RuntimeException {

  public JdbcStoreException(String message, SQLException cause) {
    super(message + ": " + cause.getMessage(), cause);
  }
}
