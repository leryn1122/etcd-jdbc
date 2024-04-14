package io.github.leryn.etcd.calcite.table;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition;
import io.github.leryn.etcd.Constants;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.type.SqlTypeName;

public final class KubernetesCustomResourceDefinitionTable extends KubernetesNativeResourceTable {

  public KubernetesCustomResourceDefinitionTable(Client client, ObjectMapper objectMapper) {
    super("CustomResourceDefinitions", client, objectMapper);
  }

  @Override
  public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
    return relDataTypeFactory.createStructType(
      ImmutableMap.ofEntries(
        Map.entry("Name", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)),
        Map.entry("APIVersion", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)),
        Map.entry("Kind", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)),
        Map.entry("Plural", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)),
        Map.entry("Namespaced", relDataTypeFactory.createSqlType(SqlTypeName.BOOLEAN)),
        Map.entry("ShortNames", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR))
      ).entrySet().asList()
    );
  }

  @Override
  protected void initKubernetesResource() {
    // It is intended to be blank.
  }

  @Override
  public Enumerable<Object[]> scan(DataContext context) {
    try {
      KV kvClient = getClient().getKVClient();
      ByteSequence prefix = ByteSequence.from(Constants.CRD_DEFINITIONS_KEY_PREFIX, StandardCharsets.UTF_8);
      GetResponse response = kvClient.get(prefix, GetOption.builder()
        .isPrefix(true)
        .build()).get(10, TimeUnit.SECONDS);
      List<KeyValue> kvs = response.getKvs();

      Collection<Object[]> results = new ArrayList<>(kvs.size());
      for (KeyValue kv : kvs) {
        CustomResourceDefinition customResourceDefinition = getObjectMapper().readValue(kv.getValue().getBytes(), CustomResourceDefinition.class);
        results.add(new Object[] {
          /* Name        */customResourceDefinition.getSpec().getNames().getPlural(),
          /* APIVersion  */customResourceDefinition.getSpec().getGroup() + "/" + customResourceDefinition.getSpec().getVersion(),
          /* Kind        */customResourceDefinition.getSpec().getNames().getKind(),
          /* Plural      */customResourceDefinition.getSpec().getNames().getPlural(),
          /* Namespaced  */"Namespaced".equals(customResourceDefinition.getSpec().getScope()),
          /* ShortNames  */Joiner.on(",").join(customResourceDefinition.getSpec().getNames().getShortNames())
        });
      }
      return Linq4j.asEnumerable(results);
    } catch (InterruptedException | ExecutionException | TimeoutException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}
