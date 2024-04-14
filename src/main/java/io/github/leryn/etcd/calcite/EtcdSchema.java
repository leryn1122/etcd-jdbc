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
import io.etcd.jetcd.Client;
import io.github.leryn.etcd.EtcdConfiguration;
import io.github.leryn.etcd.calcite.table.AbstractEtcdTable;
import io.github.leryn.etcd.calcite.table.EtcdStatusTable;
import org.apache.calcite.schema.Function;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

public class EtcdSchema extends AbstractSchema implements Schema {

  protected final ObjectMapper objectMapper;
  private final Cache<EtcdConfiguration, Client> cacheClients = CacheBuilder.newBuilder()
    .maximumSize(64)
    .removalListener(new RemovalListener<EtcdConfiguration, Client>() {
      @Override
      public void onRemoval(RemovalNotification<EtcdConfiguration, Client> notification) {
        notification.getValue().close();
        ;
      }
    })
    .build();
  private EtcdConfiguration configuration;

  public EtcdSchema(EtcdConfiguration configuration) {
    this.configuration = configuration;
    ObjectMapper objectMapper = new ObjectMapper();
    this.objectMapper = objectMapper;
  }

  @Override
  protected Map<String, Table> getTableMap() {
    Map<String, AbstractEtcdTable> tableMap = new HashMap<>();
    for (AbstractEtcdTable table : getBuiltinTables()) {
      tableMap.put(table.getTableName(), table);
    }
    return ImmutableMap.copyOf(tableMap);
  }

  @Override
  protected Multimap<String, Function> getFunctionMultimap() {
    return super.getFunctionMultimap();
  }

  private Client getClient(EtcdConfiguration configuration) {
    try {
      return cacheClients.get(configuration, new Callable<Client>() {
        @Override
        public Client call() throws Exception {
          return Client.builder()
            .endpoints(configuration.getEndpoints().toArray(new String[0]))
            .build();
        }
      });
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  protected Client getClient() {
    return getClient(this.configuration);
  }

  protected ObjectMapper getObjectMapper() {
    return this.objectMapper;
  }

  private Collection<AbstractEtcdTable> getBuiltinTables() {
    return Set.of(new EtcdStatusTable(getClient(this.configuration)));
  }
}
