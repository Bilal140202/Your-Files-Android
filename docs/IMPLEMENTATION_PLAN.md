# Your Files Android — 6-Phase Comprehensive Fix Plan

**Branch**: `fix/comprehensive-fixes`  
**Base**: `fix/es-elegance-v2` (HEAD: `240350d`)  
**Goal**: Fix all bugs, implement missing features, remove dead code, and prepare for merge to `main`.

---

## Phase Overview

| Phase | Name | Focus | Scope |
|-------|------|-------|-------|
| 1 | Critical Bug Fixes | Broken navigation, broken trash, file util | 3 bugs |
| 2 | Security & Build Hygiene | Hardcoded credentials, dead code, dual deps | 4 items |
| 3 | Toolbar Organiser Feature | 3-dot menu + FolderOrganiserScreen | 1 feature |
| 4 | Storage Analyzer Rewrite | ES-style 6-stage flow | 1 major feature |
| 5 | Feature Gaps & UX Polish | Favorites, Smart Select, UX improvements | 5 items |
| 6 | Final QA, Cleanup & Merge | Full regression, docs, merge prep | 1 phase |

---

## Phase 1: Critical Bug Fixes

> **Motto**: "Fix what's visibly broken first"

### 1.1 Research Tasks

- [ ] **DrawerContent.kt L84** — Trace full navigation flow: why does `Routes.HOME` render instead of `Routes.ANALYZER`? Test tap behavior in current build.
- [ ] **TrashPage.kt L126-127 + TrashManager.kt L29,110,124** — Understand full trash lifecycle: `doTrash()` → in-memory map → app restart → map cleared → restore impossible. Design a persistent trash record (Room DB or JSON file in app internal storage).
- [ ] **FileUtil.kt L70-73** — Find ALL callers of `File.size()` across codebase. Determine if any caller compensates for the KB-return (e.g., divides by 1024 again). Decide: fix to return bytes, or rename to `sizeInKB()`.

### 1.2 Implementation Tasks

| # | Task | File(s) | Change |
|---|------|---------|--------|
| 1.1.1 | Fix Storage Analyzer navigation | `DrawerContent.kt:84` | Change `route = Routes.HOME` → `route = Routes.ANALYZER` |
| 1.1.2 | Fix "APKs" drawer item | `DrawerContent.kt` | Change APKs route from hardcoded Download path to proper route constant |
| 1.1.3 | Fix File.size() | `FileUtil.kt:70-73` | Rename to `sizeInKB()`, fix comment to say "KB" not "MB" |
| 1.1.4 | Add persistent trash records | `TrashManager.kt`, new `TrashRecord` entity | Store `{originalPath, trashPath, trashedAt}` in SharedPreferences or Room so restore survives restart |
| 1.1.5 | Fix restore logic | `TrashPage.kt:126-127` | Use stored originalPath from persistent records instead of deriving from filename |

### 1.3 Testing Checklist

- [ ] Tap "Storage Analyzer" in drawer → navigates to analyzer screen (not home)
- [ ] Tap "APKs" in drawer → navigates to correct APK listing
- [ ] Trash a file → kill app → reopen → restore file → file appears at original location
- [ ] `File.size()` — verify all callers work correctly after rename/fix
- [ ] Build compiles with zero errors

### 1.4 Log Output

- `docs/phases/phase-1-critical-bugs/RESEARCH_LOG.md` — findings from research tasks
- `docs/phases/phase-1-critical-bugs/IMPLEMENTATION_LOG.md` — what was changed, with before/after code snippets
- `docs/phases/phase-1-critical-bugs/TEST_REPORT.md` — test results, pass/fail for each checklist item

---

## Phase 2: Security & Build Hygiene

> **Motto**: "Clean house before building new rooms"

### 2.1 Research Tasks

- [ ] **build.gradle L24-29** — Check if `yourfiles-release.jks` is in `.gitignore`. If not, the keystore is in the repo (critical). Plan: move credentials to `keystore.properties` (gitignored) and reference via `signingConfigs`.
- [ ] **Dead code mapping** — Verify each dead file has zero runtime references:
  - `HomePage.kt`, `HomeVM.kt`, `ScanningComposable.kt`, `ScanResultComposable.kt`
  - `app/ui/Color.kt`, `Shape.kt`, `Theme.kt`, `Type.kt` (keep `Modifiers.kt` — used by `SelectableFileItem.kt`)
  - `res/navigation/app_navigation.xml`
  - `misc/ui/ncdl.java`
  - `res/layout/connect_account_fragment.xml`
  - `res/values-night/themes.xml`, `res/values-v31/themes.xml`, `res/values/themes.xml` — check if any are still referenced by AndroidManifest or activities
- [ ] **Glide dependency** — Grep for `Glide`, `glide`, `GlideImage`, `rememberGlidePainter` across entire codebase. Confirm zero usage. Plan removal.
- [ ] **Orphaned FAVORITES route** — Decide: remove route constant or add a placeholder screen?

### 2.2 Implementation Tasks

| # | Task | File(s) | Change |
|---|------|---------|--------|
| 2.1.1 | Externalize signing credentials | `app/build.gradle`, new `keystore.properties` | Move passwords/alias to gitignored properties file |
| 2.1.2 | Remove dead code — screens | Delete 4 files | `HomePage.kt`, `HomeVM.kt`, `ScanningComposable.kt`, `ScanResultComposable.kt` |
| 2.1.3 | Remove dead code — old M2 theme | Delete 4 files | `app/ui/Color.kt`, `Shape.kt`, `Theme.kt`, `Type.kt` |
| 2.1.4 | Remove dead code — resources | Delete 3 files | `app_navigation.xml`, `ncdl.java`, `connect_account_fragment.xml` |
| 2.1.5 | Remove dead code — old themes | Audit & delete | `values-night/themes.xml`, `values-v31/themes.xml`, `values/themes.xml` (if unused) |
| 2.1.6 | Remove Glide dependency | `app/build.gradle` | Remove 3 Glide lines, run `./gradlew :app:dependencies` to verify no breakage |
| 2.1.7 | Remove orphaned FAVORITES route | `Router.kt:143` | Delete `const val FAVORITES = "/favorites"` |

### 2.3 Testing Checklist

- [ ] `./gradlew assembleDebug` compiles successfully after all deletions
- [ ] `./gradlew assembleRelease` signs correctly with `keystore.properties`
- [ ] App launches, drawer works, all screens navigate correctly
- [ ] No `Glide` class references in APK (`./gradlew :app:assembleDebug && dexdump` or APK analyzer)
- [ ] `grep -r "Glide\|HomePage\|HomeVM\|ScanningComposable\|ScanResultComposable" app/src/` returns zero results (except build artifacts)

### 2.4 Log Output

- `docs/phases/phase-2-hygiene/RESEARCH_LOG.md`
- `docs/phases/phase-2-hygiene/IMPLEMENTATION_LOG.md`
- `docs/phases/phase-2-hygiene/TEST_REPORT.md`

---

## Phase 3: Toolbar Organiser Feature

> **Motto**: "Add the Organiser button that was promised but never delivered"

### 3.1 Research Tasks

- [ ] **ES File Explorer reference** — Study ES File Explorer's folder organize feature: what operations does it provide? (Sort by name/size/date/type, group by type, flat view toggle, etc.)
- [ ] **FileBrowserScreen.kt top bar** — Read the full `TopAppBar` composable (lines ~493-557). Understand current action slot layout. Plan where to add the Organiser icon (left of Search) or 3-dot overflow menu.
- [ ] **FileExplorerViewModel.kt** — Understand current sort/filter state. What sort options exist? How is `fileList` ordered?
- [ ] **Navigation pattern** — Study how other screens are added to `Router.kt` (import, composable block, route constant). Template for adding FolderOrganiserScreen.

### 3.2 Implementation Tasks

| # | Task | File(s) | Change |
|---|------|---------|--------|
| 3.1.1 | Add 3-dot overflow menu to FileBrowser top bar | `FileBrowserScreen.kt` | Add `IconButton(Icons.Default.MoreVert)` with `DropdownMenu` containing "Organise" option |
| 3.1.2 | Add FOLDER_ORGANISER route | `Router.kt` | Add `const val FOLDER_ORGANISER = "/folder-organiser"` in Routes, add `composable()` block |
| 3.1.3 | Create FolderOrganiserScreen.kt | New file | Full Compose screen with: folder grouping by type, sort options, flat/grouped view toggle |
| 3.1.4 | Create FolderOrganiserViewModel.kt | New file | VM with sort state, group-by logic, file list from current directory |
| 3.1.5 | Wire navigation | `FileBrowserScreen.kt` → `Router.kt` | On "Organise" tap → `navController.navigate(Routes.FOLDER_ORGANISER + "/${currentPath}")` |

### 3.3 Testing Checklist

- [ ] 3-dot menu appears in FileBrowser top bar
- [ ] Tapping 3-dot shows "Organise" option
- [ ] Tapping "Organise" navigates to FolderOrganiserScreen
- [ ] FolderOrganiserScreen displays current directory's contents
- [ ] Sort by name/size/date/type works
- [ ] Group by file type works
- [ ] Back navigation returns to FileBrowser
- [ ] Build compiles with zero errors

### 3.4 Log Output

- `docs/phases/phase-3-toolbar-organiser/RESEARCH_LOG.md`
- `docs/phases/phase-3-toolbar-organiser/IMPLEMENTATION_LOG.md`
- `docs/phases/phase-3-toolbar-organiser/TEST_REPORT.md`

---

## Phase 4: Storage Analyzer Rewrite (ES-Style)

> **Motto**: "Transform the basic donut chart into a full ES-style storage breakdown"

### 4.1 Research Tasks

- [ ] **ES File Explorer storage analysis flow** — Study ES's 6-stage analysis: (1) Overview with total/used/available, (2) Category breakdown (Images/Videos/Audio/APKs/Docs/Other/System), (3) Largest files, (4) Recently added files, (5) Duplicate files hint, (6) Cleanup suggestions.
- [ ] **Current StorageAnalyzerVM.kt** — Full read. Understand: recursive walk performance, 7-category classification, top-10 heap, 1-hour cache, StatFs usage. Identify what to keep vs rewrite.
- [ ] **Current StorageAnalyzerScreen.kt** — Full read. Identify all composable sections. Plan which to keep (category colors, donut chart as sub-component) and which to replace.
- [ ] **Performance research** — Current `walkTopDown()` on root can scan 100K+ files. Research: (a) coroutine cancellation support, (b) progress reporting granularity, (c) caching strategy, (d) incremental scanning option.

### 4.2 Implementation Tasks

| # | Task | File(s) | Change |
|---|------|---------|--------|
| 4.1.1 | Redesign StorageUiState | `StorageUiState.kt` | New sealed interface: `Scanning(progress) → Overview(stats) → CategoryBreakdown(categories) → LargestFiles(files) → RecentFiles(files) → CleanupSuggestions(suggestions)` |
| 4.1.2 | Rewrite StorageAnalyzerVM | `StorageAnalyzerVM.kt` | Support cancellation, add recent-files tracking, add cleanup-suggestion logic, keep 1-hour cache |
| 4.1.3 | Build Overview section | `StorageAnalyzerScreen.kt` | Total/Used/Available cards with progress bar |
| 4.1.4 | Build Category Breakdown section | `StorageAnalyzerScreen.kt` | Keep donut chart, add expandable category rows with file counts and sizes |
| 4.1.5 | Build Largest Files section | `StorageAnalyzerScreen.kt` | Top 20 (up from 10), tap to navigate to file location |
| 4.1.6 | Build Recently Added section | `StorageAnalyzerScreen.kt` | Files modified in last 7 days, sorted by date |
| 4.1.7 | Build Cleanup Suggestions section | `StorageAnalyzerScreen.kt` | APK suggestions, large file warnings, empty folder hints |
| 4.1.8 | Add pull-to-refresh + rescan | `StorageAnalyzerScreen.kt` | Swipe to invalidate cache and re-scan |

### 4.3 Testing Checklist

- [ ] Storage Analyzer opens from drawer correctly (Phase 1 fix verified)
- [ ] Scanning progress shows file count and percentage
- [ ] Overview shows correct Total/Used/Available matching device settings
- [ ] Category breakdown sums to total scanned size
- [ ] Tapping a category shows files in that category
- [ ] Largest files list is accurate and tap navigates correctly
- [ ] Recently added shows files from last 7 days
- [ ] Cleanup suggestions are reasonable (not suggesting system files)
- [ ] Cache works: second open within 1 hour is instant
- [ ] Pull-to-refresh invalidates cache and rescans
- [ ] Back press during scan cancels the scan coroutine
- [ ] No ANR on devices with 100K+ files

### 4.4 Log Output

- `docs/phases/phase-4-storage-analyzer/RESEARCH_LOG.md`
- `docs/phases/phase-4-storage-analyzer/IMPLEMENTATION_LOG.md`
- `docs/phases/phase-4-storage-analyzer/TEST_REPORT.md`

---

## Phase 5: Feature Gaps & UX Polish

> **Motto**: "Fill the holes and smooth the edges"

### 5.1 Research Tasks

- [ ] **FlatDuplicatesFileManagerPage.kt** — Study `autoSelectBest` logic. What criteria does it use? Design a "Smart Select" strategy: keep highest resolution image, newest version of document, largest APK, etc.
- [ ] **FileBrowserScreen.kt** — Research multi-select UX: is there "Select All" option? Long-press behavior? Batch operation feedback?
- [ ] **DrawerContent.kt** — Full audit of all menu items. Check each route resolves correctly. Verify icon choices match Material Icons library.
- [ ] **ESHomeScreen.kt** — Study the home screen layout. Are storage stats accurate? Do quick-action buttons all work? Is the visual design consistent?

### 5.2 Implementation Tasks

| # | Task | File(s) | Change |
|---|------|---------|--------|
| 5.1.1 | Smart Select for duplicates | `FlatDuplicatesFileManagerPage.kt`, `BestFileSelector.kt` | Implement per-type selection strategy (images: highest res; docs: newest; APKs: largest) |
| 5.1.2 | Add "Select All" to multi-select mode | `FileBrowserScreen.kt` | Add "Select All" / "Deselect All" toggle in multi-select action bar |
| 5.1.3 | Drawer menu audit & fix | `DrawerContent.kt` | Verify ALL routes, fix any remaining wrong routes, add icons for missing entries |
| 5.1.4 | Empty state screens | Multiple screens | Add placeholder composables for empty directories, no search results, no duplicates found |
| 5.1.5 | Consistent loading indicators | Multiple screens | Ensure all async operations show `CircularProgressIndicator` with descriptive text |

### 5.3 Testing Checklist

- [ ] Smart Select keeps highest resolution image in each duplicate group
- [ ] Smart Select keeps newest document version
- [ ] "Select All" selects all visible files in FileBrowser
- [ ] "Deselect All" clears selection
- [ ] All drawer menu items navigate to correct screens
- [ ] Empty directory shows "No files" message instead of blank space
- [ ] Search with no results shows "No results found" message
- [ ] Loading states appear for all async operations
- [ ] Build compiles with zero errors

### 5.4 Log Output

- `docs/phases/phase-5-features-ux/RESEARCH_LOG.md`
- `docs/phases/phase-5-features-ux/IMPLEMENTATION_LOG.md`
- `docs/phases/phase-5-features-ux/TEST_REPORT.md`

---

## Phase 6: Final QA, Cleanup & Merge

> **Motto**: "Ship it clean"

### 6.1 Research Tasks

- [ ] **Full regression scope** — List every screen and navigation path. Create a master test matrix covering all user-facing flows.
- [ ] **APK size comparison** — Build before (Phase 2 start) and after (Phase 6 end). Measure APK size reduction from dead code + Glide removal.
- [ ] **ProGuard rules audit** — Review `proguard-rules.pro` for completeness. Ensure Room entities, Coil, ExoPlayer, and OkHttp rules are present.

### 6.2 Implementation Tasks

| # | Task | File(s) | Change |
|---|------|---------|--------|
| 6.1.1 | Full regression test | All screens | Manual test every screen, every navigation path, every user flow |
| 6.1.2 | Fix any regressions found | TBA | Fix issues discovered during regression |
| 6.1.3 | Clean up debug/logging code | Multiple files | Remove `Log.d()` statements, ensure `DebugUtil` usage is appropriate |
| 6.1.4 | Update ProGuard rules | `proguard-rules.pro` | Add missing keep rules for all libraries |
| 6.1.5 | Verify release build | `build.gradle` | `./gradlew assembleRelease` — ensure signed APK generates correctly |
| 6.1.6 | Create keystore.properties template | `keystore.properties.example` | Document required signing config without exposing secrets |
| 6.1.7 | Merge to main | Git | `git checkout main && git merge fix/comprehensive-fixes --no-ff` |

### 6.3 Testing Checklist

- [ ] **Full navigation matrix**: Every drawer item → correct screen
- [ ] **File operations**: Browse, open, share, delete, trash, restore, rename, copy, move
- [ ] **Media players**: Video, audio, image viewer all launch and play correctly
- [ ] **Tools**: Duplicates, large files, WhatsApp cleaner, image optimizer, screenshots
- [ ] **Storage Analyzer**: Full 6-stage flow from Phase 4
- [ ] **Folder Organiser**: From Phase 3 — sort, group, navigate
- [ ] **Trash**: Delete → trash → restore cycle works across app restart
- [ ] **Settings**: All toggles work, dark mode applies correctly
- [ ] **Onboarding**: Fresh install shows onboarding, permissions flow works
- [ ] **Release APK**: Signs correctly, installs on device, all features work
- [ ] **No crashes**: Check logcat for exceptions during full walkthrough

### 6.4 Log Output

- `docs/phases/phase-6-final-qa/RESEARCH_LOG.md`
- `docs/phases/phase-6-final-qa/IMPLEMENTATION_LOG.md`
- `docs/phases/phase-6-final-qa/TEST_REPORT.md`
- `docs/phases/phase-6-final-qa/REGRESSION_MATRIX.md` — master test matrix with pass/fail
- `docs/phases/phase-6-final-qa/APK_SIZE_COMPARISON.md` — before/after size analysis

---

## Branch Management

```
main ──────────────────────────────────────────── (target)
  └── fix/es-elegance-v2 (HEAD: 240350d)
        └── fix/comprehensive-fixes  ← WE ARE HERE
              ├── Phase 1 commit(s)
              ├── Phase 2 commit(s)
              ├── Phase 3 commit(s)
              ├── Phase 4 commit(s)
              ├── Phase 5 commit(s)
              └── Phase 6 commit(s)
                    └── merge --no-ff → main
```

**Commit convention**: `<type>: <short description>`
- `fix:` — bug fixes
- `feat:` — new features
- `refactor:` — code restructuring
- `chore:` — cleanup, build, deps
- `docs:` — documentation only
- `test:` — test additions

---

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Trash restore breaks existing trash files | Medium | High | Backup trash directory before modifying TrashManager |
| Removing dead code causes compile errors | Low | Medium | Remove one file at a time, compile after each |
| Storage Analyzer rewrite is too slow on large devices | Medium | High | Keep cancellation support, add timeout, test on 100K+ file device |
| Glide removal breaks hidden dependency | Low | Medium | Grep thoroughly before removing, test image loading |
| Signing config change breaks CI/CD | Medium | High | Keep `keystore.properties.example` with instructions |

---

## Issue-to-Phase Mapping

| Issue # | Description | Phase | Priority |
|---------|-------------|-------|----------|
| — | DrawerContent Storage Analyzer → wrong route | 1 | Critical |
| — | TrashPage restore = no-op | 1 | Critical |
| — | File.size() returns KB, says MB | 1 | High |
| — | Hardcoded signing credentials | 2 | High (Security) |
| — | 4 dead screen files | 2 | Medium |
| — | 4 dead M2 theme files | 2 | Low |
| — | 3 dead resource/Java files | 2 | Low |
| — | Glide + Coil dual deps | 2 | Medium |
| — | Orphaned FAVORITES route | 2 | Low |
| — | No Organiser button in FileBrowser | 3 | High |
| — | No FolderOrganiserScreen | 3 | High |
| — | No FOLDER_ORGANISER route | 3 | High |
| — | Storage Analyzer = basic donut | 4 | High |
| — | No Smart Select for duplicates | 5 | Medium |
| — | No Select All in multi-select | 5 | Medium |
| — | No empty state screens | 5 | Low |
| — | Inconsistent loading indicators | 5 | Low |