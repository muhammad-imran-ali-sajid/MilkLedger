# MilkLedger Testing Setup

This project should use a clean emulator for connected Android tests.

Do not run connected tests against a phone that contains important real MilkLedger client data unless the debug build uses a separate application id and test data is isolated.

## Why The Physical Phone Test Failed

The first connected test attempt failed before any test executed.

Observed facts:

- Gradle generated a connected test report with `0 tests` and `0 failures`.
- The UTP log only reported a fatal runner startup error.
- `adb shell pm list instrumentation` did not show `com.miassolutions.milkledger.test`, which means the MilkLedger test instrumentation package was not installed.
- The connected Vivo device had multiple Android users/profiles:
  - user `0`: primary user
  - user `10`: Private Space
  - user `999`: App Clone
- `adb` printed `Shell does not have permission to access user 10`.

Conclusion: this was a device/test-runner setup failure, not a sale repository assertion failure.

## Recommended Emulator For This Laptop

Detected machine:

- Lenovo `20UGS40100`
- AMD Ryzen 5 PRO 4650U
- 6 cores / 12 logical processors
- 16 GB RAM
- Integrated AMD Radeon graphics

Recommended Android Studio AVD:

```text
Device definition: Pixel 4a or Pixel 5
System image: Google APIs x86_64
API level: 35 preferred
RAM: 2048 MB
VM heap: 256 MB
Internal storage: 4 GB to 8 GB
SD card: none
Graphics: Hardware / Auto
Startup: Quick Boot
Camera: None
Network: default
Multi-Core CPU: 2 cores
```

Use `Google APIs` for normal testing. Use `Google Play` only when testing flows that require Play Store/account behavior.

Avoid heavy tablet/foldable devices for routine development. Avoid very high-resolution devices unless UI testing specifically needs them.

Avoid experimental/canary images for routine refactoring safety checks. In particular, avoid `sdk_gphone16k_x86_64` / 16 KB page-size images unless you are specifically testing Android 15+ 16 KB compatibility. They are useful compatibility targets, but they are not the best daily safety-net emulator.

## Android Studio Steps

1. Open Device Manager.
2. Create virtual device.
3. Pick `Pixel 4a` or `Pixel 5`.
4. Pick a `Google APIs x86_64` image.
5. Open advanced settings.
6. Set RAM to `2048 MB`.
7. Set VM heap to `256 MB`.
8. Set internal storage to `4 GB` or `8 GB`.
9. Disable front and back camera.
10. Keep graphics as `Hardware` or `Auto`.
11. Start the emulator.
12. Run `:app:connectedDebugAndroidTest`.

If Gradle's connected test task reports `0 tests`, verify whether the test APK actually installed:

```powershell
adb -s emulator-5554 shell pm list instrumentation
```

You can run the sale safety tests directly with:

```powershell
adb -s emulator-5554 shell am instrument -w -r -e class com.miassolutions.milkledger.features.sale.data.MilkSaleRepositoryTest com.miassolutions.milkledger.test/androidx.test.runner.AndroidJUnitRunner
```

Direct instrumentation is a useful diagnostic fallback, but the long-term goal is still to make the normal Gradle/Android Studio test runner work on a stable emulator image.

## Current Emulator Finding

The first emulator created for this refactor session was:

```text
Model: sdk_gphone16k_x86_64
API: 37
Page size: 16384
```

On that image, Gradle/UTP still produced a connected test report with `0 tests`, but manual APK install and direct instrumentation worked. The sale repository safety tests passed with:

```text
OK (3 tests)
```

Recommendation: keep this emulator if you want to test future Android compatibility, but create a second daily-development emulator using API 35 Google APIs x86_64.

## Developer Mental Model

Use the emulator as a disposable lab.

The real client-data phone is for manual acceptance checks. The emulator is for automated safety checks.

For ledger refactoring:

```text
Real phone:
  - manual workflow confidence
  - real-world smoke testing
  - never destructive testing

Clean emulator:
  - repository tests
  - DAO tests
  - migration tests
  - repeatable refactor safety
```

This distinction keeps professional discipline: we protect client data and still move fast.
