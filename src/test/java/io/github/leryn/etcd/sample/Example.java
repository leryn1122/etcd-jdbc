package io.github.leryn.etcd.sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.calcite.jdbc.CalciteConnection;

public class Example {

  public static void main(String[] args) throws Exception {
    Connection connection = DriverManager.getConnection("jdbc:etcd://localhost:2379");
    CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
    Statement statement = calciteConnection.createStatement();
    ResultSet resultSet = statement.executeQuery("""
      select * from k8s.APIResources
      """);
    list(resultSet);
    closeQuietly(resultSet, statement, calciteConnection, connection);
  }

  public static void list(ResultSet resultSet) throws SQLException {
    ResultSetMetaData metaData = resultSet.getMetaData();
    while (resultSet.next()) {
      for (int column = 1; column <= metaData.getColumnCount(); column++) {
        try {
          String columnName = metaData.getColumnName(column);
          Object value = resultSet.getObject(column);
          System.out.println("Column: " + columnName + ", Value: " + value);
        } catch (ClassCastException e) {
          System.err.println("Column: " + metaData.getColumnName(column) + ", " + column);
        }
      }
    }
  }

  public static void closeQuietly(AutoCloseable... closeables) {
    for (AutoCloseable closeable : closeables) {
      if (null != closeable) {
          try {
              closeable.close();
          } catch (Exception ignored) {
          }
      }
    }
  }

}
