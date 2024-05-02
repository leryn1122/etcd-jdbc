package io.github.leryn.etcd.support;

import java.util.function.Supplier;

import io.github.leryn.etcd.exceptions.RuntimeSQLException;

public abstract class Asserts {

  public static void isTrue(boolean expression, Supplier<String> message) {
    if (!expression) {
      throw new RuntimeSQLException(message.get());
    }
  }

  private Asserts() {}
}
