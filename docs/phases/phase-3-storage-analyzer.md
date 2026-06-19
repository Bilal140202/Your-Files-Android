# Phase 3: Storage Analyzer Rewrite (ES File Explorer Flow)

## Goal
Completely replace the current pie-chart-only StorageAnalyzerScreen/VM with a multi-screen ES File Explorer-style flow:
1. Analyzer Home (select what to scan)
2. Permission Gate
3. Results Dashboard (clickable category cards)
4. Category File List (actual files for a category)
5. Folder Size View (WinDirStat-style)
6. Duplicate Files (with Smart Select)
7. Large Files (configurable threshold)
8. Junk Cleaner (obsolete APKs, ad junk, thumbnails)

---

## 3.1 RESEARCH

### 3.1.1 Current Implementation (To Be Replaced)

**StorageAnalyzerVM.kt** (216 lines):
- `AndroidViewModel` with `StateFlow<AnalyzerUiState>`
- Scans from `Environment.getExternalStorageDirectory()` recursively
- Categories: IMAGES, VIDEOS, AUDIO, DOCUMENTS, APK, ARCHIVES, OTHER
- Uses a min-heap for top-10 largest files
- Progress: estimates total files via heuristic `rootChildren.size * 20`
- 1-hour cache
- No permission check
- No MD5/duplicate detection (relies on extension-based categorization)

**StorageAnalyzerScreen.kt** (507 lines):
- Donut chart (Canvas-based arcs)
- Category breakdown list (click → opens folder path)
- Top 10 largest files list
- Storage summary (Total/Used/Scanned)
- Uses `CATEGORY_FOLDERS` mapping (e.g., VIDEOS → "Movies") — this is WRONG because it only opens one folder, not all videos on device

### 3.1.2 ES File Explorer Flow Analysis

ES File Explorer's storage analyzer had this exact flow:

**Step 1 — ANALYZER HOME**: Checkboxes to select scan scope:
- Large Files (>10MB)
- Redundant (Duplicates)
- Recently Created (last 7 days)
- All Files
- App Folders (scan app-specific directories)

**Step 2 — PERMISSION**: If `MANAGE_EXTERNAL_STORAGE` not granted, show explanation + "Go to Settings" button. For Android/data and Android/obb, use SAF (Storage Access Framework) picker since even MANAGE_EXTERNAL_STORAGE can't access these on Android 11+.

**Step 3 — RESULTS DASHBOARD**: After scanning, show:
- Storage overview (total/used/free bar)
- Category cards in a grid:
  - Pictures (icon + size + file count) — clickable
  - Videos (icon + size + file count) — clickable
  - Audio (icon + size + file count) — clickable
  - Documents (icon + size + file count) — clickable
  - APKs (icon + size + file count) — clickable
  - Others (icon + size + file count) — clickable
- Bottom section: "Tools" — Duplicate Files, Large Files, Junk Cleaner

**Step 4 — CATEGORY FILE LIST**: Click "Videos" card → shows ALL video files on device sorted by size descending. Each row: thumbnail/icon, filename, size. Long-press → select → delete.

**Step 5 — FOLDER SIZE VIEW**: Shows top-level folders with their total sizes. Can drill into subfolders. Like a simplified WinDirStat. Each row: folder icon, name, size, progress bar showing % of parent.

**Step 6 — DUPLICATE FILES**: MD5-based grouping. Each group shows duplicate files. Smart Select options: "Keep Newest", "Keep Oldest", "Keep Shortest Path". Select duplicates → delete.

**Step 7 — LARGE FILES**: Files >10MB (configurable threshold). Checkbox per file. Show: name, path, size. "Delete Selected" button.

**Step 8 — JUNK CLEANER**: Categories of junk:
- Obsolete APKs (APK files in Download/ that match an installed app version)
- Ad Junk (ad cache folders from apps)
- Download folder cleanup (old files in Download/)
- Thumbnails (`.thumbnails` directory cache)
Each category expandable to preview files. "CLEAN NOW" button.

### 3.1.3 Architecture Decision

**Navigation approach**: Since all these are sub-screens of the Analyzer, we have two options:
- **Option A**: Single composable with internal state machine (steps 1-8 managed by VM state)
- **Option B**: Separate routes for each sub-screen

**Decision**: **Option A with selective routes**. The main flow (Home → Permission → Dashboard) stays within one composable managed by a state machine. But Category File List, Folder Size View, Duplicate Files, Large Files, and Junk Cleaner get their own routes since they are also reachable from other parts of the app.

**New Routes Needed**:
```
/analyzer                           → AnalyzerHomeScreen (replaces StorageAnalyzerScreen)
/analyzer/category/{categoryType}   → AnalyzerCategoryScreen (replaces MediaStoreCategory for analyzer context)
/analyzer/folders                   → AnalyzerFolderSizeScreen
/analyzer/duplicates                → FlatFileManager (existing, but maybe enhance)
/analyzer/large-files               → FlatLargeFilesManager (existing)
/analyzer/junk                      → JunkCleanerScreen (new)
```

Wait — actually, the existing `MediaStoreCategoryScreen` already does what "Category File List" needs. We should **reuse** it, not create a duplicate. The analyzer dashboard just needs to navigate to it.

Similarly, `FlatDuplicatesFileManagerPage` (existing) already handles duplicates. And `FlatLargeFilesManager` handles large files.

So the NEW screens needed are:
1. **AnalyzerHomeScreen** (replaces StorageAnalyzerScreen — checkbox selection + permission + dashboard)
2. **AnalyzerFolderSizeScreen** (new — folder size tree view)
3. **JunkCleanerScreen** (new — junk categories with expand/clean)

### 3.1.4 VM Architecture

Replace `StorageAnalyzerVM` with `AnalyzerHomeVM`:
```kotlin
sealed interface AnalyzerStep {
    object SelectScope : AnalyzerStep        // Checkbox screen
    object PermissionRequired : AnalyzerStep  // Permission gate
    object Scanning : AnalyzerStep            // Progress
    data class Dashboard(val results: AnalyzerResults) : AnalyzerStep
}

data class AnalyzerResults(
    val totalUsedBytes: Long,
    val totalFreeBytes: Long,
    val categories: List<CategoryStats>,
    val categoryFiles: Map<StorageCategory, List<FileItem>>,  // pre-scanned files per category
)
```

The VM manages the step transitions. Category files are scanned once during the "Scanning" step and cached.

### 3.1.5 Folder Size Calculation

For the Folder Size View, we need recursive directory size calculation:
```kotlin
data class FolderSizeEntry(
    val path: String,
    val name: String,
    val totalSize: Long,
    val childCount: Int,
    val isDirectory: Boolean,
)
```

Algorithm:
1. List top-level directories in a given path
2. For each, recursively calculate total size (sum of all file lengths)
3. Sort by size descending
4. Cache results (invalidate on manual refresh)
5. On drill-in: repeat for the selected subdirectory

**Performance concern**: On a real device with 50,000+ files, naive recursion is slow. Use `walkTopDown()` with early termination for large directories. Show progress.

### 3.1.6 Junk Detection Logic

**Obsolete APKs**: Scan Download/ for .apk files. For each, parse `PackageManager.getPackageArchiveInfo()` to get version. Compare with `PackageManager.getInstalledPackages()` — if installed version >= APK version, the APK is obsolete.

**Ad Junk**: Known ad cache paths:
- `/Android/data/*/cache/ads/`
- `/Android/data/*/files/ads/`
- Specific app patterns (varies by device)

**Download cleanup**: Files in Download/ older than 30 days that are not .apk.

**Thumbnails**: `/storage/emulated/0/.thumbnails/` directory, `.thumbdata` files in DCIM folders.

---

## 3.2 PLAN

### Acceptance Criteria
- [ ] 3.1 Analyzer Home screen shows checkboxes (Large Files, Redundant, Recently Created, All Files, App Folders)
- [ ] 3.2 Tapping "Scan" with no permission shows permission request screen
- [ ] 3.3 Permission request navigates to system settings for MANAGE_EXTERNAL_STORAGE
- [ ] 3.4 After permission granted + scan, Dashboard shows category cards with correct sizes
- [ ] 3.5 Dashboard cards are clickable and navigate to the right file lists
- [ ] 3.6 Folder Size screen shows directories sorted by size
- [ ] 3.7 Folder Size allows drill-in to subdirectories
- [ ] 3.8 Junk Cleaner shows 4 categories with expandable preview
- [ ] 3.9 Junk Cleaner "CLEAN NOW" moves files to trash
- [ ] 3.10 Old StorageAnalyzerScreen and VM are fully removed
- [ ] 3.11 All routes work (analyzer, analyzer/folders, analyzer/junk)
- [ ] 3.12 Existing FlatFileManager (duplicates) and FlatLargeFilesManager still work

### Files to Create
| File | Description |
|------|-------------|
| `presentation/ui/pages/AnalyzerHomeScreen.kt` | Main analyzer composable (state machine) |
| `presentation/vm/AnalyzerHomeVM.kt` | New VM replacing StorageAnalyzerVM |
| `presentation/ui/pages/AnalyzerFolderSizeScreen.kt` | Folder size tree view |
| `presentation/vm/AnalyzerFolderSizeVM.kt` | Folder size calculation VM |
| `presentation/ui/pages/JunkCleanerScreen.kt` | Junk cleaner composable |
| `presentation/vm/JunkCleanerVM.kt` | Junk detection VM |

### Files to Modify
| File | Change |
|------|--------|
| `Router.kt` | Replace ANALYZER route, add FOLDER_SIZE and JUNK routes |
| `DrawerContent.kt` | Ensure ANALYZER route is correct (from Phase 1) |
| `ESHomeScreen.kt` | Update "Analyzer" tool to point to new analyzer |
| `StorageAnalyzerScreen.kt` | DELETE |
| `StorageAnalyzerVM.kt` | DELETE |

### Files to Keep Unchanged
| File | Reason |
|------|--------|
| `FlatDuplicatesFileManagerPage.kt` | Already works, linked from analyzer |
| `FlatLargeFilesManager.kt` | Already works, linked from analyzer |
| `MediaStoreCategoryScreen.kt` | Already works, linked from analyzer dashboard |

---

## 3.3 IMPLEMENTATION NOTES

### Step 1: Create AnalyzerHomeVM

Key state:
```kotlin
sealed interface AnalyzerStep { ... }
data class AnalyzerUiState(
    val step: AnalyzerStep = AnalyzerStep.SelectScope,
    val selectedScopes: Set<ScanScope> = emptySet(),
    val scanProgress: Int = 0,
    val scanMessage: String = "",
    val results: AnalyzerResults? = null,
)
enum class ScanScope { LARGE_FILES, REDUNDANT, RECENT, ALL_FILES, APP_FOLDERS }
```

### Step 2: Create AnalyzerHomeScreen

State machine rendering:
```kotlin
when (state.step) {
    is AnalyzerStep.SelectScope -> ScopeSelectionContent(...)
    is AnalyzerStep.PermissionRequired -> PermissionRequiredContent(...)
    is AnalyzerStep.Scanning -> ScanningContent(...)
    is AnalyzerStep.Dashboard -> DashboardContent(...)
}
```

### Step 3: Folder Size VM

Use `walkTopDown()` with a depth limit of 1 for the top level, then full recursion for size calculation. Cache results in a `MutableStateFlow`. Show progress during calculation.

### Step 4: Junk Cleaner VM

Scan each junk category in parallel using `async`/`awaitAll`. For APKs, use `PackageManager` to compare versions. For ad junk, check known paths. For thumbnails, check `.thumbnails/` and `.thumbdata` files.

---

## 3.4 TEST CHECKLIST

| # | Test Case | Steps | Expected Result | Pass/Fail |
|---|-----------|-------|-----------------|-----------|
| 3.1.1 | Analyzer Home shows checkboxes | Navigate to Analyzer | 5 checkboxes visible: Large Files, Redundant, Recently Created, All Files, App Folders | |
| 3.1.2 | Scan button disabled when nothing selected | Deselect all checkboxes | "Scan" button disabled | |
| 3.1.3 | Select "All Files" selects all | Tap "All Files" checkbox | All 5 checkboxes selected | |
| 3.2.1 | Permission gate shows | Revoke permission → tap Scan | Permission explanation + "Go to Settings" button | |
| 3.2.2 | Permission granted → scanning | Grant permission → tap Scan | Progress bar appears, scans storage | |
| 3.3.1 | Dashboard shows after scan | Wait for scan to complete | 6 category cards visible with sizes | |
| 3.3.2 | Dashboard categories have correct data | Check Videos card | Shows actual total video size on device | |
| 3.4.1 | Tap Videos card → file list | Tap Videos card on dashboard | MediaStoreCategoryScreen opens with videos | |
| 3.4.2 | Tap APKs card → file list | Tap APKs card | APK category list opens | |
| 3.5.1 | Folder Size view opens | Tap "Folder Sizes" on dashboard | Top-level directories with sizes | |
| 3.5.2 | Drill into folder | Tap a folder in Folder Size view | Shows subdirectories with sizes | |
| 3.5.3 | Back from drill-in | Press back | Returns to parent folder list | |
| 3.6.1 | Junk Cleaner opens | Tap "Junk Cleaner" on dashboard | 4 junk categories visible | |
| 3.6.2 | Expand junk category | Tap "Obsolete APKs" | Shows list of obsolete APKs | |
| 3.6.3 | Clean Now works | Select junk items → tap "Clean Now" | Items moved to trash, counts update | |
| 3.7.1 | Duplicate Files links correctly | Tap "Duplicate Files" on dashboard | Existing FlatFileManager opens | |
| 3.7.2 | Large Files links correctly | Tap "Large Files" on dashboard | Existing FlatLargeFilesManager opens | |
| 3.8.1 | Old files deleted | Check project | StorageAnalyzerScreen.kt and StorageAnalyzerVM.kt removed | |
| 3.9.1 | Build compiles | `./gradlew assembleDebug` | Build succeeds | |
| 3.9.2 | No broken imports | Grep for StorageAnalyzerVM/Screen | Zero references (except in delete commit) | |

---

## 3.5 COMMIT TEMPLATE
```
phase-3: rewrite Storage Analyzer with ES File Explorer flow

- Replace StorageAnalyzerScreen/VM with AnalyzerHomeScreen/AnalyzerHomeVM
- Add scan scope selection (Large Files, Redundant, Recent, All, App Folders)
- Add MANAGE_EXTERNAL_STORAGE permission gate
- Add results dashboard with clickable category cards
- Create AnalyzerFolderSizeScreen with drill-in folder size view
- Create JunkCleanerScreen (obsolete APKs, ad junk, downloads, thumbnails)
- Add /analyzer/folders and /analyzer/junk routes
- Delete old StorageAnalyzerScreen.kt and StorageAnalyzerVM.kt
- Reuse existing MediaStoreCategoryScreen, FlatFileManager, FlatLargeFilesManager
```