---
last_updated: 2026-10-08
scope: architecture
---

# Build Commands

Maven-команды для сборки, тестирования и публикации проекта.

## Сборка проекта

```shell
# Полная сборка без тестов
mvn clean install -DskipTests

# Сборка с тестами
mvn clean install

# Сборка конкретного модуля
mvn -pl logger-spring-boot-starter clean install
mvn -pl http-client-spring-boot-starter clean install
mvn -pl vych-spring-toolkit-tests clean install
```

## Управление версиями

```shell
# Поднять версию проекта и всех модулей
mvn versions:set -DnewVersion=<NEW-VERSION>

# Отменить последние изменения версий
mvn versions:revert

# Зафиксировать изменения версий
mvn versions:commit
```

## Запуск тестов

```shell
# Все тесты
mvn test

# Юнит-тесты
mvn -pl logger-spring-boot-starter test
mvn -pl http-client-spring-boot-starter test

# Интеграционные тесты
mvn -pl vych-spring-toolkit-tests test
```

## Публикация в GitHub Packages

```shell
# Сборка и публикация (требует settings.xml с GitHub credentials)
mvn clean deploy

# Сборка без публикации
mvn clean install -DskipTests
```
