# Phase 6: Integration Testing & Release Preparation

## Goal
Full regression testing of the entire app, build release APK, and prepare for merge to main.

---

## 6.1 RESEARCH

### 6.1.1 Build Environment

```
JAVA_HOME=/home/z/jdk-21
ANDROID_HOME=/home/z/android-sdk
PLATFORMS=android-36
BUILD_TOOLS=36.0.0
GRADLE=./gradlew
```

### 6.1.2 Build Commands

```bash
# Clean build
./gradlew clean

# Debug APK
./gradlew assembleDebug

# Release APK (signed)
./gradlew assembleRelease

# Run all unit tests (currently only boilerplate)
./gradlew test

# Run instrumented tests (currently only boilerplate)
./gradlew connectedAndroidTest

# Lint
./gradlew lint

# Check for dependency updates
./gradlew dependencyUpdates
```

### 6.1.3 APK Output Locations

```
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release.apk
```

### 6.1.4 Release Checklist

- [ ] Version bump in `build.gradle` (versionName, versionCode)
- [ ] Update `SettingsPage.kt` version display
- [ ] Update `DrawerContent.kt` version subtitle
- [ ] ProGuard minification enabled for release
- [ ] Signing works with local.properties credentials
- [ ] No hardcoded secrets in codebase
- [ ] All TODO/FIXME comments resolved or documented

### 6.1.5 Git Workflow for Merge

```bash
# On feature/phase-implementation:
git log --oneline  # verify all 6 phase commits

# Create PR on GitHub:
# feature/phase-implementation → main

# After review & approval:
git checkout main
git merge feature/phase-implementation
git push origin main

# Tag release:
git tag v2.0.0
git push origin v2.0.0
```

### 6.1.6 Regression Test Matrix

Every screen and feature must be tested end-to-end after all 6 phases are complete.

---

## 6.2 PLAN — FULL REGRESSION TEST SUITE

### 6.2.1 Navigation & Core Screens (21 tests)

| # | Screen | Test | Expected | Pass/Fail |
|---|--------|------|----------|-----------|
| R.01 | Home | App launches | ESHomeScreen with storage card, categories, tools | |
| R.02 | Home | Storage card tap | Opens internal storage in FileBrowser | |
| R.03 | Home | SD card tap (if present) | Opens SD card in FileBrowser | |
| R.04 | Home | Image category tap | MediaStoreCategoryScreen with images | |
| R.05 | Home | Video category tap | MediaStoreCategoryScreen with videos | |
| R.06 | Home | Document category tap | MediaStoreCategoryScreen with documents | |
| R.07 | Home | Music category tap | MediaStoreCategoryScreen with audio | |
| R.08 | Home | APK category tap | MediaStoreCategoryScreen with APKs | |
| R.09 | Home | Cleaner tool tap | FlatFileManager (duplicates) | |
| R.10 | Home | Analyzer tool tap | New Analyzer Home (from Phase 3) | |
| R.11 | Home | Optimise tool tap | ImageOptimiserPage | |
| R.12 | Home | Recycle Bin tool tap | TrashPage | |
| R.13 | Drawer | All drawer items | Each navigates correctly | |
| R.14 | FileBrowser | Navigate into folders | Tap folders to drill down | |
| R.15 | FileBrowser | Navigate up | Back navigates to parent | |
| R.16 | FileBrowser | Breadcrumb tap | Jumps to that level | |
| R.17 | FileBrowser | Open a file | File detail viewer / appropriate viewer | |
| R.18 | FileBrowser | Long-press → select | Multi-select mode activates | |
| R.19 | FileBrowser | Delete selected | Files moved to trash | |
| R.20 | FileBrowser | Search | Search bar, results filter | |
| R.21 | FileBrowser | Grid/List toggle | View switches (Phase 4) | |

### 6.2.2 File Operations (12 tests)

| # | Operation | Test | Expected | Pass/Fail |
|---|-----------|------|----------|-----------|
| F.01 | Create folder | New folder dialog | Folder created in current directory | |
| F.02 | Rename | Rename dialog | File/folder renamed | |
| F.03 | Copy + Paste | Copy file → navigate → paste | File copied | |
| F.04 | Cut + Paste | Cut file → navigate → paste | File moved | |
| F.05 | Delete (single) | Delete file | File in trash | |
| F.06 | Delete (multiple) | Select 3 → delete | 3 files in trash | |
| F.07 | Share | Share menu | Share dialog opens | |
| F.08 | Properties | Info bottom sheet | Shows name, size, path, date | |
| F.09 | APK install | Tap .apk file | Install prompt | |
| F.10 | 3-dot menu (Phase 2) | Open menu | 4 items visible | |
| R.11 | Organiser (Phase 2) | Tap Organiser | FolderOrganiserScreen opens | |
| R.12 | New Folder from menu | Menu → New Folder | Dialog appears | |

### 6.2.3 Cleaner Tools (10 tests)

| # | Tool | Test | Expected | Pass/Fail |
|---|------|------|----------|-----------|
| C.01 | Duplicates | Load | Duplicate groups shown | |
| C.02 | Duplicates | Auto Select Best | Best files kept | |
| C.03 | Duplicates | Delete selected | Files to trash, undo works | |
| C.04 | Large Files | Load | Files > threshold shown | |
| C.05 | Large Files | Select All (Phase 4) | All selected | |
| C.06 | Large Files | Delete | Files to trash | |
| C.07 | Images | Load | Images in grid | |
| C.08 | Image Optimiser | Select + Optimise | Images compressed | |
| C.09 | WhatsApp | Load | Categories with counts | |
| C.10 | WhatsApp | Delete | Files to trash | |

### 6.2.4 Storage Analyzer (Phase 3) (10 tests)

| # | Feature | Test | Expected | Pass/Fail |
|---|---------|------|----------|-----------|
| A.01 | Analyzer Home | Open analyzer | Checkboxes shown | |
| A.02 | Scan | Select All → Scan | Progress → Dashboard | |
| A.03 | Dashboard | Category cards | 6 cards with sizes | |
| A.04 | Category tap | Tap Videos | Video file list | |
| A.05 | Folder sizes | Open folder sizes | Directories with sizes | |
| A.06 | Drill-in | Tap subfolder | Sub-items shown | |
| A.07 | Junk cleaner | Open junk | Categories shown | |
| A.08 | Junk clean | Select + Clean Now | Files to trash | |
| A.09 | Permission | Revoke → open | Permission screen | |
| A.10 | Back nav | Back from dashboard | Returns to home | |

### 6.2.5 Trash & Restore (Phase 1 fix) (5 tests)

| # | Feature | Test | Expected | Pass/Fail |
|---|---------|------|----------|-----------|
| T.01 | Trash list | Delete files → open trash | Files shown | |
| T.02 | Restore | Tap restore | File returns to original location | |
| T.03 | Empty trash | Tap Empty Trash | All deleted | |
| T.04 | Auto-cleanup | (30-day auto-cleanup runs on app start) | Old files removed | |
| T.05 | Undo delete | Delete → undo snackbar | File restored | |

### 6.2.6 Phase 4 Features (8 tests)

| # | Feature | Test | Expected | Pass/Fail |
|---|---------|------|----------|-----------|
| P.01 | Sort by name | FileBrowser → sort → Name | Sorted A-Z | |
| P.02 | Sort by size | FileBrowser → sort → Size | Largest first | |
| P.03 | Sort by date | FileBrowser → sort → Date | Newest first | |
| P.04 | Select All | FlatLargeFiles → Select All | All selected | |
| P.05 | Favorites add | Long-press → Favorite | Added | |
| P.06 | Favorites view | Drawer → Favorites | List shown | |
| P.07 | Favorites remove | Remove from favorites | Removed | |
| P.08 | Grid view | Toggle grid | Thumbnails grid | |

### 6.2.7 Build & Security (Phase 5) (8 tests)

| # | Feature | Test | Expected | Pass/Fail |
|---|---------|------|----------|-----------|
| B.01 | Debug build | `assembleDebug` | Success | |
| B.02 | Release build | `assembleRelease` | Success, signed APK | |
| B.03 | APK size | Check release APK size | Reasonable (< 15MB) | |
| B.04 | No secrets | `rg "ghp_\|yourfiles_2024" build.gradle` | Zero results | |
| B.05 | No dead imports | Build completes cleanly | No unresolved references | |
| B.06 | Lint | `./gradlew lint` | No critical issues | |
| B.07 | ProGuard | Release APK runs | No runtime crashes from obfuscation | |
| B.08 | Version | Check Settings page | Shows correct version | |

---

## 6.3 RELEASE PREPARATION

### Pre-Merge Checklist
- [ ] All 6 phases committed and pushed
- [ ] All regression tests pass
- [ ] No TODO/FIXME in new code (or documented)
- [ ] Version bumped to 2.0.0
- [ ] Release APK built and tested
- [ ] APK sent to Telegram
- [ ] GitHub release created with changelog

### Version Bump
```groovy
// app/build.gradle
versionCode 200
versionName "2.0.0"
```

### Changelog (for GitHub release)
```
v2.0.0 — Major Update

Storage Analyzer:
- Complete rewrite with ES File Explorer-style flow
- Scan scope selection (Large Files, Duplicates, Recent, All, App Folders)
- Category dashboard with clickable cards
- Folder size view with drill-in
- Junk cleaner (obsolete APKs, ad junk, downloads, thumbnails)
- MANAGE_EXTERNAL_STORAGE permission gate

File Browser:
- Added Organiser button and 3-dot overflow menu
- Added grid/list view toggle
- Added sort options (Name, Size, Date, Type)
- Added favorites support

Bug Fixes:
- Fixed Drawer "Storage Analyzer" route (was navigating to Home)
- Fixed Drawer "APKs" route (was opening Downloads)
- Fixed Trash restore (files now return to original location)
- Fixed File.size() naming (renamed to sizeInKb)
- Added permission check before Storage Analyzer scan

Cleanup:
- Removed unused legacy code (old theme, HomePage, dead layouts)
- Removed unused Glide dependency
- Moved signing credentials to local.properties
- Added ProGuard rules
- Added Select All to all file manager pages
```

---

## 6.4 COMMIT TEMPLATE
```
phase-6: integration testing, version bump, release preparation

- Run full regression test suite (74 test cases)
- Fix any regressions found during testing
- Bump version to 2.0.0 (versionCode 200)
- Update version display in Settings and Drawer
- Build and verify release APK
- Send APK to Telegram
- Create GitHub release v2.0.0 with changelog
```