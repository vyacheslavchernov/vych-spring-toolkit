# Vych Spring Toolkit — BOM

Bill of Materials (BOM) для управления версиями модулей тулкита.

## Назначение

Определяет версии всех компонентов `Vych Spring Toolkit` в одном месте. Благодаря BOM в проекте-потребителе не требуется указывать версию каждого подключаемого стартера отдельно.

## Состав модулей

BOM управляет версиями следующих модулей:

| Модуль | Описание |
|---|---|
| `http-client-spring-boot-starter` | Spring Boot Starter для работы с HTTP-клиентом (builder-паттерн, chain API, interceptors) |
| `logger-spring-boot-starter` | Spring Boot Starter для кастомного логгера с ANSI-раскраской и настраиваемыми аппендерами |

## Подключение

Добавьте BOM в `dependencyManagement` вашего проекта:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>ru.vych</groupId>
            <artifactId>vych-spring-toolkit-bom</artifactId>
            <version>0.0.5-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Подключаете только нужные стартеры, версию указывать не нужно -->
    <dependency>
        <groupId>ru.vych</groupId>
        <artifactId>http-client-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>ru.vych</groupId>
        <artifactId>logger-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

После подключения BOM вы можете добавлять любые стартеры из toolkit'а — версия будет подтянута автоматически из BOM.

## Использование

### Пример: подключение только HTTP-клиента

```xml
<dependencies>
    <dependency>
        <groupId>ru.vych</groupId>
        <artifactId>http-client-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

### Пример: подключение всех модулей

```xml
<dependencies>
    <dependency>
        <groupId>ru.vych</groupId>
        <artifactId>http-client-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>ru.vych</groupId>
        <artifactId>logger-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

### Обновление версии

Чтобы обновить все модули toolkit'а в вашем проекте, достаточно изменить версию BOM в одном месте:

```xml
<dependency>
    <groupId>ru.vych</groupId>
    <artifactId>vych-spring-toolkit-bom</artifactId>
    <version>0.0.6-SNAPSHOT</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

Все стартеры, подключённые через BOM, автоматически получат новые версии.

## Как это работает

BOM (Bill of Materials) — это POM-файл с `<type>pom</type>`, который содержит только `<dependencyManagement>`. При импорте через `<scope>import</scope>` Maven подменяет ваш `dependencyManagement` зависимостями из BOM. Это означает:

* Все артефакты из BOM получают централизованное управление версиями
* При добавлении зависимости без версии Maven автоматически подставляет версию из BOM
* Можно подключать любые комбинации стартеров без конфликтов версий
