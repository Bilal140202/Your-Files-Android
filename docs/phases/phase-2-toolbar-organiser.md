# Phase 2: FileBrowser Toolbar Enhancement

## Goal
Add the Organiser button and 3-dot overflow menu to the FileBrowser normal-mode TopAppBar, and create the FolderOrganiserScreen.

---

## 2.1 RESEARCH

### Current FileBrowserScreen TopAppBar (Normal Mode)

**File**: `app/src/main/java/com/yourfiles/manager/presentation/ui/pages/FileBrowserScreen.kt`
**Lines**: 493-557

Current `actions` block contains ONLY a search icon:
```kotlin
actions = {
    IconButton(onClick = { isSearchActive = true }) {
        Icon(Icons.Outlined.Search, contentDescription = "Search", ...)
    }
},
```

**Missing**: Organiser icon button, 3-dot overflow menu with Refresh/Storage Analyzer/New Folder/Organise.

### What the Menu Should Do

| Menu Item | Icon | Action |
|-----------|------|--------|
| (Direct button) | `Icons.Outlined.AutoFixHigh` | Navigate to FolderOrganiserScreen with current path |
| (Direct button) | `Icons.Outlined.Search` | Activate search (existing) |
| Refresh | `Icons.Outlined.Refresh` | `viewModel.navigateTo(state.currentPath)` (re-scan current folder) |
| Storage Analyzer | `Icons.Outlined.Analytics` | Navigate to `Routes.ANALYZER` |
| New Folder | `Icons.Outlined.CreateNewFolder` | Show `showCreateFolderDialog = true` |
| Organise | `Icons.Outlined.AutoFixHigh` | Navigate to FolderOrganiserScreen with current path |

### FolderOrganiserScreen — Research

**Status**: File does NOT exist. Must be created from scratch.

**Route**: Need to add `FOLDER_ORGANISER` to Routes and Router.kt.

**What should it do?**
Based on the session context: "Organise" opens a file organiser for the current folder. This is like a file sorting/organising tool — potentially:
- Show files grouped by type
- Allow batch move/copy to sorted subfolders
- Preview of how files would be reorganized

However, the exact spec was not fully defined in the conversation. We should create a **basic scaffold** that:
1. Accepts `initialPath` parameter
2. Shows the folder name in the title
3. Lists files grouped by category (Images, Videos, Documents, Other)
4. Has a "Sort into subfolders" button that creates category subfolders and moves files

### Dependencies
- `Router.kt` — add FOLDER_ORGANISER route
- `FileBrowserScreen.kt` — add imports for DropdownMenu, AutoFixHigh, Refresh, CreateNewFolder
- New file: `FolderOrganiserScreen.kt`
- New file: `FolderOrganiserVM.kt` (or inline logic if simple enough)

### Risk Assessment
- **Low risk** for the 3-dot menu — it's pure UI addition
- **Medium risk** for FolderOrganiserScreen — new screen with file operations (move files)
- Must test on a test folder first, never on user's actual files during development

---

## 2.2 PLAN

### Acceptance Criteria
- [ ] 2.1 Organiser icon (AutoFixHigh) visible in FileBrowser normal-mode toolbar
- [ ] 2.2 Tapping Organiser opens FolderOrganiserScreen for current folder
- [ ] 2.3 3-dot menu (MoreVert) visible next to Organiser and Search
- [ ] 2.4 Menu items: Refresh, Storage Analyzer, New Folder, Organise — all functional
- [ ] 2.5 FolderOrganiserScreen shows files grouped by type
- [ ] 2.6 FolderOrganiserScreen has a "Sort into subfolders" action
- [ ] 2.7 FOLDER_ORGANISER route registered in Router.kt
- [ ] 2.8 Back navigation from FolderOrganiserScreen returns to FileBrowser

### Files to Create/Modify
| File | Change Type | Description |
|------|-------------|-------------|
| `FileBrowserScreen.kt` | Modify | Add DropdownMenu, Organiser button, new imports |
| `Router.kt` | Modify | Add FOLDER_ORGANISER route with path parameter |
| `presentation/ui/pages/FolderOrganiserScreen.kt` | **Create** | New organiser screen composable |
| `presentation/vm/FolderOrganiserVM.kt` | **Create** | ViewModel for organiser logic |

---

## 2.3 IMPLEMENTATION NOTES

### FileBrowserScreen — Add to imports
```kotlin
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
```

### FileBrowserScreen — Add state
```kotlin
var showTopMenu by remember { mutableStateOf(false) }
```

### FileBrowserScreen — Replace normal mode actions block (lines 542-549)
```kotlin
actions = {
    // Organiser button
    IconButton(onClick = {
        val currentDir = state.currentPath
        App.instance.navController().navigate(
            "${Routes.FOLDER_ORGANISER}?path=${android.net.Uri.encode(currentDir)}"
        )
    }) {
        Icon(Icons.Outlined.AutoFixHigh, contentDescription = "Organise",
            tint = MaterialTheme.colorScheme.onPrimary)
    }
    // Search button (existing)
    IconButton(onClick = { isSearchActive = true }) {
        Icon(Icons.Outlined.Search, contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onPrimary)
    }
    // 3-dot overflow menu
    Box {
        IconButton(onClick = { showTopMenu = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onPrimary)
        }
        DropdownMenu(
            expanded = showTopMenu,
            onDismissRequest = { showTopMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Refresh") },
                onClick = {
                    showTopMenu = false
                    viewModel.navigateTo(state.currentPath)
                },
                leadingIcon = { Icon(Icons.Outlined.Refresh, null) }
            )
            DropdownMenuItem(
                text = { Text("Storage Analyzer") },
                onClick = {
                    showTopMenu = false
                    App.instance.navController().navigate(Routes.ANALYZER)
                },
                leadingIcon = { Icon(Icons.Outlined.Analytics, null) }
            )
            DropdownMenuItem(
                text = { Text("New Folder") },
                onClick = {
                    showTopMenu = false
                    showCreateFolderDialog = true
                },
                leadingIcon = { Icon(Icons.Outlined.CreateNewFolder, null) }
            )
            DropdownMenuItem(
                text = { Text("Organise") },
                onClick = {
                    showTopMenu = false
                    val currentDir = state.currentPath
                    App.instance.navController().navigate(
                        "${Routes.FOLDER_ORGANISER}?path=${android.net.Uri.encode(currentDir)}"
                    )
                },
                leadingIcon = { Icon(Icons.Outlined.AutoFixHigh, null) }
            )
        }
    }
},
```

### Router.kt — Add route
```kotlin
// In Routes companion object:
const val FOLDER_ORGANISER = "/folder-organiser"

// In buildAppGraph():
composable(
    route = "${Routes.FOLDER_ORGANISER}?path={path}",
    arguments = listOf(
        navArgument("path") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    )
) { backStackEntry ->
    val path = backStackEntry.arguments?.getString("path")
    FolderOrganiserScreen(initialPath = path)
}
```

### FolderOrganiserScreen — Skeleton
- Accept `initialPath: String?`
- Scan directory, group files by category
- Show grouped lists (Images, Videos, Documents, Other)
- "Sort into subfolders" button creates: `Images/`, `Videos/`, `Documents/`, `Other/` and moves files

---

## 2.4 TEST CHECKLIST

| # | Test Case | Steps | Expected Result | Pass/Fail |
|---|-----------|-------|-----------------|-----------|
| 2.1.1 | Organiser button visible | Open any folder in FileBrowser | AutoFixHigh icon visible in toolbar | |
| 2.1.2 | Organiser button navigates | Tap AutoFixHigh icon | FolderOrganiserScreen opens with folder name in title | |
| 2.2.1 | 3-dot menu opens | Tap MoreVert icon | Dropdown with 4 items appears | |
| 2.2.2 | Refresh works | Tap Refresh in menu | Folder re-scans, list refreshes | |
| 2.2.3 | Storage Analyzer works | Tap Storage Analyzer in menu | Analyzer screen opens | |
| 2.2.4 | New Folder works | Tap New Folder in menu | Create folder dialog appears | |
| 2.2.5 | Organise menu item works | Tap Organise in menu | FolderOrganiserScreen opens | |
| 2.2.6 | Menu dismisses on tap outside | Open menu → tap outside | Menu closes | |
| 2.3.1 | FolderOrganiser shows grouped files | Open organiser on a folder with mixed files | Files grouped by type (Images, Videos, etc.) | |
| 2.3.2 | Back navigation works | Press back from FolderOrganiser | Returns to FileBrowser | |
| 2.3.3 | Sort into subfolders | Tap "Sort into subfolders" | Subfolders created, files moved | |
| 2.4.1 | Search still works | Tap search icon | Search bar appears (unchanged) | |
| 2.4.2 | Selection mode unaffected | Long-press a file | Selection mode toolbar shows (no menu visible) | |
| 2.5.1 | Build compiles | `./gradlew assembleDebug` | Build succeeds | |

---

## 2.5 COMMIT TEMPLATE
```
phase-2: add organiser button, 3-dot menu, and FolderOrganiserScreen

- Add AutoFixHigh Organiser button to FileBrowser normal-mode TopAppBar
- Add 3-dot overflow menu (Refresh, Storage Analyzer, New Folder, Organise)
- Create FolderOrganiserScreen with file grouping by type
- Create FolderOrganiserVM for organiser logic
- Add FOLDER_ORGANISER route to Router.kt with path parameter
```