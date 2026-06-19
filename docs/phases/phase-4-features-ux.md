# Phase 4: Missing Features & UX Polish

## Goal
Add commonly-expected file manager features that are currently missing, and polish existing ones.

---

## 4.1 RESEARCH

### 4.1.1 Select All in Flat File Manager Pages

**Problem**: `FlatLargeFilesManager.kt`, `FlatVideosFileManagerPage.kt`, `FlatImagesFileManagerPage.kt`, `FlatScreenshotsFileManagerPage.kt`, and `WhatsAppCleanerPage.kt` all have "Select" / "Cancel" buttons but NO "Select All" / "Deselect All" button.

The `FlatDuplicatesFileManagerPage.kt` DOES have select all/deselect all — it's the only one.

**Files affected**:
- `FlatLargeFilesManager.kt` (line 44: only Select/Cancel)
- `FlatVideosFileManagerPage.kt` (line 44: only Select/Cancel)
- `FlatImagesFileManagerPage.kt` (need to verify)
- `FlatScreenshotsFileManagerPage.kt` (need to verify)
- `WhatsAppCleanerPage.kt` (line 305: only Select/Cancel)

**Fix**: Add a `TextButton` in the actions that toggles between "Select All" and "Deselect All", same pattern as duplicates page.

**Pattern from duplicates page**:
```kotlin
TextButton(onClick = { vm.toggleAllGroups() }) {
    Text(if (allGroupsSelected) "Deselect All" else "Select All")
}
```

For flat file managers, need to check the VM — `SelectableDeletableVM` base class should have `selectedFiles: MutableStateFlow<Set<String>>`. The toggle logic:
```kotlin
if (allSelected) vm.selectedFiles.value = emptySet()
else vm.selectedFiles.value = files.map { it.id }.toSet()
```

### 4.1.2 Sort Options for File Lists

**Problem**: All file lists (FileBrowser, MediaStore category, flat file managers) have fixed sort orders. Users cannot sort by name, size, date, or type.

**Scope**: This is a LARGE feature. For Phase 4, we'll implement it for the most-used screens:
1. FileBrowserScreen (already has some sort capability in the VM?)
2. MediaStoreCategoryScreen

**FileBrowser research**: The `FileExplorerViewModel` likely has a `sortOrder` field. Let me check...

Actually, from the deep dive, the FileBrowser sorts files alphabetically with directories first. The VM may or may not have configurable sorting. This needs more research during implementation.

**For MediaStoreCategoryScreen**: The `MediaStoreCategoryVM` queries MediaStore and returns items sorted by date DESC. To add sort options, we'd need to change the query or sort in-memory.

**Minimal viable approach**: Add a sort dropdown/chip bar to FileBrowser and MediaStoreCategoryScreen with options:
- Name (A-Z / Z-A)
- Size (largest first / smallest first)
- Date (newest first / oldest first)
- Type (by extension)

### 4.1.3 Grid/List View Toggle

**Problem**: FileBrowser only has list view. ES File Explorer and most file managers offer both grid and list views.

**Implementation**:
- Add a toggle icon button in the FileBrowser normal-mode TopAppBar
- Store preference in SharedPreferences
- When grid: show `LazyVerticalGrid` with thumbnails (similar to WhatsApp cleaner grid)
- When list: show current `LazyColumn`

**Risk**: Medium — significant UI change. Need to make sure both views share the same data source and selection logic.

### 4.1.4 Favorites Screen

**Problem**: `Routes.FAVORITES = "/favorites"` is defined but has NO route in `Router.kt` and NO screen file.

**Implementation**:
- Create `FavoritesScreen.kt` — shows a list of favorited file paths
- Store favorites in SharedPreferences (simple Set<String>)
- Add "Add to Favorites" option in FileBrowser long-press menu
- Add "Favorites" to the drawer

### 4.1.5 File Sort in Flat File Managers

The existing flat file managers (large files, videos, images, screenshots) show files in whatever order the DB query returns. For "Large Files" this should be sorted by size DESC. For "Videos" by size or date. Verify and fix if needed.

### 4.1.6 Long-press Preview in File Browser

Currently, long-pressing a file in FileBrowser enters multi-select mode. ES File Explorer shows a quick preview popup on long-press (thumbnail + name + size + type). This is a nice-to-have but low priority.

---

## 4.2 PLAN

### Priority Order Within Phase 4

| Sub-task | Priority | Complexity |
|----------|----------|------------|
| 4A: Select All for flat file managers | High | Low (copy pattern from duplicates) |
| 4B: Sort options for FileBrowser | High | Medium |
| 4C: Sort options for MediaStoreCategoryScreen | Medium | Low |
| 4D: Favorites screen | Medium | Medium |
| 4E: Grid/List toggle | Low | High |
| 4F: Sort order verification for flat managers | High | Low |

### Acceptance Criteria
- [ ] 4A.1 FlatLargeFiles has Select All/Deselect All
- [ ] 4A.2 FlatVideos has Select All/Deselect All
- [ ] 4A.3 FlatImages has Select All/Deselect All
- [ ] 4A.4 FlatScreenshots has Select All/Deselect All
- [ ] 4A.5 WhatsApp has Select All/Deselect All
- [ ] 4B.1 FileBrowser has sort dropdown (Name, Size, Date, Type)
- [ ] 4B.2 Sort persists across navigation within session
- [ ] 4C.1 MediaStoreCategoryScreen has sort options
- [ ] 4D.1 Favorites screen exists and is navigable
- [ ] 4D.2 "Add to Favorites" works from FileBrowser
- [ ] 4E.1 Grid/List toggle works in FileBrowser
- [ ] 4F.1 Large Files sorted by size DESC
- [ ] 4F.2 Videos sorted by size DESC
- [ ] 4G.1 Build compiles

### Files to Create
| File | Description |
|------|-------------|
| `presentation/ui/pages/FavoritesScreen.kt` | Favorites list screen |
| `utils/FavoritesManager.kt` | SharedPreferences-backed favorites store |

### Files to Modify
| File | Change |
|------|--------|
| `FlatLargeFilesManager.kt` | Add Select All button |
| `FlatVideosFileManagerPage.kt` | Add Select All button |
| `FlatImagesFileManagerPage.kt` | Add Select All button |
| `FlatScreenshotsFileManagerPage.kt` | Add Select All button |
| `WhatsAppCleanerPage.kt` | Add Select All button |
| `FileBrowserScreen.kt` | Add sort dropdown, grid/list toggle, "Add to Favorites" in menu |
| `FileExplorerViewModel.kt` | Add sort state, grid/list state |
| `MediaStoreCategoryScreen.kt` | Add sort dropdown |
| `MediaStoreCategoryVM.kt` | Add sort parameter to query |
| `Router.kt` | Add FAVORITES route |
| `DrawerContent.kt` | Add Favorites to drawer |

---

## 4.3 TEST CHECKLIST

| # | Test Case | Steps | Expected Result | Pass/Fail |
|---|-----------|-------|-----------------|-----------|
| 4A.1 | Select All — Large Files | Open Large Files → tap Select → tap Select All | All files selected, button says "Deselect All" | |
| 4A.2 | Deselect All — Large Files | After select all → tap Deselect All | No files selected | |
| 4A.3 | Select All — Videos | Open Videos → tap Select → tap Select All | All videos selected | |
| 4A.4 | Select All — WhatsApp | Open WhatsApp Cleaner → tap Select → tap Select All | All files in current category selected | |
| 4B.1 | Sort by Name | FileBrowser → tap sort → Name A-Z | Files sorted alphabetically | |
| 4B.2 | Sort by Size | FileBrowser → tap sort → Size (largest) | Largest files first | |
| 4B.3 | Sort by Date | FileBrowser → tap sort → Date (newest) | Newest files first | |
| 4C.1 | Category sort | MediaStoreCategoryScreen → change sort | Files re-sort | |
| 4D.1 | Add to Favorites | Long-press file → "Add to Favorites" | Toast/File added to favorites | |
| 4D.2 | View Favorites | Drawer → Favorites | Favorited files listed | |
| 4D.3 | Remove from Favorites | Favorites → swipe/tap remove | File removed | |
| 4E.1 | Grid view | FileBrowser → tap grid icon | Files show as thumbnail grid | |
| 4E.2 | List view | Grid view → tap list icon | Files show as list | |
| 4E.3 | Grid selection works | Grid view → long-press → select multiple | Selection mode works in grid | |
| 4F.1 | Large files sort order | Open Large Files | Files sorted by size, largest first | |
| 4G.1 | Build compiles | `./gradlew assembleDebug` | Build succeeds | |

---

## 4.4 COMMIT TEMPLATE
```
phase-4: add select all, sort options, favorites, grid/list toggle

- Add Select All/Deselect All to all flat file manager pages
- Add sort dropdown to FileBrowser (Name, Size, Date, Type)
- Add sort options to MediaStoreCategoryScreen
- Create FavoritesScreen with SharedPreferences storage
- Add "Add to Favorites" to FileBrowser long-press menu
- Add grid/list view toggle to FileBrowser
- Add FAVORITES route and drawer entry
- Verify sort order in flat file managers
```