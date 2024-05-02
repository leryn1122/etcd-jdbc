package io.github.leryn.etcd.calcite.rel;

import io.github.leryn.etcd.EtcdEntryTable;
import io.github.leryn.etcd.QueryContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;

public interface EtcdEntryRel extends RelNode {

  Convention CONVENTION = new Convention.Impl("ETCD_ENTRY", EtcdEntryRel.class);

  void implement(Implementor implementor);

  @Getter
  class Implementor extends QueryContext {
    @Setter
    private RelOptTable table;
    @Setter
    private EtcdEntryTable etcdEntryTable;

    public void visitChild(int ordinal, RelNode input) {
      assert ordinal == 0;
      ((EtcdEntryRel) input).implement(this);
    }
  }

}
