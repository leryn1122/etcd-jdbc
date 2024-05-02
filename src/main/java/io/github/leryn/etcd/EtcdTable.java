package io.github.leryn.etcd;

import org.jetbrains.annotations.NotNull;

public interface EtcdTable {

  /**
   * Return the etcd transport.
   * @return Etcd transport.
   */
  @NotNull EtcdTransport getTransport();

}
