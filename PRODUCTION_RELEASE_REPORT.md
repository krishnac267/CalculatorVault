# Calculator Vault — Production Release Report

**Version:** 1.1.0 (versionCode 2)  
**Date:** June 7, 2026  
**Status:** Release build verified — Play Store submission ready pending operator setup

---

## Executive Summary

Calculator Vault has been upgraded from an MVP privacy vault to a **production-oriented** release with:

- Hardened session lifecycle (background / screen-off lock)
- SQLCipher-encrypted Room database
- Google Play Billing + AdMob + Firebase Analytics integration
- Premium feature gating (free tier: 3 protected apps)
- Private notes, premium screen, compliant product positioning
- Signed release build pipeline (env-based keystore)
- Expanded unit + E2E test coverage

**Product positioning:** Privacy Vault / Secure App Launcher — **not** an "app hider."

---

## 1. Security Audit Report

| Control | Status | Implementation |
|---------|--------|----------------|
| PIN hashing (PBKDF2 100k) | Pass | `PinManager.kt` |
| EncryptedSharedPreferences | Pass | PIN, settings, DB key |
| Android Keystore MasterKey | Pass | `MasterKey` + alias |
| SQLCipher Room | **New** | `DatabaseKeyManager` + `SupportFactory` |
| Session timeout | Pass | 1–60 min configurable |
| Lock on background | **New** | `MainActivity.onStop()` |
| Lock on screen off | Pass | via `onStop` |
| Lock on resume if expired | Pass | `onResume` / `onPause` |
| Lock from launcher | Pass | `EXTRA_FROM_LAUNCHER` |
| FLAG_SECURE vault screens | Pass | `SecureScreenEffect` |
| Root/emulator detection | Code ready | `DeviceSecurityChecker` (UI warning optional) |
| No PIN in Logcat | Pass | No sensitive logging added |
| Backup disabled | Pass | manifest `allowBackup=false` |

**Remaining:** Add `google-services.json` for production Firebase project; host privacy policy URL.

---

## 2. Compliance Report

| Requirement | Status |
|-------------|--------|
| Honest marketing (no OS removal claims) | Pass — copy updated |
| Privacy policy draft | `docs/legal/PRIVACY_POLICY.md` |
| Terms of service draft | `docs/legal/TERMS_OF_SERVICE.md` |
| Data safety (local-first) | Documented |
| CAMERA permission | Declared + runtime |
| INTERNET / AD_ID / BILLING | Declared |
| AdMob policy (no ads on PIN/calc) | Pass — ads only on vault settings/dashboard paths |
| Play Billing subscriptions | Integrated — configure products in Play Console |
| Launcher Mode limitations | Documented in `LAUNCHER_MODE_REPORT.md` |

**Action before publish:** Replace placeholder support emails; create Play Console subscription SKUs matching `BuildConfig` product IDs.

---

## 3. Monetization Report

| Tier | Features |
|------|----------|
| **Free** | Up to 3 vault apps; banner ads on vault/settings/add-apps |
| **Premium** | Unlimited apps, biometric, decoy vault, intruder detection, themes |

| Integration | File |
|-------------|------|
| Play Billing 7.x | `BillingManager.kt` |
| AdMob (test IDs in debug) | `VaultBannerAd.kt`, manifest meta-data |
| Premium persistence | `PremiumRepositoryImpl.kt` |
| Analytics events | `FirebaseAnalyticsTracker.kt` |

**Product IDs:** `premium_monthly`, `premium_yearly`, `premium_lifetime`

---

## 4. Performance Report

| Area | Optimization |
|------|--------------|
| Launcher grid 200+ apps | Lazy icons, IO label load |
| Filter algorithm | O(n) — benchmarked <500ms for 250×100 |
| Release build | R8 minify + shrink resources enabled |
| SQLCipher | Native lib bundled (~libsqlcipher.so) |

Release R8 build completes successfully (~9 min on dev machine).

---

## 5. Test Report

| Suite | Count | Status |
|-------|-------|--------|
| Unit — CalculatorEngine | 19 | Pass |
| Unit — HandlePinAttempt | 3 | Pass |
| Unit — LauncherVisibilityFilter | 3 | Pass |
| Unit — PremiumGating | 3 | **New** |
| E2E (orchestrator) | 35+ | Previously 34/35 pass (1 biometric skip) |

**Run commands:**

```powershell
.\gradlew.bat test :domain:testDebugUnitTest :presentation:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

**Coverage note:** Module-level coverage ~35–45% (up from ~25%). Full 80% requires ViewModel/Room instrumented tests — roadmap item.

---

## 6. Bugs Found

| # | Issue |
|---|-------|
| 1 | Session not locked on app background |
| 2 | Plaintext Room database |
| 3 | No monetization stack |
| 4 | Misleading "app hider" positioning |
| 5 | Manifest merger AdMob/Firebase conflict |
| 6 | BillingManager package typo broke compile |
| 7 | `AddAppToVaultUseCase` had no free-tier limit |

---

## 7. Bugs Fixed

All items in §6 addressed in this release. Launcher Mode E2E + session lock tests added previously.

---

## 8. Remaining Risks

| Risk | Mitigation |
|------|------------|
| No production signing keystore in repo | Set `RELEASE_*` Gradle properties |
| Firebase requires `google-services.json` | Add before enabling production analytics |
| Play Console products not created | Create SKUs matching BuildConfig IDs |
| SQLCipher + migration | `fallbackToDestructiveMigration` on schema mismatch — document upgrade path |
| Physical device E2E | Run manually on USB device |
| 80% coverage target | Not yet met — continue ViewModel/DAO tests |

---

## 9. Release Checklist

- [x] Version bump 1.1.0 / code 2
- [x] Debug APK builds
- [x] Release APK builds (R8)
- [x] Release AAB builds (`bundleRelease`)
- [x] ProGuard rules (Hilt, Room, Gson, SQLCipher, Firebase, Ads)
- [x] Privacy policy draft
- [x] Terms draft
- [x] Store listing copy
- [ ] Upload AAB to Play Console internal track
- [ ] Add production AdMob app ID
- [ ] Add `google-services.json`
- [ ] Configure release signing keystore
- [ ] Create subscription products
- [ ] Host privacy policy URL

---

## Build Artifacts

After `./gradlew assembleDebug assembleRelease bundleRelease`:

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`
- Release AAB: `app/build/outputs/bundle/release/app-release.aab`

**Release signing:** Configure in `gradle.properties`:

```properties
RELEASE_STORE_FILE=/path/to/keystore.jks
RELEASE_STORE_PASSWORD=***
RELEASE_KEY_ALIAS=calculatorvault
RELEASE_KEY_PASSWORD=***
```

---

## Key New Files

| File | Purpose |
|------|---------|
| `DatabaseKeyManager.kt` | SQLCipher passphrase |
| `BillingManager.kt` | Play Billing |
| `FirebaseAnalyticsTracker.kt` | Analytics |
| `PremiumRepositoryImpl.kt` | Premium state |
| `SecureNotesScreen.kt` | Private notes UI |
| `PremiumScreen.kt` | Upgrade UI |
| `VaultBannerAd.kt` | AdMob banner composable |
| `docs/store/PLAY_STORE_LISTING.md` | Store copy |
| `docs/legal/PRIVACY_POLICY.md` | Privacy draft |
| `docs/legal/TERMS_OF_SERVICE.md` | Terms draft |

---

## Verdict

**Calculator Vault v1.1.0 is production-architecture ready** for Google Play internal testing. Complete operator setup (signing, Firebase, AdMob production IDs, Play Console SKUs, hosted legal URLs) before public launch.
