package io.github.leryn.etcd.calcite.rel;

import org.apache.calcite.plan.RelOptRule;

class EtcdEntryRules {

  static final RelOptRule[] RULES = {
    EtcdEntrySortRule.INSTANCE,
//    EtcdEntryFilterRule.INSTANCE,
  };

  private EtcdEntryRules() {}
}
