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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/** Conversions shared by the JDBC stores. */
final class JdbcSupport {

  private JdbcSupport() {
  }

  /** Instants are stored with microsecond precision, the finest most databases keep. */
  static Instant toStoredPrecision(Instant instant) {
    return instant == null ? null : instant.truncatedTo(ChronoUnit.MICROS);
  }

  /** Writes an instant to a TIMESTAMP column as UTC; null writes SQL NULL. */
  static void setInstant(PreparedStatement statement, int index, Instant instant) throws SQLException {
    statement.setObject(index, instant == null ? null : LocalDateTime.ofInstant(toStoredPrecision(instant), ZoneOffset.UTC));
  }

  /** Reads a UTC TIMESTAMP column as an instant; SQL NULL reads as null. */
  static Instant getInstant(ResultSet resultSet, String column) throws SQLException {
    LocalDateTime value = resultSet.getObject(column, LocalDateTime.class);
    return value == null ? null : value.toInstant(ZoneOffset.UTC);
  }

  /** Whether the error is an integrity-constraint violation, e.g. a duplicate primary key (SQLState class 23). */
  static boolean isConstraintViolation(SQLException e) {
    return e.getSQLState() != null && e.getSQLState().startsWith("23");
  }

  /** Rolls back, keeping the original error if the rollback fails too. */
  static void rollbackQuietly(java.sql.Connection connection, SQLException original) {
    try {
      connection.rollback();
    } catch (SQLException rollbackFailure) {
      original.addSuppressed(rollbackFailure);
    }
  }
}
