package io.github.leryn.etcd.kubernetes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class CustomResourceDefinition {
  private String name;
  @JsonProperty("apiVersion")
  private String APIVersion;
  private String kind;
  private String plural;
  private boolean namespaced;
  private String shortNames;
}
