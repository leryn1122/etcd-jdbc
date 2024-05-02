package io.github.leryn.etcd.calcite.table;

import io.github.leryn.etcd.EtcdMetadataTable;
import io.github.leryn.etcd.EtcdTransport;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.impl.AbstractTable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Etcd table for those data fetched from Etcd entries
 * which holds an Etcd client instance.
 */
abstract class AbstractEtcdMetadataTable extends AbstractTable implements EtcdMetadataTable {

  protected Logger log = LoggerFactory.getLogger(EtcdMetadataTable.class);

  /**
   * Table name;
   */
  private @NotNull final String tableName;

  /**
   * Etcd client
   */
  private @NotNull final EtcdTransport transport;

  AbstractEtcdMetadataTable(@NotNull String tableName, @NotNull EtcdTransport transport) {
    this.tableName = tableName;
    this.transport = transport;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull String getTableName() {
    return this.tableName;
  }

  /**
   * JDBC table type is always a table.
   *
   * @return {@link  Schema.TableType#TABLE}.
   */
  @Override
  public Schema.TableType getJdbcTableType() {
    return Schema.TableType.TABLE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract RelDataType getRowType(RelDataTypeFactory relDataTypeFactory);

  public @NotNull final EtcdTransport getTransport() {
    return this.transport;
  }
}
