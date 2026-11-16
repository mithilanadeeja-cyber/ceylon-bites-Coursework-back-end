# Ceylon Bites — Backend Coursework

CMJD Batch 112 · Backend Development with Spring Boot and MySQL

Student name: Y.M.M Nadeeja 

Backend repository: https://github.com/mithilanadeeja-cyber/ceylon-bites-Coursework-back-end


## What this repository contains

Spring Boot, Spring Data JPA, Spring Security JWT, MySQL/Flyway schema, eight entities, controller/service/repository layers, validation, logging, exception handling, role/ownership checks and integration tests. The React frontend is submitted in a separate repository.

## Recommended Windows setup (Docker)

1. Start Docker Desktop (Linux containers).
2. Extract this archive and open **ceylon-bites-backend** in VS Code. pom.xml and compose.yaml should be directly inside this folder.
3. Run from its PowerShell terminal:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\setup.ps1
docker compose up --build -d
```

4. Check `docker compose logs -f backend` until startup completes. Press Ctrl+C to stop viewing logs.
5. Open http://localhost:8080/api/health. It should return status UP.
6. Start the frontend separately using npm run dev in its own folder.

The setup script generates private configuration in .env. The initial admin email is admin@ceylon.test; find ADMIN_PASSWORD inside .env. MySQL is started by this backend's compose.yaml. No separate MySQL installation is needed with Docker. New customers register through the frontend or API.

macOS/Linux: `sh scripts/setup.sh`, then `docker compose up --build -d`.

Stop safely: `docker compose down`. Start again: `docker compose up -d`. The database persists in a Docker volume. Do not use `down -v` unless you intend to erase its data. Changing .env after first initialization does not reset existing MySQL/admin passwords.

## Manual setup without Docker

Install a full JDK 21, Maven 3.9+ and MySQL 8.4. Create the database and an application user using MySQL Workbench:

```sql
CREATE DATABASE ceylon_bites CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ceylon'@'localhost' IDENTIFIED BY 'REPLACE_WITH_YOUR_DB_PASSWORD';
GRANT ALL PRIVILEGES ON ceylon_bites.* TO 'ceylon'@'localhost';
```

From this backend folder in PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\setup.ps1
Get-Content .env | Where-Object { $_ -match '^[A-Z_]+=' } | ForEach-Object {
  $parts = $_ -split '=', 2
  [Environment]::SetEnvironmentVariable($parts[0], $parts[1], 'Process')
}
$env:DB_USER = 'ceylon'
$env:DB_PASSWORD = 'REPLACE_WITH_YOUR_DB_PASSWORD'
$env:DB_URL = 'jdbc:mysql://localhost:3306/ceylon_bites?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
mvn spring-boot:run
```

Use your actual database password. Leave this terminal running. Flyway creates the schema automatically; do not manually execute the migration as well.

## Tests and packaging

```bash
mvn verify
```

Nine backend integration tests passed before packaging. Tests use an isolated H2 database in MySQL compatibility mode. Separate live MySQL 8.4.7 HTTP and restart-persistence checks also passed; see docs/VERIFICATION.md. Docker/Windows setup was documented but not directly executed in the build environment.

The executable JAR is generated at target/food-ordering-api-1.0.0.jar. Run it with `java -jar target/food-ordering-api-1.0.0.jar` after setting the same environment variables. It is not necessary to commit the JAR or target/ to GitHub.

Optional HTTP smoke test against a disposable running database: `python scripts/api_smoke_test.py`. It requests an administrator password if ADMIN_PASSWORD is not in the environment and creates fictional test records. Do not run it against a production database.

## Features and design

- User: ADMIN / CUSTOMER; registration cannot select ADMIN.
- Food: AVAILABLE / OUT_OF_STOCK; CRUD and soft deletion.
- Category: one-to-many foods.
- Cart: one per user, persistent items, quantity validation.
- Order: PLACED / PREPARING / DELIVERED / CANCELLED, validated transitions.
- Order items preserve historical price/name values.
- Payment: PENDING / COMPLETED / FAILED, cash on delivery.
- JWT uses HS256, issuer/expiry validation and environment-supplied secret; passwords use BCrypt.
- Server calculates totals; checkout transaction writes order/payment and clears the cart together.
- Customers access only their own records. Admin manages the catalog/orders/payments.

Read docs/API.md and docs/ARCHITECTURE.md for endpoint examples, entity diagram and business rules. src/main/resources/db/migration/V1__schema.sql is the database schema. The optional MERN migration bonus is not implemented. Payment records do not process bank cards or arrange actual delivery.





