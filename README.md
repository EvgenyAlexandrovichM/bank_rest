# 📘 Card Management API

API‑сервис для управления пользователями, банковскими картами и переводами.  
Поддерживает регистрацию и аутентификацию пользователей (JWT), операции с картами (создание, блокировка, переводы), а также административные функции.

---

## 🚀 Возможности
- Регистрация и аутентификация пользователей (JWT)
- Управление картами: создание, активация, блокировка, удаление
- Переводы между картами с проверкой баланса
- Роли пользователей (`ROLE_USER`, `ROLE_ADMIN`)
- Централизованная обработка ошибок
- Документация API в формате OpenAPI/Swagger

---

## 📂 Технологии
- Java 17
- Spring Boot 3.1.6 (Web, Security, Data JPA, Validation)
- PostgreSQL 15
- Liquibase 5.0.1
- Docker 27.x + Docker Compose v2.x
- JUnit 5, Mockito 5
- Lombok 1.18.28
- MapStruct 1.5.5.Final
- Springdoc OpenAPI 2.1.0

---

## ⚙️ Требования
- Docker и Docker Compose
- JDK 17 (для локального запуска без контейнеров)
- Maven

---

## 📂 Структура проекта
- src/main/java — исходный код приложения

- src/main/resources — конфигурация и ресурсы (application.yml, Liquibase)

- src/test/java — модульные и интеграционные тесты

- docs/ — документация (OpenAPI)

- docker-compose.yml — контейнеры приложения и БД

- .env — переменные окружения для запуска
- ---

## 🔑 Переменные окружения

| Переменная                  | Назначение                                          |
|------------------------------|----------------------------------------------------|
| `SPRING_DATASOURCE_URL`      | строка подключения к PostgreSQL                    |
| `SPRING_DATASOURCE_USERNAME` | имя пользователя БД                                |
| `SPRING_DATASOURCE_PASSWORD` | пароль БД                                          |
| `ADMIN_PASSWORD_BCRYPT`      | bcrypt‑хэш пароля для сидированного администратора |
| `JWT_SECRET_BASE64`          | секрет для подписи JWT (base64)                    |
| `CARD_ENCRYPTION_KEY_BASE64` | ключ для шифрования номеров карт (base64)          |
| `JWT_EXPIRATION_MS`          | время

---

## ⚡ Быстрый старт
### 1. Склонировать репозиторий:
```bash
git clone https://github.com/EvgenyAlexandrovichM/bank_rest.git
cd bank_rest
```

### 2. Создать файл .env и заполнить переменные окружения (см. таблицу выше).

### 3. Запустить проект:

- Запуск через Docker Compose
```bash
docker-compose up --build
```

- Запуск локально
```bash
mvn clean package -DskipTests
java -jar target/card-service-0.0.1-SNAPSHOT.jar
```

⚠️ Примечание

При запуске через `docker-compose` все переменные окружения автоматически подхватываются из файла `.env`.  
При локальном запуске (`java -jar ...`) их нужно выставить вручную в вашей системе (например, через PowerShell или Bash), иначе приложение не сможет создать `JwtServiceImpl` и упадёт с ошибкой `WeakKeyException`.

---

## 📑 Документация API

- OpenAPI спецификация: docs/openapi.yaml

- Swagger UI: http://localhost:8080/swagger-ui.html

---

## 👤 Доступы по умолчанию
- username: admin

- password: см. значение ADMIN_PASSWORD_BCRYPT в .env (bcrypt‑хэш)

---
## 📌 Примеры запросов

### 🔐 Вход

```bash
curl -X POST http://localhost:8080/api/auth/login \
-H "Content-Type: application/json" \
-d '{"username":"user","password":"P@ssw0rd!"}'
```

### 📝 Регистрация

```bash
curl -X POST http://localhost:8080/api/auth/register \
-H "Content-Type: application/json" \
-d '{"username":"newuser","password":"P@ssw0rd!"}'
```

### 💳 Перевод между картами

```bash
curl -X POST http://localhost:8080/api/cards/transfer \
-H "Authorization: Bearer <JWT>" \
-H "Content-Type: application/json" \
-d '{"fromCardId":1,"toCardId":2,"amount":100.00,"description":"Test transfer"}'
```

---

## 🧪 Тестирование
```bash
mvn test
```
---


