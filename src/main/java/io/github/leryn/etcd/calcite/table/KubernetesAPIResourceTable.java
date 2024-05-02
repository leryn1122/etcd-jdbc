package io.github.leryn.etcd.calcite.table;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import io.github.leryn.etcd.kubernetes.APIResource;
import io.github.leryn.etcd.support.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.jetbrains.annotations.NotNull;
import org.semver4j.Semver;

/**
 * Table for API resources in Kubernetes cluster, which is the same result of the command:
 * <p>
 * {@code kubectl api-resources -o wide}
 */
@Slf4j
public final class KubernetesAPIResourceTable extends AbstractTable
  implements KubernetesTable<APIResource>, ScannableTable {

  private @NotNull final Semver kubeVersion;

  private @NotNull final List<APIResource> resources;

  public KubernetesAPIResourceTable(@NotNull final Semver kubeVersion, @NotNull final Iterable<APIResource> resources) {
    this.kubeVersion = kubeVersion;
    this.resources = ImmutableList.copyOf(resources);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
    JavaTypeFactory javaTypeFactory = (JavaTypeFactory) relDataTypeFactory;
    return relDataTypeFactory.createStructType(
      ImmutableList.copyOf(
        Arrays.stream(APIResource.class.getDeclaredFields())
          .map(field -> Map.entry(StringUtils.toUpperCamelCase(
            field.getName()), javaTypeFactory.createType(field.getType())))
          .toList()
      )
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Enumerable<Object[]> scan(DataContext context) {
    Collection<Object[]> results = this.resources.stream().map(r -> new Object[]{
      /* Name        */r.getName(),
      /* APIVersion  */r.getAPIVersion(),
      /* Kind        */r.getKind(),
      /* Plural      */r.getPlural(),
      /* Namespaced  */r.isNamespaced(),
      /* ShortNames  */r.getShortNames()
    }).collect(Collectors.toList());
    return Linq4j.asEnumerable(results);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getTableName() {
    return "APIResources";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isNamespaced() {
    return false;
  }
}
