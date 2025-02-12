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
   git clone https://github.com/your-username/democrudproject.git
