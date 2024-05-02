package io.github.leryn.etcd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.Getter;
import org.apache.calcite.rel.RelFieldCollation;
import org.apache.calcite.util.Pair;

@Getter
public class QueryContext {

  private List<String> fields = new ArrayList<>();
  private final List<Map.Entry<String, RelFieldCollation.Direction>> sort = new ArrayList<>();
  private Long offset;
  private Long fetch;

  public void addSort(String field, RelFieldCollation.Direction direction) {
    Objects.requireNonNull(field, "Field name must not be null.");
    this.sort.add(new Pair<>(field, direction));
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public void setFetch(long fetch) {
    this.fetch = fetch;
  }
}
