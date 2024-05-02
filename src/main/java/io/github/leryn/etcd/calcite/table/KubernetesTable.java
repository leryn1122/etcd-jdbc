package io.github.leryn.etcd.calcite.table;

import io.github.leryn.etcd.NamedTable;

/**
 * Kubernetes table interface.
 */
public interface KubernetesTable<T> extends NamedTable {

  /**
   * True if the resource is namespaced.
   */
  boolean isNamespaced();
}
