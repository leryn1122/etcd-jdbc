package io.github.leryn.etcd;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.options.GetOption;
import io.github.leryn.etcd.exceptions.RuntimeSQLException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * A simple REST client with Etcd client API.
 */
@Slf4j
public final class EtcdTransport implements AutoCloseable {
  /** Jackson JSON (de)serializer. */
  private @NotNull final ObjectMapper objectMapper;
  /** Jetcd client. */
  private @NotNull final Client client;
  /** Default fetch size. */
  private final int fetchSize;

  EtcdTransport(@NotNull final Client client, @NotNull final ObjectMapper objectMapper, final int fetchSize) {
    this.client = Objects.requireNonNull(client);
    this.objectMapper = Objects.requireNonNull(objectMapper);
    this.fetchSize = fetchSize;
  }

  public List<KeyValue> getKeyValues(@NotNull final String key) {
    return getKeyValues(key, GetOption.DEFAULT);
  }

  public List<KeyValue> getKeyValues(@NotNull final String key, GetOption option) {
    try {
      ByteSequence prefix = ByteSequence.from(key, StandardCharsets.UTF_8);
      KV kvClient = getClient().getKVClient();
      return kvClient.get(prefix, option).get(10, TimeUnit.SECONDS).getKvs();
    } catch (ExecutionException | InterruptedException | TimeoutException e) {
      Supplier<String> message = () ->
        "Failed to fetch keys from etcd server: " + e.getMessage();
      log.error(message.get());
      throw new RuntimeSQLException(message.get(), e);
    }
  }

  public @NotNull Client getClient() {
    return this.client;
  }

  public @NotNull ObjectMapper getObjectMapper() {
    return this.objectMapper;
  }

  @Override
  public void close() {
    this.client.close();
  }
}
