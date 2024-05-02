package io.github.leryn.etcd.calcite;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import io.github.leryn.etcd.EtcdConfiguration;
import io.github.leryn.etcd.EtcdConfigurationAccessor;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;

public class KubernetesCrdSchemaFactory implements SchemaFactory {

  @Override
  public Schema create(SchemaPlus parentSchema, String name, Map<String, Object> operand) {
    Properties props = new Properties();
    for (Map.Entry<String, Object> entry : operand.entrySet()) {
      props.setProperty(entry.getKey(), Objects.toString(entry.getValue()));
    }
    EtcdConfiguration configuration = EtcdConfigurationAccessor.fromProperties(props);
    KubernetesCrdSchema schema = new KubernetesCrdSchema(configuration);
    parentSchema.add(name, schema);
    return schema;
  }
}
