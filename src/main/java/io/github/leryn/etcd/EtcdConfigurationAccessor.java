package io.github.leryn.etcd;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import io.etcd.jetcd.Client;

public class EtcdConfigurationAccessor {

  public static EtcdConfiguration fromProperties(Properties properties) {
    EtcdConfiguration configuration = new EtcdConfiguration();
    configuration.setEndpoints(
      Arrays.stream(Objects.requireNonNull(properties.getProperty("endpoints")).split(","))
        .map(endpoint -> "ip://" + endpoint)
        .toList()
    );
    configuration.setKeepaliveTimeout(
      Long.parseLong(Optional.ofNullable(properties.getProperty("keepaliveTimeout")).orElse("30000"))
    );
    configuration.setKeepaliveTime(
      Long.parseLong(Optional.ofNullable(properties.getProperty("keepaliveTime")).orElse("30000"))
    );
    configuration.setConnectTimeout(
      Long.parseLong(Optional.ofNullable(properties.getProperty("timeout")).orElse("30000"))
    );
    configuration.setMaxInboundMessageSize(
      Integer.parseInt(Optional.ofNullable(properties.getProperty("maxInboundMessageSize")).orElse("8388608"))
    );
    return configuration;
  }

  public static Client toClient(EtcdConfiguration configuration) {
    return Client.builder()
      .endpoints(configuration.getEndpoints().toArray(new String[0]))
      .keepaliveTimeout(Duration.ofMillis(configuration.getKeepaliveTimeout()))
      .keepaliveTime(Duration.ofMillis(configuration.getKeepaliveTime()))
      .connectTimeout(Duration.ofMillis(configuration.getConnectTimeout()))
      .maxInboundMessageSize(configuration.getMaxInboundMessageSize())
      .build();
  }

}
