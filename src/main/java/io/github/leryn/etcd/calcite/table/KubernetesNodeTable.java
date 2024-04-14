package io.github.leryn.etcd.calcite.table;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.etcd.jetcd.Client;
import io.github.leryn.etcd.NamedTable;
import org.apache.calcite.schema.Schema;

/**
 * Alias for Kubernetes node, as it was named minion in the earlier version. However, it is still recorded
 * with the prefix {@code /registry/minions/}.
 */
public final class KubernetesNodeTable extends KubernetesNativeResourceTable implements NamedTable {

  public KubernetesNodeTable(Client client, ObjectMapper objectMapper) {
    super("nodes", client, objectMapper);
  }

  @Override
  public String getTableName() {
    return getName();
  }

  @Override
  public Schema.TableType getJdbcTableType() {
    return Schema.TableType.ALIAS;
  }
}
