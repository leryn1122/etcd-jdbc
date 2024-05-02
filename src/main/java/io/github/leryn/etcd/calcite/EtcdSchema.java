package io.github.leryn.etcd.calcite;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import io.github.leryn.etcd.EtcdConfiguration;
import io.github.leryn.etcd.EtcdConfigurationAccessor;
import io.github.leryn.etcd.EtcdMetadataTable;
import io.github.leryn.etcd.EtcdTransport;
import io.github.leryn.etcd.calcite.table.EtcdRoleTable;
import io.github.leryn.etcd.calcite.table.EtcdStatusTable;
import io.github.leryn.etcd.calcite.table.EtcdUserTable;
import io.github.leryn.etcd.exceptions.RuntimeSQLException;
import org.apache.calcite.schema.Function;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public sealed class EtcdSchema extends AbstractSchema implements Schema
  permits KubernetesSchema, KubernetesCrdSchema {

  protected static final Logger log = LoggerFactory.getLogger(Schema.class);

  protected final ObjectMapper objectMapper;
  private final Cache<EtcdConfiguration, EtcdTransport> cacheClients = CacheBuilder.newBuilder()
    .maximumSize(64)
    .removalListener(new RemovalListener<EtcdConfiguration, EtcdTransport>() {
      @Override
      public void onRemoval(@NotNull RemovalNotification<EtcdConfiguration, EtcdTransport> notification) {
        notification.getValue().close();
      }
    })
    .build();

  private final EtcdConfiguration configuration;

  public EtcdSchema(EtcdConfiguration configuration) {
    this.configuration = configuration;
      this.objectMapper = new ObjectMapper();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Map<String, Table> getTableMap() {
    Map<String, EtcdMetadataTable> tableMap = new HashMap<>();
    for (EtcdMetadataTable table : getBuiltinTables()) {
      tableMap.put(table.getTableName(), table);
    }
    return ImmutableMap.copyOf(tableMap);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Multimap<String, Function> getFunctionMultimap() {
    return super.getFunctionMultimap();
  }

  private EtcdTransport getTransport(EtcdConfiguration configuration) {
    try {
      return this.cacheClients.get(configuration, new Callable<EtcdTransport>() {
        @Override
        public EtcdTransport call() throws Exception {
          return EtcdConfigurationAccessor.toTransport(configuration);
        }
      });
    } catch (ExecutionException e) {
      throw new RuntimeSQLException(e);
    }
  }

  protected EtcdTransport getTransport() {
    return getTransport(this.configuration);
  }

  private Collection<EtcdMetadataTable> getBuiltinTables() {
    final EtcdTransport transport = getTransport(this.configuration);
    return Set.of(
      new EtcdStatusTable(transport),
      new EtcdUserTable(transport),
      new EtcdRoleTable(transport)
    );
  }
}
