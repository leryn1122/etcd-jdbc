package io.github.leryn.etcd.calcite.table;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.github.leryn.etcd.Constants;
import io.github.leryn.etcd.exceptions.RuntimeSQLException;
import io.github.leryn.etcd.annotation.LazyInit;
import io.github.leryn.etcd.support.StringUtils;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql.type.SqlTypeName;

public final class KubernetesCustomResourceDefinitionDetailTable extends AbstractKubernetesTable
  implements Table, ScannableTable {

  /**
   * Kubernetes CRD name
   */
  private final String fullName;

  /**
   * Table name: {@code customresourcedefinition.domain.com} into {@code CustomResourceDefinition_DomainCom}
   */
  private final String tableName;

  /**
   * Custom Resource Definition.
   */
  @LazyInit
  private CustomResourceDefinition customResourceDefinition;

  public KubernetesCustomResourceDefinitionDetailTable(String fullName, Client client, ObjectMapper objectMapper) {
    super(client, objectMapper);
    this.fullName = fullName;
    this.tableName = StringUtils.toUpperCamelCase(fullName);
  }

  @Override
  public String getTableName() {
    return this.tableName;
  }

  @Override
  public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
    lazyInit();
    return relDataTypeFactory.createStructType(
      ImmutableMap.ofEntries(
        Map.entry("Name", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)),
        Map.entry("Namespace", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)),
        Map.entry("Version", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT)),
        Map.entry("CreateRevision", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT)),
        Map.entry("ModifyRevision", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT)),
        Map.entry("Size", relDataTypeFactory.createSqlType(SqlTypeName.INTEGER)),
        Map.entry("Value", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR))
      ).entrySet().asList()
    );
  }

  @Override
  public Enumerable<Object[]> scan(DataContext root) {
    lazyInit();
    String group = this.customResourceDefinition.getSpec().getGroup();
    String plural = this.customResourceDefinition.getSpec().getNames().getPlural();

    try {
      KV kvClient = getClient().getKVClient();
      ByteSequence prefix = ByteSequence.from(
        new StringBuilder()
          .append(Constants.REGISTRY_PREFIX)
          .append(group)
          .append(Constants.KEY_ENTRY_SEPARATOR)
          .append(plural)
          .append(Constants.KEY_ENTRY_SEPARATOR)
          .toString(),
        StandardCharsets.UTF_8
      );
      List<KeyValue> kvs = kvClient.get(prefix, GetOption.builder()
        .isPrefix(true)
        .build()).get(10, TimeUnit.SECONDS).getKvs();

      List<Object[]> results = new ArrayList<>(kvs.size());
      final int offset = isNamespaced()
        ? Constants.REGISTRY_PREFIX_LENGTH + group.length() + plural.length() + 2
        : Constants.REGISTRY_PREFIX_LENGTH + group.length() + plural.length() + 1;
      int index = offset;

      for (KeyValue kv : kvs) {
        String key = kv.getKey().toString();
        String namespace = "N/A";

        if (isNamespaced()) {
          index = key.indexOf(Constants.KEY_ENTRY_SEPARATOR, offset);
          namespace = key.substring(offset, index);
        }

        results.add(new Object[] {
          /*        */key.substring(index + 1),
          /*        */namespace,
          /*        */kv.getVersion(),
          /*        */kv.getCreateRevision(),
          /*        */kv.getModRevision(),
          /*        */kv.getValue().size(),
          /*        */new String(kv.getValue().getBytes()),
        });
      }
      return Linq4j.asEnumerable(results);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeSQLException(e);
    }
  }

  @Override
  protected void initKubernetesResource() throws ExecutionException, InterruptedException, TimeoutException, IOException {
    KV kvClient = getClient().getKVClient();
    ByteSequence key = ByteSequence.from(Constants.CRD_DEFINITIONS_KEY_PREFIX + this.fullName, StandardCharsets.UTF_8);
    GetResponse response = kvClient.get(key, GetOption.builder().withLimit(1).build()).get(10, TimeUnit.SECONDS);
    List<KeyValue> kvs = response.getKvs();
    if (kvs.size() != 1) {
      throw new RuntimeSQLException("KV must be only one unique entry.");
    } else {
      KeyValue kv = kvs.get(0);
      ObjectMapper objectMapper = getObjectMapper();
      try (InputStream ins = new BufferedInputStream(new ByteArrayInputStream(kv.getValue().getBytes()))) {
        this.customResourceDefinition = objectMapper.readValue(ins, CustomResourceDefinition.class);
      }
    }
  }

  private boolean isNamespaced() {
    lazyInit();
    return "Namespaced".equals(this.customResourceDefinition.getSpec().getScope());
  }
}
