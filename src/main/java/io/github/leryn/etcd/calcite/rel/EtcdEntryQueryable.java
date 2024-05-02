package io.github.leryn.etcd.calcite.rel;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import io.etcd.jetcd.options.GetOption;
import io.github.leryn.etcd.EtcdEntryTable;
import io.github.leryn.etcd.QueryContext;
import io.github.leryn.etcd.annotation.Invisible;
import io.github.leryn.etcd.exceptions.RuntimeSQLException;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.linq4j.function.Function1;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AbstractTableQueryable;

@Slf4j
public class EtcdEntryQueryable<T> extends AbstractTableQueryable<T> {

  public EtcdEntryQueryable(QueryProvider queryProvider, SchemaPlus schema, EtcdEntryTable table, String tableName) {
    super(queryProvider, schema, table, tableName);
  }

  @Override
  public Enumerator<T> enumerator() {
    // It is intended to be blank.
    return null;
  }

  private EtcdEntryTable getTable() {
    return (EtcdEntryTable) super.table;
  }

  public Enumerable<Object[]> getKeyValue(QueryContext context) {
    GetOption.Builder option = GetOption.builder();
    try {
      Iterable<?> iterable = getTable().queryByCondition(context, option);
      Enumerable<?> enumerable = Linq4j.asEnumerable(iterable);
      return enumerable.select(toEntityMapper());
    } catch (Exception e) {
      Supplier<String> message = () ->
        "Failed to query the result set: " + context;
      log.error(message.get());
      throw new RuntimeSQLException(message.get() + e);
    }
  }

  private <R> Function1<R, Object[]> toEntityMapper() {
    return entity -> {
      try {
        List<Field> fields = Arrays.stream(entity.getClass().getDeclaredFields())
          .filter(field -> !field.isAnnotationPresent(Invisible.class))
          .toList();
        Object[] results = new Object[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
          Field field = fields.get(i);
          field.setAccessible(true);
          Object o = field.get(entity);
          results[i] = o;
        }
        return results;
      } catch (IllegalArgumentException | IllegalAccessException e) {
        Supplier<String> message = () ->
          "Failed to convert entity in reflection from " + entity.getClass() + ": " + entity;
        log.error(message.get());
        throw new RuntimeSQLException(message.get(), e);
      }
    };
  }
}
