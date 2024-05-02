package io.github.leryn.etcd.calcite.rel;

import java.util.Objects;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Filter;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rex.RexNode;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EtcdEntryFilter extends Filter implements EtcdEntryRel {

  EtcdEntryFilter(RelOptCluster cluster, RelTraitSet traits, RelNode child, RexNode condition) {
    super(cluster, traits, child, condition);

    assert getConvention() == EtcdEntryRel.CONVENTION;
    assert getConvention() == child.getConvention();
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
  public Filter copy(RelTraitSet traitSet, RelNode input, RexNode condition) {
    return new EtcdEntryFilter(getCluster(), traitSet, input, condition);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void implement(Implementor implementor) {
  }
}
