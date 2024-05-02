package io.github.leryn.etcd.calcite.rel;

import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import io.github.leryn.etcd.QueryContext;
import lombok.Getter;
import org.apache.calcite.linq4j.tree.Types;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
@Getter
enum EtcdMethod {
  ETCD_ENTRY_GET(EtcdEntryQueryable.class, "getKeyValue", QueryContext.class);

  public static final Map<Method, EtcdMethod> MAP;

  static {
    ImmutableMap.Builder<Method, EtcdMethod> builder = ImmutableMap.builder();
    for (EtcdMethod value : EtcdMethod.values()) {
      builder.put(value.method, value);
    }
    MAP = builder.build();
  }

  public @NotNull final Method method;

  EtcdMethod(Class<?> clazz, String method, Class<?>... argumentTypes) {
    this.method = Types.lookupMethod(clazz, method, argumentTypes);
  }
}
