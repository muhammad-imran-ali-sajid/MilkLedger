# Project Review

Scope: Android app module under `app/`, Gradle configuration, manifest, local database, backup/sync code, and tests.

## 1. Architecture Issues

1. **Single app module carries all layers and feature code**
   - Evidence: all production code is in `app/src/main/java/com/miassolutions/milkledger`.
   - Risk: UI, repositories, Room DAOs, Firebase, Drive backup, PDF generation, and business calculations are tightly shipped as one module. This slows builds, makes boundaries informal, and makes it harder to test domain logic without Android dependencies.
   - Suggested direction: split at least `core:model`, `core:database`, `core:backup`, and feature modules once the current behavior is stable.

2. **Feature boundaries are inconsistent**
   - Evidence: some features use `data/domain/ui` style, while sale and purchase mix UI models, repository logic, and ledger rules in `features/sale/data/MilkSaleRepository.kt` and `features/purchase/data/MilkPurchaseRepository.kt`.
   - Risk: money rules are duplicated across sale and purchase flows and can drift.
   - Suggested direction: move ledger-entry creation/update rules into shared domain services/use cases.

3. **Database integrity depends mostly on repository code**
   - Evidence: `AccountEntity`, `MilkTransactionEntity`, and `FinancialLedgerEntity` define indices but no Room `ForeignKey` constraints. Ledger rows reference accounts and transactions through plain strings.
   - Risk: orphan ledger rows or milk transactions can exist if a restore, future sync, or repository bug writes inconsistent data.
   - Suggested direction: add foreign keys where possible, or add explicit integrity checks and repair tools if soft-delete/sync rules prevent strict constraints.

4. **Room schema history is disabled**
   - Evidence: `AppDatabase.kt` has `exportSchema = false`.
   - Risk: migrations become harder to review and test, especially for user ledger data.
   - Suggested direction: enable schema export and commit Room schemas.

5. **Two backup implementations coexist**
   - Evidence: `core/localdb/backup/BackupManager.kt` backs up/restores raw DB files, while `features/backup/data/BackupRepository.kt` creates JSON/gzip `.mlbackup` backups.
   - Risk: two backup formats mean two restore paths, two validation models, and ambiguous product behavior.
   - Suggested direction: choose one supported backup path. Prefer structured JSON backups if cross-version migration is important.

6. **Activities own authentication and remote role fetch logic directly**
   - Evidence: `LoginActivity.kt` uses `FirebaseAuth` and `FirebaseFirestore` directly.
   - Risk: login behavior is hard to test and hard to reuse.
   - Suggested direction: move auth and role loading behind a repository/use case and expose UI state from a ViewModel.

## 2. Performance Issues

1. **"Daily" backup is scheduled every 15 minutes**
   - Evidence: `BackupWorkScheduler.kt` uses `PeriodicWorkRequestBuilder<DailyBackupWorker>(15, TimeUnit.MINUTES)`.
   - Risk: frequent Drive checks/uploads can drain battery and network. Even when unchanged, WorkManager wakes often.
   - Quick fix: change interval to 24 hours, or use a less frequent cadence plus manual backup.

2. **Full-dataset backup serialization happens on every backup**
   - Evidence: `BackupRepository.createBackupObject()` reads all accounts, milk transactions, ledger entries, expenses, and notes into memory before gzip/upload.
   - Risk: backup cost grows linearly with user history and may cause memory pressure on older devices.
   - Suggested direction: keep for now if datasets are small, but add limits/telemetry and consider streaming export if ledger history grows.

3. **Large SQL DAOs combine many reporting concerns**
   - Evidence: `MIlkDao.kt` is 587 lines and contains multiple list/detail/history/report queries with correlated subqueries for running balances and previous rates.
   - Risk: queries become hard to optimize and may degrade as transaction count grows.
   - Suggested direction: add query-level tests, indexes matching common filters, and consider precomputed daily/account balances if history gets large.

4. **Indexes are too broad for common multi-column filters**
   - Evidence: entities mostly index `accountId`, `dateMillis`, and `referenceId` separately.
   - Risk: queries filtering by `accountId`, `type`, `dateMillis`, and `deletedAtMillis` may scan more rows than needed.
   - Suggested direction: add composite indexes for hot paths, for example `(accountId, type, dateMillis, deletedAtMillis)` and `(referenceId, type, deletedAtMillis)`.

5. **WorkManager debug logging is enabled globally**
   - Evidence: `MilkLedgerApp.kt` sets `.setMinimumLoggingLevel(Log.DEBUG)`.
   - Risk: noisy logs in release builds and minor runtime overhead.
   - Suggested direction: use `if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO/WARN`.

6. **Potential observer leak in test backup helper**
   - Evidence: `BackupWorkScheduler.runBackupNowForTest()` uses `observeForever`.
   - Risk: observers can live beyond the caller and leak context/state.
   - Suggested direction: remove test helper from production code or unregister the observer.

## 3. Security Issues

1. **Release builds are not minified or shrunk**
   - Evidence: `app/build.gradle.kts` has `release { isMinifyEnabled = false }`.
   - Risk: larger APK, easier reverse engineering, and no resource/code shrink benefits.
   - Suggested direction: enable R8/minification for release after adding/validating keep rules.

2. **App data is broadly included in Android backup**
   - Evidence: manifest has `android:allowBackup="true"`, and `backup_rules.xml` includes all files, shared preferences, and databases.
   - Risk: ledger data, local backups, and preferences may be backed up to cloud/device transfer without a narrow data policy.
   - Suggested direction: explicitly exclude sensitive database/local backup files, or disable platform backup if Drive backup is the intended mechanism.

3. **Signing key is present in the repository tree**
   - Evidence: `JKS/milk_ledger_jks` exists.
   - Risk: if the repo is shared, release signing material may be exposed.
   - Suggested direction: remove from version control, rotate if it was ever pushed/shared, and store signing config outside the repo or in CI secrets.

4. **In-app APK update path is risky**
   - Evidence: manifest requests `REQUEST_INSTALL_PACKAGES`; `ForceUpdateActivity.kt` downloads an APK from Remote Config `apk_url` and launches package install.
   - Risk: sideload update flows are sensitive, depend on remote config integrity, and can violate store policies depending on distribution channel.
   - Suggested direction: use Play in-app updates for Play-distributed builds. If sideloading is required, enforce HTTPS, validate signature/checksum, and fail closed.

5. **Remote Config fetch interval is zero**
   - Evidence: `RemoteConfigManager.kt` sets `minimumFetchIntervalInSeconds = 0`.
   - Risk: excessive fetches and easier abuse of remote-controlled flags in production.
   - Suggested direction: use zero only for debug builds; use a production interval for release.

6. **Sensitive logging is present**
   - Evidence: Remote Config logs `apkUrl`, update messages, and feature flags; Drive backup logs file names and sizes; login logs auth failures.
   - Risk: business/user metadata can appear in logs.
   - Suggested direction: gate debug logs behind `BuildConfig.DEBUG` and avoid logging URLs or backup file names in release.

7. **Backups are checksummed but not encrypted**
   - Evidence: `BackupRepository` signs integrity with SHA-256 checksum but writes compressed JSON backup bytes to app files and Drive.
   - Risk: checksum detects corruption/tampering but does not protect ledger contents from disclosure.
   - Suggested direction: encrypt backup payloads before local storage/upload, ideally with a user-controlled passphrase or Android Keystore-backed key.

8. **Broad/sensitive permissions should be reviewed**
   - Evidence: manifest requests exact alarm, full-screen intent, foreground service/system-exempted, install packages, wake lock, vibration, and legacy external storage.
   - Risk: Play policy and user trust risk if permissions are not strictly justified.
   - Suggested direction: keep only permissions tied to active features and document the policy rationale.

## 4. Technical Debt

1. **No meaningful tests**
   - Evidence: only `ExampleUnitTest.kt` and `ExampleInstrumentedTest.kt` exist.
   - Risk: ledger math, migrations, backup restore, and report queries can regress silently.
   - Suggested direction: start with repository/DAO tests around sale, purchase, payment edit/delete, backup validation, and migration 1->2.

2. **Large files concentrate complexity**
   - Evidence: `MIlkDao.kt` 587 lines, `BackupRestoreFragment.kt` 336 lines, `SaleFormViewModel.kt` 329 lines, `DateFilterView.kt` 314 lines, `LedgerDao.kt` 305 lines.
   - Risk: high-change files become harder to reason about and review.
   - Suggested direction: split DAOs by reporting/read model versus writes; split UI setup, event rendering, and navigation code in large fragments.

3. **Naming and typo debt**
   - Evidence: `MIlkDao.kt` has unusual capitalization; `core/contstants` and `utils/extensions/Extenstions.kt` are misspelled; `features/owner/dasboard` is misspelled.
   - Risk: navigation and imports become harder to search and maintain.
   - Suggested direction: fix package/file names in one controlled refactor.

4. **Many force unwraps and late initialization assumptions**
   - Evidence: `BaseFragment.binding`, `ForceUpdateActivity` URL, account form edit paths, sale selected customer, and bottom sheets use `!!`/`lateinit`.
   - Risk: crash-prone flows when navigation args, lifecycle timing, or UI state are unexpected.
   - Suggested direction: replace with explicit failure states or early returns where user input/navigation can be missing.

5. **Mixed persistence APIs**
   - Evidence: `SharedPrefsHelper`, Hilt-provided `SharedPreferences`, backup prefs, and DataStore theme preferences coexist.
   - Risk: settings and auth state become scattered.
   - Suggested direction: migrate simple preferences to DataStore, keeping SharedPreferences only where there is a compatibility reason.

6. **Release/dependency configuration is duplicated**
   - Evidence: version catalog defines Room `2.8.4`, but `app/build.gradle.kts` hardcodes Room `2.8.1`; many dependencies are hardcoded outside the catalog.
   - Risk: dependency drift and harder upgrades.
   - Suggested direction: move all dependency versions into `gradle/libs.versions.toml`.

7. **Generated/build artifacts and crash logs exist in the workspace**
   - Evidence: root contains `hs_err_pid33176.log`, `replay_pid33176.log`, and build outputs are present.
   - Risk: noisy scans, accidental commits, and large repository churn.
   - Suggested direction: ensure logs/build outputs are ignored and remove accidental tracked artifacts if any.

## 5. Quick Wins

1. Change `BackupWorkScheduler` interval from 15 minutes to 24 hours or a user-configurable cadence.
2. Gate WorkManager, Remote Config, Drive, and auth logs behind `BuildConfig.DEBUG`.
3. Enable `exportSchema = true` and add the Room schema location to Gradle.
4. Move all hardcoded dependency versions into `libs.versions.toml`.
5. Remove or quarantine `runBackupNowForTest()` from production builds.
6. Add composite indexes for the highest-traffic Room queries.
7. Add first real tests: milk sale save/update/delete, purchase save/update/delete, backup checksum validation, and migration 1->2.
8. Replace the raw database backup path or mark it deprecated so only one backup format remains.
9. Review `backup_rules.xml` and exclude databases/backups/preferences that should not be part of Android cloud backup.
10. Enable release minification and verify Firebase/Drive/PDF flows with R8.
11. Move signing keys out of the repository and update `.gitignore` to exclude `JKS/`.
12. Add a small architecture decision record documenting money sign conventions: debit, credit, payable, receivable, and `profitImpact`.
