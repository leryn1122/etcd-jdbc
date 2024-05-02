package io.github.leryn.etcd.calcite.table;

import io.github.leryn.etcd.EtcdEntry;
import io.github.leryn.etcd.EtcdEntryTable;
import io.github.leryn.etcd.EtcdTransport;
import io.github.leryn.etcd.calcite.rel.EtcdEntryQueryable;
import io.github.leryn.etcd.calcite.rel.EtcdEntryRel;
import io.github.leryn.etcd.calcite.rel.EtcdEntryTableScan;
import org.apache.calcite.adapter.java.AbstractQueryableTable;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.linq4j.Queryable;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Schemas;
import org.jetbrains.annotations.NotNull;

abstract class AbstractQueryableEtcdEntryTable<T> extends AbstractQueryableTable implements EtcdEntryTable {

  private @NotNull final String tableName;

  private @NotNull final EtcdTransport transport;

  private final Class<T> entityClass;

  public AbstractQueryableEtcdEntryTable(
    @NotNull final String tableName, @NotNull final EtcdTransport transport, @NotNull final Class<T> clazz) {
    super(EtcdEntry.class);
    this.tableName = tableName;
    this.transport = transport;
    this.entityClass = clazz;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull String getTableName() {
    return this.tableName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract RelDataType getRowType(RelDataTypeFactory relDataTypeFactory);

  /**
   * {@inheritDoc}
   */
  @Override
  public <R> Queryable<R> asQueryable(QueryProvider queryProvider, SchemaPlus schema, String tableName) {
    return new EtcdEntryQueryable<>(queryProvider, schema, this, this.getTableName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final Expression getExpression(SchemaPlus schema, String tableName, Class clazz) {
    return Schemas.tableExpression(schema, this.elementType, tableName, clazz);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final RelNode toRel(RelOptTable.ToRelContext context, RelOptTable relOptTable) {
    final RelOptCluster cluster = context.getCluster();
    return new EtcdEntryTableScan(cluster, cluster.traitSetOf(EtcdEntryRel.CONVENTION), relOptTable,
      this, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull EtcdTransport getTransport() {
    return this.transport;
  }
}
