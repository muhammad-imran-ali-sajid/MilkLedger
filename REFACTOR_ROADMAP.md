# MilkLedger Refactor Roadmap

This document is the working plan for improving MilkLedger without breaking ledger behavior, existing client data, or the current Room database structure.

The goal is not to rewrite the app. The goal is to make the codebase easier to scale, reason about, test, and maintain while preserving every important business result.

## Core Mental Model

MilkLedger is a ledger app, so the database is not just storage. It is the source of financial truth.

Every refactor must respect this order of importance:

1. Data safety
2. Ledger correctness
3. Existing user workflow
4. Test coverage
5. Code clarity
6. Package and formatting polish

That means we do not start by moving files around. We first protect behavior with tests, then refactor one business flow at a time.

## Non-Negotiable Guardrails

These rules apply until we intentionally decide otherwise:

- Do not change existing Room table names.
- Do not rename existing Room columns.
- Do not change primary key formats.
- Do not change existing entity fields unless a tested migration is created.
- Do not change money units. Stored money remains in paisa/long values where it currently does.
- Do not change debit, credit, or profit impact semantics without an explicit architecture note and tests.
- Do not rewrite multiple features in one step.
- Do not perform large package renames before behavior tests exist.
- Do not remove old backup paths until the supported backup behavior is documented and verified.

## Target Architecture

The best fit for this project is:

MVVM + Repository + UDF, with selective domain use cases/services for important business rules.

This avoids a heavy full Clean Architecture rewrite while still giving the project professional boundaries.

Target feature shape:

```text
features/<feature>/
  data/
    Repository.kt
    Mapper.kt
    local/ or remote/ when needed

  domain/
    model/
    usecase/
    service/
    validator/

  ui/
    <screen>/
      Fragment.kt
      ViewModel.kt
      Contract.kt
      Adapter.kt
```

For XML/ViewBinding screens:

```text
Fragment:
  - render UiState
  - forward UiEvent
  - handle UiEffect
  - Android view logic only

ViewModel:
  - own UiState
  - reduce UiEvent
  - call use cases/repositories
  - expose one-time effects

UseCase/Domain Service:
  - validate business rules
  - calculate ledger effects
  - coordinate high-level app action

Repository:
  - persist and query data
  - run Room transactions
  - map entities and domain models
  - avoid UI state and screen-specific decisions
```

## Current Architecture Diagnosis

The project already has a good foundation:

- Hilt dependency injection is in place.
- Room is centralized through `AppDatabase`.
- Many screens already use `UiState`, `UiEvent`, and `UiEffect`.
- `BaseViewModel<S, E, F>` gives a UDF-style pattern.
- Feature folders exist.
- Some domain/use case code already exists for account and purchase flows.

The main issues are not that the app has "no architecture". The issue is that boundaries are inconsistent.

Examples:

- Some repositories return UI state directly.
- Some ViewModels contain validation and business calculations.
- Some repositories contain ledger business rules, backup invalidation, mapping, and persistence together.
- Some features have domain use cases, while others call repositories directly.
- Large DAO and repository files make important money behavior harder to audit.

## Refactor Strategy

We will use a "safety net, then thin slice" strategy.

For every major feature:

1. Capture current behavior with tests.
2. Extract pure calculations first.
3. Extract use case/domain service next.
4. Keep repository transaction behavior intact until tests pass.
5. Make ViewModel thinner.
6. Make Fragment more render-only.
7. Run build/tests.
8. Move to the next feature.

This keeps the app shippable after each slice.

## Phase 1: Safety Net

Purpose: make sure future refactors do not break money and ledger behavior.

Initial tests to add:

- Sale save creates the expected milk transaction and ledger entries.
- Sale update modifies the expected milk transaction and ledger entries.
- Sale delete soft-deletes related milk and ledger entries.
- Purchase save creates the expected milk transaction and ledger entries.
- Purchase update modifies the expected milk transaction and ledger entries.
- Purchase delete soft-deletes related milk and ledger entries.
- Account save creates or updates opening balance correctly.
- Account delete is blocked or handled correctly when balance rules apply.
- Expense save creates correct business or personal ledger effect.
- Dashboard totals match known seeded data.
- Backup export and restore preserve record counts.
- Room migration 1 to 2 preserves data and adds `paymentDateMillis`.

Preferred test order:

1. Pure calculator/domain tests
2. Repository tests with Room in-memory database
3. ViewModel tests for UDF state/effect behavior
4. Instrumented tests only where Android framework behavior is required

## Phase 2: Formatting And Encoding Cleanup

Purpose: clean the code without changing behavior.

Known cleanup items:

- Remove broken comment characters such as mojibake text.
- Convert mixed-language noisy comments into clear, short English comments.
- Standardize imports and file formatting.
- Keep user-facing strings unchanged unless intentionally reviewed.
- Avoid mass renaming packages at this stage.

Rule: formatting-only commits should not contain behavior changes.

## Phase 3: Standardize UDF Contracts

Purpose: make every screen predictable.

Each main screen should have:

- `UiState`
- `UiEvent`
- `UiEffect`
- `ViewModel : BaseViewModel<State, Event, Effect>`

Preferred contract file pattern:

```text
SaleFormContract.kt
  data class SaleFormUiState(...)
  sealed interface SaleFormUiEvent
  sealed interface SaleFormUiEffect
```

Fragment responsibilities:

- collect state
- render state
- collect effects
- emit events

Fragment should not:

- calculate money
- decide ledger behavior
- call repositories
- know database entities

## Phase 4: Thin Repositories

Purpose: make repositories persistence-focused.

Repositories may:

- read from DAOs
- write to DAOs
- run `db.withTransaction`
- map entity to domain/data models
- mark backup data changed after successful write

Repositories should avoid:

- returning `UiState`
- containing screen-specific strings
- doing complex validation
- duplicating sale/purchase ledger rules
- owning calculations that can be pure domain functions

## Phase 5: Extract Ledger Domain Rules

Purpose: make money behavior explicit and testable.

Candidate domain services:

- `SaleLedgerService`
- `PurchaseLedgerService`
- `ExpenseLedgerService`
- `OpeningBalanceService`
- `LedgerBalanceCalculator`

Candidate use cases:

- `SaveSaleUseCase`
- `UpdateSaleUseCase`
- `DeleteSaleUseCase`
- `SavePurchaseUseCase`
- `UpdatePurchaseUseCase`
- `DeletePurchaseUseCase`
- `SaveExpenseUseCase`
- `SaveAccountUseCase`

These classes should make the business story obvious:

```text
User saves sale
  -> validate sale input
  -> calculate sale total
  -> create milk transaction
  -> create sale debit ledger entry
  -> optionally create cash received ledger entry
  -> mark backup data changed
```

## Phase 6: DAO And Query Cleanup

Purpose: make data access easier to maintain without changing schema.

Safe DAO cleanup options:

- Split read/report query groups into separate DAO interfaces if Room allows it cleanly.
- Move repeated query result models into focused files.
- Add tests before changing complex queries.
- Add indexes only through explicit Room migrations.

Avoid early:

- changing table names
- changing column names
- changing query semantics without expected-result tests

## Phase 7: Professional Project Polish

After safety and architecture are stable:

- Move dependency versions into `libs.versions.toml`.
- Enable Room schema export.
- Add an `ARCHITECTURE.md`.
- Add a `LEDGER_RULES.md` that documents debit, credit, payable, receivable, and profit impact.
- Clean `.gitignore` for local logs/build artifacts/signing material.
- Review backup and security behavior.
- Consider module splitting only if the single module becomes painful after refactor.

## Suggested Refactor Order

1. Sale
2. Purchase
3. Account
4. Expense
5. Dashboard/reporting
6. Backup/restore
7. Notes/alarm
8. Settings/theme
9. Package naming and formatting polish

Sale and purchase come first because they are the center of the ledger.

## Done Definition For Each Refactor Slice

A slice is complete only when:

- Existing behavior is covered by at least one focused test.
- Production code compiles.
- The feature still uses the same database schema.
- The ViewModel is thinner or clearer than before.
- Repository responsibilities are narrower or better documented.
- No unrelated files were reformatted.
- The diff can be reviewed in one sitting.

## First Working Slice

The recommended first implementation slice is:

1. Add test dependencies/helpers if needed.
2. Write current-behavior tests for sale save/update/delete.
3. Extract sale input validation and sale total calculation if not already pure.
4. Add `SaveSaleUseCase` without changing database schema.
5. Make `SaleFormViewModel` call the use case.
6. Run tests and debug build.

This first slice will teach the pattern we can reuse across the rest of the app.

## Progress Log

### 2026-06-18

- Added the first sale repository safety tests for save, update, and delete behavior.
- Verified `assembleDebug` passes.
- Verified `:app:compileDebugAndroidTestKotlin` passes.
- Attempted `:app:connectedDebugAndroidTest`; the connected runner failed before executing tests and reported zero tests in the generated HTML report.
- Device diagnostics showed a Vivo `V2424` on Android 16/API 36 with primary user `0`, Private Space user `10`, and App Clone user `999`. The test instrumentation package `com.miassolutions.milkledger.test` was not installed after the failed Gradle run, so the failure happened before test execution.
- Treat the connected-test failure as an Android Gradle Plugin/UTP/device setup issue, not as a sale repository assertion failure.
- Do not run connected tests on a phone that contains important real MilkLedger data until the debug build has a separate `applicationIdSuffix` or the tests are run on a clean emulator/fresh test device.
- Created a clean emulator and verified the sale repository tests through direct instrumentation. The emulator image was API 37 / 16 KB page size, so Gradle/UTP still produced a `0 tests` report, but manual install plus `adb shell am instrument` executed the tests.
- Sale repository safety tests passed directly on the emulator: `OK (3 tests)`.
- Adjusted the update-sale test expectation to match current app behavior: `toDisplayDate()` formats payment notes as `dd/MM`, for example `(Dated: 17/01)`.
- Created a second stable AVD named `MilkLedger_Pixel5_API36` using Android 36 Google APIs x86_64, 2 GB RAM, and 2 CPU cores. It boots as `emulator-5556`, and the sale repository safety tests pass there through direct instrumentation: `OK (3 tests)`.
- Added purchase repository safety tests for save, update, and delete behavior.
- Purchase repository safety tests pass on `MilkLedger_Pixel5_API36` through direct instrumentation: `OK (3 tests)`.
- Captured current purchase money behavior: quality purchase totals can truncate by one paisa because the app calculates with `Double` and then calls `toLong()`. This is now documented by the test before any rounding refactor.
