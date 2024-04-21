package io.github.leryn.etcd.calcite;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.ImmutableMap;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.github.leryn.etcd.Constants;
import io.github.leryn.etcd.EtcdConfiguration;
import io.github.leryn.etcd.exceptions.RuntimeSQLException;
import io.github.leryn.etcd.calcite.table.AbstractEtcdTable;
import io.github.leryn.etcd.calcite.table.AbstractKubernetesTable;
import io.github.leryn.etcd.calcite.table.KubernetesAPIResourceTable;
import io.github.leryn.etcd.calcite.table.KubernetesCustomResourceDefinitionDetailTable;
import io.github.leryn.etcd.calcite.table.KubernetesCustomResourceDefinitionTable;
import io.github.leryn.etcd.calcite.table.KubernetesNativeResourceTable;
import io.github.leryn.etcd.calcite.table.KubernetesNodeTable;
import io.github.leryn.etcd.kubernetes.APIResources;
import org.apache.calcite.schema.Table;

public class KubernetesSchema extends EtcdSchema {

  public KubernetesSchema(EtcdConfiguration configuration) {
    super(configuration);
  }

  @Override
  protected Map<String, Table> getTableMap() {
    Map<String, AbstractEtcdTable> tableMap = new HashMap<>();
    for (AbstractEtcdTable table : getCRDTable()) {
      tableMap.put(table.getTableName(), table);
    }
    for (AbstractKubernetesTable table : getKubernetesTable()) {
      tableMap.put(table.getTableName(), table);
    }
    return ImmutableMap.copyOf(tableMap);
  }

  private Iterable<AbstractEtcdTable> getCRDTable() {
    KV kvClient = getClient().getKVClient();
    ByteSequence key = ByteSequence.from(Constants.CRD_DEFINITIONS_KEY_PREFIX, StandardCharsets.UTF_8);
    GetResponse response = null;
    try {
      response = kvClient.get(key, GetOption.builder()
        .isPrefix(true)
        .withKeysOnly(true)
        .withLimit(1024)
        .build()).get(10, TimeUnit.SECONDS);

      List<AbstractEtcdTable> results = new ArrayList<>(response.getKvs().size());
      for (KeyValue keyValue : response.getKvs()) {
        String crdName = keyValue.getKey().substring(Constants.CRD_DEFINITIONS_KEY_PREFIX.length()).toString();
        KubernetesCustomResourceDefinitionDetailTable table = new KubernetesCustomResourceDefinitionDetailTable(crdName, getClient(), getObjectMapper());
        results.add(table);
      }

      return results;
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeSQLException(e);
    }
  }

  private Iterable<AbstractKubernetesTable> getKubernetesTable() {
    List<AbstractKubernetesTable> results = new ArrayList<>(1 << 10);
    for (String resource : APIResources.KUBERNETES_API_RESOURCES.keySet()) {
      KubernetesNativeResourceTable table = new KubernetesNativeResourceTable(resource, getClient(), getObjectMapper());
      results.add(table);
    }

    results.add(new KubernetesNodeTable(getClient(), getObjectMapper()));
    results.add(new KubernetesAPIResourceTable(getClient(), getObjectMapper()));
    results.add(new KubernetesCustomResourceDefinitionTable(getClient(), getObjectMapper()));

    return results;
  }
}
