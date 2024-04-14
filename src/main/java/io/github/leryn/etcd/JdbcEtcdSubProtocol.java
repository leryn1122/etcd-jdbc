package io.github.leryn.etcd;

import java.util.Optional;

import lombok.Getter;

@Getter
public enum JdbcEtcdSubProtocol {
  ETCD("jdbc:etcd:"),
  ETCD_CLUSTER("jdbc:etcd-cluster:");

  private final String scheme;

  JdbcEtcdSubProtocol(String scheme) {
    this.scheme = scheme;
  }

  public static Optional<JdbcEtcdSubProtocol> fromUrl(String url) {
    for (JdbcEtcdSubProtocol protocol : JdbcEtcdSubProtocol.values()) {
      if (url.startsWith(protocol.getScheme())) {
        return Optional.of(protocol);
      }
    }
    return Optional.empty();
  }

}
