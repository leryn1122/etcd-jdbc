package io.github.leryn.etcd.calcite.rel;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class EtcdEntryConverterRule extends ConverterRule {

  protected EtcdEntryConverterRule(Config config) {
    super(config);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract @Nullable RelNode convert(RelNode rel);
}
