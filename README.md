# BadWallet — Mobile Wallet & Bill Payment Platform

Exam project for the **Design Patterns** course (L3 S2 2026). Simulates a mobile-money
wallet (à la Orange Money / Wave) split into two independently deployable Spring Boot
services that talk to each other over HTTP.

| Service | Port | Role |
|---|---|---|
| `badwallet-api` | 8080 | Wallets, deposits, withdrawals, transfers, bill payments, transaction history. Public-facing API. |
| `payment-service` | 8081 | Simulates the external biller (ISM, WOYAFAL): tracks invoices ("factures") per wallet and exposes them for consultation/payment. |

`badwallet-api` never talks to `payment-service`'s database directly — it always goes
through an HTTP **Proxy** component (see [ARCHITECTURE.md](ARCHITECTURE.md)).

## Quick start

Each service is a standalone Maven project with the Maven Wrapper checked in, so only a
JDK 17+ is required.

```bash
# Terminal 1
cd payment-service
./mvnw spring-boot:run        # starts on :8081

# Terminal 2
cd badwallet-api
./mvnw spring-boot:run        # starts on :8080
```

Both apps boot an in-memory H2 database that is recreated on every restart. Use the
seed endpoints to populate sample data — see [requests.http](requests.http) for ready-to-run
sample requests (works with the VS Code "REST Client" extension).

## Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) — design patterns used and why, module layout, domain model.
- [requests.http](requests.http) — full request collection for every endpoint of both services.

## Git workflow

This repository follows **Feature Branching / GitFlow**:

- `main` — production-ready, tagged releases only.
- `develop` — integration branch; every finished feature is merged here.
- `feature/*` — one branch per endpoint group, branched from and merged back into `develop`.

See [ARCHITECTURE.md](ARCHITECTURE.md#git-workflow) for the full branch-to-endpoint mapping.
