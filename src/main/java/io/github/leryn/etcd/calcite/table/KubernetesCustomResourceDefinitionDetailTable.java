package io.github.leryn.etcd.calcite.table;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.etcd.jetcd.Client;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.github.leryn.etcd.support.StringUtils;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql.type.SqlTypeName;

public final class KubernetesCustomResourceDefinitionDetailTable extends AbstractKubernetesTable
  implements Table, ScannableTable {

  /**
   * Kubernetes CRD name
   */
  private final String name;

  /**
   * Table name: {@code customresourcedefinition.domain.com} into {@code CustomResourceDefinition_DomainCom}
   */
  private final String tableName;

  /**
   * True if the CRD is namespaced.
   */
  private final boolean namespaced;

  public KubernetesCustomResourceDefinitionDetailTable(Client client, ObjectMapper objectMapper, CustomResourceDefinition customResourceDefinition) {
    super(client, objectMapper);
    this.name = customResourceDefinition.getSpec().getNames().getPlural();
    this.tableName = customResourceDefinition.getSpec().getNames().getKind() + "_"
      + StringUtils.toUpperCamelCase(customResourceDefinition.getSpec().getGroup());
    this.namespaced = "Namespaced".equals(customResourceDefinition.getSpec().getScope());
  }

  @Override
  public String getTableName() {
    return this.tableName;
  }

  @Override
  public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
    return relDataTypeFactory.createStructType(
      ImmutableMap.ofEntries(
        Map.entry("Name", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)),
        Map.entry("Namespace", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)),
        Map.entry("Revision", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT)),
        Map.entry("CreateRevision", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT)),
        Map.entry("ModifyRevision", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT)),
        Map.entry("Size", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT)),
        Map.entry("Value", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR))
      ).entrySet().asList()
    );
  }

  @Override
  public Enumerable<Object[]> scan(DataContext root) {
    // TODO:
    return Linq4j.emptyEnumerable();
  }

  @Override
  protected void initKubernetesResource() {
  }
}
