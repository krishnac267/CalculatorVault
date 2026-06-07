# Launcher Mode — Implementation & Verification Report

**Project:** Calculator Vault  
**Date:** 2026-06-07  
**Scope:** Primary launcher experience with vault app filtering

---

## Executive Summary

Launcher Mode makes Calculator Vault an optional **default home app**. When enabled and set as the system home role, apps stored in the vault are **removed from the home grid** and accessible only after entering the calculator PIN. The implementation uses `RoleManager.ROLE_HOME`, reactive package monitoring, and a performance-oriented grid model suitable for 200+ installed apps.

---

## 1. Which Apps Are Filtered

| Category | Filtered from home grid? | Still launchable from vault? |
|----------|--------------------------|------------------------------|
| Apps in real vault (`vault_apps`, `isFake = 0`) | **Yes** | **Yes** (via PIN → vault dashboard) |
| Calculator Vault itself (`ownPackage`) | **Yes** (always hidden from grid) | N/A (use lock icon → calculator) |
| Decoy/fake vault apps (`isFake = 1`) | **No** (not in real vault filter set) | Via fake PIN only |
| All other installed launchable apps | **No** | N/A |

**Filter logic:** `LauncherVisibilityFilter.filterVisiblePackages()` excludes `ownPackage` and every package in the real vault bookmark list.

**Example (typical emulator after adding Settings to vault):**

| Package | In vault? | On home grid? |
|---------|-----------|---------------|
| `com.android.settings` | Yes | Hidden |
| `com.android.chrome` | No | Visible |
| `com.calculator.vault.debug` | N/A (own app) | Hidden |

---

## 2. Launcher Behavior — Before Changes

| Behavior | Status |
|----------|--------|
| `launcherModeEnabled` setting | Stored but limited UX |
| Default home prompt | Manual button only; no auto-prompt on toggle |
| Home grid filtering | Partial / inconsistent refresh |
| Hidden count banner | Generic wording (`"X app(s) hidden in vault"`) |
| Lock icon → calculator + PIN lock | Not wired |
| Auto-refresh on install/uninstall/vault change | Incomplete |
| Performance with 200+ apps | Icons loaded eagerly in ViewModel |
| E2E coverage for launcher | None |

---

## 3. Launcher Behavior — After Changes

### 3.1 Enable flow (Settings)

1. User toggles **Enable launcher mode** → setting persisted via `UpdateSettingsUseCase`.
2. If Calculator Vault is **not** already the default home app, **`RoleManager.ROLE_HOME`** request launches automatically (API 29+).
3. **Set as default home app** button remains for manual re-prompt; uses shared `LauncherHomeRole` helper.

### 3.2 Home screen (`VaultLauncherActivity`)

| Feature | Implementation |
|---------|----------------|
| Grid contents | `VaultLauncherViewModel` + `LauncherVisibilityFilter` |
| Banner | `"X apps hidden in Calculator Vault"` via `LauncherModeMessages` |
| Lock icon | Opens `MainActivity` with `EXTRA_FROM_LAUNCHER` → locks session, shows calculator |
| Refresh triggers | `ON_RESUME`, vault Flow updates, `LauncherPackageChangeMonitor` (PACKAGE_ADDED/REMOVED/REPLACED) |
| Uninstall cleanup | `RemoveAppFromVaultUseCase` when package removed |
| Performance | Labels on `Dispatchers.Default`; icons lazy-loaded in Compose per visible item |

### 3.3 User flows

```
[Home grid] ──tap lock──► [Calculator] ──PIN──► [Vault] ──tap app──► [Hidden app launches]
[Home grid] ──tap visible app──► [App launches normally]
```

---

## 4. Verification Results

### 4.1 Unit tests (`domain`)

| Test | Result |
|------|--------|
| `filterVisiblePackages_excludesVaultAppsAndOwnPackage` | Pass |
| `filterVisiblePackages_250Apps_completesUnder500ms` | Pass (100 iterations) |
| `hiddenAppsBanner_formatsCorrectly` | Pass |

### 4.2 E2E tests (Android Test Orchestrator)

| Test class | Test | Result (emulator API 35) |
|------------|------|--------------------------|
| `LauncherModeE2ETest` | `hiddenVaultApp_doesNotAppearInLauncherGrid` | **Pass** |
| `AuthenticationE2ETest` | `openedFromLauncher_requiresPinBeforeVault` | **Pass** |

Full suite: **34/35 pass** on `Pixel_API_35` (1 skipped: biometric hardware). Run via:

```powershell
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat :app:connectedDebugAndroidTest
```

**Physical device:** Connect via USB debugging and run the same command. `ROLE_HOME` assignment requires manual confirmation on device; automated tests launch `VaultLauncherActivity` directly and seed vault data programmatically.

### 4.3 Manual checklist

- [ ] Enable launcher mode → ROLE_HOME prompt appears  
- [ ] Set as default home → press Home → Calculator Vault grid shown  
- [ ] Add app to vault → app disappears from grid; banner count updates  
- [ ] Remove app from vault → app reappears on grid  
- [ ] Install new app → appears on grid (if not vaulted)  
- [ ] Uninstall vaulted app → removed from vault DB and grid  
- [ ] Tap lock → calculator shown; PIN required for vault  
- [ ] Launch hidden app from vault → app opens normally  

---

## 5. Remaining Android Limitations

| Limitation | Impact |
|------------|--------|
| **Not system-wide hiding** | Apps remain in non-default launchers, Settings → Apps, notifications, deep links |
| **User must set default home** | Filtering applies only when Calculator Vault holds `ROLE_HOME` |
| **`ROLE_HOME` is consent-based** | User can decline or switch back to Pixel/Samsung launcher anytime |
| **No `setApplicationHidden()`** | Requires Device Owner / enterprise provisioning |
| **Cannot disable other apps' launcher icons** | No API for normal third-party apps |
| **Recent apps / global search** | Unaffected by custom launcher grid |
| **Work profile / Private Space** | Not integrated |
| **Dual launcher icons** | Calculator Vault still has a LAUNCHER entry (calculator disguise) in app drawer on some OEMs |

---

## 6. Key Files

| File | Role |
|------|------|
| `VaultLauncherActivity.kt` | HOME activity, grid UI, lock icon, package monitor |
| `VaultLauncherViewModel.kt` | Filtered app list, refresh, uninstall cleanup |
| `LauncherVisibilityFilter.kt` | Pure filter logic |
| `LauncherModeMessages.kt` | Banner copy |
| `LauncherHomeRole.kt` | `ROLE_HOME` intent + default-home detection |
| `LauncherPackageChangeMonitor.kt` | Install/uninstall broadcasts |
| `SettingsScreen.kt` | Toggle + auto-prompt |
| `MainActivity.kt` | `EXTRA_FROM_LAUNCHER` session lock |

---

## 7. Conclusion

Launcher Mode delivers the **best achievable consumer-grade hiding**: vault-listed apps are omitted from Calculator Vault's home grid when it is the default launcher, with automatic refresh and PIN-gated access via the lock icon. True OS-level invisibility is **not possible** without device-owner or root privileges; this design matches Play Store–compliant architecture documented in `APP_HIDING_REPORT.md`.
