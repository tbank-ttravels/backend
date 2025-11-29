# TTravels Backend

Бэкенд для совместного планирования путешествий. Проект готов к сборке как jar и к развёртыванию в Docker.

## Требования

- Java 21+
- Maven 3.9+
- Docker и Docker Compose v2 (для контейнеризации)
- PostgreSQL

## Конфигурация

- Локально: скопируйте `src/main/resources/application-local.properties.example` в
  `src/main/resources/application-local.properties` и задайте значения.
- Сервер: создайте `.env` из `.env.example` (используется в `docker-compose.yml`) или задайте переменные окружения,
  которые читает профиль `prod` (`src/main/resources/application-prod.properties`).

### Основные переменные окружения

- `SPRING_PROFILES_ACTIVE` — профиль (`local` или `prod`)
- `SERVER_PORT` — внешний порт приложения (по умолчанию 8080)
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` — строка подключения и учётные данные PostgreSQL
- `JWT_ISSUER`, `JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET`, `JWT_ACCESS_TTL`, `JWT_REFRESH_TTL` — параметры для выпуска и
  валидации JWT

## Сборка и тесты

- `mvn clean verify` — полный прогон тестов
- `mvn clean package -DskipTests` — сборка fat-jar без тестов (артефакт будет в `target/`)

## Запуск для разработки
```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

Базовый путь API: `/api/v1`.

## Развёртывание на сервере

### Через Docker Compose

1. Скопируйте `.env.example` в `.env` и укажите реальные значения (особенно JWT-секреты).
2. Соберите и поднимите сервисы:
   ```bash
   docker compose up -d --build
   ```
3. Приложение будет доступно на `http://<host>:SERVER_PORT/api/v1`. PostgreSQL живёт в контейнере `db`, данные
   сохраняются в томе `postgres-data`. Liquibase применит миграции при старте.

## Полезное

- Swagger UI доступен по `http://<host>:PORT/api/v1/swagger-ui.html`.
