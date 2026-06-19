# Research: Phase 2 — FileBrowser Toolbar Enhancement

## Research Date: 2026-06-19
## Status: COMPLETE

---

## 1. FileBrowserScreen TopAppBar Architecture

### Three Modes
FileBrowser has THREE different TopAppBar configurations based on state:

1. **Multi-select mode** (lines 376-425): Shows count, Select All, Interval, Close
2. **Search mode** (lines 426-492): Shows search TextField, back arrow, clear button
3. **Normal mode** (lines 493-557): Shows breadcrumb, hamburger menu, search icon

### Normal Mode Actions (Current — lines 542-549)
```kotlin
actions = {
    IconButton(onClick = { isSearchActive = true }) {
        Icon(Icons.Outlined.Search, ...)
    }
},
```

Only ONE action button. We need to add:
- Organiser icon button (AutoFixHigh)
- 3-dot overflow (MoreVert) with DropdownMenu

### Space Considerations
TopAppBar in Material 3 can fit 2-3 icon buttons comfortably. With:
1. Hamburger (navigationIcon)
2. Search (existing)
3. Organiser (new)
4. MoreVert (new)

That's 2 navigation + 3 actions = 5 total. Should fit on most screens. On narrow devices, the breadcrumb title might get truncated.

### Existing Import Check
Already imported:
- `Icons.Filled.MoreVert` (line 41) ✅
- `Icons.Outlined.Search` (line 54) ✅

Need to add:
- `Icons.Outlined.AutoFixHigh`
- `Icons.Outlined.Refresh`
- `Icons.Outlined.CreateNewFolder`
- `Icons.Outlined.Analytics`
- `DropdownMenu`
- `DropdownMenuItem`
- `Box` (already imported? need to verify)

### State Variable
Need to add: `var showTopMenu by remember { mutableStateOf(false) }`
Already exists nearby: `var showCreateFolderDialog`, `var showDeleteDialog`, etc.

---

## 2. FolderOrganiserScreen Research

### What Exists
- No file exists
- No route exists
- No VM exists
- The concept was mentioned in the session summary but never committed

### Design Requirements

Based on "Organiser" in ES File Explorer:
- Shows files in a folder grouped by type
- Allows moving files into categorized subfolders
- Common use case: "I downloaded 50 mixed files, organize them into Images/Videos/Documents subfolders"

### Implementation Plan

**Screen Structure**:
```
Scaffold
  TopAppBar: "Organise: <FolderName>" + Back
  Content:
    if (no files) → Empty state
    else →
      LazyColumn of groups:
        Group: Images (5 files, 23 MB) [→ Move to Images/]
        Group: Videos (3 files, 150 MB) [→ Move to Videos/]
        Group: Documents (2 files, 1 MB) [→ Move to Documents/]
        Group: Other (4 files, 500 KB) [→ Move to Other/]
      
      Bottom: [Sort into Subfolders] button
```

**VM Logic**:
```kotlin
class FolderOrganiserVM(application: Application) : AndroidViewModel(application) {
    private val _groups = MutableStateFlow<List<FileGroup>>(emptyList())
    val groups: StateFlow<List<FileGroup>> = _groups.asStateFlow()
    
    data class FileGroup(
        val category: StorageCategory,  // reuse from StorageAnalyzerVM
        val files: List<File>,
        val totalSize: Long,
    )
    
    fun loadFolder(path: String) { /* scan and group */ }
    fun sortIntoSubfolders() { /* create dirs, move files */ }
}
```

**Categorization**: Reuse the extension-based categorization from current `StorageAnalyzerVM.categorize()`. Extract it to a shared utility.

### Route Design
```kotlin
const val FOLDER_ORGANISER = "/folder-organiser"

composable(
    route = "$FOLDER_ORGANISER?path={path}",
    arguments = listOf(navArgument("path") {
        type = NavType.StringType; nullable = true; defaultValue = null
    })
) { backStackEntry ->
    FolderOrganiserScreen(initialPath = backStackEntry.arguments?.getString("path"))
}
```

### Navigation from FileBrowser
Two paths lead to this screen:
1. Direct Organiser icon button → `${Routes.FOLDER_ORGANISER}?path=${encode(currentPath)}`
2. 3-dot menu "Organise" → same route

---

## 3. Dependencies & Risks

### Import: Icons.Outlined.AutoFixHigh
Verify this icon exists in Material Icons:
- `Icons.Outlined.AutoFixHigh` — YES, exists in `material-icons-extended`

### Import: Icons.Outlined.Refresh
- `Icons.Outlined.Refresh` — YES, standard icon

### Import: Icons.Outlined.CreateNewFolder
Already imported in FileBrowserScreen.kt (line 44)

### DropdownMenu in TopAppBar
Known pattern — Material 3 supports this. Use `Box` wrapper around MoreVert IconButton for anchor positioning.

### Risk: LOW
- Pure UI additions
- No existing code is modified (only the actions block is replaced)
- New screen is isolated

### Edge Cases
- Organiser on root `/` — should show all top-level folders with their contents? Or block?
- Organiser on empty folder — show empty state
- Sort into subfolders when subfolders already exist — merge or skip? (Skip existing, move only new files)
- Sort into subfolders when files have same name — add suffix? (Use File.exists() check, append number if conflict)