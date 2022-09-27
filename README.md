# workflow

Java workflow implementation for DAG task processing

Inspired by nirmata/workflow (https://github.com/nirmata/workflow)

This library aims to have similar structure with simplicity, robustness and generic implementation which can be used
with any data stores.

## Initial release will have following features

1. Implementation using redis
2. Decision tasks (Task executor need to return which path to execute, other paths will be ignored)
3. Async tasks (when task executor completes, task will not be marked completed until externally marked completed)

## Beta version available.

* Maven dependency
```
    <dependency>
      <groupId>io.github.pavansharma36</groupId>
      <artifactId>workflow-jedis</artifactId>
      <version>0.0.1-beta</version>
    </dependency>
```