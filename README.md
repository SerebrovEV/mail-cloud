# Тестовое задание

Разработано 2 микросервиса (core и mail) на Spring Boot.

## Микросервис MAIL

- Получает события от микросервиса **CORE** через брокер сообщений и отправляет на email пользователя письмо с соответствующим описанием события.

Для сборки проекта в модуле MAIL используется `build.gradle`. `mail/build.gradle`

### Технологии в проекте

- Java 21
- Spring Boot
- Spring Data Mail
- Log4j
- RabbitMQ
- Lombok
- Gradle

## Микросервис CORE

- Реализует авторизацию и аутентификацию пользователей.
- У пользователя может быть роль USER или MODERATOR.

Для сборки проекта в модуле CORE используется `build.gradle`. `core/build.gradle`

### Технологии в проекте

- Java 21
- Spring Boot
- Spring Data JPA
- Spring Security
- AWS Java SDK для Amazon S3
- Log4j
- PostgreSQL
- Hibernate
- RabbitMQ
- Lombok
- Gradle

### Эндпоинты

- **`http://localhost:xxxx/login`** - эндпоинт для авторизации пользователя
- **`http://localhost:xxxx/register`** - эндпоинт для регистрации пользователей
- **`http://localhost:xxxx/images/upload`** - эндпоинт для загрузки списка изображений (jpg или png), каждое весом до 10 MB, на облачное хранилище
- **`http://localhost:xxxx/images/get`** - эндпоинт для получения списка загруженных изображений с возможностью сортировки и фильтрации по дате загрузки, id и размеру
- **`http://localhost:xxxx/images/getUserImages`** - эндпоинт для получения списка всех загруженных пользователями изображений с возможностью сортировки и фильтрации по дате загрузки, id и размеру (доступно модераторам)
- **`http://localhost:xxxx/images/download/{id}`** - эндпоинт для скачивания изображения. Запрос отклоняется, если изображение не принадлежит пользователю.
- **`http://localhost:xxxx/users/{id}/blockUser`** - эндпоинт для блокировки или разблокировки пользователя (доступно модераторам).

## Запуск приложения с помощью Docker Compose

Это приложение состоит из нескольких сервисов, которые работают вместе: основной сервис, сервис для обработки почты, база данных PostgreSQL и RabbitMQ. Все сервисы управляются с помощью Docker Compose.

### Структура проекта

├── core  Основной сервис

├── mail Сервис для обработки почты

├── docker-compose.yml

└── README.md

### Запуск приложения

1. Клонируйте репозиторий или скачайте проект на ваш локальный компьютер.

2. Перейдите в директорию проекта:

   ```bash
   cd /path/to/your/project
   
3. Создайте файл .env для хранения конфиденциальных данных (подключение к базе, подключение к RabbitMQ, подключение к Cloud и почтовый сервер).

```
# URL подключения к базе данных
SPRING_DATASOURCE_URL=  

# Имя базы данных
SPRING_DATASOURCE_NAME=

# Имя пользователя для подключения к базе данных
SPRING_DATASOURCE_USERNAME=

# Пароль для подключения к базе данных
SPRING_DATASOURCE_PASSWORD=

# Хост RabbitMQ
SPRING_RABBITMQ_HOST=

# Пользователь RabbitMQ
SPRING_RABBITMQ_USERNAME=

# Пароль RabbitMQ
SPRING_RABBITMQ_PASSWORD=

# Имя очереди RabbitMQ
RABBIT_QUEUE_NAME=

# Ключ доступа к облаку
CLOUD_ACCESS_KEY=

# Секретный ключ облака
CLOUD_SECRET_KEY=

# Имя облачного хранилища
CLOUD_BUCKET_NAME=

# Максимальный размер файла (по умолчанию 10Мб)
FILE_MAX_SIZE=

# Максимальный размер запроса (по умолчанию 50Мб)
MAX_REQUEST_SIZE=

# Хост почтового сервера
SPRING_MAIL_HOST=

# Имя пользователя почтового сервера
SPRING_MAIL_USERNAME=

# Пароль почтового сервера
SPRING_MAIL_PASSWORD=
```
4. Запустите все сервисы с помощью Docker Compose:

```bash
docker-compose up --build
```
5. Дождитесь завершения инициализации всех сервисов. Вы должны увидеть вывод, показывающий, что все контейнеры успешно запущены.

### Остановка приложения

Чтобы остановить все запущенные сервисы, выполните:

```bash
docker-compose down
```
