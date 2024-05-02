package io.github.leryn.etcd;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class EtcdConfiguration {
  /** Endpoints to the Etcd database. */
  @NotNull
  private List<String> endpoints;
  /** Connect timeout. */
  private long connectTimeout;
  /** Keepalive time. */
  private long keepaliveTime;
  /** Keepalive timeout. */
  private long keepaliveTimeout;
  /** Max inbound message size. Turn up if gRPC exceeds the max size. */
  private int maxInboundMessageSize;
  /** CA Certificate */
  @Nullable
  private String CACert;
  /** Client certificate */
  @Nullable
  private String cert;
  /** Client key */
  @Nullable
  private String key;

  @Override
  public int hashCode() {
    return endpoints.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof EtcdConfiguration that) {
      return this.endpoints.equals(that.endpoints);
    }
    return false;
  }
}
