package io.github.leryn.etcd.support;

public abstract class StringUtils {

  public static boolean isEmpty(String s) {
    return s == null || s.isEmpty();
  }

  public static String toUpperCamelCase2(String input) {
    if (StringUtils.isEmpty(input)) {
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

  public static String toUpperCamelCase(String input) {
    if (StringUtils.isEmpty(input)) {
      return input;
    }
    StringBuilder sb = new StringBuilder(input);
    sb.setCharAt(0, Character.toUpperCase(input.charAt(0)));
    return sb.toString();
  }
}
