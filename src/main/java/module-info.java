module etcd.jdbc {
  requires java.sql;

  requires avatica.core;
  requires calcite.core;
  requires calcite.linq4j;
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.databind;
  requires com.google.common;
  requires io.kubernetes.client.java.proto;
  requires io.netty.handler;
  requires jetcd.core;
  requires kubernetes.model.apiextensions;
  requires kubernetes.model.common;
  requires kubernetes.model.core;
  requires static lombok;
  requires org.checkerframework.checker.qual;
  requires org.jetbrains.annotations;
  requires org.semver4j;
  requires org.slf4j;

  exports io.github.leryn.etcd;
  exports io.github.leryn.etcd.annotation;
  exports io.github.leryn.etcd.calcite;
  exports io.github.leryn.etcd.exceptions;
  exports io.github.leryn.etcd.jdbc;
  exports io.github.leryn.etcd.kubernetes;
  exports io.github.leryn.etcd.support;
  exports io.github.leryn.etcd.calcite.rel;
}
