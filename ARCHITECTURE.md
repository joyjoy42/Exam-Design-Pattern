# Architecture

## 1. Services

```
                         ┌────────────────────────┐
   HTTP clients  ──────► │   badwallet-api  :8080  │
                         │  (wallets, transactions, │
                         │   bill payments)          │
                         └───────────┬──────────────┘
                                     │ HTTP (Proxy pattern)
                                     ▼
                         ┌──────────────────────────┐
                         │  payment-service  :8081   │
                         │  (ISM / WOYAFAL factures)  │
                         └──────────────────────────┘
```

`badwallet-api` is the only public-facing service. It owns wallets, transactions and
the bill-payment use cases. `payment-service` simulates the external biller: it owns
invoices ("factures") per wallet code and never talks back to `badwallet-api`.

## 2. Design patterns applied (badwallet-api)

This is a Design Patterns course exam, so each pattern below was picked because it
solves a concrete problem in this domain — not for decoration.

| Pattern | Where | Why |
|---|---|---|
| **Builder** | `Wallet`, `Transaction`, `Facture` (Lombok `@Builder`) | Entities have 6-10 fields; builders keep seeding/assembly code readable without telescoping constructors. |
| **Facade** | `WalletService` | Controllers only ever call this one class. It hides the repository, the strategy factory, the proxy and the event publisher behind plain methods (`deposit`, `withdraw`, `transfer`, `pay`, ...). |
| **Strategy** | `service.strategy.PaymentMethodStrategy` (`CreditCardDepositStrategy`, `WalletTargetDepositStrategy`) and `service.fee.WithdrawalFeeStrategy` | Deposits validate differently depending on funding source (card network cap vs. uncapped agent cash-in); withdrawals compute a fee (1%, capped at 5000 XOF) that is intentionally pluggable. |
| **Factory** | `PaymentStrategyFactory` | Resolves the right `PaymentMethodStrategy` bean for a given `PaymentMethod` enum value at runtime. |
| **Observer** | `event.TransactionRecordedEvent` + `TransactionHistoryListener` | Balance-mutating code (`WalletService`) never touches the `Transaction` ledger table directly — it publishes an event. Listeners (today: history recording) can be added later (notifications, fraud checks) without touching the operations. |
| **Proxy** | `service.proxy.FactureServiceProxy` | `badwallet-api` never calls `payment-service` with a raw `RestClient` scattered across the codebase. Every wallet→payment-service call (consultation *and* payment confirmation) goes through this one component, which centralizes the base URL, error translation (`ExternalServiceException`) and is what `/api/external/factures/...` simply re-exposes. |
| **Template Method** | `service.payment.AbstractBillPaymentProcessor` (`CurrentMonthPaymentProcessor`, `SpecificFacturesPaymentProcessor`) | Paying "this month's bill" (1.9) and paying specific factures (1.10) share the exact same skeleton — resolve wallet → quote amount due via the proxy → debit wallet → confirm with the provider → record transaction — and differ only in how the amount due is resolved and which proxy call settles it. |

## 3. Git workflow

Per the exam brief, this repo follows **Feature Branching** (GitFlow-style):

- **`main`** — production-ready, stable. Every merge here is a tagged release (`v1.0.0`).
- **`develop`** — integration branch. Every finished feature is merged here first.
- **`feature/*`** — one branch per endpoint group below, branched from `develop` and
  merged back with `--no-ff` once the endpoint(s) work.

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
| Proxy API | `GET /api/external/factures/...` | `feature/proxy-factures` |

`payment-service` itself (the external biller simulator) isn't one of the wallet
endpoints above, so it was set up directly on `develop` as a prerequisite: the proxy
and payment branches both need something running on `:8081` to call.

Build order deviates slightly from the table above where a real dependency exists
(e.g. `feature/proxy-factures` lands before `feature/payment-services` because the
latter reuses `FactureServiceProxy`; `feature/wallet-seeder` lands last because it
generates sample deposits/withdrawals/transfers/payments and therefore needs every
other transaction feature to already exist).

## 4. Domain model (badwallet-api)

- **Wallet** — `code` (`WLT-0000001`…), `phoneNumber`, `email`, `balance`, `currency`.
- **Transaction** — one ledger line per wallet per movement. Transfers write two rows
  (`TRANSFER_OUT` for the sender, `TRANSFER_IN` for the receiver) so each wallet's
  history reads naturally from its own point of view.

## 5. Domain model (payment-service)

- **Facture** — `reference` (`FAC-{SERVICE}-{walletNumber}-{index}`), `walletCode`,
  `serviceName` (`ISM`/`WOYAFAL`), `amount`/`amountDue`, `status`, billing
  `periodMonth`/`periodYear`, `issuedAt`.
