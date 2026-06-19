# Phase 6 — Full Regression Test Log

## Date: ___________
## Tester: ___________
## Debug APK: [ ] PASS / [ ] FAIL
## Release APK: [ ] PASS / [ ] FAIL

---

## R. Navigation & Core Screens (21)
| # | Test | Expected | Actual | Pass/Fail |
|---|------|----------|--------|-----------|
| R.01 | App launches | ESHomeScreen | | |
| R.02 | Storage card | FileBrowser | | |
| R.03 | SD card | FileBrowser | | |
| R.04 | Image category | MediaStoreCategory | | |
| R.05 | Video category | MediaStoreCategory | | |
| R.06 | Document category | MediaStoreCategory | | |
| R.07 | Music category | MediaStoreCategory | | |
| R.08 | APK category | MediaStoreCategory | | |
| R.09 | Cleaner tool | FlatFileManager | | |
| R.10 | Analyzer tool | AnalyzerHomeScreen | | |
| R.11 | Optimise tool | ImageOptimiserPage | | |
| R.12 | Recycle Bin | TrashPage | | |
| R.13 | Drawer items | All navigate correctly | | |
| R.14 | Folder navigation | Drill down | | |
| R.15 | Navigate up | Parent folder | | |
| R.16 | Breadcrumb | Jump to level | | |
| R.17 | Open file | Detail viewer | | |
| R.18 | Long-press select | Multi-select | | |
| R.19 | Delete selected | To trash | | |
| R.20 | Search | Results filter | | |
| R.21 | Grid/List toggle | View switches | | |

## F. File Operations (12)
| # | Test | Expected | Actual | Pass/Fail |
|---|------|----------|--------|-----------|
| F.01 | Create folder | Created | | |
| F.02 | Rename | Renamed | | |
| F.03 | Copy + Paste | Copied | | |
| F.04 | Cut + Paste | Moved | | |
| F.05 | Delete single | To trash | | |
| F.06 | Delete multiple | To trash | | |
| F.07 | Share | Share dialog | | |
| F.08 | Properties | Info shown | | |
| F.09 | APK install | Install prompt | | |
| F.10 | 3-dot menu | 4 items | | |
| F.11 | Organiser | FolderOrganiserScreen | | |
| F.12 | New Folder from menu | Dialog | | |

## C. Cleaner Tools (10)
| # | Test | Expected | Actual | Pass/Fail |
|---|------|----------|--------|-----------|
| C.01 | Duplicates load | Groups shown | | |
| C.02 | Auto Select Best | Best kept | | |
| C.03 | Delete duplicates | To trash | | |
| C.04 | Large Files load | Files shown | | |
| C.05 | Select All (large) | All selected | | |
| C.06 | Delete large | To trash | | |
| C.07 | Images load | Grid shown | | |
| C.08 | Image Optimiser | Compressed | | |
| C.09 | WhatsApp load | Categories | | |
| C.10 | WhatsApp delete | To trash | | |

## A. Storage Analyzer (10)
| # | Test | Expected | Actual | Pass/Fail |
|---|------|----------|--------|-----------|
| A.01 | Analyzer Home | Checkboxes | | |
| A.02 | Scan | Progress → Dashboard | | |
| A.03 | Dashboard cards | 6 cards | | |
| A.04 | Category tap | File list | | |
| A.05 | Folder sizes | Directories | | |
| A.06 | Drill-in | Sub-items | | |
| A.07 | Junk cleaner | Categories | | |
| A.08 | Junk clean | To trash | | |
| A.09 | Permission gate | Permission screen | | |
| A.10 | Back nav | Returns home | | |

## T. Trash & Restore (5)
| # | Test | Expected | Actual | Pass/Fail |
|---|------|----------|--------|-----------|
| T.01 | Trash list | Files shown | | |
| T.02 | Restore | Original location | | |
| T.03 | Empty trash | Deleted | | |
| T.04 | Auto-cleanup | Old removed | | |
| T.05 | Undo delete | Restored | | |

## P. Phase 4 Features (8)
| # | Test | Expected | Actual | Pass/Fail |
|---|------|----------|--------|-----------|
| P.01 | Sort name | A-Z | | |
| P.02 | Sort size | Largest first | | |
| P.03 | Sort date | Newest first | | |
| P.04 | Select All | All selected | | |
| P.05 | Favorites add | Added | | |
| P.06 | Favorites view | List | | |
| P.07 | Favorites remove | Removed | | |
| P.08 | Grid view | Thumbnails | | |

## B. Build & Security (8)
| # | Test | Expected | Actual | Pass/Fail |
|---|------|----------|--------|-----------|
| B.01 | Debug build | Success | | |
| B.02 | Release build | Signed APK | | |
| B.03 | APK size | < 15MB | | |
| B.04 | No secrets | Zero results | | |
| B.05 | No dead imports | Clean | | |
| B.06 | Lint | No critical | | |
| B.07 | ProGuard | No crashes | | |
| B.08 | Version display | v2.0.0 | | |

---

## FINAL SUMMARY
- **Total Test Cases**: 84
- **Passed**: ___
- **Failed**: ___
- **Blocked**: ___

## Release Approval
[ ] ALL 84 tests pass
[ ] Release APK built and signed
[ ] APK sent to Telegram
[ ] GitHub release v2.0.0 created
[ ] Ready to merge to main