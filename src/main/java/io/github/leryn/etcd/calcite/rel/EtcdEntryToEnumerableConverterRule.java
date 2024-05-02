package io.github.leryn.etcd.calcite.rel;

import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EtcdEntryToEnumerableConverterRule extends ConverterRule {

  static final ConverterRule INSTANCE = Config.INSTANCE
    .withConversion(RelNode.class, EtcdEntryRel.CONVENTION, EnumerableConvention.INSTANCE,
      EtcdEntryToEnumerableConverterRule.class.getSimpleName()
    )
    .withRuleFactory(EtcdEntryToEnumerableConverterRule::new)
    .toRule(EtcdEntryToEnumerableConverterRule.class);

  protected EtcdEntryToEnumerableConverterRule(Config config) {
    super(config);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @Nullable RelNode convert(RelNode relNode) {
    RelTraitSet newTraitSet = relNode.getTraitSet().replace(getOutConvention());
    return new EtcdEntryToEnumerableConverter(relNode.getCluster(), newTraitSet, relNode);
  }
}
