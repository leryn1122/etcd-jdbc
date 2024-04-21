package io.github.leryn.etcd.kubernetes;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.common.collect.ImmutableMap;
import io.github.leryn.etcd.calcite.table.KubernetesNativeResourceTable;

public abstract class APIResources {

  public static final Map<String, APIResource> KUBERNETES_API_RESOURCES;

  static {
    try (InputStream ins = KubernetesNativeResourceTable.class.getClassLoader().getResourceAsStream("api-resources.json")) {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
      objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
      objectMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);

      ObjectReader objectReader = objectMapper.readerForListOf(APIResource.class);
      List<APIResource> apiResources = objectReader.readValue(ins);

      ImmutableMap.Builder<String, APIResource> builder = ImmutableMap.builder();
      apiResources.forEach(r -> builder.put(r.getPlural(), r));
      KUBERNETES_API_RESOURCES = builder.build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private APIResources() {}
}
