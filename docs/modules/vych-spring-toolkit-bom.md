---
last_updated: 2026-10-08
scope: module
---

# BOM (Bill of Materials)

Maven BOM для управления версиями зависимостей потребителей проекта.

## Назначение

Позволяет потребителям подключать starters проекта без указания версий — версии управляются через BOM в `<dependencyManagement>`.

## Структура

Единственный файл `pom.xml` без исходного кода. Определяет `<dependencyManagement>` с managed-зависимостями для:

- `logger-spring-boot-starter`
- `http-client-spring-boot-starter`

## Кросс-ссылки

- [modules/README.md](README.md) — обзор модулей
- [GIGACODE.md](../GIGACODE.md) — информация о публикации артефактов
