# Democrudproject

A demo Spring Boot CRUD application that manages users, supports CSV import, and provides a search endpoint. This project uses Spring Boot, JPA, and MySQL (with Testcontainers for integration testing) and is containerized using Docker and Docker Compose.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Building the Project](#building-the-project)
- [Running with Docker](#running-with-docker)
  - [Dockerfile](#dockerfile)
  - [docker-compose.yml](#docker-composeyml)
- [Manual Testing](#manual-testing)
  - [Testing with Postman](#testing-with-postman)
  - [Testing with Curl](#testing-with-curl)
    - [Create a New User](#create-a-new-user)
    - [Get a User by ID](#get-a-user-by-id)
    - [Get All Users](#get-all-users)
    - [Update a User](#update-a-user)
    - [Delete a User](#delete-a-user)
    - [Search Users](#search-users)
    - [CSV Import](#csv-import)
- [Notes](#notes)

## Prerequisites

- **Java 21** (or the specified Java version in the [pom.xml](./pom.xml))
- **Maven 3.8+**
- **Docker** and **Docker Compose**
- A REST client like **Postman** or **cURL** (for manual API testing)

## Building the Project

1. **Clone the repository:**

   ```bash
   git clone https://github.com/StefanGMK/democrudproject.git
   cd democrudproject
   ```

2. **Build the project using Maven:**

   ```bash
   mvn clean package
   ```

   This command compiles the code, runs the tests, and produces a JAR file under the `target` directory.

## Running with Docker

### Dockerfile

A sample `Dockerfile` is provided that uses an official OpenJDK image and copies the built JAR file:

```dockerfile
# Use the official OpenJDK image
FROM openjdk:21-slim

# Create a directory for the app
WORKDIR /app

# Copy the built jar file into the container
COPY target/democrudproject-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml

The provided `docker-compose.yml` configures both the application and a MySQL database:

```yaml
version: '3.8'
services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/mydb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: mypassword
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
    depends_on:
      db:
        condition: service_healthy

  db:
    image: mysql:8.0
    container_name: mysql-db
    environment:
      MYSQL_DATABASE: mydb
      MYSQL_ROOT_PASSWORD: mypassword
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql-data:
```

3. **Run the application using Docker Compose:**

   ```bash
   docker-compose up --build
   ```

   This command builds the Docker image, starts the MySQL container, and then starts your Spring Boot application on port 8080.

## Manual Testing

Once the application is running (locally on port 8080), you can test the endpoints with either Postman or cURL.

### Testing with Postman

#### Create a New User
POST http://localhost:8080/api/users

Body: (raw JSON)
```json
{
  "name": "Alice",
  "surname": "Wonderland",
  "email": "alice@example.com",
  "address": "123 Wonderland Ave"
}
```

#### Get a User by ID
```
GET http://localhost:8080/api/users/1
```

#### Get All Users
```
GET http://localhost:8080/api/users
```

#### Update a User
PUT http://localhost:8080/api/users/1

Body: (raw JSON)
```json
{
  "name": "Alice",
  "surname": "Liddell",
  "email": "alice@example.com",
  "address": "123 Wonderland Ave"
}
```

#### Delete a User
```
DELETE http://localhost:8080/api/users/1
```

#### Search for Users
```
GET http://localhost:8080/api/users/search?name=Alice&surname=Liddell
```

#### CSV Import
```
POST http://localhost:8080/api/users/import
```
Body: select your CSV file.
Example CSV content:
```csv
name,surname,email,address
Bob,Builder,bob@example.com,456 Build St
Charlie,Chocolate,charlie@example.com,789 Sweet St
```

### Testing with cURL

#### Create a User
```bash
curl -X POST http://localhost:8080/api/users \
     -H "Content-Type: application/json" \
     -d '{"name":"Alice","surname":"Wonderland","email":"alice@example.com","address":"123 Wonderland Ave"}'
```

#### Get a User by ID
```bash
curl http://localhost:8080/api/users/1
```

#### Get All Users
```bash
curl http://localhost:8080/api/users
```

#### Update a User
```bash
curl -X PUT http://localhost:8080/api/users/1 \
     -H "Content-Type: application/json" \
     -d '{"name":"Alice","surname":"Liddell","email":"alice@example.com","address":"123 Wonderland Ave"}'
```

#### Delete a User
```bash
curl -X DELETE http://localhost:8080/api/users/1
```

#### Search for Users
```bash
curl "http://localhost:8080/api/users/search?name=Alice&surname=Liddell"
```

#### CSV Import
users.csv
```csv
name,surname,email,address
Bob,Builder,bob@example.com,456 Build St
Charlie,Chocolate,charlie@example.com,789 Sweet St
```
```bash
curl -X POST http://localhost:8080/api/users/import \
     -H "Content-Type: multipart/form-data" \
     -F "file=@users.csv"
```

## Notes

- **Profiles:** The application is configured with a `dev` profile for Dockerized integration testing.
- **Docker Compose:** Ensure that the environment variables in your compose file match your application configuration.
- **Testing:** Integration tests are set up using Testcontainers.
