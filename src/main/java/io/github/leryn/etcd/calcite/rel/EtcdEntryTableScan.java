package io.github.leryn.etcd.calcite.rel;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import io.github.leryn.etcd.EtcdEntryTable;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rel.type.RelDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EtcdEntryTableScan extends TableScan implements EtcdEntryRel {

  @NotNull
  private final EtcdEntryTable etcdTable;

  @Nullable
  private final RelDataType projectRowType;

  public EtcdEntryTableScan(RelOptCluster cluster, RelTraitSet traitSet, RelOptTable table,
                            @NotNull EtcdEntryTable etcdTable, @Nullable RelDataType projectRowType) {
    super(cluster, traitSet, ImmutableList.of(), table);
    this.etcdTable = Objects.requireNonNull(etcdTable);
    this.projectRowType = projectRowType;

    assert getConvention() == EtcdEntryRel.CONVENTION;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RelNode copy(RelTraitSet traitSet, List<RelNode> inputs) {
    assert inputs.isEmpty();
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RelDataType deriveRowType() {
    return null != this.projectRowType ? this.projectRowType : super.deriveRowType();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @Nullable RelOptCost computeSelfCost(RelOptPlanner planner,
                                              RelMetadataQuery metadataQuery) {
    final float factor = null == this.projectRowType
      ? 1f
      : (float) projectRowType.getFieldCount() / 1000f;
    return Objects.requireNonNull(super.computeSelfCost(planner, metadataQuery))
      .multiplyBy(0.1f * factor);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void register(RelOptPlanner planner) {
    planner.addRule(EtcdEntryToEnumerableConverterRule.INSTANCE);
    for (RelOptRule rule : EtcdEntryRules.RULES) {
      planner.addRule(rule);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void implement(Implementor implementor) {
    implementor.setTable(this.table);
    implementor.setEtcdEntryTable(this.etcdTable);
  }
}
