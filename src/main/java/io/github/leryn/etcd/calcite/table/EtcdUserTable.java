package io.github.leryn.etcd.calcite.table;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.auth.AuthUserListResponse;
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

public final class EtcdUserTable extends AbstractEtcdMetadataTable implements EtcdMetadataTable {

  public EtcdUserTable(@NotNull final EtcdTransport transport) {
    super("Users", transport);
  }

  @Override
  public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
    JavaTypeFactory javaTypeFactory = (JavaTypeFactory) relDataTypeFactory;
    return javaTypeFactory.createStructType(
      ImmutableMap.ofEntries(
        Map.entry("User", relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR))
      ).entrySet().asList()
    );
  }

  @Override
  public Enumerable<Object[]> scan(DataContext root) {
    try {
      Client client = getTransport().getClient();
      AuthUserListResponse response = client.getAuthClient().userList().get(10, TimeUnit.SECONDS);
      return Linq4j.asEnumerable(
        response.getUsers()
          .stream()
          .map(u -> new Object[] {
            u
          }).toList());
    } catch (ExecutionException | InterruptedException | TimeoutException e) {
      Supplier<String> message = () -> "Failed to get Etcd cluster users： " + e.getMessage();
      log.error(message.get());
      throw new RuntimeSQLException(message.get(), e);
    }
  }
}
