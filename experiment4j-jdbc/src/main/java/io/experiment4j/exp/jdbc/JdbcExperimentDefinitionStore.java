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

import io.experiment4j.exp.store.ExperimentDefinition;
import io.experiment4j.exp.store.ExperimentDefinitionStore;
import io.experiment4j.exp.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.experiment4j.exp.jdbc.JdbcSupport.getInstant;
import static io.experiment4j.exp.jdbc.JdbcSupport.isConstraintViolation;
import static io.experiment4j.exp.jdbc.JdbcSupport.setInstant;
import static io.experiment4j.exp.jdbc.JdbcSupport.toStoredPrecision;

/**
 * {@link ExperimentDefinitionStore} backed by the {@code experiment_definition}
 * table (see {@link JdbcSchema}). Uses only standard SQL; each call takes a
 * connection from the {@link DataSource} and returns it before returning.
 */
public class JdbcExperimentDefinitionStore implements ExperimentDefinitionStore {

  private static final String COLUMNS =
      "name, description, owner_name, active_from, active_until, initial_traffic_percent, max_traffic_percent";
  private static final String UPDATE = "UPDATE experiment_definition SET description = ?, owner_name = ?, "
      + "active_from = ?, active_until = ?, initial_traffic_percent = ?, max_traffic_percent = ? WHERE name = ?";
  private static final String INSERT = "INSERT INTO experiment_definition (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?)";

  private final DataSource dataSource;

  public JdbcExperimentDefinitionStore(DataSource dataSource) {
    Assert.notNull(dataSource, "dataSource must be non-null");
    this.dataSource = dataSource;
  }

  @Override
  public ExperimentDefinition save(ExperimentDefinition definition) {
    Assert.notNull(definition, "definition must be non-null");
    try (Connection connection = dataSource.getConnection()) {
      // update first; insert if missing; if another writer inserted it meanwhile, update again
      if (update(connection, definition) == 0) {
        try {
          insert(connection, definition);
        } catch (SQLException e) {
          if (!isConstraintViolation(e) || update(connection, definition) == 0) {
            throw e;
          }
        }
      }
      return stored(definition);
    } catch (SQLException e) {
      throw new JdbcStoreException("cannot save experiment definition '" + definition.name() + "'", e);
    }
  }

  @Override
  public Optional<ExperimentDefinition> find(String name) {
    if (name == null) {
      return Optional.empty();
    }
    try (Connection connection = dataSource.getConnection();
         PreparedStatement select = connection.prepareStatement(
             "SELECT " + COLUMNS + " FROM experiment_definition WHERE name = ?")) {
      select.setString(1, name);
      try (ResultSet rows = select.executeQuery()) {
        return rows.next() ? Optional.of(read(rows)) : Optional.empty();
      }
    } catch (SQLException e) {
      throw new JdbcStoreException("cannot read experiment definition '" + name + "'", e);
    }
  }

  @Override
  public List<ExperimentDefinition> findAll() {
    try (Connection connection = dataSource.getConnection();
         PreparedStatement select = connection.prepareStatement(
             "SELECT " + COLUMNS + " FROM experiment_definition ORDER BY name");
         ResultSet rows = select.executeQuery()) {
      List<ExperimentDefinition> definitions = new ArrayList<>();
      while (rows.next()) {
        definitions.add(read(rows));
      }
      return List.copyOf(definitions);
    } catch (SQLException e) {
      throw new JdbcStoreException("cannot read experiment definitions", e);
    }
  }

  private static int update(Connection connection, ExperimentDefinition d) throws SQLException {
    try (PreparedStatement update = connection.prepareStatement(UPDATE)) {
      update.setString(1, d.description());
      update.setString(2, d.owner());
      setInstant(update, 3, d.activeFrom());
      setInstant(update, 4, d.activeUntil());
      update.setInt(5, d.initialTrafficPercent());
      update.setInt(6, d.maxTrafficPercent());
      update.setString(7, d.name());
      return update.executeUpdate();
    }
  }

  private static void insert(Connection connection, ExperimentDefinition d) throws SQLException {
    try (PreparedStatement insert = connection.prepareStatement(INSERT)) {
      insert.setString(1, d.name());
      insert.setString(2, d.description());
      insert.setString(3, d.owner());
      setInstant(insert, 4, d.activeFrom());
      setInstant(insert, 5, d.activeUntil());
      insert.setInt(6, d.initialTrafficPercent());
      insert.setInt(7, d.maxTrafficPercent());
      insert.executeUpdate();
    }
  }

  private static ExperimentDefinition read(ResultSet row) throws SQLException {
    return new ExperimentDefinition(
        row.getString("name"),
        row.getString("description"),
        row.getString("owner_name"),
        getInstant(row, "active_from"),
        getInstant(row, "active_until"),
        row.getInt("initial_traffic_percent"),
        row.getInt("max_traffic_percent"));
  }

  /** The definition as the database stores it (instants at microsecond precision). */
  private static ExperimentDefinition stored(ExperimentDefinition d) {
    return new ExperimentDefinition(d.name(), d.description(), d.owner(),
        toStoredPrecision(d.activeFrom()), toStoredPrecision(d.activeUntil()),
        d.initialTrafficPercent(), d.maxTrafficPercent());
  }
}
