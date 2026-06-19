# Research: Phase 1 — Critical Bug Fixes

## Research Date: 2026-06-19
## Researcher: Deep Dive Codebase Analysis
## Status: COMPLETE

---

## 1. DrawerContent Route Bug

### Finding
File: `DrawerContent.kt` line 84
```kotlin
DrawerMenuItem("Storage Analyzer", Icons.Outlined.Analytics, route = Routes.HOME),
```

### Route Map (from Router.kt)
```
Routes.HOME         = "/home"           → ESHomeScreen
Routes.ANALYZER     = "/analyzer"       → StorageAnalyzerScreen
Routes.EXPLORER     = "/explorer"       → FileBrowserScreen
Routes.SETTINGS     = "/settings"       → SettingsPage
Routes.TRASH        = "/trash"          → TrashPage
Routes.OPTIMISE_IMAGES = "/optimise-images" → ImageOptimiserPage
```

### Impact
Users clicking "Storage Analyzer" in the drawer see the home screen. They might think the feature doesn't exist.

### Fix Complexity: TRIVIAL
Single string change.

---

## 2. Trash Restore Bug

### Architecture Analysis

TrashManager.moveToTrash():
1. Gets trash dir: `context.getExternalFilesDir(null)/Trash/`
2. Creates unique name: `<timestamp>_<originalName>`
3. `source.renameTo(trashFile)` — moves file
4. Stores in `lastTrashedEntries: Map<String, String>` (originalPath → trashPath)
5. This map is CLEARED on every new moveToTrash call

TrashPage.kt restore:
```kotlin
val originalName = file.name.substringAfter("_", file.name)
val restored = TrashManager.undoTrash(mapOf(file.absolutePath to file.absolutePath))
```

### Problem Breakdown
1. `file.absolutePath` = the trash file's path (e.g., `/data/.../Trash/1718800000_photo.jpg`)
2. Map key AND value are the SAME (trash path)
3. `undoTrash()` does: `val originalFile = File(originalPath)` → this is the trash file itself
4. Then: `trashFile.renameTo(originalFile)` → renames file to itself → returns false

### Why It Was Written This Way
The developer likely intended to reconstruct the original path from the filename but made a mistake. The `lastTrashedEntries` map has the right data but it's ephemeral (cleared on next delete) and not accessible from TrashPage.

### Fix Options Evaluated

**Option A: SharedPreferences sidecar**
- Store `{ "originalPath": "..." }` in a JSON file per trashed file
- Pros: Survives app restart, clean
- Cons: Extra I/O per delete

**Option B: Metadata file alongside trash**
- Write `<trashname>.meta` containing original path
- Pros: Simple, self-contained
- Cons: Need to keep in sync

**Option C: Embedded in filename**
- Encode original path in filename: `<timestamp>_<base64(originalPath)>_<name>`
- Pros: No extra files
- Cons: Long filenames, path encoding issues

**Decision: Option B** — most robust and clean.

### Files Using TrashManager
```
FlatDuplicatesFileManagerPage.kt → moveToTrash() + undoTrash()
WhatsAppCleanerPage.kt → moveToTrash()
FileBrowserScreen.kt → deleteSelected → TrashManager (need to verify)
FlatFileManagerDeleteComposable.kt → used by multiple pages
```

### Risk: MEDIUM
The TrashManager API change (writing metadata) must be backward-compatible with existing trash files that have no metadata.

---

## 3. File.size() Naming

### All Callers (from grep)
Need to run: `rg '\.size\(\)' --type kotlin` to find exact callers.

Known usage in `LocalFile.size` field — this is a different `size` (Room column, in KB by convention). The extension function `File.size()` on `java.io.File` is the problem.

### Decision: Rename to `File.sizeInKb()`
- Clear naming
- Easy to grep and update
- Document with KDoc

---

## 4. Analyzer Permission Gate

### Current Permission Flow
- `AndroidManifest.xml` declares `MANAGE_EXTERNAL_STORAGE`
- `OnboardingPage.kt` requests this permission on first launch
- But if user denies, there's no re-prompt in StorageAnalyzerScreen

### Permission Check Code
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    if (!Environment.isExternalStorageManager()) {
        // Show permission request
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}
```

### Where to Add
Wrap the VM creation in StorageAnalyzerScreen:
- If no permission → show permission explanation + "Grant Permission" button
- If permission granted → create VM, show scanning/results

### Risk: LOW
Standard Android pattern.

---

## 5. Drawer APKs Route

### Current
```kotlin
DrawerMenuItem("APKs", Icons.Outlined.Memory, path = "$primaryPath/Download"),
```

### Available Routes
`MediaStoreCategoryScreen` accepts `CategoryType` enum: IMAGES, VIDEOS, AUDIO, DOCUMENTS, APK

### Fix
```kotlin
DrawerMenuItem("APKs", Icons.Outlined.Memory, route = "${Routes.MEDIA_STORE_CATEGORY}/apk"),
```

### Risk: NONE
Route and screen already exist.

---

## Research Summary

| Bug | Severity | Fix Complexity | Risk | Files Affected |
|-----|----------|---------------|------|---------------|
| Drawer ANALYZER route | High | Trivial | None | 1 |
| Trash restore broken | High | Medium | Medium | 2 |
| File.size() naming | Low | Low | Low | 2-5 |
| Analyzer no permission | Medium | Low | Low | 2 |
| Drawer APKs route | Medium | Trivial | None | 1 |