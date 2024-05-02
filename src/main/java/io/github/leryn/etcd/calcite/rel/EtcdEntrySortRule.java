package io.github.leryn.etcd.calcite.rel;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollations;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Sort;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EtcdEntrySortRule extends EtcdEntryConverterRule {

  static final EtcdEntrySortRule INSTANCE = Config.INSTANCE
    .withConversion(Sort.class, Convention.NONE, EtcdEntryRel.CONVENTION, "EtcdEntrySortRule")
    .withRuleFactory(EtcdEntrySortRule::new)
    .toRule(EtcdEntrySortRule.class);

  protected EtcdEntrySortRule(Config config) {
    super(config);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @Nullable RelNode convert(RelNode relNode) {
    final Sort sort = (Sort) relNode;
    final RelTraitSet traitSet = sort.getTraitSet().replace(super.out).replace(sort.getCollation());
    return new EtcdEntrySort(relNode.getCluster(), traitSet,
      convert(sort.getInput(),
        traitSet.replace(RelCollations.EMPTY)), sort.getCollation(), sort.offset, sort.fetch);
  }
}
