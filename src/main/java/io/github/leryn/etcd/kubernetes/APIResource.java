package io.github.leryn.etcd.kubernetes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class APIResource {
  private String name;
  @JsonProperty("apiVersion")
  private String APIVersion;
  private String kind;
  private String plural;
  @Nullable
  private String shortNames;
  private boolean namespaced;
  @Nullable
  private String javaProtoParser;
}
