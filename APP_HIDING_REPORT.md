# App Hiding Implementation — Analysis Report

**Project:** Calculator Vault  
**Date:** 2026-06-07

---

## 1. How Hidden Apps Are Stored

Hidden apps are **not hidden at the Android OS level**. They are stored as **metadata bookmarks** in a local Room database.

| Layer | Detail |
|-------|--------|
| **Database** | Room `VaultDatabase`, table `vault_apps` |
| **Entity** | `VaultAppEntity`: `packageName`, `appName`, `isFavorite`, `isFake`, `lastOpenedAt`, `addedAt` |
| **DAO** | `VaultAppDao` — insert/delete/query by package name |
| **Repository** | `VaultRepositoryImpl` maps entities ↔ domain `VaultApp` |
| **Add flow** | User picks from installed apps in `AddAppsScreen`; `AddAppToVaultUseCase` inserts a row |
| **Remove flow** | Delete row from `vault_apps`; app remains installed on device |

**Important:** No APK is moved, copied, or disabled. The vault only records *which package names the user wants to manage through the vault UI*.

Fake/decoy vault apps use the same table with `isFake = 1` (e.g. `fake.notes`, `fake.gallery` seeded by `FakeVaultRepositoryImpl`).

---

## 2. How Hidden Apps Are Launched

Launch uses the **standard Android activity intent** mechanism:

```kotlin
// InstalledAppRepositoryImpl.launchApp()
val launchIntent = pm.getLaunchIntentForPackage(packageName)
launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
context.startActivity(launchIntent)
```

**Flow:**
1. User enters PIN on calculator → navigates to vault dashboard
2. User taps app in Hidden / Favorites / Recent tab
3. `VaultViewModel.launchApp()` → `LaunchVaultAppUseCase`
4. Records `lastOpenedAt` in DB, refreshes session, starts app's default launcher activity

There are **no vault-specific shortcuts**, **no private activity aliases**, and **no work-profile containers**. It is identical to tapping the app in the system launcher.

---

## 3. Mechanism Classification

| Mechanism | Used? | Evidence |
|-----------|-------|----------|
| **Vault bookmarks + PIN-gated launcher UI** | **Yes (primary)** | Room `vault_apps`, vault dashboard, `LaunchVaultAppUseCase` |
| **Custom launcher (optional home app)** | **Yes (partial)** | `VaultLauncherActivity` with `CATEGORY_HOME`; filters vault apps from *its* grid |
| **Device Owner / DPM hide** | **No** | No `DeviceAdminReceiver`, no `DevicePolicyManager.setApplicationHidden()` |
| **Root / su** | **No** | `DeviceSecurityChecker` only *detects* root; no hide logic |
| **Work profile / shelter apps** | **No** | Single-user, no `DevicePolicyManager` provisioning |
| **Disable launcher component** | **No** | No `PackageManager.setComponentEnabledSetting()` |
| **Private space (Android 15)** | **No** | Not integrated |

**Conclusion:** This is a **PIN-protected vault catalog** plus an **optional custom home launcher** that omits vault-listed apps from its own grid. It is **not** system-wide app hiding.

---

## 4. Why Apps Remain Visible Outside the Vault

1. **Adding to vault ≠ hiding from OS** — Only a DB row is created; the app's launcher icon and `PackageManager` entry are unchanged.

2. **Default launcher unchanged** — Unless the user sets Calculator Vault as the **default home app**, Pixel Launcher / Samsung Home / etc. still show every installed app.

3. **Settings → Apps** — All installed packages remain visible in system Settings regardless of vault membership.

4. **Direct intents** — Other apps, notifications, and deep links can still open vault-listed packages.

5. **Recent apps / search** — System recents and global search are unaffected.

6. **`launcherModeEnabled` was informational only** — Previously a settings flag with no toggle or home-role prompt; now improved (see §8).

---

## 5. Is True System-Wide Hiding Possible With Current Architecture?

**No — not without changing the trust model and deployment method.**

The current stack (normal Play Store / sideload app, no device admin provisioning) **cannot** remove apps from:
- The system default launcher (unless this app *is* the default launcher)
- Settings → Apps
- Other apps' ability to launch packages
- Notification-driven entry points

The architecture was intentionally **Play Store compliant** (see README: *"Modern Android does not allow third-party apps to invisibly hide arbitrary apps system-wide without launcher or device-owner privileges."*).

---

## 6. Android Restrictions Preventing True Hiding

| Restriction | Impact |
|-------------|--------|
| **No hide API for normal apps** | `PackageManager.setApplicationHidden()` is **Device Owner / Profile Owner only** (API 21+) |
| **Launcher icons are manifest-declared** | Third parties cannot remove another app's `MAIN/LAUNCHER` activity from the system launcher |
| **Package visibility (API 30+)** | Limits *querying* apps; does not hide installed apps from the user |
| **Scoped storage & sandboxing** | Vault app cannot modify other apps' data or install location |
| **Play Store policy** | Apps that mislead about hiding, require Device Owner without enterprise use, or use root are rejected |
| **User consent for home role** | Replacing the launcher requires explicit user choice (`RoleManager.ROLE_HOME`) |
| **Private Space (Android 15)** | OEM/system feature; not available to arbitrary third-party vault apps |

**Device Owner path:** Requires NFC/USB provisioning, MDM enrollment, or `adb dpm set-device-owner` — suitable for **enterprise**, not consumer calculator-vault apps.

**Root path:** Could hide/disable packages via `pm hide` / `pm disable` — voids warranty, fails SafetyNet/Play Integrity, not shippable on Play Store.

---

## 7. Recommended Best Achievable Solution

For a **consumer disguised vault app**, the best realistic model is:

### Tier A — What you have now (improve UX)
- **Vault catalog** behind calculator PIN (access control, not OS hiding)
- **Decoy vault** for duress scenarios
- **Optional custom launcher** as default home → hides vault apps **from the home screen only**

### Tier B — Enhancements (implemented / recommended)
- ✅ Filter vault apps from `VaultLauncherActivity` grid (`LauncherVisibilityFilter`)
- ✅ Toggle + “Set as default home app” in Settings (`RoleManager.ROLE_HOME`)
- ✅ Refresh launcher grid on resume when apps are added/installed
- 📋 In-app onboarding explaining: *“Hidden = hidden from home when using Calculator Vault as launcher; open vault via PIN to launch”*
- 📋 Shortcuts to vault apps on vault dashboard (already present via tap-to-launch)

### Tier C — Not recommended for this product
- **Device Owner hiding** — enterprise-only, heavy provisioning, Play policy risk
- **Root hiding** — unstable, detectable, not distributable on Play Store
- **Claiming “invisible to system”** — inaccurate and policy-violating

---

## 8. Custom Launcher Implementation (Done)

**Existing:** `VaultLauncherViewModel` excluded packages listed in `vault_apps`.

**Enhancements added:**
- `LauncherVisibilityFilter` — shared filter logic (data layer)
- Reactive reload of installed apps on home resume
- Hidden-app count banner on home screen
- Calculator icon in home toolbar → opens disguised `MainActivity`
- Settings: **Enable launcher mode** toggle + **Set as default home app** button

**Behavior when user sets Calculator Vault as default home:**
- Vault-listed apps **do not appear** on the home grid
- User opens calculator (toolbar) → PIN → vault → launch hidden apps
- Non-vault apps appear normally on home
- Pressing Home returns to this filtered grid

**Limitation:** If user switches back to Pixel Launcher, all apps are visible again.

---

## 9. Device Owner APIs — Not Implemented (By Design)

`DevicePolicyManager.setApplicationHidden(componentName, packageName, true)` can hide apps from the launcher **only when the app is Device Owner or Profile Owner**.

**Why not implemented:**
- Requires provisioning flow outside normal app install
- Incompatible with casual sideload / Play Store consumer use
- Google Play restricts Device Admin abuse
- Would change app identity from “calculator vault” to “MDM / enterprise tool”

**If ever needed (enterprise build flavor only):**
- Separate `enterprise` product flavor
- `DeviceAdminReceiver` + provisioning QR / NFC
- Call `setApplicationHidden` on vault add/remove
- Still cannot hide from Settings on all OEMs; behavior varies by Android version

---

## 10. Comparison Table

| Aspect | Current behavior | Desired behavior (user expectation) | Android limitation | Recommended implementation |
|--------|------------------|-------------------------------------|--------------------|----------------------------|
| **Add app to vault** | Saves package name in Room DB | App disappears from phone | Normal apps cannot hide other packages | Keep DB bookmark; explain clearly in UI |
| **App icon on home screen** | Still visible in default launcher | Hidden everywhere | Only custom default launcher can filter its own grid | User sets Calculator Vault as **default home** |
| **Launch hidden app** | Tap in vault after PIN | Tap hidden icon / name | No OS “hidden launch” API | Vault dashboard launch via `getLaunchIntentForPackage()` |
| **Settings → Apps list** | Always visible | Hidden | No API for third-party apps | Not achievable without Device Owner |
| **Notifications** | Still delivered | Suppressed | Requires notification listener / DND per app | Out of scope; optional future feature |
| **Uninstall detection** | Vault row remains if app removed | Auto-remove from vault | Can listen to `PACKAGE_REMOVED` | Optional: broadcast receiver cleanup |
| **Fake vault** | Decoy dashboard | N/A | N/A | Keep current decoy PIN flow |
| **Security model** | PIN + encrypted prefs | N/A | N/A | Keep; add launcher onboarding |
| **Play Store viability** | Compliant | N/A | DPM/root approaches fail review | Custom launcher + vault catalog |
| **Enterprise full hide** | Not supported | Full hide | Requires Device Owner | Separate enterprise flavor if needed |

---

## Summary

Calculator Vault implements **access-controlled app management**, not **OS-level app removal**. Hidden apps are **Room DB entries** launched via **standard intents**. Optional **custom launcher mode** provides the **maximum hiding Android allows** to a normal app: vault apps disappear from **your home screen only** when Calculator Vault is the default launcher. True system-wide invisibility is **not technically or policy-wise achievable** with the current consumer architecture; Device Owner and root paths were intentionally excluded.
