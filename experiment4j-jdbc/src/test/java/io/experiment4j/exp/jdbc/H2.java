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

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.util.UUID;

/** New, empty in-memory H2 databases for tests. */
final class H2 {

  private H2() {
  }

  /** A new, empty database without tables. {@code mode} is an H2 compatibility mode, or null. */
  static JdbcDataSource emptyDatabase(String mode) {
    JdbcDataSource dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1"
        + (mode == null ? "" : ";MODE=" + mode + ";DATABASE_TO_LOWER=TRUE"));
    return dataSource;
  }

  /** A new database with the experiment4j tables. */
  static DataSource withSchema(String mode) {
    DataSource dataSource = emptyDatabase(mode);
    JdbcSchema.createTables(dataSource);
    return dataSource;
  }
}
