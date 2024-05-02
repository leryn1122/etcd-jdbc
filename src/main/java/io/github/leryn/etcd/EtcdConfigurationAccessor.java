package io.github.leryn.etcd;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.ClientBuilder;
import io.github.leryn.etcd.support.StringUtils;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public class EtcdConfigurationAccessor {

  public static EtcdConfiguration fromProperties(Properties properties) {
    EtcdConfiguration.Builder builder = EtcdConfiguration.builder();
    builder.withEndpoints(
      Arrays.stream(Objects.requireNonNull(properties.getProperty("endpoints")).split(","))
        .map(endpoint -> "ip://" + endpoint)
        .toList()
    );
    builder.withKeepaliveTimeout(
      Long.parseLong(Optional.ofNullable(properties.getProperty("keepaliveTimeout")).orElse("30000000"))
    );
    builder.withKeepaliveTime(
      Long.parseLong(Optional.ofNullable(properties.getProperty("keepaliveTime")).orElse("30000000"))
    );
    builder.withConnectTimeout(
      Long.parseLong(Optional.ofNullable(properties.getProperty("timeout")).orElse("30000000"))
    );
    builder.withMaxInboundMessageSize(
      Integer.parseInt(Optional.ofNullable(properties.getProperty("maxInboundMessageSize")).orElse("8388608"))
    );
    builder.withCACert(properties.getProperty("cacert"));
    builder.withCert(properties.getProperty("cert"));
    builder.withKey(properties.getProperty("key"));
    return builder.build();
  }

  public static EtcdTransport toTransport(EtcdConfiguration configuration) throws Exception {
    ClientBuilder builder = Client.builder()
      .endpoints(configuration.getEndpoints().toArray(new String[0]))
      .keepaliveTimeout(Duration.ofMillis(configuration.getKeepaliveTimeout()))
      .keepaliveTime(Duration.ofMillis(configuration.getKeepaliveTime()))
      .connectTimeout(Duration.ofMillis(configuration.getConnectTimeout()))
      .maxInboundMessageSize(configuration.getMaxInboundMessageSize());

    if (!StringUtils.isEmpty(configuration.getCert())) {
      SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
      SslContext sslContext = sslContextBuilder.build();
      builder.sslContext(sslContext);
    }

    Client client = builder.build();

    ObjectMapper objectMapper = new ObjectMapper();

    return new EtcdTransport(client, objectMapper, 100);
  }
}
