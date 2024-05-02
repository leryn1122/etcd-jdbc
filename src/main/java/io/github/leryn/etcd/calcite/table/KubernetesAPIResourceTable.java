package io.github.leryn.etcd.calcite.table;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.common.collect.ImmutableMap;
import io.github.leryn.etcd.kubernetes.APIResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.semver4j.Semver;

/**
 * Table for API resources in Kubernetes cluster, which is the same result of the command:
 * <p>
 * {@code kubectl api-resources -o wide}
 */
@Slf4j
public final class KubernetesAPIResourceTable extends AbstractTable
  implements KubernetesTable<APIResource>, ScannableTable {

  private final Semver kubeVersion;

  public KubernetesAPIResourceTable(Semver kubeVersion) {
    this.kubeVersion = kubeVersion;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
    return relDataTypeFactory.createStructType(
      ImmutableMap.ofEntries(
        Map.entry("Name", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)),
        Map.entry("APIVersion", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)),
        Map.entry("Kind", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)),
        Map.entry("Plural", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)),
        Map.entry("Namespaced", relDataTypeFactory.createSqlType(SqlTypeName.BOOLEAN)),
        Map.entry("ShortNames", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR))
      ).entrySet().asList()
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Enumerable<Object[]> scan(DataContext context) {
    ClassLoader classLoader = KubernetesAPIResourceTable.class.getClassLoader();
    String path = "META-INF/kubernetes/v" + this.kubeVersion + "/api-resources.jsonl";
    try (InputStream ins = classLoader.getResourceAsStream(path)) {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
      objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
      objectMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);

      ObjectReader objectReader = objectMapper.readerForListOf(APIResource.class);
      List<APIResource> resources = objectReader.readValue(ins);

      Collection<Object[]> results = resources.stream().map(r -> new Object[]{
        /* Name        */r.getName(),
        /* APIVersion  */r.getAPIVersion(),
        /* Kind        */r.getKind(),
        /* Plural      */r.getPlural(),
        /* Namespaced  */r.isNamespaced(),
        /* ShortNames  */r.getShortNames()
      }).collect(Collectors.toList());
      return Linq4j.asEnumerable(results);
    } catch (IOException e) {
      Supplier<String> message = () ->
        "Failed to get Kubernetes API resources from the driver built-in property file: [api-resources.jsonl]";
      log.error(message.get());
      throw new RuntimeException(message.get(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getTableName() {
    return "APIResources";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isNamespaced() {
    return false;
  }
}
