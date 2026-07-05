# MilkLedger Freelance Readiness Report

Date reviewed: 2026-06-29

## Project Overview

MilkLedger is a local-first Android business ledger app for a milk/dairy shop or milk collection business. It helps a shop owner record daily milk purchases from suppliers, milk sales to customers, cash payments, expenses, owner withdrawals, notes/reminders, profit, cash flow, and backup/restore data.

The problem it solves is practical: small dairy businesses often track purchases, sales, supplier balances, customer balances, cash movement, and profit manually. This app turns that into a structured Android workflow with history, summaries, reports, and data backup.

Main user flows:

- Login with Firebase Auth and load user role from Firestore.
- Complete owner setup on first app start.
- View dashboard with date filters, totals, profit, milk quantity, average price, FAT/LR/TS summaries.
- Add, edit, list, and delete milk purchases from suppliers.
- Add, edit, list, and delete milk sales to customers.
- Maintain customer/supplier accounts, opening balances, default rates, active/archive status.
- View customer and supplier history with running balances.
- Add business and personal expenses.
- View owner wallet, retained profit, daily profit, drawings, and withdrawals.
- View cash flow for a date/range.
- Create notes and note alarms.
- Export PDF reports.
- Create local backups, restore backups, upload/list/download Google Drive backups, and schedule automatic backup work.
- Change appearance settings such as theme, dynamic colors, and font scale.

## Tech Stack

UI:

- XML layouts, not Jetpack Compose.
- ViewBinding is enabled and used.
- Material Components, AppCompat, ConstraintLayout, RecyclerView-style screens.
- Bottom navigation, drawer navigation, toolbar menus, bottom sheets, dialogs, custom date filter view.

Architecture:

- Feature-based package structure under `features`.
- MVVM pattern with `ViewModel`, `StateFlow`, `SharedFlow`, UI state, UI events, and one-time effects.
- Repository layer for core business operations.
- Some use cases exist for account, sale, and purchase flows.
- This is not pure Clean Architecture, but it is much stronger than Activity-only code.

Local data:

- Room database with entities for accounts, milk transactions, financial ledger entries, expenses, and notes.
- DAOs contain real business SQL for summaries, running balances, history, profit, cash flow, and soft deletes.
- Room migration exists from database version 1 to 2.
- DataStore is used for appearance settings.
- SharedPreferences is also used for auth/user role and backup state.

Backend/cloud:

- Firebase Auth for login.
- Firestore for role lookup.
- Firebase Remote Config for feature/update flags.
- Firebase Analytics dependency is present.
- Google Drive API for backup upload/download/listing.

Background/work:

- WorkManager with Hilt worker support.
- Daily backup worker.
- Foreground/alarm-related note reminder service.

Reports/files:

- PDF generation using iText.
- FileProvider for opening/exporting files.
- Custom backup format using JSON serialization, gzip compression, checksum validation, and restore validation.

Dependency injection:

- Hilt is used for app, database, Firebase, repositories/workers, and injected services.

Navigation:

- Android Navigation Component with XML nav graph.
- Safe Args plugin is configured.
- MainActivity dynamically sets start destination depending on whether owner setup exists.

Not present:

- No Jetpack Compose.
- No Retrofit/API client service layer.
- No AdMob implementation found.
- No full sync engine with a remote database.
- No CI/CD configuration found.

## Portfolio Description

Honest Upwork/Fiverr/LinkedIn portfolio description:

> Built a Kotlin Android business ledger app for dairy/milk shop management using XML UI, MVVM, Room, Hilt, Firebase Auth, Firebase Remote Config, WorkManager, DataStore, Google Drive backup, and PDF reports. The app supports daily purchases, sales, accounts, customer/supplier ledgers, expenses, owner wallet, profit dashboard, cash flow, notes/reminders, soft delete, local backup/restore, and automated cloud backup.

Strong features to mention:

- Built a complete local-first business app with real CRUD workflows.
- Designed Room database models and DAO queries for financial ledger, balances, summaries, and reports.
- Implemented MVVM with ViewModels, repositories, StateFlow UI state, and lifecycle-aware collection.
- Used Hilt for dependency injection.
- Implemented transactional save/update/delete logic so milk transactions and ledger entries stay consistent.
- Added soft-delete behavior for safer business records.
- Added Google Drive backup/restore with validation and automatic scheduled backups.
- Added PDF report export.
- Added Firebase login, Firestore role lookup, Remote Config feature/update flags.
- Added theme/font settings with DataStore.
- Wrote Android instrumentation tests for money-critical repository behavior.

Problems this project proves you can solve:

- Offline-first Android apps for small businesses.
- Local database design with Room.
- Financial/accounting-style transaction handling.
- Dashboard/reporting queries.
- Backup and restore workflows.
- Firebase login and configuration.
- Android release and ProGuard troubleshooting.
- XML UI maintenance and business-form screens.
- Background work with WorkManager.

What not to overclaim:

- Do not claim Compose experience from this app.
- Do not claim AdMob setup from this app.
- Do not claim Retrofit/API integration from this app.
- Do not claim enterprise-grade architecture.
- Do not claim production-ready public release until lint, secrets, and Play policy issues are cleaned up.

## Client-Facing Description

Simple client explanation:

> I built MilkLedger, an Android app for managing a milk shop's daily business. It records supplier purchases, customer sales, payments, expenses, owner withdrawals, notes, cash flow, and profit. The app stores data locally with Room, shows summaries on a dashboard, exports PDF reports, and protects data with local and Google Drive backups.

Architecture explanation:

> I used MVVM because it keeps screen logic separate from business/data logic. Fragments handle UI, ViewModels hold screen state and user actions, repositories handle database transactions, and Room DAOs store and query data. Hilt is used to provide dependencies like the database, DAOs, repositories, PDF generator, backup tools, and background workers.

Data flow:

> When the user saves a sale or purchase, the ViewModel validates the form and calls a use case or repository. The repository writes the milk transaction and related financial ledger rows inside a Room transaction. The DAO exposes Flow-based queries, so list screens, summaries, balances, and dashboards update from database changes. Backup state is marked dirty after important data changes, and WorkManager can later upload a backup to Google Drive.

Hard problems solved:

- Keeping sale/purchase records and ledger balances consistent.
- Calculating profit, average purchase/sale price, milk quantity difference, FAT/LR/TS summaries, cash in/out, and retained profit.
- Handling edit/delete with soft delete instead of destroying records immediately.
- Exporting business reports to PDF.
- Creating compressed backup files with checksum validation.
- Restoring all important tables in one database transaction.
- Uploading backups to Google Drive and keeping only recent backups.
- Scheduling automatic backup work.

What I would improve next:

- Fix lint errors and reduce important warnings.
- Remove tracked signing/release/credential files before making the repo public.
- Add more tests for backup/restore, migrations, ViewModels, and UI flows.
- Clean comments/encoding artifacts and remove unused code.
- Improve release readiness around permissions and Play Store policy-sensitive features.
- Add a short README with screenshots, demo video, architecture diagram, and setup instructions.

## Suggested Upwork Services

1. Android bug fixing and crash fixes for Kotlin/XML apps.

   Based on this project, you can confidently fix form validation, lifecycle issues, navigation issues, Room data bugs, RecyclerView list bugs, and release/lint issues.

2. Room database and local ledger features.

   Offer CRUD screens, local database setup, Room migrations, soft delete, summaries, balances, and offline-first business data modules.

3. XML UI improvement and screen polishing.

   Offer layout cleanup, Material Components styling, dashboard redesign, form UX improvement, date filters, bottom sheets, empty states, and responsive XML layouts.

4. Firebase setup for small Android apps.

   Offer Firebase Auth login, Firestore role/profile lookup, Firebase Remote Config feature flags, and basic analytics setup.

5. Backup, restore, PDF, and Play Store release fixes.

   Offer local backup/restore, Google Drive backup, PDF export, WorkManager background jobs, ProGuard fixes, target SDK fixes, and Play Store policy/lint cleanup.

Extra services you can mention after more polish:

- XML to Compose migration for existing Android screens.
- DataStore settings screens.
- Android notification/reminder setup.
- Business dashboard/reporting screens.

## Suggested Screenshots And Video List

Screenshots to capture:

- Login screen.
- Dashboard with date filter and profit/cash summary visible.
- Add sale form with customer selection and calculated total.
- Add purchase form with supplier selection, FAT/LR/TS fields, and payment.
- Customer history screen with balance/payment history.
- Supplier history screen with purchase quality fields.
- Account list with customer/supplier balances.
- Expense list and add expense screen.
- Owner wallet/profit details screen.
- Cash flow screen.
- PDF export success/open report.
- Backup/restore screen with Google Drive backup list.
- Settings screen with theme/font controls.

Demo video flow:

1. Start on dashboard and change date filter.
2. Add a supplier purchase.
3. Add a customer sale.
4. Show dashboard totals updated.
5. Open customer/supplier history and show balance.
6. Add an expense and show cash flow/profit impact.
7. Export a PDF report.
8. Open backup screen and show local/Drive backup capability.

Keep the demo short: 60 to 120 seconds is enough for Upwork/Fiverr.

## Portfolio-Worthy Strengths

- This is a real business workflow app, not a demo counter/todo app.
- Good use of Kotlin, AndroidX, Material UI, Navigation, Hilt, Room, coroutines, DataStore, WorkManager, Firebase, Google Drive, and PDF generation.
- The financial ledger model shows real thinking: sales, purchases, cash received, cash paid, expenses, drawings, profit impact, opening balances, soft deletes.
- Repository methods use Room transactions for multi-table writes.
- Backup/restore is unusually strong for a junior/intermediate portfolio.
- PDF reports make the app feel useful for actual clients.
- Tests exist for important repository behavior.
- Release build has minification and resource shrinking enabled.
- Navigation graph covers many complete app flows.

## Technical Weaknesses

High priority before showing clients:

- `lintDebug` currently fails with 2 errors and 573 warnings.
- Lint errors are API-level issues in:
  - `CustomerSelectionBottomSheet.kt`
  - `SupplierSelectionBottomSheet.kt`
- The project tracks `JKS/milk_ledger_jks`. Do not publish this repo publicly with signing keys included.
- The project tracks `app/google-services.json`. For public portfolio use, replace with a sample file or document setup steps.
- Release output files under `app/release` are tracked. These should not be part of a clean source repo.
- `MainActivity` uses `runBlocking` indirectly through `readSettingsBlocking()` during startup/context attachment. It may be acceptable for a small app, but it is not ideal.
- Force update flow uses `intent.getStringExtra("url")!!`, which can crash if the URL extra is missing.
- Manifest includes Play-policy-sensitive permissions: exact alarm, full-screen intent, request install packages, foreground service system exempted. These need clear justification before Play Store submission.

Medium priority:

- Room `exportSchema = false`; for a serious production database, schema export should be enabled and migration history should be tested.
- DAO files contain large SQL queries and business logic. This is practical, but hard to maintain as the app grows.
- Some ViewModels are doing significant form/business logic.
- Comments include garbled emoji-like encoding artifacts and mixed-language notes. This hurts portfolio polish.
- Many strings are hardcoded in XML and Kotlin, which limits localization and triggers lint warnings.
- Many formatting calls use default locale.
- Some direct `FirebaseAuth.getInstance()` / `FirebaseFirestore.getInstance()` usage remains in activities instead of consistent DI.
- Some exception handling shows raw exception messages to users.
- Some code uses `!!`; most are controlled, but they should be reviewed.
- `RemoteConfigManager` has `minimumFetchIntervalInSeconds = 0`, useful for development but not ideal for production.
- There are many debug logs in production paths.

Lower priority:

- XML UI is fine for freelancing, but Compose clients may ask for Compose samples.
- Some files have naming/spelling issues such as `MIlkDao`, `contstants`, and `dasboard`.
- No README portfolio page was found.
- No CI workflow was found.
- No screenshots/video assets were found.

## Production Quality Assessment

Current quality: strong functional prototype / private-client-ready after targeted cleanup.

Not yet: polished public portfolio repo or Play Store-ready codebase.

Good signs:

- Modern Android stack.
- Feature-based organization.
- Hilt dependency injection.
- Room transactions.
- Real business logic.
- Backup and PDF features.
- Some automated tests.
- Release minification configured.

Blocking issues for public/client presentation:

- Fix lint errors.
- Remove tracked signing key and release outputs.
- Clean credentials strategy.
- Add README, screenshots, and demo instructions.
- Clean obvious encoding/comment polish issues.

## Verification Results

Commands run:

- `./gradlew.bat testDebugUnitTest` passed.
- `./gradlew.bat lintDebug` failed.

Lint result:

- 2 errors.
- 573 warnings.
- First error: API 30 requirement flagged while minSdk is 27 in `CustomerSelectionBottomSheet.kt`.
- Second error: same issue in `SupplierSelectionBottomSheet.kt`.

Important note: I did not run connected Android instrumentation tests during this review. Existing instrumentation tests are present for sale, purchase, account, and expense repositories.

## 7-Day Improvement Plan

Day 1: Fix release blockers.

- Fix the two lint errors.
- Review or suppress only valid API warnings.
- Remove `JKS`, release outputs, and private config from public tracking strategy.
- Update `.gitignore` for keystore/release outputs.

Day 2: Portfolio polish.

- Add a professional `README.md`.
- Include project summary, tech stack, screenshots, demo video link, setup notes, and test commands.
- Clean visible encoding artifacts in strings/comments that appear in UI or public docs.

Day 3: Stability cleanup.

- Replace risky `!!` in user-facing flows.
- Fix `ForceUpdateActivity` missing URL handling.
- Reduce raw exception messages shown to users.
- Review exact alarm/full-screen/install permissions and document why they exist.

Day 4: Testing.

- Add tests for backup/restore validation.
- Add Room migration test for version 1 to 2.
- Add ViewModel tests for sale/purchase form validation.
- Keep existing repository tests as proof of business-rule testing.

Day 5: UI/UX presentation.

- Capture clean screenshots with sample data.
- Record a 60-120 second demo video.
- Improve empty states and any cramped form/list screens you notice while recording.

Day 6: Release readiness.

- Run `assembleDebug`, `lintDebug`, and available tests.
- Run a release build if signing config is safe locally.
- Confirm ProGuard does not break navigation/PDF/backup flows.
- Test backup and restore on a clean emulator.

Day 7: Freelance packaging.

- Write 2 Upwork project descriptions: one short and one detailed.
- Create 5 service gigs from this project.
- Prepare interview answers using the client-facing description above.
- Add this app to LinkedIn as a featured project with screenshots/video.

## Confidence Score

Current confidence score: 7/10 for private freelance portfolio demo.

Public GitHub confidence score: 5.5/10 until signing keys, release outputs, Firebase config strategy, lint errors, and README/screenshots are fixed.

After the 7-day cleanup plan: 8/10 for small-to-mid freelance Android work.

Best services to sell right now:

- Kotlin/XML Android bug fixing.
- Room database features.
- Business ledger/reporting features.
- Firebase Auth/Remote Config setup.
- Backup/restore/PDF export features.
- Android release/lint cleanup.

Bottom line: this project is portfolio-worthy, but show it as a practical business Android app, not as a perfect architecture showcase. The business depth is its biggest strength. Clean the repo hygiene and lint issues before making it public.
