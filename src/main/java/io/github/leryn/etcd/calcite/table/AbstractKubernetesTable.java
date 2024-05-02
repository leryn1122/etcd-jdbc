package io.github.leryn.etcd.calcite.table;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import io.github.leryn.etcd.Constants;
import io.github.leryn.etcd.EtcdTransport;
import io.github.leryn.etcd.exceptions.RuntimeSQLException;
import io.github.leryn.etcd.support.StringUtils;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Kubernetes table.
 */
abstract class AbstractKubernetesTable<T> extends AbstractQueryableEtcdEntryTable<T> implements KubernetesTable<T> {

  protected static final Logger log = LoggerFactory.getLogger(KubernetesTable.class);

  private final Object lock = new Object();

  private volatile boolean initialized = false;

  AbstractKubernetesTable(@NotNull final String tableName,
                          @NotNull final EtcdTransport transport,
                          @NotNull final Class<T> clazz) {
    super(tableName, transport, clazz);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract RelDataType getRowType(RelDataTypeFactory relDataTypeFactory);

  protected RelDataType getRowTypeFromJavaClass(final Class<?> clazz, RelDataTypeFactory relDataTypeFactory) {
    JavaTypeFactory javaTypeFactory = (JavaTypeFactory) relDataTypeFactory;
    return relDataTypeFactory.createStructType(
      ImmutableList.copyOf(
        Arrays.stream(clazz.getDeclaredFields())
          .map(field -> Map.entry(StringUtils.toUpperCamelCase(field.getName()), javaTypeFactory.createType(field.getType())))
          .toList()
      )
    );
  }

  /**
   * Shall be invoked before the query method.
   */
  protected final void lazyInit() {
    if (!this.initialized) {
      synchronized (this.lock) {
        if (!this.initialized) {
          try {
            initKubernetesResource();
            this.initialized = true;
          } catch (Exception e) {
            Supplier<String> message = () -> "Failed to lazy initialize Kubernetes resources.";
            log.error(message.get());
            throw new RuntimeSQLException(message.get(), e);
          }
        }
      }
    }
  }

  protected abstract String etcdKeyPrefix();

  protected Pair<String, @Nullable String> extractNameAndNamespaceFromKey(String key) {
    final int _start = etcdKeyPrefix().length();
    if (isNamespaced()) {
      final int _offset = key.indexOf(Constants.KEY_ENTRY_SEPARATOR, etcdKeyPrefix().length()) + 1;
      return Pair.of(key.substring(_offset), key.substring(_start, _offset - 1));
    } else {
      return Pair.of(key.substring(_start), null);
    }
  }

  /**
   * Initialized the Kubernetes resources through lazy initialized
   * @throws Exception if occurred.
   */
  protected void initKubernetesResource() throws Exception {
    // It is intended to be blank.
  }
}
