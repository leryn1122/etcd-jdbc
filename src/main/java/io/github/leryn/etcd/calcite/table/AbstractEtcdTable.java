package io.github.leryn.etcd.calcite.table;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.etcd.jetcd.Client;
import io.github.leryn.etcd.NamedTable;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.impl.AbstractTable;

/**
 * Abstract Etcd table for those data fetched from Etcd entries
 * which holds an Etcd client instance.
 */
public abstract class AbstractEtcdTable extends AbstractTable implements NamedTable {

  /**
   * Etcd client
   */
  private final Client client;

  /**
   * Jackson object mapper
   */
  private final ObjectMapper objectMapper;

  public AbstractEtcdTable(Client client, ObjectMapper objectMapper) {
    this.client = client;
    this.objectMapper = objectMapper;
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

  protected Client getClient() {
    return this.client;
  }

  protected ObjectMapper getObjectMapper() {
    return this.objectMapper;
  }

}
