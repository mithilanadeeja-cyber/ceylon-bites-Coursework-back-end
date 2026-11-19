1 Ceylon Bites — Backend Coursework

CMJD Batch 112 · Backend Development with Spring Boot and MySQL

Student name: Y.M.M Nadeeja 

Backend repository: https://github.com/mithilanadeeja-cyber/ceylon-bites-Coursework-back-end


2 Recommended Windows setup (Docker)

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



Stop safely: `docker compose down`. Start again: `docker compose up -d`. The database persists in a Docker volume. Do not use `down -v` unless you intend to erase its data. Changing .env after first initialization does not reset existing MySQL/admin passwords.




3 Tests and packaging

```bash
mvn verify
```

Nine backend integration tests passed before packaging. Tests use an isolated H2 database in MySQL compatibility mode. Separate live MySQL 8.4.7 HTTP and restart-persistence checks also passed; see docs/VERIFICATION.md. Docker/Windows setup was documented but not directly executed in the build environment.

The executable JAR is generated at target/food-ordering-api-1.0.0.jar. Run it with `java -jar target/food-ordering-api-1.0.0.jar` after setting the same environment variables. 

Optional HTTP smoke test against a disposable running database: `python scripts/api_smoke_test.py`. It requests an administrator password if ADMIN_PASSWORD is not in the environment and creates fictional test records. Do not run it against a production database.


4 Features and design


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





