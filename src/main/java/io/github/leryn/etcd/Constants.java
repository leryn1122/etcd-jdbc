package io.github.leryn.etcd;

import java.math.BigInteger;

public abstract class Constants {

  public static final char KEY_ENTRY_SEPARATOR = '/';

  public static final String REGISTRY_PREFIX = "/registry/";

  public static final int REGISTRY_PREFIX_LENGTH = REGISTRY_PREFIX.length();

  public static final String CRD_DEFINITIONS_KEY_PREFIX = "/registry/apiextensions.k8s.io/customresourcedefinitions/";

  public static final int CRD_DEFINITIONS_KEY_PREFIX_LENGTH = CRD_DEFINITIONS_KEY_PREFIX.length();

  public static final String API_SERVICE_DEFINITIONS_KEY_PREFIX = "/registry/apiregistration.k8s.io/apiservices/";

  public static final int API_SERVICE_DEFINITIONS_KEY_PREFIX_LENGTH = API_SERVICE_DEFINITIONS_KEY_PREFIX.length();

  public static final BigInteger LONG_TO_UINT64_OFFSET = BigInteger.TWO.pow(64);

  private Constants() {
  }
}
