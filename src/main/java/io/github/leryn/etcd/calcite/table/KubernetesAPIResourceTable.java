package io.github.leryn.etcd.calcite.table;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.common.collect.ImmutableMap;
import io.etcd.jetcd.Client;
import io.github.leryn.etcd.kubernetes.APIResource;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql.type.SqlTypeName;

/**
 * Table for API resources in Kubernetes cluster, which is the same result of the command:
 * <p>
 * {@code kubectl api-resources -o wide}
 */
public final class KubernetesAPIResourceTable extends AbstractKubernetesTable
  implements Table, ScannableTable {

  public KubernetesAPIResourceTable(Client client, ObjectMapper objectMapper) {
    super(client, objectMapper);
  }

  @Override
  public String getTableName() {
    return "APIResources";
  }

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

  @Override
  protected void initKubernetesResource() throws Exception {
    // It is intended to be blank.
  }

  @Override
  public Enumerable<Object[]> scan(DataContext context) {
    try (InputStream ins = KubernetesNativeResourceTable.class.getClassLoader().getResourceAsStream("api-resources.json")) {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
      objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
      objectMapper.configure(JsonParser.Feature.ALLOW_MISSING_VALUES, true);
      objectMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);

      ObjectReader objectReader = objectMapper.readerForListOf(APIResource.class);
      List<APIResource> resources = objectReader.readValue(ins);

      Collection<Object[]> results = resources.stream().map(r -> new Object[]{
        /* Name        */r.getName(),
        /* APIVersion  */r.getAPIVersion(),
        /* Kind        */r.getPlural(),
        /* Plural      */r.getKind(),
        /* Namespaced  */r.isNamespaced(),
        /* ShortNames  */r.getShortNames()
      }).collect(Collectors.toList());
      return Linq4j.asEnumerable(results);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
