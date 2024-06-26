# README

![](https://img.shields.io/badge/java%2017-F56C2D.svg?style=for-the-badge&logo=openjdk&logoColor=F56C2D&labelColor=white)
![](https://img.shields.io/badge/etcd%20v3.5.12-419EDA.svg?style=for-the-badge&logo=etcd&logoColor=419EDA&labelColor=white)
![](https://img.shields.io/badge/k8s%20v1.28.x-326CE5.svg?style=for-the-badge&logo=kubernetes&logoColor=326CE5&labelColor=white)

[![Security Status](https://www.murphysec.com/platform3/v31/badge/1785995421869469696.svg)](https://www.murphysec.com/console/report/1785978247251554304/1785995421869469696)

## TL;DR

It is a simple Etcd JDBC driver for Kubernetes.

## Table of Contents

- [Background](#background)
- [Usage](#usage)
- [Architecture](#architecture)
- [Roadmap](#roadmap)
- [Related Efforts](#related-efforts)
- [Maintainers](#maintainers)
- [License](#license)

## Background

It inspired by Kubernetes Etcd diagnostics and statistics on working. Because the requests of large amount flood into
the API servers and makes Etcd fragile. Through DevOps experience, there is no applicable Etcd management console or web
for Etcd, containing 100k+ entries, or reaching 12~14 GiB size, in the industry.

It's designed to be a simple and useful DevOps JDBC tool.

## Usage

### Install

Simply use maven wrapper to build the project.

```bash
./mvnw install package
```

Copy the `target/etcd-jdbc.jar` to your DBMS tool. Or use the `sqlline` CLI tools to connect to the database.

```bash
./sqlline
```

```sql
sqlline version 1.12.0
sqlline> !connect jdbc:etcd://localhost:2379 anonymous P@ssw0rd
0: jdbc:etcd://localhost:2379> !resize
0: jdbc:etcd://localhost:2379> !schema
0: jdbc:etcd://localhost:2379> !tables
0: jdbc:etcd://localhost:2379> select * from k8s.APIResources limit 200;
0: jdbc:etcd://localhost:2379> select * from k8s.CustomResourceDefinitions limit 50;
```

### Compatibility

Only development version is compatible under verification. It is not essential to adapt
Etcd and Kubernetes of every version.

- Java: OpenJDK 17
- JDBC 4.1
- Etcd: 3.5.12
- Kubernetes: v1.28.x

It is available to get an Etcd database from a real Kubernetes or a tailored Kubernetes distro, such as K3S,
which is used a SQLite data file as storage by default.

For Kubernetes of different version, it is designed to add an API resource JSON file under the classloader path
`META-INF/kubernetes/v1.xx.xx/api-resources.jsonl` and modified JDBC parameter `kubeVersion=v1.xx.xx`.

## Architecture

In the prototype, it is used native JDBC interfaces and Etcd client to develop, including `java.sql.Connection`
and `java.sql.Statement`, etc. It is easy to execute etcd commands delegated to the Etcd client.
But there are some obstacles to parse the Etcd commands. It costs a lot of time to design and write a suitable
AST parser for further features.

In the coming version, it is constructed with Apache Calcite as a base SQL parser and executor, and Etcd client to call
the remote Etcd RESTful APIs.

### Schema

Schema is a Calcite abstract concept for the database schema.
All the schema are readonly, due to being improper to either write or modify Etcd database instead of
invoking API servers interface.
Etcd JDBC provides three schema:

- **etcd**: Schema for Etcd metadata logical view, including etcd status, authentication, etc.
- **crd**: Kubernetes custom resource definition data managed by the Kubernetes API server.
- **k8s**: Kubernetes resource data managed by the Kubernetes API server.
- **metadata**: Builtin schema by Calcite

In `k8s` schema, there are several kinds of tables:

- **APIResources**: List all API resources, as `kubectl api-resources`.
- **APIResources Details**: List all API resource manifest, as `kubectl get pods`.
- **CustomResourceDefinitions**: List all definitions of `CustomResourceDefinitions`, as `kubectl get crd -A`.

In `crd` schema, there are tables, those which represent lists of `CustomResourceDefinitions` installed in current
cluster.

- **CustomResourceDefinitions Details**: List all manifests of certain CustomResourceDefinitions,
  as `kubectl get xxx`.


## Roadmap

Possible feature:

  - Calcite Avatica server
  - Pagination

In the future:

- v1.0.0: Minimum Viable Product

## Related Efforts

Those repos are referenced on:

## Maintainers

[@Leryn](https://github.com/leryn1122).

## License

[MIT](LICENSE-MIT) © Leryn
