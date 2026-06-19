# Phase 5: Dead Code Cleanup & Security

## Goal
Remove all unused code, legacy files, and fix security issues. Make the codebase clean and production-ready.

---

## 5.1 RESEARCH

### 5.1.1 Dead Code Inventory

| File/Directory | Reason Dead | Action |
|---------------|-------------|--------|
| `app/src/main/java/com/yourfiles/manager/app/ui/` (5 files) | Legacy Material 2 theme, replaced by `app/uim3/theme/` | DELETE entire directory |
| `app/src/main/java/com/yourfiles/manager/presentation/ui/pages/HomePage.kt` | Never referenced in Router.kt. Uses old HomeVM + worker scan flow. ESHomeScreen is the active home. | DELETE |
| `app/src/main/java/com/yourfiles/manager/presentation/ui/pages/ScanningComposable.kt` | Only used by HomePage.kt which is dead | DELETE |
| `app/src/main/java/com/yourfiles/manager/presentation/ui/pages/ScanResultComposable.kt` | Only used by HomePage.kt which is dead | DELETE |
| `app/src/main/java/com/yourfiles/manager/presentation/vm/HomeVM.kt` | Only used by HomePage.kt which is dead | DELETE |
| `app/src/main/java/com/yourfiles/manager/presentation/WorkerUIState.kt` | Only used by HomeVM.kt which is dead | DELETE |
| `app/src/main/java/com/yourfiles/manager/presentation/vm/StorageUiState.kt` | Only used by HomeVM.kt which is dead | DELETE |
| `app/src/main/java/com/yourfiles/manager/misc/ui/ncdl.java` | Empty Java placeholder, purpose unknown | DELETE |
| `app/src/main/res/navigation/app_navigation.xml` | Empty nav graph, all navigation is in Router.kt | DELETE |
| `app/src/main/res/layout/connect_account_fragment.xml` | Legacy fragment, never referenced | DELETE |
| `app/src/main/res/layout/activity_main.xml` | Legacy layout, never used (Compose only) | DELETE (carefully — verify MainActivity.kt doesn't use it) |
| `app/src/main/java/com/yourfiles/manager/misc/model/MediaItem.kt` | Need to verify if used anywhere | VERIFY then DELETE if unused |
| `app/src/main/java/com/yourfiles/manager/helper/UpdateChecksumWorker.kt` | Need to verify if used | VERIFY then DELETE if unused |

### 5.1.2 Verify Before Deleting

**Critical step**: Before deleting any file, grep for its class/function name across the entire codebase to confirm zero references.

**Example verification commands**:
```bash
# Check if HomePage is referenced
rg "HomePage\|HomeComposable" --type kotlin
# Check if WorkerUIState is referenced
rg "WorkerUIState" --type kotlin
# Check if activity_main.xml is used
rg "activity_main" --type xml
```

### 5.1.3 Dependency Cleanup

| Dependency | Status | Action |
|-----------|--------|--------|
| Glide 5 (`com.github.bumptech.glide:compose`) | Codebase uses Coil 3 everywhere | VERIFY no Glide imports → REMOVE from build.gradle |
| Paging 3 (`androidx.paging:paging-runtime`) | Declared in dependencies but may not be used | VERIFY → REMOVE if unused |
| Work Manager | Used by HomeVM (dead code after Phase 5) — but also by Syncer/ReadFileWorker which IS used for the initial DB scan | KEEP (still needed) |

### 5.1.4 Security: Signing Credentials

**File**: `app/build.gradle`
**Problem**: Release keystore credentials are hardcoded:
```groovy
storeFile file('yourfiles-release.jks')
storePassword 'yourfiles_2024'
keyAlias 'yourfiles'
keyPassword 'yourfiles_2024'
```

**Fix**: Move to `local.properties` (which is .gitignored) or environment variables:
```groovy
// In local.properties:
// KEYSTORE_FILE=yourfiles-release.jks
// KEYSTORE_PASSWORD=yourfiles_2024
// KEY_ALIAS=yourfiles
// KEY_PASSWORD=yourfiles_2024

// In build.gradle:
def props = new Properties()
props.load(new FileInputStream(rootProject.file("local.properties")))
storeFile file(props['KEYSTORE_FILE'] ?: 'yourfiles-release.jks')
storePassword props['KEYSTORE_PASSWORD'] ?: ''
keyAlias props['KEY_ALIAS'] ?: ''
keyPassword props['KEY_PASSWORD'] ?: ''
```

### 5.1.5 Security: ProGuard Rules

**File**: `app/proguard-rules.pro`
**Problem**: File is empty/template. For a release build with obfuscation, this means:
- Room entities might break (need `@Keep` or rules)
- Gson/serialization might break
- Coil might break

**Fix**: Add standard ProGuard rules for all used libraries.

### 5.1.6 Database Migration Concern

`AppDatabase` uses `fallbackToDestructiveMigration(true)` which means any schema change drops all data. This is fine for development but risky for production. For now, keep it but add a comment.

---

## 5.2 PLAN

### Acceptance Criteria
- [ ] 5.1 All dead code files removed (verified by grep)
- [ ] 5.2 No compile errors after removal
- [ ] 5.3 Glide dependency removed from build.gradle (if verified unused)
- [ ] 5.4 Unused Paging dependency removed (if verified unused)
- [ ] 5.5 Signing credentials moved to local.properties
- [ ] 5.6 ProGuard rules added for Room, Coil, Gson
- [ ] 5.7 Legacy XML layouts removed
- [ ] 5.8 Empty ncdl.java removed
- [ ] 5.9 `./gradlew assembleDebug` succeeds
- [ ] 5.10 `./gradlew assembleRelease` succeeds (test release build)

### Files to Delete
| File | Verified Unused? |
|------|-----------------|
| `app/ui/Color.kt` | Must verify |
| `app/ui/Modifiers.kt` | Must verify |
| `app/ui/Shape.kt` | Must verify |
| `app/ui/Theme.kt` | Must verify |
| `app/ui/Type.kt` | Must verify |
| `presentation/ui/pages/HomePage.kt` | Must verify |
| `presentation/ui/pages/ScanningComposable.kt` | Must verify |
| `presentation/ui/pages/ScanResultComposable.kt` | Must verify |
| `presentation/vm/HomeVM.kt` | Must verify |
| `presentation/WorkerUIState.kt` | Must verify |
| `presentation/vm/StorageUiState.kt` | Must verify |
| `misc/ui/ncdl.java` | Must verify |
| `res/navigation/app_navigation.xml` | Must verify |
| `res/layout/connect_account_fragment.xml` | Must verify |
| `res/layout/activity_main.xml` | Must verify |

### Files to Modify
| File | Change |
|------|--------|
| `app/build.gradle` | Move signing creds to local.properties, remove unused deps |
| `local.properties` | Add signing credential keys |
| `app/proguard-rules.pro` | Add library-specific rules |
| `AndroidManifest.xml` | Remove any references to deleted layouts |

---

## 5.3 TEST CHECKLIST

| # | Test Case | Steps | Expected Result | Pass/Fail |
|---|-----------|-------|-----------------|-----------|
| 5.1.1 | Debug build | `./gradlew assembleDebug` | Build succeeds | |
| 5.1.2 | Release build | `./gradlew assembleRelease` | Build succeeds, APK generated | |
| 5.2.1 | App launches | Install debug APK → launch | ESHomeScreen shows | |
| 5.2.2 | Navigation works | Navigate to all screens from drawer | All screens open | |
| 5.2.3 | File browser works | Browse folders, open files | Normal operation | |
| 5.2.4 | Duplicates work | Open Cleaner | Duplicate groups load | |
| 5.3.1 | No Glide references | `rg "glide\|Glide" --type kotlin` | Zero results | |
| 5.3.2 | No dead imports | Build with -Werror (if possible) | No warnings | |
| 5.4.1 | local.properties has creds | Check local.properties | Signing keys present | |
| 5.4.2 | build.gradle reads from props | Check signing config block | Uses `props[...]` not hardcoded strings | |
| 5.5.1 | ProGuard rules present | Check proguard-rules.pro | Rules for Room, Coil, etc. | |
| 5.6.1 | No deleted file references | `rg "HomePage\|ScanningComposable\|ScanResultComposable\|WorkerUIState\|StorageUiState\|ncdl" --type kotlin` | Zero results | |

---

## 5.4 COMMIT TEMPLATE
```
phase-5: remove dead code, fix security issues, clean dependencies

- Remove legacy Material 2 theme (app/ui/ directory)
- Remove unused HomePage, ScanningComposable, ScanResultComposable, HomeVM
- Remove WorkerUIState, StorageUiState (dead after HomePage removal)
- Remove ncdl.java, empty navigation XML, legacy layout XMLs
- Move signing credentials from build.gradle to local.properties
- Add ProGuard rules for Room, Coil, Gson, ExoPlayer
- Remove Glide dependency (verified unused, Coil 3 used instead)
- Remove unused Paging 3 dependency (if verified)
- Verify all deletions leave no dangling references
```