package io.github.leryn.etcd;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtcdConfiguration {
  @NotNull
  private List<String> endpoints;
  private long connectTimeout;
  private long keepaliveTime;
  private long keepaliveTimeout;
  private int maxInboundMessageSize;

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
