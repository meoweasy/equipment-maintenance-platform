# Equipment Maintenance Platform

Проект состоит из двух Spring Boot микросервисов:

- `equipment-service` — управление оборудованием, порт `8081`;
- `maintenance-service` — управление обслуживанием, порт `8082`.

## Требования

- Docker Desktop с Linux containers;
- Docker Compose v2+.

Локальная установка Maven и Java для запуска через Docker не требуется.

## Первый запуск

Из корня проекта выполните:

```powershell
docker compose up --build -d
```

При первой сборке Docker скачает базовые образы и Maven-зависимости. Maven-кэш
сохраняется BuildKit и переиспользуется при следующих сборках.

Проверить состояние:

```powershell
docker compose ps
docker compose logs -f equipment-service
```

Equipment Service будет доступен по адресу `http://localhost:8081`.

## Повторный запуск

Если исходный код и Dockerfile не менялись, пересборка не нужна:

```powershell
docker compose up -d
```

После изменения Java-кода выполните:

```powershell
docker compose up --build -d
```

Зависимости повторно не скачиваются, пока доступен BuildKit Maven-кэш.

## Остановка

Остановить контейнеры, сохранив данные PostgreSQL:

```powershell
docker compose down
```

Удалить контейнеры вместе с данными БД:

```powershell
docker compose down -v
```

Команда с `-v` необратимо удаляет локальные данные PostgreSQL.

## База Equipment Service

- контейнер: `equipment-db`;
- база: `equipment_db`;
- пользователь: `equipment`;
- локальный порт: `5433`;
- JDBC URL вне Docker: `jdbc:postgresql://localhost:5433/equipment_db`.

Схема создаётся Liquibase при старте Equipment Service.

## Equipment API

### Типы оборудования

- `POST /api/equipment-types`
- `GET /api/equipment-types?pageNumber=0&pageSize=20`
- `GET /api/equipment-types/{id}`
- `PUT /api/equipment-types/{id}`
- `DELETE /api/equipment-types/{id}`

### Оборудование

- `POST /api/equipment`
- `GET /api/equipment?pageNumber=0&pageSize=20`
- `GET /api/equipment/{id}`
- `PUT /api/equipment/{id}`
- `DELETE /api/equipment/{id}`

### Internal API

- `GET /internal/equipment/{id}`
- `PATCH /internal/equipment/{id}/status?status=UNDER_MAINTENANCE`

Поддерживаемые статусы: `AVAILABLE`, `UNDER_MAINTENANCE`, `DECOMMISSIONED`.
Переход из `UNDER_MAINTENANCE` непосредственно в `DECOMMISSIONED` запрещён.