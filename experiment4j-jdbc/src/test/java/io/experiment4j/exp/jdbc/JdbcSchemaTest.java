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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JdbcSchemaTest {

  @Test
  @DisplayName("the schema script has three tables and one index, without comments")
  void statements() {
    List<String> statements = JdbcSchema.statements();

    assertEquals(4, statements.size());
    assertEquals(3, statements.stream().filter(s -> s.startsWith("CREATE TABLE")).count());
    assertTrue(statements.stream().noneMatch(s -> s.contains("--")));
  }

  @Test
  @DisplayName("createTables creates the tables and can run again safely")
  void createTablesIsIdempotent() throws SQLException {
    DataSource dataSource = H2.emptyDatabase(null);

    JdbcSchema.createTables(dataSource);
    assertDoesNotThrow(() -> JdbcSchema.createTables(dataSource));

    try (Connection connection = dataSource.getConnection();
         ResultSet tables = connection.getMetaData().getTables(null, null, "EXPERIMENT%", new String[] {"TABLE"})) {
      int count = 0;
      while (tables.next()) {
        count++;
      }
      assertEquals(3, count);
    }
  }

  @Test
  @DisplayName("database errors while creating tables are reported as JdbcStoreException")
  void createTablesErrors() {
    JdbcDataSource unreachable = new JdbcDataSource();
    unreachable.setURL("jdbc:h2:mem:readonly;ACCESS_MODE_DATA=r");

    assertThrows(JdbcStoreException.class, () -> JdbcSchema.createTables(unreachable));
  }

  @Test
  @DisplayName("only SQLState class 23 counts as a constraint violation")
  void constraintViolation() {
    assertTrue(JdbcSupport.isConstraintViolation(new SQLException("duplicate", "23505")));
    assertFalse(JdbcSupport.isConstraintViolation(new SQLException("syntax", "42000")));
    assertFalse(JdbcSupport.isConstraintViolation(new SQLException("no state")));
  }

  @Test
  @DisplayName("a failed rollback is kept as a suppressed exception of the original error")
  void rollbackFailureIsSuppressed() throws SQLException {
    Connection connection = mock(Connection.class);
    SQLException rollbackFailure = new SQLException("connection lost");
    doThrow(rollbackFailure).when(connection).rollback();
    SQLException original = new SQLException("insert failed");

    JdbcSupport.rollbackQuietly(connection, original);

    assertEquals(List.of(rollbackFailure), List.of(original.getSuppressed()));
  }

  @Test
  @DisplayName("a null instant is stored and read as SQL NULL")
  void nullInstants() throws SQLException {
    assertEquals(null, JdbcSupport.toStoredPrecision(null));
    ResultSet row = mock(ResultSet.class);
    when(row.getObject("t", java.time.LocalDateTime.class)).thenReturn(null);
    assertEquals(null, JdbcSupport.getInstant(row, "t"));
  }
}
