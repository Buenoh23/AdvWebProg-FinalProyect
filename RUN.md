# Campus Store - Final Project Run Instructions

## Prerequisites
This project uses Java 17, Spring Boot, and MariaDB.

## 1. Start the Database
Ensure your MariaDB service is running before starting the application:

`sudo service mariadb start`

*Note: The application is configured to automatically create the `campus_store` database and all necessary tables.

## 2. Start the Application
Run the following command in the root directory of the project:

`./mvnw spring-boot:run`

## 3. Access the Application
Once the application has started, open your browser and navigate to:
**http://localhost:8080**

## 4. Seed Data & Credentials
The application utilizes a `DataSeeder` that automatically injects essential testing data when the server starts. 

**Default Admin Account:**
* **Email:** admin@example.com
* **Password:** admin123
* *Note: This account has exclusive access to the `/admin/**` dashboard routes.*

**Test Products:**
The database is pre-seeded with 6 active products across two categories ("Apparel" and "Electronics") to immediately demonstrate pagination (size=5) and filtering capabilities without manual setup.