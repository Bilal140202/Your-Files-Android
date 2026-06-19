# Phase 1: Critical Bug Fixes

## Goal
Fix existing broken functionality before adding new features. Every bug here affects real user workflows right now.

---

## 1.1 RESEARCH

### Bug 1: DrawerContent "Storage Analyzer" Navigates to HOME

**File**: `app/src/main/java/com/yourfiles/manager/presentation/ui/components/DrawerContent.kt`
**Line**: 84
**Current Code**:
```kotlin
DrawerMenuItem("Storage Analyzer", Icons.Outlined.Analytics, route = Routes.HOME),
```
**Problem**: Clicking "Storage Analyzer" in the navigation drawer navigates to the Home screen instead of the Storage Analyzer screen.
**Root Cause**: Copy-paste error — `Routes.HOME` was used instead of `Routes.ANALYZER`.
**Impact**: Users cannot access Storage Analyzer from the drawer at all.
**Fix**: Change `Routes.HOME` to `Routes.ANALYZER`.
**Risk**: None. Single-line fix.
**Files to Read**: DrawerContent.kt, Router.kt (verify ANALYZER route exists — it does: `"/analyzer"`)

### Bug 2: TrashPage Restore Logic is Broken

**File**: `app/src/main/java/com/yourfiles/manager/presentation/ui/pages/TrashPage.kt`
**Lines**: 126-128
**Current Code**:
```kotlin
val originalName = file.name.substringAfter("_", file.name)
val restored = TrashManager.undoTrash(mapOf(file.absolutePath to file.absolutePath))
```
**Problem**: `undoTrash()` expects a map of `originalPath → trashPath`. But the code passes `trashPath → trashPath` (same path for both keys and values). The function then tries to `renameTo` the file from itself to itself — which does nothing.
**Root Cause**: The trash system doesn't store the original path metadata. The trash filename format is `<timestamp>_<originalName>`, but the original *directory* path is lost.
**Impact**: Restore button shows a dialog and a progress spinner but the file is never actually moved back. The file stays in trash.
**Fix Strategy**:
  - Option A (minimal): Store original path in a SharedPreferences/JSON sidecar file in the trash directory
  - Option B (better): Change TrashManager to write a `.metadata` JSON file alongside each trashed file containing `{ "originalPath": "/storage/emulated/0/Download/photo.jpg" }`
  - Option C (simplest): Parse the filename and try to move back to `Environment.getExternalStorageDirectory()/<originalName>` as best-effort
**Recommended**: Option B — write a `.meta` file alongside each trashed file.
**Risk**: Medium — changes TrashManager API which is used by multiple screens (duplicates, WhatsApp, file browser).
**Files to Read**: TrashManager.kt, TrashPage.kt, FlatDuplicatesFileManagerPage.kt (uses moveToTrash), WhatsAppCleanerPage.kt (uses moveToTrash), FlatFileManagerDeleteComposable.kt

### Bug 3: File.size() Returns Kilobytes, Not Bytes

**File**: `app/src/main/java/com/yourfiles/manager/utils/FileUtil.kt`
**Line**: 71-73
**Current Code**:
```kotlin
fun File.size(): Long {
    return length() / 1024
}
```
**Problem**: The function divides by 1024, returning kilobytes. But the name `size()` implies bytes (like `File.length()`). Any caller expecting bytes gets wrong values.
**Root Cause**: Poor naming. The function should be `sizeInKB()` or return raw bytes.
**Impact**: Search for all callers:
  - Need to `grep` for `.size()` across the codebase to find usages
**Fix**: Rename to `sizeInKb()` with clear documentation. Or better, just remove it and use `file.length()` directly everywhere.
**Risk**: Low — but need to find ALL callers first.
**Files to Read**: FileUtil.kt, then grep for `.size()` usage

### Bug 4: Storage Analyzer Scans Without Permission Check

**File**: `app/src/main/java/com/yourfiles/manager/presentation/vm/StorageAnalyzerVM.kt`
**Lines**: 69-71
**Current Code**:
```kotlin
init {
    analyzeStorage()
}
```
**Problem**: The VM starts scanning immediately in `init{}` without checking if `MANAGE_EXTERNAL_STORAGE` is granted. On Android 11+, without this permission, `File.walkTopDown()` can only see the app's own directory (~80MB), not the full storage.
**Root Cause**: No permission gate.
**Impact**: Users see a "scanning" spinner, then get a tiny donut chart showing only app-private files. They think the app is broken.
**Fix**: Add a permission check in the Screen composable. If permission not granted, show a permission request screen (similar to PermissionRequiredPage.kt pattern). Only create the VM after permission is confirmed.
**Risk**: Low — this is standard Android pattern.
**Files to Read**: StorageAnalyzerVM.kt, StorageAnalyzerScreen.kt, PermissionRequiredPage.kt, OnboardingPage.kt (for permission request pattern), AndroidManifest.xml

### Bug 5: DrawerContent "APKs" Opens Downloads Folder

**File**: `app/src/main/java/com/yourfiles/manager/presentation/ui/components/DrawerContent.kt`
**Line**: 81
**Current Code**:
```kotlin
DrawerMenuItem("APKs", Icons.Outlined.Memory, path = "$primaryPath/Download"),
```
**Problem**: "APKs" menu item opens the Downloads folder. If the user wanted Downloads they'd click "Downloads".
**Fix**: Navigate to `Routes.MEDIA_STORE_CATEGORY` with `CategoryType.APK` instead of a folder path.
**Risk**: None.
**Files to Read**: DrawerContent.kt, MediaStoreCategoryScreen.kt, Router.kt

---

## 1.2 PLAN

### Acceptance Criteria
- [ ] 1.1 Clicking "Storage Analyzer" in drawer opens the Storage Analyzer screen
- [ ] 1.2 Restoring a file from Trash moves it back to its original location
- [ ] 1.3 `File.size()` is either removed or renamed to `sizeInKb()` with all callers updated
- [ ] 1.4 Storage Analyzer shows a permission request if MANAGE_EXTERNAL_STORAGE is not granted
- [ ] 1.5 Clicking "APKs" in drawer opens the APK category list (not Downloads folder)

### Files to Modify
| File | Change |
|------|--------|
| `DrawerContent.kt` | Fix ANALYZER route, fix APKs route |
| `TrashPage.kt` | Fix restore logic to use original path |
| `TrashManager.kt` | Add metadata file writing/reading for original path tracking |
| `FileUtil.kt` | Rename `size()` to `sizeInKb()` |
| `StorageAnalyzerScreen.kt` | Add permission gate before creating VM |
| `*.kt` (callers of `.size()`) | Update to use `sizeInKb()` or `length()` |

---

## 1.3 IMPLEMENTATION NOTES

### Bug 1 Fix (1 line)
```kotlin
// DrawerContent.kt line 84
DrawerMenuItem("Storage Analyzer", Icons.Outlined.Analytics, route = Routes.ANALYZER),
```

### Bug 2 Fix (TrashManager metadata)
```kotlin
// In TrashManager.moveToTrash(), after renaming:
val metaFile = File(trashDir, "${trashFile.name}.meta")
metaFile.writeText(Json.encodeToString(TrashMeta(originalPath = filePath)))

// In TrashManager.getTrashFiles(), return pairs of (trashFile, metaFile):
// In TrashManager.undoTrash(), read meta to get original path
```

### Bug 3 Fix
```kotlin
// FileUtil.kt - rename:
fun File.sizeInKb(): Long = length() / 1024
// Then grep -rn '\.size()' to find and update all callers
```

### Bug 4 Fix (Permission gate)
```kotlin
// StorageAnalyzerScreen.kt — wrap VM creation:
if (!Environment.isExternalStorageManager()) {
    PermissionRequiredContent(onPermissionGranted = { /* create VM */ })
} else {
    val viewModel: StorageAnalyzerVM = viewModel()
    // ... existing content
}
```

### Bug 5 Fix
```kotlin
// DrawerContent.kt line 81
DrawerMenuItem("APKs", Icons.Outlined.Memory, route = "${Routes.MEDIA_STORE_CATEGORY}/apk"),
```

---

## 1.4 TEST CHECKLIST

| # | Test Case | Steps | Expected Result | Pass/Fail |
|---|-----------|-------|-----------------|-----------|
| 1.1.1 | Drawer → Storage Analyzer | Open drawer → tap "Storage Analyzer" | Storage Analyzer screen opens (not Home) | |
| 1.1.2 | Drawer → APKs | Open drawer → tap "APKs" | APK category list opens (not Downloads folder) | |
| 1.2.1 | Trash restore | Delete a file → open Trash → tap Restore | File moves back to original location | |
| 1.2.2 | Trash restore — original folder deleted | Delete file → delete original folder → restore from trash | File restored, parent folder recreated | |
| 1.2.3 | Empty trash still works | Add files to trash → Empty Trash | All files permanently deleted | |
| 1.2.4 | Duplicate delete to trash still works | Delete duplicates from Cleaner → check Trash | Files appear in trash with correct metadata | |
| 1.3.1 | Build compiles | `./gradlew assembleDebug` | Build succeeds with no errors | |
| 1.4.1 | Analyzer without permission | Revoke MANAGE_EXTERNAL_STORAGE → open Analyzer | Permission request screen shown, no crash | |
| 1.4.2 | Analyzer with permission | Grant permission → open Analyzer | Scanning starts normally | |
| 1.5.1 | Build passes all Phase 1 | Clean build after all fixes | `./gradlew assembleDebug` succeeds | |

---

## 1.5 COMMIT TEMPLATE
```
phase-1: fix critical bugs (drawer routes, trash restore, file.size, analyzer permission)

- Fix DrawerContent "Storage Analyzer" route (was Routes.HOME → Routes.ANALYZER)
- Fix DrawerContent "APKs" to navigate to APK category instead of Downloads
- Fix TrashManager restore: write .meta files with original path, use on undo
- Rename File.size() to File.sizeInKb() to prevent confusion
- Add permission gate to Storage Analyzer before scanning
```