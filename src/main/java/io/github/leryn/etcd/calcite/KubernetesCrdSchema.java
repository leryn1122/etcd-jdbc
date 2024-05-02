package io.github.leryn.etcd.calcite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.options.GetOption;
import io.github.leryn.etcd.Constants;
import io.github.leryn.etcd.EtcdConfiguration;
import io.github.leryn.etcd.EtcdTransport;
import io.github.leryn.etcd.calcite.table.KubernetesCustomResourceDefinitionDetailTable;
import io.github.leryn.etcd.calcite.table.KubernetesTable;
import org.apache.calcite.schema.Table;

public final class KubernetesCrdSchema extends EtcdSchema {

  public KubernetesCrdSchema(EtcdConfiguration configuration) {
    super(configuration);
  }

  @Override
  protected Map<String, Table> getTableMap() {
    Map<String, KubernetesTable<?>> tableMap = new HashMap<>();
    for (KubernetesTable<?> table : getCRDTable()) {
      tableMap.put(table.getTableName(), table);
    }
    return ImmutableMap.copyOf(tableMap);
  }

  private Iterable<KubernetesTable<?>> getCRDTable() {
    EtcdTransport transport = getTransport();
    List<KeyValue> kvs = transport.getKeyValues(Constants.CRD_DEFINITIONS_KEY_PREFIX, GetOption.builder()
      .isPrefix(true)
      .withKeysOnly(true)
      .build());
    List<KubernetesTable<?>> results = new ArrayList<>(kvs.size());
    for (KeyValue kv : kvs) {
      String crdName = kv.getKey().substring(Constants.CRD_DEFINITIONS_KEY_PREFIX.length()).toString();
      KubernetesCustomResourceDefinitionDetailTable table = new KubernetesCustomResourceDefinitionDetailTable(crdName, getTransport());
      results.add(table);
    }
    return results;
  }
}
