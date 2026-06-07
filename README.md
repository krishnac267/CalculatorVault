# Calculator Vault

A production-quality Android vault app disguised as a standard **Calculator**. Built with Kotlin, Jetpack Compose, Clean Architecture, and Material Design 3.

## Features

- **Disguised calculator** — App name "Calculator", calculator icon, fully functional math (+-×÷, %, √, power, memory)
- **Secret PIN access** — Enter 4–8 digit PIN + `=` (e.g. `1234=`) to unlock the hidden vault
- **Fake vault** — Optional decoy PIN (e.g. `9999=`) shows fake apps/content
- **Secure storage** — PIN hashed with PBKDF2; credentials in EncryptedSharedPreferences + Android Keystore
- **Vault dashboard** — Hidden Apps, Favorites, Recent, Settings tabs with glassmorphism UI
- **App vault** — Add launchable apps to vault; access only through secret dashboard (Play Store compliant)
- **Optional launcher mode** — Set as home app to organize apps (does not remove system installs)
- **Security** — Biometric unlock, FLAG_SECURE on vault, session timeout, root/emulator detection, intruder log

## Architecture

```
app/            → Application, MainActivity, Manifest
presentation/   → Compose UI, ViewModels, Navigation
domain/         → Models, Repository interfaces, Use Cases
data/           → Room DB, Repository implementations
security/       → PIN, Biometrics, Session, Device checks, Intruder capture
```

**Stack:** Kotlin · MVVM · Clean Architecture · Jetpack Compose · Navigation Compose · Hilt · Room · Coroutines · Flow · Material 3

## Getting Started

1. Open `CalculatorVault` in **Android Studio Ladybug** (or newer)
2. Sync Gradle (JDK 17 required)
3. Run on device/emulator API 26+

```bash
cd C:\Users\krishcho\Projects\CalculatorVault
gradlew assembleDebug
```

## CI

GitHub Actions runs on every push/PR to `main`:

- **Unit tests** — `CalculatorEngineTest`, `HandlePinAttemptUseCaseTest`
- **E2E tests** — 32 Compose tests with Android Test Orchestrator on API 35 emulator

```bash
# Local E2E (emulator must be running)
./gradlew :app:connectedDebugAndroidTest
```

Workflow: [`.github/workflows/ci.yml`](.github/workflows/ci.yml)

## Download APK

Debug build for sideloading (API 26+):

- **GitHub Release:** [v1.1.0-debug](https://github.com/krishnac267/CalculatorVault/releases/tag/v1.1.0-debug)
- **In repo:** [`releases/CalculatorVault-v1.1.0-debug.apk`](releases/CalculatorVault-v1.1.0-debug.apk)

Package ID: `com.calculator.vault.debug`

## First Launch Setup

1. Create a **secret PIN** (4–8 digits)
2. Set a **security question** and answer
3. Optionally enable **fake vault PIN** and **biometric unlock**

## Unlocking the Vault

On the calculator screen, type your PIN and press **=**.

| PIN type   | Example   | Result        |
|-----------|-----------|---------------|
| Real vault | `1234=`  | Hidden dashboard |
| Fake vault | `9999=`  | Decoy dashboard  |
| Wrong PIN  | `0000=`  | Behaves as calculator input |

After **3 failed PIN attempts**, an intruder log entry is created (front camera photo when permitted).

## Database Schema

| Table          | Purpose                          |
|----------------|----------------------------------|
| `vault_apps`   | Hidden/fake apps with favorites & last opened |
| `fake_content` | Decoy photos, notes, apps        |
| `intruder_logs`| Failed access timestamps + photos |

## Permissions

- `CAMERA` — Intruder photo capture (optional, runtime)
- Package visibility via `<queries>` — List launchable apps to add to vault (Android 11+)

## Important Notes

- Modern Android **does not allow** third-party apps to invisibly hide arbitrary apps system-wide without launcher or device-owner privileges.
- This app implements a **realistic vault/launcher** model: apps stay installed; vault controls access and optional home-screen organization.
- Never store or log PINs in plain text.

## Project Location

`C:\Users\krishcho\Projects\CalculatorVault`
