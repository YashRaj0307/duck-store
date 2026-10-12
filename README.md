# Duck Store

Warehouse inventory and order-pricing exercise built with Java 21,
Spring Boot, PostgreSQL, Flyway, and React.

## Scope

-   **Warehouse API:** create, list, read, update, and soft-delete
    ducks.
-   **Inventory invariant:** an active row is unique by
    `(color, size, price)`. Creating a matching duck atomically
    increments its quantity.
-   **Order-pricing API:** determines package and protection materials
    and returns an itemized price calculation.
-   **React warehouse UI:** create and edit inventory, view stock sorted
    by quantity, and soft-delete records.
-   **Tests:** service unit tests, pricing and packaging checks,
    validation checks, and a PostgreSQL concurrency smoke test.

This is a coding-exercise implementation, not a guarantee of production
readiness. Verify environment-specific security, performance, backups,
monitoring, and release procedures before production deployment.

## Requirements

-   Java 21
-   Maven 3.9+
-   PostgreSQL 15+
-   Node.js 20+ and npm

Docker and Testcontainers are not required.

## 1. Create the database

Create an empty database using `psql` or pgAdmin:

``` sql
CREATE DATABASE duck_store;
```

Flyway applies migrations at startup. Migration `V2` upgrades the early
prototype schema (`name`/`breed`) to the assignment's
`color`/`size`/`price` model. The old fields cannot be mapped reliably,
so legacy prototype rows are retained but soft-deleted. Review the
migration before applying it to a database containing data you need.

## 2. Configure and run the backend

From the `backend` directory, configure the environment variables for
your PostgreSQL installation.

PowerShell example:

``` powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/duck_store"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-local-postgres-password"
mvn clean test
mvn spring-boot:run
```

The backend listens on `http://localhost:8080`.

  ------------------------------------------------------------------------------------------------
  Variable                 Default                                         Purpose
  ------------------------ ----------------------------------------------- -----------------------
  `DB_URL`                 `jdbc:postgresql://localhost:5432/duck_store`   JDBC connection URL

  `DB_USERNAME`            `postgres`                                      Database user

  `DB_PASSWORD`            `Postgres@123`                                  Development fallback
                                                                           password; override with
                                                                           your local password

  `CORS_ALLOWED_ORIGINS`   `http://localhost:5173`                         Comma-separated allowed
                                                                           browser origins
  ------------------------------------------------------------------------------------------------

The password default above matches
`backend/src/main/resources/application.yml`. For local setup, set
`DB_PASSWORD` explicitly to the password configured for your PostgreSQL
user. Do not commit real credentials. For production, use a managed
secret store and avoid default credentials.

## 3. Run the React frontend

In a second terminal, from the repository root:

``` powershell
cd frontend
npm ci
npm run dev
```

Open `http://localhost:5173`.

`npm ci` installs the exact dependency versions from
`package-lock.json`. If you intentionally change dependencies, use
`npm install` and commit the updated lockfile.

If the backend runs at a different URL, set `VITE_API_BASE_URL` before
starting Vite:

``` powershell
$env:VITE_API_BASE_URL = "http://localhost:8080"
npm run dev
```

Build the frontend for production with:

``` powershell
npm run build
```

The generated production files are written to `frontend/dist`.

## API

### Warehouse

  -----------------------------------------------------------------------
  Method                  Endpoint                Behavior
  ----------------------- ----------------------- -----------------------
  `POST`                  `/api/ducks`            Create a duck or merge
                                                  quantity into a
                                                  matching active
                                                  color/size/price row

  `GET`                   `/api/ducks`            List active ducks
                                                  sorted by quantity
                                                  ascending, then ID

  `GET`                   `/api/ducks/{id}`       Read one active duck

  `PUT`                   `/api/ducks/{id}`       Update price and
                                                  quantity only

  `DELETE`                `/api/ducks/{id}`       Soft-delete an active
                                                  duck
  -----------------------------------------------------------------------

Create request:

``` json
{
  "color": "Red",
  "size": "XLarge",
  "price": 12.50,
  "quantity": 10
}
```

Update request:

``` json
{
  "price": 13.00,
  "quantity": 15
}
```

Allowed colors: `Red`, `Green`, `Yellow`, `Black`.

Allowed sizes: `XLarge`, `Large`, `Medium`, `Small`, `XSmall`.

Enum parsing is case-insensitive.

### Order pricing

`POST /api/orders`

``` json
{
  "color": "Red",
  "size": "XLarge",
  "quantity": 101,
  "destinationCountry": "India",
  "shippingMode": "Land"
}
```

The order request does not accept a unit price. This implementation
deliberately uses the **lowest active unit price** for the requested
color and size; ties are resolved by ID. If no active matching inventory
exists, the API returns `404`.

### Error responses

Validation errors use a Problem Details-style JSON response. For
example, a zero quantity returns HTTP `400`:

``` json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/ducks",
  "fieldErrors": {
    "quantity": "Quantity must be greater than zero"
  }
}
```

Unsupported enum values also return HTTP `400`. A missing or
soft-deleted duck returns HTTP `404`.

## Pricing assumptions

The exercise lists percentage adjustments but does not specify whether
percentages compound. This implementation calculates each percentage
against the base amount (`unit price × quantity`) and then adds the
signed line items.

-   More than 100 units: subtract 20% of base.
-   Wood package: add 5% of base.
-   Plastic package: add 10% of base.
-   Cardboard package: subtract 1% of base.
-   USA: add 18% of base.
-   Bolivia: add 13% of base.
-   India: add 19% of base.
-   Other destinations: add 15% of base.
-   Sea shipping: add \$400 flat.
-   Land shipping: add \$10 per unit.
-   Air shipping: add \$30 per unit; orders above 1,000 units receive a
    15% reduction on the air charge.

Amounts use `BigDecimal` and two-decimal `HALF_UP` rounding. Discounts
appear as negative amounts in the breakdown. Accepted country aliases
include `US`, `USA`, `United States`, `BO`, `Bolivia`, `IN`, and
`India`; unknown countries use the "other" rate.

### Packaging

  Size            Package
  --------------- -----------
  XLarge, Large   Wood
  Medium          Cardboard
  Small, XSmall   Plastic

### Protection rules

-   Air + wood/cardboard: polystyrene balls.
-   Air + plastic: bubble-wrap bags.
-   Land: polystyrene balls.
-   Sea: moisture-absorbing beads and bubble-wrap bags.

## Tests and verification

### Backend unit tests

Run from the repository root:

``` powershell
cd backend
mvn clean test
```

**Verified local result:** 23 main source files compiled and 15 unit
tests executed across `DuckServiceTest`, `OrderServiceTest`,
`PackagingServiceTest`, and \`PricingServiceTest. All 15 passed, with
zero failures, errors, or skipped tests.

### Frontend production build

Run:

``` powershell
cd frontend
npm ci
npm run build
```

**Verified local result:** `npm ci` completed successfully and the Vite
production build completed successfully.

### PostgreSQL concurrency smoke test

Start the backend and run this script from the repository root:

``` powershell
python scripts/concurrency-smoke.py
```

**Verified local result:** 20 concurrent create requests produced one
active inventory row with a combined quantity of 20. The script then
soft-deleted its test row.

Inventory merging uses PostgreSQL `INSERT ... ON CONFLICT` backed by a
partial unique index. The service unit tests mock the repository and do
not independently prove database concurrency behavior. The smoke test
verifies the described scenario; it does not prove correctness under
every race condition or production load.

### Manual API checks performed

The following local scenarios were also checked:

-   Matching inventory creation merges quantities.
-   Inventory is sorted by quantity ascending.
-   Updating changes price and quantity without changing color or size.
-   Invalid quantity and unsupported colors return HTTP `400`.
-   Unknown or soft-deleted inventory returns HTTP `404`.
-   USA and Bolivia surcharges are applied at 18% and 13%.
-   The 20% volume discount is absent at 100 units and present at 101
    units.
-   The 15% Air shipping reduction is absent at 1,000 units and present
    at 1,001 units.
-   Package and protection selection matched the documented rules for
    the tested combinations.
-   Test inventory records were soft-deleted and no longer appeared in
    the active list.

These results describe the tested local scenarios, not exhaustive proof
of every possible edge case. Before release, also test against
representative production-like traffic and database configuration.

## Git workflow

Commit coherent, verified changes (feature, regression fix, tests,
documentation). Keep commit dates authentic. A clean commit history is
useful evidence of development, but it does not replace passing tests or
the ability to explain the implementation.
