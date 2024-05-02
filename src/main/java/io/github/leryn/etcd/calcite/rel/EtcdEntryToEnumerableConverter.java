package io.github.leryn.etcd.calcite.rel;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.leryn.etcd.QueryContext;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRelImplementor;
import org.apache.calcite.adapter.enumerable.JavaRowFormat;
import org.apache.calcite.adapter.enumerable.PhysType;
import org.apache.calcite.adapter.enumerable.PhysTypeImpl;
import org.apache.calcite.linq4j.tree.BlockBuilder;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.linq4j.tree.MethodCallExpression;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterImpl;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.util.BuiltInMethod;
import org.apache.calcite.util.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EtcdEntryToEnumerableConverter extends ConverterImpl implements EtcdEntryRel, EnumerableRel {

  EtcdEntryToEnumerableConverter(RelOptCluster cluster, RelTraitSet traits, RelNode child) {
    super(cluster, ConventionTraitDef.INSTANCE, traits, child);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RelNode copy(RelTraitSet traitSet, List<RelNode> inputs) {
    return new EtcdEntryToEnumerableConverter(getCluster(), traitSet, sole(inputs));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @Nullable RelOptCost computeSelfCost(RelOptPlanner planner, RelMetadataQuery metadataQuery) {
    return Objects.requireNonNull(super.computeSelfCost(planner, metadataQuery))
      .multiplyBy(0.1f);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Result implement(EnumerableRelImplementor relImplementor, Prefer prefer) {
    final BlockBuilder block = new BlockBuilder();
    final Implementor implementor = new Implementor();
    implementor.visitChild(0, getInput());

    final RelDataType rowType = getRowType();
    final PhysType physType = PhysTypeImpl.of(relImplementor.getTypeFactory(), rowType, prefer.prefer(JavaRowFormat.ROW));

    final Expression fields =
      block.append("fields",
        constantArrayList(
          Pair.zip(elasticsearchFieldNames(rowType),
            new AbstractList<Class<?>>() {
              @Override public Class<?> get(int index) {
                return physType.fieldClass(index);
              }

              @Override public int size() {
                return rowType.getFieldCount();
              }
            }),
          Pair.class));
    final Expression table = block.append("table", Objects.requireNonNull(implementor.getTable().getExpression(EtcdEntryQueryable.class)));
    final Expression offset = block.append("offset", Expressions.constant(implementor.getOffset()));
    final Expression fetch = block.append("fetch", Expressions.constant(implementor.getFetch()));

    final Expression context = block.append("context", Expressions.constant(implementor, QueryContext.class));

    Expression enumerable = block.append("enumerable",
      Expressions.call(table, EtcdMethod.ETCD_ENTRY_GET.getMethod(), context)
    );
    block.add(Expressions.return_(null, enumerable));
    return relImplementor.result(physType, block.toBlock());
  }

  @Override
  public void implement(Implementor implementor) {
  }

  private static <T> List<Expression> constantList(List<T> values) {
    return values.stream().map(Expressions::constant).collect(Collectors.toList());
  }

  private static <T> MethodCallExpression constantArrayList(List<T> values, Class<?> clazz) {
    return Expressions.call(BuiltInMethod.ARRAYS_AS_LIST.method,
      Expressions.newArrayInit(clazz, constantList(values)));
  }

  static List<String> elasticsearchFieldNames(final RelDataType rowType) {
    return SqlValidatorUtil.uniquify(
      new AbstractList<String>() {
        @Override public String get(int index) {
          final String name = rowType.getFieldList().get(index).getName();
          return name.startsWith("$") ? "_" + name.substring(2) : name;
        }

        @Override public int size() {
          return rowType.getFieldCount();
        }
      },
      SqlValidatorUtil.EXPR_SUGGESTER, true);
  }
}
