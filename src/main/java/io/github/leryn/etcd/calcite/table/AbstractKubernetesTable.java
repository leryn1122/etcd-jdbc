package io.github.leryn.etcd.calcite.table;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.etcd.jetcd.Client;
import io.github.leryn.etcd.exceptions.RuntimeSQLException;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;

/**
 * Abstract Kubernetes table.
 */
public abstract class AbstractKubernetesTable extends AbstractEtcdTable {

  private final Object lock = new Object();

  private volatile boolean initialized = false;

  public AbstractKubernetesTable(Client client, ObjectMapper objectMapper) {
    super(client, objectMapper);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract String getTableName();

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract RelDataType getRowType(RelDataTypeFactory relDataTypeFactory);

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
            log.error("Failed to lazy initialize Kubernetes resources.");
            throw new RuntimeSQLException("Failed to lazy initialize Kubernetes resources.", e);
          }
        }
      }
    }
  }

  protected abstract void initKubernetesResource() throws Exception;
}
