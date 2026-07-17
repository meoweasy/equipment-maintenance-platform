# Equipment Maintenance Platform

Учебная платформа для учёта оборудования и заявок на его обслуживание. Проект построен на Spring Boot и состоит из двух микросервисов, API Gateway и двух отдельных баз PostgreSQL.

## Состав системы

- `equipment-service` — типы оборудования, оборудование и его статусы;
- `maintenance-service` — заявки на обслуживание, фильтрация и изменение статусов заявок;
- `gateway` — единая публичная точка входа на `http://localhost:8080`;
- `equipment-db` — база данных оборудования;
- `maintenance-db` — база данных заявок;
- `platform-common` — общие исключения, обработка ошибок, пагинация и работа с ETag.

Клиент обращается только к gateway. Прямые порты микросервисов наружу не публикуются. Маршруты `/internal/**` используются только для взаимодействия микросервисов внутри Docker-сети.

## Требования

Для запуска необходимы:

- Docker Desktop с Linux containers;
- Docker Compose v2 или новее.

Локально устанавливать Java, Maven и PostgreSQL не требуется — сборка и запуск выполняются в Docker.

## Запуск проекта

### 1. Открыть каталог проекта

```powershell
cd D:\IntelliJProjects\SMBTechTest\equipment-maintenance-platform
```

### 2. Собрать и запустить все контейнеры

```powershell
docker compose up --build -d
```

При первом запуске Docker скачает необходимые образы и Maven-зависимости. Liquibase автоматически создаст схемы обеих баз данных.

### 3. Проверить состояние контейнеров

```powershell
docker compose ps
```

Контейнеры сервисов и баз данных должны находиться в состоянии `Up`, базы данных — `healthy`.

### 4. При необходимости посмотреть логи

```powershell
docker compose logs -f equipment-service
docker compose logs -f maintenance-service
```

Для выхода из просмотра логов нажмите `Ctrl+C`.

## Повторный запуск

Если исходный код не менялся:

```powershell
docker compose up -d
```

После изменения Java-кода, зависимостей или Dockerfile:

```powershell
docker compose up --build -d
```

## Остановка

Остановить контейнеры, сохранив данные PostgreSQL:

```powershell
docker compose down
```

Остановить контейнеры и удалить локальные данные обеих баз:

```powershell
docker compose down -v
```

Команда с `-v` необратимо удаляет Docker volumes с данными PostgreSQL.

## Swagger

Документация доступна через gateway:

- Equipment Service: http://localhost:8080/equipment-docs/swagger-ui.html
- Maintenance Service: http://localhost:8080/maintenance-docs/swagger-ui.html

В правом верхнем углу Swagger можно выбрать группу API:

- `Публичное API` — методы, доступные клиентам через gateway;
- `Внутреннее API` — контракт межсервисных методов. Посмотреть его можно, но вызов через gateway вернёт `404`.

По умолчанию выбрана публичная группа. Кнопка `Try it out` отправляет запросы через `http://localhost:8080`.

## Публичное API

Базовый адрес для Postman и других клиентов:

```text
http://localhost:8080
```

### Типы оборудования

- `POST /api/equipment-types` — создать тип;
- `GET /api/equipment-types` — получить страницу типов;
- `GET /api/equipment-types/{id}` — получить тип по идентификатору;
- `PUT /api/equipment-types/{id}` — полностью изменить тип;
- `DELETE /api/equipment-types/{id}` — удалить тип.

Тип нельзя удалить, пока к нему привязано оборудование.

### Оборудование

- `POST /api/equipment` — создать оборудование;
- `GET /api/equipment` — получить страницу оборудования;
- `GET /api/equipment/{id}` — получить оборудование по идентификатору;
- `PUT /api/equipment/{id}` — полностью изменить оборудование;
- `DELETE /api/equipment/{id}` — удалить оборудование.

Оборудование нельзя удалить, если по нему существуют активные заявки.

Поддерживаемые статусы оборудования:

- `AVAILABLE`;
- `UNDER_MAINTENANCE`;
- `DECOMMISSIONED`.

Оборудование в статусе `DECOMMISSIONED` нельзя использовать для создания новой заявки.

### Заявки на обслуживание

- `POST /api/service-requests` — создать заявку;
- `GET /api/service-requests` — получить страницу заявок;
- `GET /api/service-requests/{id}` — получить заявку по идентификатору;
- `PUT /api/service-requests/{id}` — полностью изменить заявку;
- `DELETE /api/service-requests/{id}` — удалить заявку;
- `PATCH /api/service-requests/{id}/status?status=IN_PROGRESS` — изменить статус.

Список заявок поддерживает совместимые фильтры:

```text
GET /api/service-requests?status=NEW
GET /api/service-requests?priority=HIGH
GET /api/service-requests?equipmentId={uuid}
GET /api/service-requests?status=NEW&priority=HIGH
```

Поддерживаемые статусы заявок:

- `NEW`;
- `IN_PROGRESS`;
- `DONE`;
- `CANCELLED`.

Основные правила:

- новая заявка получает статус `NEW`;
- заявку нельзя создать для несуществующего или списанного оборудования;
- заявку `IN_PROGRESS` нельзя удалить;
- заявку `DONE` нельзя редактировать;
- из `DONE` и `CANCELLED` нельзя перейти в `IN_PROGRESS`;
- при переходе в `DONE` поле `completedAt` заполняется автоматически.

## Пагинация

Для списков используются query-параметры:

```text
?pageNumber=0&pageSize=20
```

- `pageNumber` начинается с `0`;
- `pageSize` должен быть от `1` до `20`;
- значения по умолчанию: страница `0`, размер `20`.

## ETag и конкурентные изменения

Ответы Equipment Service содержат заголовок `ETag`. Для изменения или удаления оборудования и типов оборудования необходимо передать его актуальное значение в заголовке `If-Match`:

```http
If-Match: "значение-etag"
```

Рекомендуемый порядок:

1. Выполнить `GET` и получить текущий `ETag` из заголовков ответа.
2. Передать его в `If-Match` при `PUT`, `PATCH` или `DELETE`.
3. Если объект успел изменить другой запрос, API вернёт `412 Precondition Failed`.
4. Если `If-Match` не передан, API вернёт `428 Precondition Required`.

## Внутреннее API

Межсервисные маршруты недоступны с хоста и через gateway:

- `GET /internal/equipment/{id}` — maintenance-service получает данные оборудования;
- `PATCH /internal/equipment/{id}/status` — внутреннее изменение статуса оборудования;
- `GET /internal/service-requests/active?equipmentId={uuid}` — equipment-service проверяет активные заявки перед удалением оборудования.

Микросервисы вызывают эти методы по Docker DNS внутри закрытой сети:

```text
http://equipment-service:8081/internal/...
http://maintenance-service:8082/internal/...
```

Запрос `http://localhost:8080/internal/...` намеренно возвращает `404`.

## Базы данных

### Equipment DB

- адрес: `localhost:5433`;
- база: `equipment_db`;
- пользователь: `equipment`;
- пароль: `equipment`;
- JDBC URL: `jdbc:postgresql://localhost:5433/equipment_db`.

### Maintenance DB

- адрес: `localhost:5434`;
- база: `maintenance_db`;
- пользователь: `maintenance`;
- пароль: `maintenance`;
- JDBC URL: `jdbc:postgresql://localhost:5434/maintenance_db`.

Схемы обеих баз управляются Liquibase.