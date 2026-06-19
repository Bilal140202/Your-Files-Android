# Research: Phase 4 — Missing Features & UX Polish

## Research Date: 2026-06-19
## Status: COMPLETE

---

## 1. Select All — Flat File Managers

### Current State Analysis

| Page | File | Has Select All? | VM Base Class |
|------|------|----------------|--------------|
| Duplicates | FlatDuplicatesFileManagerPage.kt | YES ✅ | FlatDuplicatesFileManagerVM |
| Large Files | FlatLargeFilesManager.kt | NO ❌ | FlatLargeFileManagerVM |
| Videos | FlatVideosFileManagerPage.kt | NO ❌ | FlatVideosFileManagerVM |
| Images | FlatImagesFileManagerPage.kt | NO ❌ | FlatImagesFileManagerVM |
| Screenshots | FlatScreenshotsFileManagerPage.kt | NO ❌ | FlatScreenshotsFileManagerVM |
| WhatsApp | WhatsAppCleanerPage.kt | NO ❌ | WhatsAppCleanerVM |

### VM Inheritance Chain
```
SelectableDeletableVM (base)
  ├── FlatLargeFileManagerVM
  ├── FlatVideosFileManagerVM
  ├── FlatImagesFileManagerVM
  ├── FlatScreenshotsFileManagerVM
  └── WhatsAppCleanerVM

FlatDuplicatesFileManagerVM (separate, has its own selectedFileIds)
```

### SelectableDeletableVM API
```kotlin
abstract class SelectableDeletableVM : ViewModel() {
    val selectedFiles: MutableStateFlow<Set<String>>
    val selectedModeOn: MutableStateFlow<Boolean>
    val showDeleteDialog: MutableStateFlow<Boolean>
    val isDeleting: MutableStateFlow<Boolean>
    
    fun deleteFiles(filePaths: Set<String>)
    fun confirmDeleteFiles()
    fun cancelDelete()
}
```

### Implementation Pattern (from Duplicates page)
```kotlin
// In TopAppBar actions:
if (!list.isNullOrEmpty()) {
    val allSelected = items.all { it.id in selectedIds.value }
    TextButton(onClick = { if (allSelected) deselectAll() else selectAll() }) {
        Text(if (allSelected) "Deselect All" else "Select All")
    }
}
```

For SelectableDeletableVM-based pages:
```kotlin
val allSelected = files?.all { it.id in vm.selectedFiles.value } == true
TextButton(onClick = {
    if (allSelected) vm.selectedFiles.value = emptySet()
    else vm.selectedFiles.value = files?.map { it.id }?.toSet() ?: emptySet()
}) {
    Text(if (allSelected) "Deselect All" else "Select All")
}
```

### Risk: LOW
Copy-paste from duplicates page, adapt for different VM type.

---

## 2. Sort Options

### FileExplorerViewModel Research

Need to read `FileExplorerViewModel.kt` to understand current sorting.

The FileBrowser displays `state.displayItems` which is a list. The VM likely sorts directories first, then alphabetically. To add configurable sort, we need:
1. A `SortBy` enum (NAME, SIZE, DATE, TYPE)
2. A `SortOrder` enum (ASC, DESC)
3. State in VM: `val sortBy = MutableStateFlow(SortBy.NAME)`
4. Apply sort when computing `displayItems`

### MediaStoreCategoryVM Research

The VM queries MediaStore. Current query likely has `ORDER BY date_modified DESC`. To support sorting:
- Option A: Change the SQL query based on sort (most efficient)
- Option B: Fetch all, sort in-memory (simpler, fine for most categories)

### UI Pattern
```kotlin
// Sort chip/dropdown in TopAppBar or below it
var showSortMenu by remember { mutableStateOf(false) }
Box {
    IconButton(onClick = { showSortMenu = true }) {
        Icon(Icons.Outlined.Sort, "Sort")
    }
    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
        SortOption("Name (A-Z)", SortBy.NAME, ASC)
        SortOption("Name (Z-A)", SortBy.NAME, DESC)
        SortOption("Size (largest)", SortBy.SIZE, DESC)
        SortOption("Size (smallest)", SortBy.SIZE, ASC)
        SortOption("Date (newest)", SortBy.DATE, DESC)
        SortOption("Date (oldest)", SortBy.DATE, ASC)
        SortOption("Type", SortBy.TYPE, ASC)
    }
}
```

### Risk: MEDIUM
Sort changes the data order which could affect scroll position, search results, etc.

---

## 3. Grid/List Toggle

### Current FileBrowser Item Rendering
The FileBrowser uses `LazyColumn` with custom `FileItemCompose` for each row. Each row shows: icon/thumbnail, name, size/date meta.

### Grid View Design
Use `LazyVerticalGrid` with 3-4 columns. Each cell shows:
- Thumbnail (for images/videos) or icon (for folders/other files)
- Filename (truncated to 2 lines)
- File size below name

### Shared Data
Both views use the same `state.displayItems` from the VM. Only the rendering composable changes.

### Implementation
```kotlin
val viewMode = remember { mutableStateOf(ViewMode.LIST) }

// In TopAppBar actions:
IconButton(onClick = {
    viewMode.value = if (viewMode.value == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
}) {
    Icon(if (viewMode.value == ViewMode.LIST) Icons.Outlined.GridView else Icons.Outlined.ViewList, "Toggle view")
}

// In content:
if (viewMode.value == ViewMode.LIST) {
    LazyColumn { items(displayItems) { FileListItem(it) } }
} else {
    LazyVerticalGrid(columns = GridCells.Fixed(3)) { items(displayItems) { FileGridItem(it) } }
}
```

### Selection in Grid View
Long-press on a grid item enters multi-select mode. In grid mode, show a checkbox overlay on each item (similar to WhatsApp cleaner's grid).

### Risk: MEDIUM-HIGH
Two completely different Lazy layouts. Need to make sure selection, search, and FAB work in both modes.

---

## 4. Favorites

### Data Storage
SharedPreferences with key `"yourfiles_favorites"`:
```kotlin
object FavoritesManager {
    private const val PREFS_NAME = "yourfiles_favorites"
    private const val KEY_FAVORITES = "favorite_paths"
    
    fun getFavorites(context: Context): Set<String> { ... }
    fun addFavorite(context: Context, path: String) { ... }
    fun removeFavorite(context: Context, path: String) { ... }
    fun isFavorite(context: Context, path: String): Boolean { ... }
}
```

### Screen Design
Simple list of favorited paths. Each row:
- Star icon (filled if favorited)
- File/folder name
- Path
- Tap → navigate to file/folder

### Integration Points
- FileBrowser long-press bottom sheet: add "Add to Favorites" option
- FileBrowser file tap: add star icon in the row? (optional, might be cluttered)
- Drawer: add "Favorites" menu item (with star icon)

### Risk: LOW
Isolated feature, no dependencies on existing code.

---

## 5. Sort Order Verification

### FlatLargeFileManagerVM
Uses `getLargeFiles()` which calls HomeUseCases. Need to verify the query sorts by size DESC.

### FlatVideosFileManagerVM
Uses `getVideoFiles()`. Should sort by size DESC for a "large videos" page.

### FlatImagesFileManagerVM
Uses image-related queries. Sort by size DESC makes sense.

### Action
Read each VM's query in `HomeUseCases.kt` and verify/implement correct sort order.