package io.github.leryn.etcd.calcite.table;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.auth.AuthRoleListResponse;
import io.github.leryn.etcd.EtcdMetadataTable;
import io.github.leryn.etcd.EtcdTransport;
import io.github.leryn.etcd.exceptions.RuntimeSQLException;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.type.SqlTypeName;
import org.jetbrains.annotations.NotNull;

public final class EtcdRoleTable extends AbstractEtcdMetadataTable implements EtcdMetadataTable {

  public EtcdRoleTable(@NotNull final EtcdTransport transport) {
    super("Roles", transport);
  }

  @Override
  public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
    JavaTypeFactory javaTypeFactory = (JavaTypeFactory) relDataTypeFactory;
    return javaTypeFactory.createStructType(
      ImmutableMap.ofEntries(
        Map.entry("Role", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR))
      ).entrySet().asList()
    );
  }

  @Override
  public Enumerable<Object[]> scan(DataContext root) {
    try {
      Client client = getTransport().getClient();
      AuthRoleListResponse response = client.getAuthClient().roleList().get(10, TimeUnit.SECONDS);
      return Linq4j.asEnumerable(
        response.getRoles()
          .stream()
          .map(r -> new Object[] {
            r
          }).toList());
    } catch (ExecutionException | InterruptedException | TimeoutException e) {
      Supplier<String> message = () -> "Failed to get Etcd cluster users： " + e.getMessage();
      log.error(message.get());
      throw new RuntimeSQLException(message.get(), e);
    }
  }
}
