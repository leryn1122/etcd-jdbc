package io.github.leryn.etcd;

import io.etcd.jetcd.KeyValue;

public abstract class EtcdEntries {

  public static EtcdEntry fromKV(String name, KeyValue kv) {
    EtcdEntry.Builder builder = EtcdEntry.builder();
    builder
      .withName(name)
      .withVersion(kv.getVersion())
      .withCreateRevision(kv.getCreateRevision())
      .withModifyRevision(kv.getModRevision())
      .withSize(kv.getValue().size());
    return builder.build();
  }

  public static EtcdEntry fromKV(String name, String namespace, KeyValue kv) {
    EtcdEntry entry = fromKV(name, kv);
    entry.setNamespace(namespace);
    return entry;
  }

  private EtcdEntries() {}

}
