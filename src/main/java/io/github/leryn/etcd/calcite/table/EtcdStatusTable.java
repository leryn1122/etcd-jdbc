package io.github.leryn.etcd.calcite.table;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Cluster;
import io.etcd.jetcd.Maintenance;
import io.etcd.jetcd.cluster.Member;
import io.etcd.jetcd.maintenance.StatusResponse;
import io.github.leryn.etcd.Constants;
import io.github.leryn.etcd.exceptions.RuntimeSQLException;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.sql.type.SqlTypeName;

/**
 * System table for Etcd status, which is the same as the command:
 * <p>
 * {@code
 * ETCD_API=3 etcdctl endpoint status -w fields
 * }
 */
public final class EtcdStatusTable extends AbstractEtcdTable implements ScannableTable {

  public EtcdStatusTable(Client client) {
    super(client, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getTableName() {
    return "Status";
  }

  @Override
  public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
    return relDataTypeFactory.createStructType(
      ImmutableMap.ofEntries(
        Map.entry("ClusterID", relDataTypeFactory.createSqlType(SqlTypeName.DECIMAL)),
        Map.entry("MemberID", relDataTypeFactory.createSqlType(SqlTypeName.DECIMAL)),
        Map.entry("Revision", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT)),
        Map.entry("Version", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)),
        Map.entry("DBSize", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT)),
        Map.entry("Leader", relDataTypeFactory.createSqlType(SqlTypeName.DECIMAL)),
        Map.entry("IsLearner", relDataTypeFactory.createSqlType(SqlTypeName.BOOLEAN)),
        Map.entry("RaftTerm", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT)),
        Map.entry("RaftIndex", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT)),
        Map.entry("RaftAppliedIndex", relDataTypeFactory.createSqlType(SqlTypeName.BIGINT)),
        Map.entry("Endpoint", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR))
      ).entrySet().asList()
    );
  }

  /**
   * Return lines for Etcd status.
   *
   * @param context Data context
   * @return A single line for Etcd status.
   */
  @Override
  public Enumerable<Object[]> scan(DataContext context) {
    try {
      Cluster cluster = getClient().getClusterClient();
      List<Member> members = cluster.listMember().get(10, TimeUnit.SECONDS).getMembers();

      Maintenance maintenance = getClient().getMaintenanceClient();

      List<Object[]> results = new ArrayList<>(members.size());

      for (Member member : members) {
        StatusResponse status = maintenance.statusMember(member.getClientURIs().get(0).toString()).get(10, TimeUnit.SECONDS);
        Object[] result = new Object[]{
          /*  ClusterID         */BigInteger.valueOf(status.getHeader().getClusterId()).add(Constants.LONG_TO_UINT64_OFFSET),
          /*  MemberID          */BigInteger.valueOf(status.getHeader().getMemberId()).add(Constants.LONG_TO_UINT64_OFFSET),
          /*  Revision          */status.getHeader().getRevision(),
          /*  Version           */status.getVersion(),
          /*  DBSize            */status.getDbSize(),
          /*  Leader            */BigInteger.valueOf(status.getLeader()).add(Constants.LONG_TO_UINT64_OFFSET),
          /*  IsLearner         */member.isLearner(),
          /*  RaftTerm          */status.getRaftTerm(),
          /*  RaftIndex         */status.getRaftIndex(),
          /*  RaftAppliedIndex  */status.getHeader().getRaftTerm(),
          /*  Endpoint          */Joiner.on(",").join(member.getClientURIs()),
        };
        results.add(result);
      }
      return Linq4j.asEnumerable(results);
    } catch (ExecutionException | InterruptedException | TimeoutException e) {
      log.error("Failed to get Etcd cluster health status.");
      throw new RuntimeSQLException("Failed to get Etcd cluster health status.", e);
    }
  }
}
