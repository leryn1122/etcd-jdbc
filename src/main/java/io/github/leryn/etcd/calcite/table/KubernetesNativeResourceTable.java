package io.github.leryn.etcd.calcite.table;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.etcd.jetcd.Client;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql.type.SqlTypeName;

/**
 * Table for Kubernetes native resource.
 * <p>
 */
public class KubernetesNativeResourceTable extends AbstractKubernetesTable
  implements Table, ScannableTable {

  private final String name;

  public KubernetesNativeResourceTable(String name, Client client, ObjectMapper objectMapper) {
    super(client, objectMapper);
    this.name = name;
  }

  @Override
  public String getTableName()  {
    return name;
  }

  @Override
  public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
    return relDataTypeFactory.createStructType(
      ImmutableMap.ofEntries(
        Map.entry("Name", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)),
        Map.entry("Namespace", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)),
        Map.entry("Version", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT)),
        Map.entry("CreateRevision", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT)),
        Map.entry("ModifyRevision", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT)),
        Map.entry("Size", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT)),
        Map.entry("Value", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT))
      ).entrySet().asList()
    );
  }

  protected String getName() {
    return this.name;
  }

  @Override
  protected void initKubernetesResource() throws Exception {
  }

  @Override
  public Enumerable<Object[]> scan(DataContext context) {
    // TODO
    return Linq4j.emptyEnumerable();
  }
}
