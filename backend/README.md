# Duck Store backend

See the [project README](../README.md) for database setup, environment variables, API examples, pricing assumptions, frontend instructions, and test coverage.

Quick start from this directory:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/duck_store"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-local-postgres-password"
mvn clean test
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. PostgreSQL is required. Docker and Testcontainers are not required.
