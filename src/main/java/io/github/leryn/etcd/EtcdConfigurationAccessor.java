package io.github.leryn.etcd;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

public class EtcdConfigurationAccessor {

  public static EtcdConfiguration fromProperties(Properties properties) {
    EtcdConfiguration configuration = new EtcdConfiguration();
    configuration.setEndpoints(
      Arrays.stream(properties.getProperty("endpoints").split(","))
        .map(endpoint -> "ip://" + endpoint)
        .toList()
    );
    configuration.setKeepaliveTimeout(
      Long.valueOf(Optional.ofNullable(properties.getProperty("keepaliveTimeout")).orElse("0"))
    );
    configuration.setKeepaliveTime(
      Long.valueOf(Optional.ofNullable(properties.getProperty("keepaliveTime")).orElse("0"))
    );
    configuration.setTimeout(
      Long.valueOf(Optional.ofNullable(properties.getProperty("timeout")).orElse("0"))
    );

    configuration.setMaxInboundMessageSize(
      Integer.valueOf(Optional.ofNullable(properties.getProperty("maxInboundMessageSize")).orElse("0"))
    );

    return configuration;
  }

}
