package io.github.leryn.etcd.calcite.rel;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Filter;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EtcdEntryFilterRule extends EtcdEntryConverterRule {

  static final EtcdEntryFilterRule INSTANCE = Config.INSTANCE
    .withConversion(Filter.class, Convention.NONE, EtcdEntryRel.CONVENTION, "EtcdEntryFilterRule")
    .withRuleFactory(EtcdEntryFilterRule::new)
    .toRule(EtcdEntryFilterRule.class);

  protected EtcdEntryFilterRule(Config config) {
    super(config);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @Nullable RelNode convert(RelNode relNode) {
    final LogicalFilter filter = (LogicalFilter) relNode;
    final RelTraitSet traitSet = filter.getTraitSet().replace(out);
    return new EtcdEntryFilter(
      relNode.getCluster(),
      traitSet,
      convert(filter.getInput(), out),
      filter.getCondition()
    );
  }

}
