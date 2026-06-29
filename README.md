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

This repository follows **Feature Branching** (GitFlow-style), as required by the exam brief:

- **`main`** — production-ready, stable. Every merge here is a tagged release (`v1.0.0`).
- **`develop`** — integration branch. Every finished feature is merged here first, before
  being prepared for production.
- **`feature/*`** — one branch per endpoint (or logical group of endpoints). Created from
  `develop`, merged back into `develop` with `--no-ff` once done.

### Branch ↔ endpoint mapping

| Category | Endpoint(s) | Branch |
|---|---|---|
| Initialisation | `POST /api/wallets/seed` | `feature/wallet-seeder` |
| Gestion | `POST /api/wallets` | `feature/wallet-creation` |
| | `GET /api/wallets` (pagination) | `feature/wallet-listing` |
| | `GET /api/wallets/{phone}` & `/balance` | `feature/wallet-consultation` |
| Transactions | `POST /api/wallets/{id}/deposit` | `feature/transaction-deposit` |
| | `POST /api/wallets/withdraw` | `feature/transaction-withdraw` |
| | `POST /api/wallets/transfer` | `feature/transaction-transfer` |
| Paiements | `POST /api/wallets/pay` & `/pay-factures` | `feature/payment-services` |
| Historique | `GET /api/wallets/{phone}/transactions` | `feature/transaction-history` |
| Proxy API | `GET /api/external/factures/...` (Tous les proxy) | `feature/proxy-factures` |

Browse the [branches on GitHub](../../branches) to see each one merged into `develop`,
and the [commit history](../../commits/develop) for how each endpoint group was built.
See [ARCHITECTURE.md](ARCHITECTURE.md#3-git-workflow) for why the actual build order
deviates slightly from this table (dependency order between branches) and for the full
list of design patterns applied.
