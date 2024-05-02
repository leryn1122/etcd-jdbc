package io.github.leryn.etcd;

import io.etcd.jetcd.options.GetOption;
import org.apache.calcite.schema.QueryableTable;
import org.apache.calcite.schema.TranslatableTable;

/**
 * Interface for the table which is composed of Etcd key value
 */
public interface EtcdEntryTable extends EtcdTable, NamedTable, TranslatableTable, QueryableTable {

  <T> Iterable<T> queryByCondition(QueryContext queryContext, GetOption.Builder option) throws Exception;

}
