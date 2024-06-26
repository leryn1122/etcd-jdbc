package io.github.leryn.etcd;

import org.apache.calcite.schema.Table;

/**
 * A named table.
 */
public interface NamedTable extends Table {

  /**
   * Get table name in the schema.
   *
   * @return Table name.
   */
  String getTableName();

}
