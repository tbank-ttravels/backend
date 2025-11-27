## Требования

- JDK 21+
- Maven 3.9+
- PostgreSQL

## Подготовка окружения

1. **Склонируйте репозиторий** и перейдите в директорию проекта.
2. **Создайте файл `application-local.properties`** в папке `src/main/resources/` на основе
   `application-local.properties.example`.
3. Заполните значения:
   ```properties
   server.port=8081

   spring.datasource.url=jdbc:postgresql://localhost:5432/ttravels_db
   spring.datasource.username=postgres
   spring.datasource.password=postgres

   app.security.jwt.issuer=ttravels-backend
   app.security.jwt.access-secret=<секрет_для_access>
   app.security.jwt.refresh-secret=<секрет_для_refresh>
   ```
4. **Запустите PostgreSQL локально**

## Сборка и запуск

```bash
# Сборка
mvn clean verify

# Запуск приложения в локальном профиле
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```