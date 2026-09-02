# Vych Spring Toolkit

Набор переиспользуемых компонентов и Spring Boot Starter'ов для Java/Spring-проектов.

Проект объединяет несколько независимых инструментов в одном Maven multi-module 
репозитории. Каждый стартер можно подключать отдельно в зависимости от потребностей 
конкретного проекта.

## Зачем нужен

`vych-spring-toolkit` предназначен для вынесения часто используемой логики в готовые Spring Boot Starter'ы.

Это позволяет:

* не копировать одинаковый код между проектами;
* подключать функциональность одной Maven-зависимостью;
* автоматически настраивать компоненты через Spring Boot AutoConfiguration;
* централизованно управлять версиями всех модулей через BOM;
* развивать и тестировать несколько инструментов в одном репозитории.

## Модули

На данный момент проект содержит следующие модули:

### [logger-spring-boot-starter](./logger-spring-boot-starter)

Spring Boot Starter для настройки и использования собственного логгера.

Подключает необходимые зависимости и автоматически регистрирует конфигурацию логгера через Spring Boot AutoConfiguration.

### [http-client-spring-boot-starter](./http-client-spring-boot-starter)

Spring Boot Starter для работы с HTTP-клиентом.

Предоставляет готовую конфигурацию HTTP-клиента и интегрируется с `logger-spring-boot-starter`.

### [vych-spring-toolkit-bom](./vych-spring-toolkit-bom)

Bill of Materials (BOM) проекта.

Используется для централизованного управления версиями модулей `Vych Spring Toolkit`. Благодаря BOM в 
проекте-потребителе не требуется указывать версию каждого подключаемого стартера отдельно.

### [vych-spring-toolkit-tests](./vych-spring-toolkit-tests)

Тесты тулкита.

Модуль содержит набор интеграционных тестов для проверки работоспособности стартеров в условиях, 
максимально приближенных к реальному Spring Boot-приложению.

## Подключение в новый проект

Для подключения тулкита рекомендуется импортировать его BOM, а затем добавить только необходимые стартеры.

### 1. Подключить BOM

В `pom.xml` нового проекта добавьте:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>ru.vych</groupId>
            <artifactId>vych-spring-toolkit-bom</artifactId>
            <version>VERSION</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

`VERSION` необходимо заменить на нужную опубликованную версию тулкита.

### 2. Подключить нужный стартер

Например, если нужен только HTTP-клиент:

```xml
<dependencies>
    <dependency>
        <groupId>ru.vych</groupId>
        <artifactId>http-client-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

Версию указывать не нужно — она управляется через BOM.

Если нужен только логгер:

```xml
<dependencies>
    <dependency>
        <groupId>ru.vych</groupId>
        <artifactId>logger-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

Если нужны оба:

```xml
<dependencies>
    <dependency>
        <groupId>ru.vych</groupId>
        <artifactId>logger-spring-boot-starter</artifactId>
    </dependency>

    <dependency>
        <groupId>ru.vych</groupId>
        <artifactId>http-client-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

Подключение BOM само по себе **не подключает стартеры**. Оно только определяет их версии.

## GitHub Packages

Опубликованные артефакты тулкита доступны через GitHub Packages.

Для Maven необходимо настроить репозиторий и авторизацию в `~/.m2/settings.xml`.

Пример репозитория:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/vyacheslavchernov/vych-spring-toolkit</url>
    </repository>
</repositories>
```

Данные для авторизации должны находиться в `settings.xml` и соответствовать `id` репозитория:

```xml
<servers>
    <server>
        <id>github</id>
        <username>YOUR_GITHUB_USERNAME</username>
        <password>YOUR_GITHUB_TOKEN</password>
    </server>
</servers>
```

После этого Maven сможет получать BOM и необходимые Starter'ы из GitHub Packages.

## Пример итогового `pom.xml`

Минимальный пример проекта, использующего только HTTP-клиент:

```xml
<project>
    <!-- ... -->

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>ru.vych</groupId>
                <artifactId>vych-spring-toolkit-bom</artifactId>
                <version>VERSION</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>ru.vych</groupId>
            <artifactId>http-client-spring-boot-starter</artifactId>
        </dependency>
    </dependencies>

</project>
```

Таким образом, новый проект подключает только необходимую функциональность, а версии всех компонентов 
`Vych Spring Toolkit` централизованно определяются BOM.
