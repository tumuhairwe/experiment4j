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

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The tables the JDBC stores need. In production, copy {@link #SCHEMA_RESOURCE}
 * into your own database migrations; {@link #createTables} is a convenience for
 * development and tests. It is safe to run more than once.
 */
public final class JdbcSchema {

  /** Classpath location of the schema script. */
  public static final String SCHEMA_RESOURCE = "/io/experiment4j/exp/jdbc/schema.sql";

  private JdbcSchema() {
  }

  /** The SQL statements of the schema script, without comments. */
  public static List<String> statements() {
    String script;
    try (InputStream in = JdbcSchema.class.getResourceAsStream(SCHEMA_RESOURCE)) {
      script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("cannot read " + SCHEMA_RESOURCE, e);
    }
    String withoutComments = script.lines()
        .filter(line -> !line.strip().startsWith("--"))
        .collect(Collectors.joining("\n"));
    return Arrays.stream(withoutComments.split(";"))
        .map(String::strip)
        .filter(statement -> !statement.isEmpty())
        .toList();
  }

  /** Creates the tables and indexes if they don't exist yet. */
  public static void createTables(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement()) {
      for (String sql : statements()) {
        statement.execute(sql);
      }
    } catch (SQLException e) {
      throw new JdbcStoreException("cannot create experiment4j tables", e);
    }
  }
}
