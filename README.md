# Duck Store

Warehouse inventory and order-pricing exercise built with Java 21, Spring Boot, PostgreSQL, Flyway, and React.

## Scope

- Warehouse API: create, list, read, update, and soft-delete ducks.
- Inventory invariant: an active row is unique by `(color, size, price)`. Adding a matching duck atomically increments its quantity.
- Order pricing API: determines package/protection materials and returns an itemized price calculation.
- React warehouse UI: create/edit inventory, show stock sorted by quantity, and soft-delete records.
- Unit tests for pricing boundaries, packaging, service behavior, and conflicts.

This is a coding-exercise implementation, not a guarantee of production readiness. Verify environment-specific security, performance, backups, monitoring, and release procedures before any production deployment.

## Requirements

- Java 21
- Maven 3.9+
- PostgreSQL 15+
- Node.js 20+ and npm

Docker and Testcontainers are not required.

## 1. Create the database

Create an empty database using `psql` or pgAdmin:

```sql
CREATE DATABASE duck_store;
```

Flyway applies migrations at startup. `V2` upgrades the early prototype schema (`name`/`breed`) to the assignment's `color`/`size`/`price` model. Those old fields cannot be mapped reliably, so legacy prototype rows are retained but soft-deleted. Review the migration before applying it to any database containing data you need.

## 2. Configure and run the backend

From the `backend` directory, configure the environment variables for your PostgreSQL installation.

PowerShell example:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/duck_store"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-local-postgres-password"
mvn clean test
mvn spring-boot:run
```

The backend listens on `http://localhost:8080`.

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/duck_store` | JDBC connection URL |
| `DB_USERNAME` | `postgres` | Database user |
| `DB_PASSWORD` | `postgres` | Database password; override locally |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated allowed browser origins |

Do not commit real credentials. Use environment variables or a managed secret store.

## 3. Run the React frontend

In a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

If the backend runs at a different URL, set `VITE_API_BASE_URL` before starting Vite:

```powershell
$env:VITE_API_BASE_URL = "http://localhost:8080"
npm run dev
```

Build the frontend with `npm run build`.

## API

### Warehouse

| Method | Endpoint | Behavior |
|---|---|---|
| `POST` | `/api/ducks` | Create or merge quantity for matching active color/size/price |
| `GET` | `/api/ducks` | List active ducks sorted by quantity ascending, then ID |
| `GET` | `/api/ducks/{id}` | Read one active duck |
| `PUT` | `/api/ducks/{id}` | Update price and quantity only |
| `DELETE` | `/api/ducks/{id}` | Soft-delete an active duck |

Create request:

```json
{
  "color": "Red",
  "size": "XLarge",
  "price": 12.50,
  "quantity": 10
}
```

Update request:

```json
{
  "price": 13.00,
  "quantity": 15
}
```

Allowed colors: `Red`, `Green`, `Yellow`, `Black`. Allowed sizes: `XLarge`, `Large`, `Medium`, `Small`, `XSmall`. Enum parsing is case-insensitive.

### Order pricing

`POST /api/orders`

```json
{
  "color": "Red",
  "size": "XLarge",
  "quantity": 101,
  "destinationCountry": "India",
  "shippingMode": "Land"
}
```

The order request has no unit price. This implementation deliberately uses the **lowest active unit price** for the requested color and size; ties are resolved by ID. If no active matching inventory exists, the API returns `404`.

## Pricing assumptions

The exercise lists percentage adjustments but does not specify whether percentages compound. This implementation calculates each percentage against the base amount (`unit price × quantity`) and then adds the signed line items:

- More than 100 units: subtract 20% of base.
- Wood package: add 5% of base.
- Plastic package: add 10% of base.
- Cardboard package: subtract 1% of base.
- USA: add 18% of base; Bolivia: 13%; India: 19%; other destinations: 15%.
- Sea shipping: add $400 flat.
- Land shipping: add $10 per unit.
- Air shipping: add $30 per unit; orders above 1,000 units receive a 15% reduction on the air charge.

Amounts use `BigDecimal` and two-decimal `HALF_UP` rounding. Discounts appear as negative amounts in the breakdown. Accepted country aliases include `US`, `USA`, `United States`, `BO`, `Bolivia`, `IN`, and `India`; unknown countries use the "other" rate.

Packaging:

| Size | Package |
|---|---|
| XLarge, Large | Wood |
| Medium | Cardboard |
| Small, XSmall | Plastic |

Protection rules:

- Air + wood/cardboard: polystyrene balls.
- Air + plastic: bubble-wrap bags.
- Land: polystyrene balls.
- Sea: moisture-absorbing beads and bubble-wrap bags.

## Tests and edge cases

Run backend tests:

```powershell
cd backend
mvn clean test
```

Tests cover pricing boundaries at 100/101 and 1,000/1,001 units, country rates and aliases, packaging selection, protection materials, invalid prices/quantities, duplicate-update conflicts, and missing inventory.

Inventory merging uses PostgreSQL `INSERT ... ON CONFLICT` backed by a partial unique index. The unit tests mock the repository and do not independently prove PostgreSQL concurrency behavior. After starting the backend, run the black-box concurrency smoke test from the repository root:

```powershell
python scripts/concurrency-smoke.py
```

It sends concurrent create requests with a unique test price, checks that one active row contains the expected summed quantity, and soft-deletes its test row afterward. Before release, also test against representative production-like traffic and database configuration.

## Git workflow

Commit coherent, verified changes (feature, regression fix, tests, documentation). Keep commit dates authentic. A clean commit history is useful evidence of development, but it does not replace passing tests or the ability to explain the implementation.
