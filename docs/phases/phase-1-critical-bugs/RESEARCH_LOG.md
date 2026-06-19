# Phase 1 — Critical Bug Fixes

**Date**: 2026-06-19
**Branch**: fix/comprehensive-fixes
**Commit**: d75a718

## Research Findings

### DrawerContent L84
- Storage Analyzer menu item used `route = Routes.HOME` instead of `route = Routes.ANALYZER`
- Routes.ANALYZER ("/analyzer") is properly defined in Router.kt:155 and wired at Router.kt:100
- Users tapping "Storage Analyzer" in the drawer were sent back to the home screen

### TrashPage L126-127 + TrashManager L29,110,124
- `undoTrash()` was called with `mapOf(file.absolutePath to file.absolutePath)` — both key and value were the trash path
- The `lastTrashedEntries` map was in-memory only, cleared on each new trash operation and never persisted
- App restart = all original paths lost = restore impossible
- No way to reconstruct original path from trash filename alone (only filename recoverable, not full path)

### FileUtil.kt L70-73
- `File.size()` returned KB (length() / 1024) but comment said "mb"
- Function name `size()` implied bytes (like `File.length()`)
- Only 1 caller existed (`ZipBrowserScreen.zip.size()` which is a different `size()` on ZipFile, not our extension)

## Implementation Changes

### 1. DrawerContent.kt
- Line 84: `route = Routes.HOME` → `route = Routes.ANALYZER`

### 2. TrashManager.kt — Full rewrite
- Added `TrashRecord` data class (originalPath, trashPath, timestamp)
- Added SharedPreferences persistence: `loadRecords()`, `saveRecords()`
- Added `restoreFile(trashFile: File): Boolean` — looks up persisted original path
- Added `getOriginalPath(context, trashFile): String` — for UI display
- `moveToTrash()` now saves records to SharedPreferences
- `undoTrash()` and `emptyTrash()` now clean up persisted records
- `reconstructOriginalPath()` fallback: extracts filename from trash name, restores to Download

### 3. TrashPage.kt
- Lines 120-130: Replaced broken `undoTrash(mapOf(...))` with `TrashManager.restoreFile(file)`
- Much simpler and correct — single line call

### 4. FileUtil.kt
- Renamed `File.size()` → `File.sizeInKB()`
- Fixed comment from "mb" to "KB"
- No callers to update (only ZipFile.size() which is unrelated)

## Test Results

| Test | Status |
|------|--------|
| Drawer Storage Analyzer → navigates to analyzer screen | ✅ Verified via code |
| File.sizeInKB() returns correct KB values | ✅ Verified via code |
| TrashManager.restoreFile() uses persistent records | ✅ Verified via code |
| TrashManager reconstructOriginalPath() fallback works | ✅ Verified via code |
| No compilation errors (static analysis) | ✅ 0 errors found |