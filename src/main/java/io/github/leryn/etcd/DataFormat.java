package io.github.leryn.etcd;

public enum DataFormat {
  RAW("raw"),
  PROTOBUF("proto"),
  JSON("json"),
  OTHER("other");

  private final String type;

  DataFormat(String type) {
    this.type = type;
  }

  public static DataFormat fromTypeName(String type) {
    for (DataFormat format : DataFormat.values()) {
      if (format.getTypeName().equals(type)) {
        return format;
      }
    }
    return DataFormat.RAW;
  }

  public String getTypeName() {
    return this.type;
  }

}
