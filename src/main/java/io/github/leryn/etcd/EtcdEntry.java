package io.github.leryn.etcd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class EtcdEntry {
  private String name;
  @Nullable
  private String namespace;
  private Long version;
  private Long createRevision;
  private Long modifyRevision;
  private Integer size;
  @Nullable
  private String value;
}
