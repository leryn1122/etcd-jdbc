package io.github.leryn.etcd;

import org.apache.calcite.schema.ScannableTable;

/**
 * Interface for Etcd database metadata which is managed by Etcd management API, including Etcd status, member lists,
 * and so on.
 * <p>
 * Etcd metadata is obtained from Etcd management API, hence it is scannable table {@link ScannableTable}.
 */
public interface EtcdMetadataTable extends EtcdTable, NamedTable, ScannableTable {
}
