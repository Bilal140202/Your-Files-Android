# Phase 6 — Regression Test Matrix

**Date**:  
**Branch**: fix/comprehensive-fixes

---

## Navigation Tests

| # | Action | Expected Result | Status |
|---|--------|----------------|--------|
| 1 | Tap drawer → Home | ESHomeScreen renders | |
| 2 | Tap drawer → Storage Analyzer | StorageAnalyzerScreen renders | |
| 3 | Tap drawer → File Browser | FileBrowserScreen renders | |
| 4 | Tap drawer → Settings | SettingsPage renders | |
| 5 | Tap drawer → Trash | TrashPage renders | |

## File Operation Tests

| # | Action | Expected Result | Status |
|---|--------|----------------|--------|
| 6 | Browse directory | File list shows | |
| 7 | Tap file → open | Correct viewer launches | |
| 8 | Long-press → multi-select | Selection mode activates | |
| 9 | Multi-select → Delete | Files moved to trash | |
| 10 | Trash → Restore | Files return to original location | |
| 11 | Share file | Share sheet opens | |
| 12 | Rename file | Name updates | |

## Media Player Tests

| # | Action | Expected Result | Status |
|---|--------|----------------|--------|
| 13 | Open video | VideoPlayerScreen plays | |
| 14 | Open audio | AudioPlayerScreen plays | |
| 15 | Open image | ImageViewer displays | |
| 16 | Open PDF | PdfViewerScreen renders | |
| 17 | Open text | TextViewerScreen shows content | |

## Tool Tests

| # | Action | Expected Result | Status |
|---|--------|----------------|--------|
| 18 | Duplicates scan | Groups found | |
| 19 | Large files scan | Files listed by size | |
| 20 | WhatsApp cleaner | Categories load | |
| 21 | Image optimizer | Compression works | |
| 22 | Screenshots manager | Screenshots listed | |

## New Feature Tests

| # | Action | Expected Result | Status |
|---|--------|----------------|--------|
| 23 | FileBrowser 3-dot menu | Dropdown shows | |
| 24 | 3-dot → Organise | FolderOrganiserScreen opens | |
| 25 | Organiser → Sort by size | Files reorder | |
| 26 | Organiser → Group by type | Files grouped | |
| 27 | Storage Analyzer → Overview | Stats display | |
| 28 | Storage Analyzer → Categories | Breakdown shows | |
| 29 | Storage Analyzer → Largest files | Top 20 listed | |
| 30 | Storage Analyzer → Recent files | Last 7 days shown | |
| 31 | Storage Analyzer → Cleanup suggestions | Suggestions appear | |
| 32 | Select All in multi-select | All files selected | |

---

**Total: 32 tests**  
**Passed**: ___ / 32  
**Failed**: ___ / 32  
**Notes**: