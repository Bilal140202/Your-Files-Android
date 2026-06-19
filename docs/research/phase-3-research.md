# Research: Phase 3 — Storage Analyzer Rewrite

## Research Date: 2026-06-19
## Status: COMPLETE

---

## 1. Current Implementation Analysis

### StorageAnalyzerVM.kt (216 lines) — TO DELETE

**Data Flow**:
```
init → analyzeStorage() → walkTopDown() → categorize by extension → update StateFlow
```

**State**:
```kotlin
data class AnalyzerUiState(
    val isScanning: Boolean = true,
    val scanProgress: Int = 0,
    val scannedFileCount: Long = 0L,
    val totalUsedBytes: Long = 0L,
    val categories: List<CategoryStats> = emptyList(),
    val topLargestFiles: List<LargestFileEntry> = emptyList(),
    val error: String? = null,
)
```

**What's Good (to keep/reuse)**:
- `StorageCategory` enum — well-designed, reuse it
- `CategoryStats` data class — reuse it
- `categorize()` function and extension sets — extract to shared utility
- 1-hour cache pattern — good idea, keep it
- Min-heap for top files — good algorithm

**What's Bad (to replace)**:
- Starts scanning in `init{}` with no permission check
- Only shows pie chart, no drill-down
- Category click opens a folder path (not filtered file list)
- No duplicate detection in analyzer
- No junk cleaner
- No folder size view

### StorageAnalyzerScreen.kt (507 lines) — TO DELETE

**What to Keep**:
- Donut chart drawing code (Canvas) — it looks nice, can reuse in dashboard
- Category color mapping (`CATEGORY_COLORS`)
- Category icon mapping (`CATEGORY_ICONS`)

**What to Replace**:
- Everything else

---

## 2. Reusable Existing Screens

### MediaStoreCategoryScreen.kt
Already does exactly what "Category File List" needs:
- Shows flat list of files for a category
- Sorted by date DESC
- Has thumbnails for images/videos
- Click → file detail viewer
- Takes `CategoryType` enum as parameter

**Route**: `/media-category/{categoryType}`
**Categories**: IMAGES, VIDEOS, AUDIO, DOCUMENTS, APK

**Gap**: No "Others" or "Archives" category in CategoryType. Need to add if we want to show them from the analyzer.

### FlatFileManager (Duplicates)
Already handles duplicate detection and deletion. Can be linked from analyzer dashboard.

### FlatLargeFilesManager
Already handles large files. Can be linked from analyzer dashboard.

---

## 3. New Screen Designs

### 3.1 AnalyzerHomeScreen

**State Machine**:
```
SelectScope → (tap Scan) → CheckPermission → [Need Permission → PermissionGate → (grant) → Scanning]
                                                  [Have Permission → Scanning]
Scanning → (complete) → Dashboard
```

**SelectScope UI**:
```
Scaffold (primary TopAppBar, "Storage Analyzer")

Column {
    // Explanation text
    Text("Select what to scan:")
    
    // Scope checkboxes
    CheckboxRow("Large Files (>10 MB)", ScanScope.LARGE_FILES)
    CheckboxRow("Redundant Files", ScanScope.REDUNDANT)
    CheckboxRow("Recently Created", ScanScope.RECENT)
    CheckboxRow("All Files", ScanScope.ALL_FILES)
    CheckboxRow("App Folders", ScanScope.APP_FOLDERS)
    
    Spacer(16dp)
    
    // Scan button
    Button(onClick = startScan, enabled = selectedScopes.isNotEmpty()) {
        Text("Scan Storage")
    }
}
```

**PermissionGate UI**:
```
Column (centered) {
    Icon(Icons.Outlined.Shield, 72dp)
    Text("Storage Permission Required")
    Text("To scan your device storage,...")
    Button("Grant Permission") { openSettings() }
}
```

**Dashboard UI**:
```
LazyColumn {
    // Storage overview card
    item { StorageOverviewBar(total, used, free) }
    
    // Category cards grid (2 columns)
    item {
        Row { CategoryCard(Images) | CategoryCard(Videos) }
        Row { CategoryCard(Audio) | CategoryCard(Documents) }
        Row { CategoryCard(APKs) | CategoryCard(Others) }
    }
    
    // Tools section
    item { Text("Tools") }
    item { ToolRow("Folder Sizes", onClick) }
    item { ToolRow("Duplicate Files", onClick) }
    item { ToolRow("Large Files", onClick) }
    item { ToolRow("Junk Cleaner", onClick) }
}
```

### 3.2 AnalyzerFolderSizeScreen

**Data Structure**:
```kotlin
data class FolderNode(
    val path: String,
    val name: String,
    val size: Long,
    val fileCount: Int,
    val children: List<FolderNode> = emptyList(),  // populated on drill-in
)
```

**UI**:
```
Scaffold (TopAppBar: "Folder Sizes" + Back)

LazyColumn {
    items(folders) { folder ->
        FolderSizeRow(
            name = folder.name,
            size = formatSize(folder.size),
            percent = folder.size / parentSize,
            onClick = { drillInto(folder.path) }
        )
    }
}
```

**Performance**: For the root level, we scan ~20-30 top-level directories. Each scan is `walkTopDown()` which is fast for shallow dirs. For deep dirs, show a loading indicator.

### 3.3 JunkCleanerScreen

**Junk Categories**:
```kotlin
enum class JunkType(val label: String, val icon: ImageVector) {
    OBSOLETE_APKS("Obsolete APKs", Icons.Outlined.Android),
    AD_JUNK("Ad Junk", Icons.Outlined.Block),
    OLD_DOWNLOADS("Old Downloads", Icons.Outlined.Download),
    THUMBNAILS("Thumbnails", Icons.Outlined.Image),
}
```

**Obsolete APK Detection Algorithm**:
```kotlin
// 1. Scan Download/ for .apk files
// 2. For each APK:
//    val info = packageManager.getPackageArchiveInfo(apkPath, 0)
//    val installedInfo = packageManager.getPackageInfo(info.packageName, 0)
//    if (installedInfo != null && installedInfo.versionCode >= info.versionCode) {
//        // APK is obsolete (same or older version is installed)
//    }
```

**Ad Junk Paths** (known patterns):
```
/Android/data/com.facebook.katana/cache/ads/
/Android/data/com.instagram.android/cache/ads/
/Android/data/*/cache/.ads/
/storage/emulated/0/Android/data/com.google.android.gms/cache/
```

**Thumbnail Detection**:
```kotlin
// /storage/emulated/0/.thumbnails/
// /storage/emulated/0/DCIM/.thumbnails/
// Any .thumbdata file
```

---

## 4. VM Architecture

### AnalyzerHomeVM
```kotlin
class AnalyzerHomeVM(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(AnalyzerUiState())
    val state: StateFlow<AnalyzerUiState> = _state.asStateFlow()
    
    fun toggleScope(scope: ScanScope)
    fun startScan()
    fun refreshResults()
    
    private fun scanStorage(scopes: Set<ScanScope>) {
        viewModelScope.launch(Dispatchers.IO) {
            // Walk file system
            // Categorize files
            // Calculate folder sizes (for folder size view)
            // Detect duplicates (if REDUNDANT selected)
            // Detect junk (always)
            // Update state
        }
    }
}
```

### AnalyzerFolderSizeVM
```kotlin
class AnalyzerFolderSizeVM(app: Application) : AndroidViewModel(app) {
    private val _folders = MutableStateFlow<List<FolderNode>>(emptyList())
    private val _currentPath = MutableStateFlow(Environment.getExternalStorageDirectory().absolutePath)
    private val _isLoading = MutableStateFlow(false)
    
    fun loadFolders(path: String? = null)
    fun navigateToFolder(path: String)
    fun navigateUp()
}
```

### JunkCleanerVM
```kotlin
class JunkCleanerVM(app: Application) : AndroidViewModel(app) {
    private val _junkItems = MutableStateFlow<Map<JunkType, List<JunkItem>>>(emptyMap())
    private val _isScanning = MutableStateFlow(true)
    
    fun scanForJunk()
    fun cleanSelected(items: Set<String>)
    
    data class JunkItem(
        val path: String,
        val name: String,
        val size: Long,
        val type: JunkType,
    )
}
```

---

## 5. Route Changes

### Current Routes (to modify/delete)
```
"/analyzer" → StorageAnalyzerScreen  (DELETE this mapping)
```

### New Routes
```
"/analyzer" → AnalyzerHomeScreen  (REPLACE)
"/analyzer/folders" → AnalyzerFolderSizeScreen  (NEW)
"/analyzer/junk" → JunkCleanerScreen  (NEW)
```

### Routes to keep linking to
```
"/media-category/{categoryType}" → MediaStoreCategoryScreen  (EXISTING)
"/flat-duplicates-file-manager" → FlatFileManager  (EXISTING)
"/flat-large-file-manager" → FlatLargeFilesManager  (EXISTING)
```

---

## 6. Extractable Shared Code

From current StorageAnalyzerVM, extract to a utility:
```kotlin
// utils/FileCategorizer.kt
object FileCategorizer {
    fun categorize(filename: String): StorageCategory
    val IMG_EXTS: Set<String>
    val VID_EXTS: Set<String>
    // ... etc
}
```

This utility will be used by:
- AnalyzerHomeVM (new)
- FolderOrganiserVM (Phase 2)
- Any future code that needs file categorization