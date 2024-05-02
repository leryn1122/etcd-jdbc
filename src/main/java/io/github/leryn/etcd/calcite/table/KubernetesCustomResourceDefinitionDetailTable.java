package io.github.leryn.etcd.calcite.table;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.options.GetOption;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.github.leryn.etcd.Constants;
import io.github.leryn.etcd.EtcdEntries;
import io.github.leryn.etcd.EtcdEntry;
import io.github.leryn.etcd.EtcdTransport;
import io.github.leryn.etcd.QueryContext;
import io.github.leryn.etcd.annotation.LazyInit;
import io.github.leryn.etcd.support.Asserts;
import io.github.leryn.etcd.support.StringUtils;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class KubernetesCustomResourceDefinitionDetailTable extends AbstractKubernetesTable<EtcdEntry> {
  /** Kubernetes CRD name. */
  private final String fullName;

  /**  Custom Resource Definition. */
  @LazyInit
  private CustomResourceDefinition customResourceDefinition;

  @LazyInit
  private boolean namespaced;

  /** Table name: {@code customresourcedefinition.domain.com} into {@code CustomResourceDefinitionDomainCom} */
  public KubernetesCustomResourceDefinitionDetailTable(
    @NotNull final String fullName, @NotNull final EtcdTransport transport) {
    super(StringUtils.toUpperCamelCase2(fullName), transport, EtcdEntry.class);
    this.fullName = fullName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
    lazyInit();
    return getRowTypeFromJavaClass(EtcdEntry.class, relDataTypeFactory);
  }

  @Override
  protected String etcdKeyPrefix() {
    String group = this.customResourceDefinition.getSpec().getGroup();
    String plural = this.customResourceDefinition.getSpec().getNames().getPlural();
    return Constants.REGISTRY_PREFIX +
      group +
      Constants.KEY_ENTRY_SEPARATOR +
      plural +
      Constants.KEY_ENTRY_SEPARATOR;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterable<EtcdEntry> queryByCondition(QueryContext queryContext, GetOption.Builder option) {
    lazyInit();
    EtcdTransport transport = this.getTransport();
    List<KeyValue> kvs = transport.getKeyValues(
      etcdKeyPrefix(),
      option
        .isPrefix(true)
        .build()
    );
    List<EtcdEntry> results = new ArrayList<>(kvs.size());
    for (KeyValue kv : kvs) {
      final Pair<String, @Nullable String> pair = extractNameAndNamespaceFromKey(kv.getKey().toString());
      EtcdEntry entry = EtcdEntries.fromKV(pair.left, pair.right, kv);
      entry.setValue(kv.getValue().toString());
      results.add(entry);
    }
    return results;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void initKubernetesResource() throws IOException {
    EtcdTransport transport = getTransport();
    List<KeyValue> kvs = transport.getKeyValues(Constants.CRD_DEFINITIONS_KEY_PREFIX + this.fullName,
      GetOption.builder()
        .build());
    Asserts.isTrue(kvs.size() == 1,
      () -> "KV must be only one unique entry, now it has " + kvs.size());

    KeyValue kv = kvs.get(0);
    ObjectMapper objectMapper = transport.getObjectMapper();
    try (InputStream ins = new BufferedInputStream(new ByteArrayInputStream(kv.getValue().getBytes()))) {
      this.customResourceDefinition = objectMapper.readValue(ins, CustomResourceDefinition.class);
    }
    this.namespaced = "Namespaced".equals(this.customResourceDefinition.getSpec().getScope());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isNamespaced() {
    lazyInit();
    return this.namespaced;
  }
}
