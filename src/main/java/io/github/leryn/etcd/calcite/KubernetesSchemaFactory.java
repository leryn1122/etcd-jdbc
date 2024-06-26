package io.github.leryn.etcd.calcite;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import io.github.leryn.etcd.EtcdConfiguration;
import io.github.leryn.etcd.EtcdConfigurationAccessor;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.semver4j.Semver;

public class KubernetesSchemaFactory implements SchemaFactory {

  @Override
  public Schema create(SchemaPlus parentSchema, String name, Map<String, Object> operand) {
    Properties props = new Properties();
    for (Map.Entry<String, Object> entry : operand.entrySet()) {
      props.setProperty(entry.getKey(), Objects.toString(entry.getValue()));
    }
    EtcdConfiguration configuration = EtcdConfigurationAccessor.fromProperties(props);
    KubernetesSchema schema = new KubernetesSchema(Semver.coerce(props.getProperty("kubeVersion", "v1.28.0")), configuration);
    parentSchema.add(name, schema);
    return schema;
  }
}
