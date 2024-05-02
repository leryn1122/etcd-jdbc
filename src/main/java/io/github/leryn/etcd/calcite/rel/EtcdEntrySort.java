package io.github.leryn.etcd.calcite.rel;

import java.util.List;
import java.util.Objects;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelFieldCollation;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EtcdEntrySort extends Sort implements EtcdEntryRel {

  protected EtcdEntrySort(RelOptCluster cluster, RelTraitSet traits, RelNode child, RelCollation collation, @Nullable RexNode offset, @Nullable RexNode fetch) {
    super(cluster, traits, child, collation, offset, fetch);

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
  public Sort copy(RelTraitSet traitSet, RelNode relNode, RelCollation relCollation, @Nullable RexNode offset, @Nullable RexNode fetch) {
    return new EtcdEntrySort(getCluster(), traitSet, relNode, super.collation, offset, fetch);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void implement(Implementor implementor) {
    implementor.visitChild(0, getInput());

    final List<RelDataTypeField> fields = getRowType().getFieldList();
    for (RelFieldCollation fieldCollation : collation.getFieldCollations()) {
      final String name = fields.get(fieldCollation.getFieldIndex()).getName();
      implementor.addSort(name, fieldCollation.getDirection());
    }

    if (this.offset != null) {
      //noinspection DataFlowIssue
      implementor.setOffset(((RexLiteral) this.offset).getValueAs(Long.class));
    }

    if (this.fetch != null) {
      //noinspection DataFlowIssue
      implementor.setFetch(((RexLiteral) this.fetch).getValueAs(Long.class));
    }
  }
}
