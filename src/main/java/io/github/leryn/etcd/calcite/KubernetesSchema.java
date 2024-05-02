package io.github.leryn.etcd.calcite;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.leryn.etcd.EtcdConfiguration;
import io.github.leryn.etcd.calcite.table.KubernetesAPIResourceTable;
import io.github.leryn.etcd.calcite.table.KubernetesCustomResourceDefinitionTable;
import io.github.leryn.etcd.calcite.table.KubernetesNativeResourceTable;
import io.github.leryn.etcd.calcite.table.KubernetesTable;
import io.github.leryn.etcd.exceptions.RuntimeSQLException;
import io.github.leryn.etcd.kubernetes.APIResource;
import org.apache.calcite.schema.Table;
import org.jetbrains.annotations.NotNull;
import org.semver4j.Semver;

public final class KubernetesSchema extends EtcdSchema {

  private final Semver kubeVersion;

  public KubernetesSchema(Semver kubeVersion, EtcdConfiguration configuration) {
    super(configuration);
    this.kubeVersion = kubeVersion;
  }

  @Override
  protected Map<String, Table> getTableMap() {
    Map<String, KubernetesTable<?>> tableMap = new HashMap<>();
    for (KubernetesTable<?> table : getKubernetesTable()) {
      tableMap.put(table.getTableName(), table);
    }
    return ImmutableMap.copyOf(tableMap);
  }

  private Iterable<KubernetesTable<?>> getKubernetesTable() {
    List<KubernetesTable<?>> results = new ArrayList<>(1 << 8);
    Iterable<APIResource> resources = readAPIResourcesFromClasspath(this.kubeVersion);
    for (APIResource resource : resources) {
      if ("CustomResourceDefinitions".equals(resource.getPlural())) {
        continue;
      }
      try {
        Class<?> clazz = Class.forName(resource.getJavaProtoParser());
        KubernetesNativeResourceTable<?> table = new KubernetesNativeResourceTable<>(
          resource.getPlural(), getTransport(), clazz, resource);
        results.add(table);
      } catch (ClassNotFoundException ignored) {
        Supplier<String> message = () ->
          "Java Proto parser is not found: " + resource.getAPIVersion() + "/" + resource.getPlural();
        log.error(message.get());
        throw new RuntimeSQLException(message.get());
      }
    }
    results.add(new KubernetesAPIResourceTable(this.kubeVersion, resources));
    results.add(new KubernetesCustomResourceDefinitionTable(getTransport()));
    return results;
  }

  private Iterable<APIResource> readAPIResourcesFromClasspath(@NotNull final Semver kubeVersion) {
    final ClassLoader classLoader = APIResource.class.getClassLoader();
    List<APIResource> resources = null;
    final String path = "META-INF/kubernetes/v" + kubeVersion + "/api-resources.jsonl";
    try (InputStream ins = classLoader.getResourceAsStream(path)) {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
      objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
      objectMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
      ObjectReader objectReader = objectMapper.readerForListOf(APIResource.class);
      resources = objectReader.readValue(ins);
      return ImmutableList.copyOf(resources);
    } catch (IOException e) {
      Supplier<String> message = () ->
        "Failed to read [api-resources.jsonl] under classpath with kube version: " + this.kubeVersion;
      log.error(message.get());
      throw new RuntimeSQLException(message.get(), e);
    }
  }
}
