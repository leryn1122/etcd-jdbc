package io.github.leryn.etcd.calcite.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.options.GetOption;
import io.github.leryn.etcd.Constants;
import io.github.leryn.etcd.EtcdTransport;
import io.github.leryn.etcd.QueryContext;
import io.github.leryn.etcd.annotation.LazyInit;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.jetbrains.annotations.NotNull;

public final class KubernetesCustomResourceDefinitionTable
  extends AbstractKubernetesTable<io.github.leryn.etcd.kubernetes.CustomResourceDefinition> {

  @LazyInit
  private io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition customResourceDefinition;

  public KubernetesCustomResourceDefinitionTable(@NotNull final EtcdTransport transport) {
    super("CustomResourceDefinitions", transport,
      io.github.leryn.etcd.kubernetes.CustomResourceDefinition.class
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
    return getRowTypeFromJavaClass(
      io.github.leryn.etcd.kubernetes.CustomResourceDefinition.class,
      relDataTypeFactory
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String etcdKeyPrefix() {
    return Constants.CRD_DEFINITIONS_KEY_PREFIX;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterable<io.github.leryn.etcd.kubernetes.CustomResourceDefinition>
    queryByCondition(QueryContext queryContext, GetOption.Builder option) throws IOException {
    EtcdTransport transport = getTransport();
    List<KeyValue> kvs = transport.getKeyValues(
      etcdKeyPrefix(),
      option
        .isPrefix(true)
        .build()
    );
    final ObjectMapper objectMapper = transport.getObjectMapper();

    List<io.github.leryn.etcd.kubernetes.CustomResourceDefinition> results = new ArrayList<>(kvs.size());
    for (KeyValue kv : kvs) {
      io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition customResourceDefinition =
        objectMapper.readValue(kv.getValue().getBytes(),
          io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition.class);
      io.github.leryn.etcd.kubernetes.CustomResourceDefinition.Builder builder =
        io.github.leryn.etcd.kubernetes.CustomResourceDefinition.builder();
      io.github.leryn.etcd.kubernetes.CustomResourceDefinition result = builder
        .withName(customResourceDefinition.getSpec().getNames().getPlural())
        .withAPIVersion(customResourceDefinition.getSpec().getGroup() + Constants.KEY_ENTRY_SEPARATOR
          + customResourceDefinition.getSpec().getVersion())
        .withKind(customResourceDefinition.getSpec().getNames().getKind())
        .withPlural(customResourceDefinition.getSpec().getNames().getPlural())
        .withNamespaced("Namespaced".equals(customResourceDefinition.getSpec().getScope()))
        .withShortNames(
          customResourceDefinition.getSpec().getNames().getShortNames().isEmpty()
          ? null
          : Joiner.on(",").join(customResourceDefinition.getSpec().getNames().getShortNames()))
        .build();
      results.add(result);
    }
    return results;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isNamespaced() {
    return false;
  }
}
