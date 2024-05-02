package io.github.leryn.etcd.jdbc;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.options.GetOption;
import io.github.leryn.etcd.EtcdConfiguration;
import io.github.leryn.etcd.EtcdConfigurationAccessor;
import io.github.leryn.etcd.EtcdTransport;
import io.github.leryn.etcd.JdbcEtcdSubProtocol;
import io.github.leryn.etcd.calcite.EtcdSchemaFactory;
import io.github.leryn.etcd.calcite.KubernetesCrdSchemaFactory;
import io.github.leryn.etcd.calcite.KubernetesSchemaFactory;
import io.github.leryn.etcd.exceptions.RuntimeSQLException;
import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.DriverVersion;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Driver
  extends org.apache.calcite.jdbc.Driver
  implements java.sql.Driver {

  private static final Logger log = LoggerFactory.getLogger(Driver.class);

  static {
    try {
      DriverManager.registerDriver(new Driver());
    } catch (SQLException e) {
      Supplier<String> message = () ->
        "Failed to register JDBC driver: " + Driver.class.getCanonicalName() + ", due to: " + e.getMessage();
      log.error(message.get());
      throw new RuntimeException(message.get(), e);
    }
  }

  public Driver() throws SQLException {
  }

  @Override
  protected final String getConnectStringPrefix() {
    throw new UnsupportedOperationException("Ensure it is unused");
  }

  @Override
  protected final DriverVersion createDriverVersion() {
    return DriverVersion.load(Driver.class,
      "etcd-jdbc-driver.properties",
      "Etcd-JDBC",
      "0.1.0",
      "Etcd",
      "3.5.12");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean acceptsURL(String url) throws SQLException {
    return JdbcEtcdSubProtocol.fromUrl(url).isPresent();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Connection connect(String url, Properties info) throws SQLException {
    if (!acceptsURL(url)) {
      return null;
    }
    log.info("Create one connection: {}", url);

    Properties props = extractProperties(url);
    url = remakeJdbcUrlAsCalciteUrl(props);
    testEtcdConnectionOrFail(props);

    AvaticaConnection connection = factory.newConnection(this, factory, url, props);
    handler.onConnectionInit(connection);
    CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);

    // Add builtin schema.
    SchemaPlus rootSchema = calciteConnection.getRootSchema();
    setupBuiltinSchema(rootSchema, props);
    return connection;
  }

  /**
   * Create a single Etcd Client instance to test the connection to the Etcd server.
   * @param props Connection properties.
   * @throws SQLException Thrown if the Etcd server is unavailable.
   */
  private void testEtcdConnectionOrFail(Properties props) throws SQLException {
    EtcdConfiguration configuration = EtcdConfigurationAccessor.fromProperties(props);
    try(EtcdTransport transport = EtcdConfigurationAccessor.toTransport(configuration);
        Client client = transport.getClient()) {
      ByteSequence prefix = ByteSequence.from("/", StandardCharsets.UTF_8);
      client.getKVClient()
        .get(prefix, GetOption.builder()
          .isPrefix(true)
          .withKeysOnly(true)
          .withLimit(1)
          .build())
        .get(10, TimeUnit.SECONDS);
    } catch (Exception e) {
      Supplier<String> message = () ->
          "The server is not available: " + e.getMessage();
      log.error(message.get());
      throw new RuntimeSQLException(message.get(), e);
    }
  }

  private Properties defaultProperties() {
    return new Properties() {{
      setProperty("lex", "ORACLE");
      setProperty("caseSensitive", "false");
      setProperty("fun", "standard,oracle");
      setProperty("schema", "k8s");
    }};
  }

  /**
   * Remake the JDBC URL as Calcite one.
   * @param props Connection properties.
   * @return Calcite URL
   */
  private String remakeJdbcUrlAsCalciteUrl(Properties props) {
    StringBuilder sb = new StringBuilder("jdbc:calcite:");
    for (Map.Entry<Object, Object> entry : props.entrySet()) {
      sb.append(entry.getKey())
        .append("=")
        .append(entry.getValue())
        .append(";");
    }
    return sb.toString();
  }

  /**
   * Extract the connection properties from the given JDBC URL below.
   * {@code jdbc:[etcd|etcd-cluster]://[localhost:2379,localhost:2379]/?property=value}
   *
   * @param url JDBC URL.
   * @return Connection properties from the given JDBC URL.
   */
  private Properties extractProperties(String url) throws SQLException {
    try {
      String urlSuffix = url.substring("jdbc:".length());
      URI uri = URI.create(urlSuffix);
      Properties props = new Properties();
      props.setProperty("protocol", uri.getScheme());
      props.setProperty("endpoints", uri.getAuthority());
      if (null != uri.getQuery()) {
        for (String query : Optional.ofNullable(uri.getQuery()).orElse("").split("&")) {
          String[] kv = query.split("=");
          props.setProperty(kv[0], kv[1]);
        }
      }

      // Overwrite the manipulated properties by default.
      for (Map.Entry<Object, Object> entry : defaultProperties().entrySet()) {
        props.put(entry.getKey(), entry.getValue().toString());
      }
      return props;
    } catch (Exception e) {
      Supplier<String> message = () ->
        "Malformed JDBC URL: " + url;
      log.error(message.get(), e);
      throw new RuntimeSQLException(message.get(), e);
    }
  }

  /**
   * Setup builtin schemas.
   * <ul>
   *   <li><b>etcd</b>: Schema for Etcd metadata logical view, including etcd status, authentication, etc.</li>
   *   <li><b>crd</b>: Kubernetes custom resource definition data managed by the Kubernetes API server.</li>
   *   <li><b>k8s</b>: Kubernetes resource data managed by the Kubernetes API server.</li>
   *   <li><b>metadata</b>: Builtin schema by Calcite</li>
   * </ul>
   * @param rootSchema Root schema.
   * @param props Connection properties.
   */
  private void setupBuiltinSchema(SchemaPlus rootSchema, Properties props) {
    Map<String, Object> operands = new HashMap<>();
    for (Map.Entry<Object, Object> entry : props.entrySet()) {
      operands.put((String) entry.getKey(), Objects.toString(entry.getValue()));
    }

    EtcdSchemaFactory etcdSchemaFactory = new EtcdSchemaFactory();
    etcdSchemaFactory.create(rootSchema, "etcd", operands);

    KubernetesSchemaFactory kubernetesSchemaFactory = new KubernetesSchemaFactory();
    kubernetesSchemaFactory.create(rootSchema, "k8s", operands);

    KubernetesCrdSchemaFactory kubernetesCrdSchemaFactory = new KubernetesCrdSchemaFactory();
    kubernetesCrdSchemaFactory.create(rootSchema, "crd", operands);
  }
}
