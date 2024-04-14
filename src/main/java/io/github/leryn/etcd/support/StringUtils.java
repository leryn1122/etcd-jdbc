package io.github.leryn.etcd.support;

public abstract class StringUtils {

  public static String toUpperCamelCase(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }

    char c = input.charAt(0);
    char uc = Character.toUpperCase(c);
    StringBuilder sb = new StringBuilder();
    sb.append(uc);

    int len = input.length();
    if (len > 1) {
      for (int i = 1; i < len; i++) {
        c = input.charAt(i);
        if (c == '.') {
          continue;
        } else if (Character.isLowerCase(c) && input.charAt(i - 1) == '.') {
          uc = Character.toUpperCase(c);
          sb.append(uc);
        } else {
          sb.append(c);
        }
      }
    }
    return sb.toString();
  }
}
