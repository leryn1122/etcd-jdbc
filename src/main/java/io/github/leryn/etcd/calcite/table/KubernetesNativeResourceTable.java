package io.github.leryn.etcd.calcite.table;

import java.util.ArrayList;
import java.util.List;

import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.options.GetOption;
import io.github.leryn.etcd.Constants;
import io.github.leryn.etcd.EtcdEntries;
import io.github.leryn.etcd.EtcdEntry;
import io.github.leryn.etcd.EtcdTransport;
import io.github.leryn.etcd.QueryContext;
import io.github.leryn.etcd.annotation.LazyInit;
import io.github.leryn.etcd.kubernetes.APIResource;
import io.github.leryn.etcd.support.StringUtils;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Table for Kubernetes native resource.
 * <p>
 */
public class KubernetesNativeResourceTable<T> extends AbstractKubernetesTable<T> {

  private final APIResource resource;

  @LazyInit
  private boolean namespaced;

  public KubernetesNativeResourceTable(
    @NotNull final String name,
    @NotNull final EtcdTransport transport,
    @NotNull final Class<T> clazz,
    @NotNull final APIResource resource
    ) {
    super(name, transport, clazz);
    this.resource = resource;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
    return getRowTypeFromJavaClass(EtcdEntry.class, relDataTypeFactory);
  }

  @Override
  protected String etcdKeyPrefix() {
    if (!StringUtils.isEmpty(this.resource.getEtcdAliasKey())) {
      return Constants.REGISTRY_PREFIX + this.resource.getEtcdAliasKey() + Constants.KEY_ENTRY_SEPARATOR;
    } else {
      return Constants.REGISTRY_PREFIX + this.resource.getName() + Constants.KEY_ENTRY_SEPARATOR;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterable<EtcdEntry> queryByCondition(QueryContext queryContext, GetOption.Builder option) {
    EtcdTransport transport = getTransport();
    List<KeyValue> kvs = transport.getKeyValues(
      etcdKeyPrefix(),
      option
        .isPrefix(true)
        .build()
    );

    List<EtcdEntry> results = new ArrayList<>(kvs.size());

    for (KeyValue kv : kvs) {
//      Object invoke = null;
//      try {
//        Class<?> clazz = (Class<?>) super.elementType;
//        Method method = clazz.getMethod("parseFrom", byte[].class);
//        invoke = method.invoke(clazz, kv.getValue().getBytes());
//      } catch (ReflectiveOperationException e) {
//        throw new RuntimeSQLException(e);
//      }
      final Pair<String, @Nullable String> pair = extractNameAndNamespaceFromKey(kv.getKey().toString());
      EtcdEntry entry = EtcdEntries.fromKV(pair.left, pair.right, kv);
      results.add(entry);
    }
    return results;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isNamespaced() {
    return this.resource.isNamespaced();
  }
}
