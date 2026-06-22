# Competitive Feature Analysis — Your Files vs Market Leaders

> **Date:** June 2025  
> **Version:** 1.0.0  
> **Author:** Senior App Development Expert Review  

---

## Executive Summary

**Your Files** is an Android file manager built with **Jetpack Compose + Material Design 3**, positioning itself as a modern, privacy-first, local-only file management tool. It competes in a crowded market dominated by incumbents like **Files by Google** (free, lightweight), **Solid Explorer** (premium dual-pane), **MiXplorer** (power-user Swiss army knife), and **MT Manager** (APK editor + file manager).

### Competitive Position

| Dimension | Assessment |
|---|---|
| **Core File Operations** | ✅ **Competitive** — Full clipboard (copy/cut/paste), multi-select with range selection, rename, delete-to-trash, create folder, breadcrumb navigation, search, folder caching |
| **Advanced Features** | ⚠️ **Limited** — No root access, no cloud storage, no network/FTP/SFTP, no archive creation/extraction, no dual-pane |
| **Media & Viewing** | ✅ **Strong** — In-app image viewer, video player (ExoPlayer), audio player (ExoPlayer), PDF viewer (native PdfRenderer), text/code viewer, ZIP browser, APK info viewer |
| **Tools & Utilities** | ✅ **Differentiated** — Duplicate finder with smart "best file" selection, storage analyzer with cleanup suggestions, image optimizer, WhatsApp cleaner, recycle bin with restore |
| **UI/UX** | ✅ **Modern** — Material Design 3, dark mode, shimmer loading states, empty states, onboarding flow |
| **Security & Privacy** | ✅ **Best-in-class** — Fully offline, no ads, no tracking, no data collection, all processing on-device |

**Overall verdict:** Your Files offers a **unique combination** of smart cleaning tools (duplicates, WhatsApp cleaner, image optimizer) and built-in media viewers that most competitors don't bundle together. Its weakness is the absence of connectivity features (cloud, network, FTP) and power-user features (root, archives, dual-pane). The strongest positioning is as a **privacy-first smart cleaner + file manager** for mainstream Android users who want a clean, ad-free experience.

---

## 1. Methodology

This comparison was conducted through:

1. **Web research** (June 2025) — Current feature sets gathered from Google Play listings, XDA Forums, Reddit discussions, F-Droid listings, and review sites for all 10 competitors.
2. **Source code audit** — Every `.kt` file in the Your Files project was read and catalogued (73 files across presentation, domain, data, utils, helper, and app layers).
3. **Feature mapping** — Each source file was analyzed for implemented functionality, and features were categorized into standardized comparison groups.

### Competitors Selected

| # | App | Selected Because |
|---|---|---|
| 1 | Files by Google | Google's official free file manager — the default for many users |
| 2 | Solid Explorer | Top-rated premium dual-pane file manager |
| 3 | MiXplorer | Power-user favorite with unmatched configurability |
| 4 | MT Manager | APK editor + file manager hybrid, very popular in modding community |
| 5 | Cx File Explorer | Free, clean alternative with network/cloud support |
| 6 | Amaze File Manager | Leading open-source file manager |
| 7 | ZArchiver | Best-in-class archive manager |
| 8 | ES File Explorer | The discontinued legend — set the standard for file managers |
| 9 | Root Explorer | Root-focused classic |
| 10 | X-plore File Manager | Dual-pane pioneer, recently removed from Play Store |

> **Note:** Root Explorer, ES File Explorer, and X-plore were included for historical reference and feature completeness comparison. ES File Explorer was removed from Google Play in 2019 for ad fraud; X-plore was reportedly removed from Play Store in 2025.

---

## 2. Competitor Profiles

### 2.1 Files by Google
- **Overview:** Google's official file manager, pre-installed on many Android devices. Focuses on simplicity and storage cleanup.
- **Key Features:** Browse files, free up space suggestions, share files offline, cloud storage access (Google Drive only), media categorization, clean recommendations.
- **Pricing:** Free, no ads
- **Ratings:** ~4.5★ with 5B+ downloads (pre-installed)
- **Strengths:** Zero-config, clean UI, Google ecosystem integration, reliable.
- **Weaknesses:** Limited file operations (no copy/cut/paste clipboard), no root, no FTP/network, basic search.

### 2.2 Solid Explorer
- **Overview:** Premium dual-pane file manager with Material Design UI. Long-standing favorite since 2016.
- **Key Features:** Dual-pane layout, cloud storage (Google Drive, Dropbox, OneDrive, Box, ownCloud), FTP/SFTP/SMB, root access, archive support (create/extract), encryption (AES-256), customizable themes, plugins.
- **Pricing:** Paid (~$2.99), 14-day free trial
- **Ratings:** ~4.4★ with 1M+ downloads
- **Strengths:** Dual-pane, encryption, cloud support, plugins, Material Design.
- **Weaknesses:** Paid, UI considered dated by some users, no built-in duplicate finder or WhatsApp cleaner.

### 2.3 MiXplorer
- **Overview:** The most feature-rich Android file manager, maintained by a single developer (Hootan Parsa). Available via XDA Forums.
- **Key Features:** Multi-tab, dual-pane, root access, FTP/SFTP/FTP/FTPS/SFTP/WebDAV, SMB/CIFS, cloud storage (20+ providers), archive support (RAR, 7z, ZIP, tar, etc.), text editor, image viewer, APK management, SQLite viewer, hex editor, Material Design themes, configurable UI.
- **Pricing:** Free (XDA) / Paid on Play Store (~$4.99)
- **Ratings:** ~4.7★ (limited Play Store presence, ~10K downloads)
- **Strengths:** Unmatched feature set, cloud/network support, customization, archive handling.
- **Weaknesses:** Not on Play Store (side-load), UI can be overwhelming for new users, no duplicate finder.

### 2.4 MT Manager
- **Overview:** Dual-panel APK editor and file manager. Popular in Android modding community.
- **Key Features:** Dual-pane, APK editing (DEX, ARSC, manifest), root access, archive support, text editor, image viewer, sign APK, decompile/recompile APK.
- **Pricing:** Free (ad-supported) / VIP unlock
- **Ratings:** ~4.6★ with 10M+ downloads
- **Strengths:** APK editing capabilities, dual-pane, root access.
- **Weaknesses:** Niche focus (APK editing), Chinese-first UI, ads, no cloud/network.

### 2.5 Cx File Explorer
- **Overview:** Free, clean file manager with network and cloud storage support. Popular as an ES alternative.
- **Key Features:** Browse/copy/move/delete/rename, cloud storage (Google Drive, OneDrive, Dropbox, Box), network storage (SMB/FTP), root access (optional), archive support, built-in browser, multiple sorting options.
- **Pricing:** Free, no ads
- **Ratings:** ~4.5★ with 10M+ downloads
- **Strengths:** Clean UI, free without ads, cloud + network, good sorting.
- **Weaknesses:** No dual-pane, limited media viewers, no duplicate finder, no WhatsApp cleaner.

### 2.6 Amaze File Manager
- **Overview:** Open-source file manager following Material Design guidelines. Community-maintained.
- **Key Features:** Browse/copy/move/delete, compress/extract (ZIP), FTP/SFTP, SMB, root access, cloud storage (Google Drive, Dropbox, OneDrive, Box), SQLite viewer, built-in text editor, theme support.
- **Pricing:** Free, open source (no ads)
- **Ratings:** ~4.2★ with 5M+ downloads
- **Strengths:** Open source, no ads, clean Material Design, root + network.
- **Weaknesses:** F-Droid version outdated, Play Store version no longer fully open source, occasional performance issues, no duplicate finder.

### 2.7 ZArchiver
- **Overview:** Archive-focused tool — the go-to for compressed file management on Android.
- **Key Features:** Create/extract ZIP, RAR, 7z, tar, gz, bz2, xz, iso, lzma, etc. Archive encryption, partial extraction, multi-part archives, password-protected archives.
- **Pricing:** Free, no ads
- **Ratings:** ~4.7★ with 50M+ downloads
- **Strengths:** Best archive support, clean UI, free, reliable.
- **Weaknesses:** Archive-only (not a full file manager), no browsing/network.

### 2.8 ES File Explorer
- **Overview:** The legendary file manager that defined the category. Removed from Play Store in 2019 for ad fraud (click fraud by parent company DO Global).
- **Key Features:** Dual-pane, cloud storage (all major), FTP/SMB/network, root access, archive support, app manager, built-in media players, download manager, remote manager.
- **Pricing:** Was free with ads; now discontinued
- **Ratings:** N/A (removed from Play Store)
- **Strengths:** Set the standard — most features ever in a single app.
- **Weaknesses:** Discontinued, privacy violations, bloated.

### 2.9 Root Explorer
- **Overview:** One of the oldest root-focused file managers. Simple, no-nonsense interface.
- **Key Features:** Root access, mount R/W, SQLite editor, text editor, create/extract ZIP/TAR/GZ, Google Drive support, SMB support, multi-select, search.
- **Pricing:** Paid (~$3.99)
- **Ratings:** ~4.3★ with 100K+ downloads
- **Strengths:** Root support, SQLite editor, simple interface.
- **Weaknesses:** Paid, dated UI, minimal features, no dual-pane.

### 2.10 X-plore File Manager
- **Overview:** Long-standing dual-pane file manager by Lonely Cat Games. Recently removed from Play Store.
- **Key Features:** Dual-pane, tree view, root access, cloud storage, SMB/FTP/WebDAV, archive support, WiFi file sharing, media player, text editor, hex viewer, SQLite database viewer.
- **Pricing:** Free with ads / Pro unlock
- **Ratings:** ~4.5★ with 10M+ downloads (before removal)
- **Strengths:** Dual-pane, WiFi sharing, SQLite viewer, extensive format support.
- **Weaknesses:** Dated UI, removed from Play Store, ads in free version.

---

## 3. Feature Comparison Matrix

### 3.1 Core File Operations

| Feature | Your Files | Files by Google | Solid Explorer | MiXplorer | MT Manager | Cx Explorer | Amaze | ZArchiver | X-plore |
|---|---|---|---|---|---|---|---|---|---|
| Browse folders | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| Copy files | ✅ | ⚠️ limited | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| Cut/Move files | ✅ | ⚠️ limited | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| Delete files | ✅ (trash) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| Rename files | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| Create folders | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| Multi-select | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |
| Select all | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |
| Range selection | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | N/A | ❌ |
| Search (in-folder) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| Global search | ❌ | ✅ | ✅ | ✅ | ✅ | ⚠️ | ✅ | ❌ | ✅ |
| Sort by name | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |
| Sort by size | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |
| Sort by date | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |
| Sort by type | ✅ (organiser) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |
| Group by type | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | N/A | ❌ |
| Breadcrumb nav | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |
| Clipboard (copy/cut) | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |
| Show hidden files | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |
| File details info | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| Folder caching (LRU) | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ | ⚠️ | N/A | ✅ |
| Long-press context menu | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |
| Drag & drop | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | N/A | ❌ |
| Bookmarks/Favorites | ❌ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | N/A | ✅ |

### 3.2 Advanced Features

| Feature | Your Files | Files by Google | Solid Explorer | MiXplorer | MT Manager | Cx Explorer | Amaze | ZArchiver | X-plore |
|---|---|---|---|---|---|---|---|---|---|
| Root access | ❌ | ❌ | ✅ | ✅ | ✅ | ⚠️ | ✅ | ❌ | ✅ |
| Cloud storage (Drive/Dropbox/OneDrive) | ❌ | ✅ (Drive only) | ✅ (all) | ✅ (20+) | ❌ | ✅ (all) | ✅ (4) | ❌ | ✅ |
| FTP / SFTP | ❌ | ❌ | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ | ✅ |
| SMB / LAN network | ❌ | ⚠️ NAS | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ | ✅ |
| WebDAV | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| WiFi file sharing (server) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| Create archives (ZIP/RAR/7z) | ❌ | ❌ | ✅ | ✅ | ✅ | ⚠️ | ⚠️ | ✅ | ✅ |
| Extract archives | ❌ | ❌ | ✅ | ✅ | ✅ | ⚠️ | ⚠️ | ✅ | ✅ |
| ZIP browsing (in-app) | ✅ | ❌ | ✅ | ✅ | ✅ | ⚠️ | ⚠️ | ✅ | ✅ |
| APK info viewer | ✅ | ❌ | ⚠️ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| APK install trigger | ✅ | ❌ | ⚠️ | ✅ | ✅ | ❌ | ❌ | ❌ | ⚠️ |
| APK editing (DEX/ARSC) | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Text editor | ⚠️ viewer only | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| SQLite viewer | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ✅ | ❌ | ✅ |
| Hex editor | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| Multi-tab browsing | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ | ⚠️ | N/A | ✅ |
| Dual-pane | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ | N/A | ✅ |
| Encrypt files (AES) | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| Hash calculator (MD5/SHA) | ✅ (partial MD5) | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Open with / Intent chooser | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |

### 3.3 Media & Viewing

| Feature | Your Files | Files by Google | Solid Explorer | MiXplorer | MT Manager | Cx Explorer | Amaze | ZArchiver | X-plore |
|---|---|---|---|---|---|---|---|---|---|
| Image viewer (in-app) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| Image viewer with swipe (pager) | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ⚠️ | ❌ | ✅ |
| Video player (in-app) | ✅ (ExoPlayer) | ⚠️ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| Audio player (in-app) | ✅ (ExoPlayer) | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| PDF viewer (in-app) | ✅ (PdfRenderer) | ❌ | ❌ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ |
| PDF pinch-to-zoom | ✅ | ❌ | ❌ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Text/code viewer (monospace) | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| ZIP archive browser | ✅ | ❌ | ✅ | ✅ | ✅ | ⚠️ | ⚠️ | ✅ | ✅ |
| Thumbnail generation | ✅ (Coil) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| Video thumbnail (Coil video frame) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| MediaStore category views | ✅ (5 categories) | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ | ✅ |
| File type icon mapping | ✅ (color-coded) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |

### 3.4 Tools & Utilities

| Feature | Your Files | Files by Google | Solid Explorer | MiXplorer | MT Manager | Cx Explorer | Amaze | ZArchiver | X-plore |
|---|---|---|---|---|---|---|---|---|---|
| Duplicate file finder | ✅ (MD5 hash) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Smart duplicate resolution (keep best) | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Large file finder | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Screenshot finder | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Storage analyzer (pie chart) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Storage category breakdown | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Top-largest-files list | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Recent files list | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Cleanup suggestions | ✅ (smart) | ✅ (basic) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Empty folder finder | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Image optimizer (JPEG compression) | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Space saved tracking | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| WhatsApp media cleaner | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Recycle bin (restore) | ✅ | ❌ | ⚠️ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Trash auto-cleanup (30 days) | ✅ | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A |
| Folder organizer (sort + group) | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

### 3.5 UI/UX Features

| Feature | Your Files | Files by Google | Solid Explorer | MiXplorer | MT Manager | Cx Explorer | Amaze | ZArchiver | X-plore |
|---|---|---|---|---|---|---|---|---|---|
| Material Design 3 | ✅ | ✅ (2) | ⚠️ (2) | ⚠️ | ❌ | ⚠️ | ⚠️ (2) | ❌ | ❌ |
| Dark mode (system follow) | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ✅ |
| Custom themes | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ✅ |
| Onboarding flow | ✅ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Shimmer loading placeholders | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Empty states | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |
| Loading indicators | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |
| Navigation drawer | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |
| SD card support | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |
| Storage card on home | ✅ (used/free) | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | N/A | ❌ |
| Compose-only (no XML) | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | N/A | ❌ |
| Zero-transition navigation | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | N/A | ❌ |
| Confirmation dialogs | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |
| Error handling states | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |

### 3.6 Security & Privacy

| Feature | Your Files | Files by Google | Solid Explorer | MiXplorer | MT Manager | Cx Explorer | Amaze | ZArchiver | X-plore |
|---|---|---|---|---|---|---|---|---|---|
| File encryption | ❌ | ❌ | ✅ (AES-256) | ❌ | ❌ | ❌ | ❌ | ✅ (archives) | ❌ |
| Hidden files visibility | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |
| App lock / biometric | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| No ads | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ❌ |
| No tracking / analytics | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| All processing on-device | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Privacy policy page | ✅ | ✅ | ✅ | N/A | ❌ | ❌ | ✅ | ✅ | ❌ |
| Open source | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ⚠️ | ❌ | ❌ |
| Permission request screen | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | N/A | ✅ |

---

## 4. Your Files — Complete Feature Inventory

### 4.1 Screens / Pages (17 routes)

| Route | Screen | File Reference | Description |
|---|---|---|---|
| `/home` | ESHomeScreen | `pages/ESHomeScreen.kt` | Dashboard with storage cards, category icons, tool tiles |
| `/explorer` | FileBrowserScreen | `pages/FileBrowserScreen.kt` | Full file browser with search, multi-select, clipboard, breadcrumbs |
| `/media-category/{type}` | MediaStoreCategoryScreen | `pages/MediaStoreCategoryScreen.kt` | Flat list of files by type (Images, Videos, Audio, Documents, APKs) |
| `/flat-duplicates-file-manager` | FlatFileManager | `pages/FlatDuplicatesFileManagerPage.kt` | Duplicate file detector with smart selection |
| `/flat-images-file-manager` | FlatImagesFileManager | `pages/FlatImagesFileManagerPage.kt` | All images flat list with selection |
| `/flat-videos-file-manager` | FlatVideosFileManager | `pages/FlatVideosFileManagerPage.kt` | All videos flat list with selection |
| `/flat-large-file-manager` | FlatLargeFilesManager | `pages/FlatLargeFilesManager.kt` | Large files (5MB+) flat list with selection |
| `/flat-screenshots-file-manager` | FlatScreenshotsFileManager | `pages/FlatScreenshotsFileManagerPage.kt` | Screenshots flat list with selection |
| `/flat-whatsapp-file-manager` | WhatsAppCleanerPage | `pages/WhatsAppCleanerPage.kt` | WhatsApp media cleaner (Images, Videos, Documents, Voice Notes) |
| `/analyzer` | StorageAnalyzerScreen | `pages/StorageAnalyzerScreen.kt` | Storage usage analysis with charts, top files, cleanup suggestions |
| `/optimise-images` | ImageOptimiserPage | `pages/ImageOptimiserPage.kt` | JPEG image optimizer with quality compression |
| `/trash` | TrashPage | `pages/TrashPage.kt` | Recycle bin with restore and empty |
| `/settings` | SettingsPage | `pages/SettingsPage.kt` | Settings: confirm before delete, dark mode, about, privacy |
| `/onboarding` | OnboardingPage | `pages/OnboardingPage.kt` | Multi-page onboarding flow |
| `/file-detail-viewer` | FileDetailViewerPage | `pages/FileDetailViewerPage.kt` | Universal file viewer (image/video/audio/text/PDF/ZIP/APK/system) |
| `/folder-organiser` | FolderOrganiserScreen | `pages/FolderOrganiserScreen.kt` | Folder view with sort options and type grouping |
| N/A | PermissionRequiredPage | `pages/PermissionRequiredPage.kt` | Permission request screen |

### 4.2 ViewModels (11)

| ViewModel | File | Responsibilities |
|---|---|---|
| FileExplorerViewModel | `vm/FileExplorerViewModel.kt` | Folder navigation, folder caching (LRU, 20 entries), multi-select with range selection, copy/cut/paste clipboard, delete-to-trash, rename, create folder, search, breadcrumbs |
| MediaStoreCategoryVM | `vm/MediaStoreCategoryVM.kt` | MediaStore queries for 5 category types (Images, Videos, Audio, Documents, APKs) |
| StorageAnalyzerVM | `vm/StorageAnalyzerVM.kt` | Full filesystem scan, category breakdown (7 types), top-20 largest files, recent files (7 days), cleanup suggestions, empty folder detection, progress reporting, 1-hour result cache |
| FolderOrganiserViewModel | `vm/FolderOrganiserViewModel.kt` | Folder content loading, 6 sort options (name, size, date asc/desc), group-by-type toggle |
| FlatDuplicatesFileManagerVM | `vm/FlatDuplicatesFileManagerVM.kt` | Duplicate detection via MD5 hash matching |
| FlatImagesFileManagerVM | `vm/FlatImagesFileManagerVM.kt` | All image files listing with selection |
| FlatVideosFileManagerVM | `vm/FlatVideosFileManagerVM.kt` | All video files listing with selection |
| FlatLargeFileManagerVM | `vm/FlatLargeFileManagerVM.kt` | Large file listing (5MB+) with selection |
| FlatScreenshotsFileManagerVM | `vm/FlatScreenshotsFileManagerVM.kt` | Screenshot detection and listing |
| ImageOptimiserVM | `vm/ImageOptimiserVM.kt` | JPEG optimization pipeline with progress tracking |
| FileDetailViewerVM | `vm/FileDetailViewerVM.kt` | File loading by category or MD5, file info display, delete confirmation |
| SelectableDeletableVM | `vm/SelectableDeletableVM.kt` | Base VM for select-and-delete workflow (shared by flat managers) |

### 4.3 Use Cases / Interactors (3)

| Interactor | File | Operations |
|---|---|---|
| FileUseCases | `domain/interactors/FileUseCases.kt` | `countFiles()`, `syncAllFilesToDb()`, `getFileById()`, `deleteFile()`, `deleteFiles()`, `getMediaFiles()`, `getLargeFiles()`, `getVideoFiles()`, `getImageFiles()`, `getDuplicateMediaFiles()`, `getDuplicateCopies()`, `getScreenshotFiles()`, `getOptimizableImages()`, `getFilesByMd5()`, `getFileInfo()` |
| HomeUseCases | `domain/interactors/HomeUseCases.kt` | `getAnyThreeDuplicateGroups()`, `getVideoFile()`, `getImageFiles()`, `getLargeFiles()`, `getTotalSizeOfMimeType()`, `getTotalSizeOfDuplicates()`, `getDuplicatesCount()`, `getImagesCount()`, `getVideosCount()`, `getLargeFilesCount()`, `getScreenshotsCount()`, `getScreenshotsTotalSize()`, `getOptimizableImagesCount()`, `getOptimizableImagesTotalSize()`, preview methods |
| BestFileSelector | `domain/interactors/BestFileSelector.kt` | Smart duplicate resolution: scores files by location (DCIM +10, Downloads +8, Pictures +5), date (oldest +5), size (largest +3), tiebreaks by shortest path. Selects best to keep, marks rest for deletion |

### 4.4 Utilities (7)

| Utility | File | Purpose |
|---|---|---|
| TrashManager | `utils/TrashManager.kt` | Move files to trash (`/Android/data/.../files/Trash/`), restore individual files, undo last trash operation, auto-cleanup after 30 days, persist records in SharedPreferences (JSON), trash state Flow for reactive UI, empty trash |
| ImageOptimizer | `utils/ImageOptimizer.kt` | JPEG compression at 80% quality, EXIF marker to prevent re-optimization, OOM-safe, estimated savings calculator |
| StorageHelper | `utils/StorageHelper.kt` | Storage volume detection (internal + SD card), total/used/free space calculation, multi-volume support (API 24+ StorageManager, legacy fallback) |
| SavedMemoryTracker | `utils/SavedMemoryTracker.kt` | Track total bytes saved via image optimization, persisted in SharedPreferences, reactive StateFlow |
| FileUtil | `utils/FileUtil.kt` | MIME type detection (50+ extensions), MD5 hash (full file), partial MD5 (head + tail, 4KB each), file type helpers (isImage, isVideo, isAudio, isPdf, isArchive, isApk, isOffice, isFileCode, isFileText), optimized image detection via EXIF |
| UIUtil | `utils/UIUtil.kt` | UI helpers |
| DebugUtil | `utils/DebugUtil.kt` | Debug logging utilities |

### 4.5 Background Workers (2)

| Worker | File | Purpose |
|---|---|---|
| ReadFileWorker (Syncer) | `helper/Syncer.kt` | WorkManager foreground service. Scans all storage volumes, traverses directories recursively, computes partial MD5 for each file, inserts into Room DB in batches via Channel. Shows notification with progress. |
| UpdateChecksumWorker | `helper/UpdateChecksumWorker.kt` | Background worker that computes full MD5 checksums for files that only have partial hashes. Processes in batches of 50, uses parallel coroutines, Room transactions. |

### 4.6 Data Layer

| Component | File | Purpose |
|---|---|---|
| AppDatabase | `data/db/AppDatabase.kt` | Room database (`yourfiles-database`) with fallback to destructive migration |
| LocalFilesDao | `data/db/dao/LocalFilesDao.kt` | DAO with raw query support, file CRUD, duplicate detection queries, optimization tracking |
| LocalFile | `data/model/LocalFile.kt` | Room entity: id (path), fileName, fileType (MIME), size, modifiedTime, md5CheckSum, isOptimised |
| LocalFilesRepo | `domain/repository/LocalFilesRepo.kt` | Repository interface |
| LocalFilesRepoImpl | `data/repository/LocalFilesRepoImpl.kt` | Repository implementation with Room |

### 4.7 UI Components

| Component | File | Purpose |
|---|---|---|
| ESDrawerContent | `components/DrawerContent.kt` | Navigation drawer: Home, Internal Storage, Downloads, Images, Videos, Documents, Music, APKs, Cleaner, Analyzer, Optimiser, Recycle Bin, Settings |
| ImageViewer | `components/common/ImageViewer.kt` | Zoomable, pannable image viewer |
| VideoPlayer | `components/common/VideoPlayer.kt` | In-app video player |
| FileThumbnailCompose | `components/common/thumbnail/FileThumbnailCompose.kt` | Auto-selects correct thumbnail (image/video/other) |
| ImageThumbnailCompose | `components/common/thumbnail/ImageThumbnailCompose.kt` | Image-specific thumbnail with Coil |
| VideoThumbnailCompose | `components/common/thumbnail/VideoThumbnailCompose.kt` | Video frame thumbnail with Coil |
| OtherFileThumbnailCompose | `components/common/thumbnail/OtherFileThumbnailCompose.kt` | Icon-based thumbnail for non-media files |
| SelectableFileItem | `components/SelectableFileItem.kt` | Grid item with checkbox for selection |
| FileItemCompose | `components/common/FileItemCompose.kt` | File list item with icon, name, size, date |
| EmptyStateCompose | `components/common/EmptyStateCompose.kt` | Empty state placeholder |
| PopupCompose | `components/common/PopupCompose.kt` | Modal popup wrapper |
| BackNavigationIconCompose | `components/common/BackNavigationIconCompose.kt` | Back arrow icon button |
| FlatFileManagerContent | `components/common/flatFileManager/FlatFileManagerContent.kt` | Shared content composable for flat file manager pages |
| FlatFileManagerDeleteComposable | `components/common/flatFileManager/FlatFileManagerDeleteComposable.kt` | FAB delete button with size display |

### 4.8 Complete Feature List (by Category)

#### File Operations
- ✅ Browse folders with folder caching (LRU, 20 entries)
- ✅ Navigate into folders, navigate up, breadcrumb navigation
- ✅ Copy to clipboard, Cut to clipboard, Paste (with folder invalidation)
- ✅ Delete to trash (with TrashManager persistence)
- ✅ Rename files and folders
- ✅ Create new folders
- ✅ Search within current folder (case-insensitive)
- ✅ Multi-select with tap
- ✅ Select all in current view
- ✅ Range select (interval mode: first tap, then second tap selects all in between)
- ✅ Long-press context menu (share, info, open, delete, cut, copy, rename)
- ✅ File properties display (name, path, size, type, modified date, child count)
- ✅ Folders-first sorting, alphabetical within each group
- ✅ Folder child count computed asynchronously (non-blocking)

#### Media Viewers
- ✅ In-app image viewer with zoom/pan (ImageViewer)
- ✅ In-app video player (VideoPlayer, ExoPlayer)
- ✅ In-app audio player with seek bar, play/pause, rewind/forward 10s (AudioPlayerScreen, ExoPlayer)
- ✅ In-app PDF viewer with swipe navigation, pinch-to-zoom, double-tap reset, page cache (PdfViewerScreen, PdfRenderer)
- ✅ In-app text/code viewer with monospace font, horizontal scroll, 500KB initial limit with "Load full file" (TextViewerScreen)
- ✅ In-app ZIP archive browser (entries, sizes, dates, directory tree)
- ✅ APK info viewer (package name, version, SDK, icon, size, install button)
- ✅ HorizontalPager for swiping between files in category views

#### Smart Cleaning Tools
- ✅ Duplicate file finder (MD5-based, partial hash for speed)
- ✅ Smart "best file" selection algorithm (scores by location, date, size)
- ✅ Large file finder (>5MB)
- ✅ Screenshot finder (path-based detection)
- ✅ Image optimizer (JPEG compression at 80% quality, EXIF marker, OOM-safe)
- ✅ Total space saved tracker (persistent across restarts)
- ✅ WhatsApp media cleaner (4 categories: Images, Videos, Documents, Voice Notes)
- ✅ WhatsApp recursive directory scanning
- ✅ Storage analyzer with donut chart visualization
- ✅ Storage category breakdown (Images, Videos, Audio, Documents, APKs, Archives, Other)
- ✅ Top-20 largest files list
- ✅ Recent files list (modified in last 7 days)
- ✅ Cleanup suggestions (old downloads, large APKs, empty folders)
- ✅ Empty folder detection

#### Recycle Bin
- ✅ Move to trash (preserves original path)
- ✅ Restore individual files to original location
- ✅ Undo last trash operation
- ✅ Auto-cleanup after 30 days
- ✅ Empty trash (permanent delete all)
- ✅ Trash state reactive Flow for UI updates
- ✅ Original path reconstruction fallback

#### Settings
- ✅ Confirm before delete toggle
- ✅ Dark mode (follows system)
- ✅ Privacy policy (no data collected, on-device)
- ✅ About section (app version, suite info)
- ✅ Version display

#### UI/UX
- ✅ Material Design 3 theme
- ✅ Jetpack Compose (zero XML layouts)
- ✅ Zero-transition navigation (prevents ghost overlay)
- ✅ Shimmer loading placeholders (image optimizer)
- ✅ Empty states for all screens
- ✅ Loading spinners (circular progress indicators)
- ✅ Confirmation dialogs (delete, restore, empty trash, optimize)
- ✅ Error states with retry buttons
- ✅ Navigation drawer with categorized sections
- ✅ Storage cards on home screen (internal + SD card)
- ✅ Category quick-access icons (Images, Videos, Documents, Music, APKs)
- ✅ Tool grid (Cleaner, Analyzer, Optimise, Recycle Bin)
- ✅ Color-coded file type icons
- ✅ Coil image loading with memory + disk cache
- ✅ Video frame thumbnails (VideoFrameDecoder)
- ✅ Onboarding flow (multi-page with illustrations)
- ✅ Permission request screen with lifecycle-aware refresh
- ✅ FileProvider for safe URI sharing (APK install, open with)
- ✅ Orientation-responsive grid layout (3 cols portrait, 6 cols landscape)

---

## 5. Gap Analysis — What We're Missing

### Tier 1: Must-Have Before Launch (High User Demand)

| Priority | Feature | User Demand | Effort | Notes |
|---|---|---|---|---|
| 🔴 P0 | Show hidden files toggle | Very High | Low | Settings option + filter in FileExplorerViewModel |
| 🔴 P0 | Bookmarks / Favorite folders | High | Medium | Add to drawer, persist in SharedPreferences |
| 🔴 P0 | Compress to ZIP | High | Medium | Use java.util.zip or Apache Commons Compress |
| 🔴 P0 | Extract archives (ZIP, RAR, 7z) | High | Medium | Use library (e.g., Apache Commons Compress) |
| 🔴 P0 | Global search (across all files) | High | Low | Room DB already has all files indexed — query from there |

### Tier 2: Important for Competitiveness

| Priority | Feature | User Demand | Effort | Notes |
|---|---|---|---|---|
| 🟡 P1 | Cloud storage (Google Drive, Dropbox, OneDrive) | Very High | High | Major differentiator; consider Google Drive API first |
| 🟡 P1 | SMB/LAN network share browsing | Medium | High | jcifs or Android SMB client library |
| 🟡 P1 | FTP/SFTP support | Medium | High | Apache Commons Net or JSch |
| 🟡 P1 | Root access (optional) | Medium | High | Su binary execution, mount points |
| 🟡 P1 | Multi-tab browsing | Medium | Medium | Tab bar with SavedStateHandle per tab |
| 🟡 P1 | Text editor (not just viewer) | Medium | Medium | Add edit capability to TextViewerScreen |
| 🟡 P1 | Grid/List view toggle | Medium | Low | Add view mode preference in file browser |
| 🟡 P1 | File size column toggle | Low | Low | Show/hide size in list items |

### Tier 3: Nice-to-Have

| Priority | Feature | User Demand | Effort | Notes |
|---|---|---|---|---|
| 🟢 P2 | Dual-pane mode | Medium | High | Tablet-centric feature |
| 🟢 P2 | AES file encryption | Low | Medium | javax.crypto + file streaming |
| 🟢 P2 | App lock / biometric | Low | Medium | BiometricPrompt API |
| 🟢 P2 | WiFi file sharing server | Low | Medium | NanoHTTPD embedded server |
| 🟢 P2 | Custom color themes | Low | Medium | Extend Theme.kt with accent color options |
| 🟢 P2 | SQLite database viewer | Low | Medium | Room/SQLite asset helper |
| 🟢 P2 | Drag & drop (touch) | Low | High | Complex Compose gesture handling |
| 🟢 P2 | APK editing (DEX/manifest) | Very Low (niche) | Very High | MT Manager territory |
| 🟢 P2 | Hex editor | Very Low (niche) | Very High | MT Manager territory |

### Tier 4: Future Roadmap

| Priority | Feature | User Demand | Effort | Notes |
|---|---|---|---|---|
| 🔵 P3 | SMB server mode | Low | Medium | Make device discoverable on LAN |
| 🔵 P3 | Cloud sync (two-way) | Low | Very High | Complex conflict resolution |
| 🔵 P3 | Chromecast / DLNA streaming | Low | High | Media casting from file manager |
| 🔵 P3 | Open source release | Low | Medium | Code cleanup, documentation, CI/CD |
| 🔵 P3 | Accessibility improvements | Medium | Medium | TalkBack support, larger touch targets |
| 🔵 P3 | Internationalization (i18n) | Medium | Medium | String resources extraction |

---

## 6. What We Do Better

### Features Where Your Files Excels Beyond ALL Competitors

| Feature | Detail | Why It Matters |
|---|---|---|
| **Smart Duplicate Resolution** | `BestFileSelector` scores files by location (DCIM +10, Downloads +8), date (oldest +5), size (largest +3) to automatically keep the best copy | No other competitor does this — they show duplicates and let users guess which to keep |
| **WhatsApp Media Cleaner** | Dedicated page with 4 categories (Images, Videos, Documents, Voice Notes), recursive scanning, grid thumbnails, selection + delete | Only a few standalone WhatsApp cleaner apps exist; no other file manager bundles this |
| **Image Optimizer with Savings Tracking** | JPEG compression at 80% quality, EXIF marker to prevent re-processing, persistent bytes-saved counter | No competitor offers built-in image optimization |
| **Storage Analyzer with Smart Cleanup Suggestions** | Full filesystem scan → donut chart → category breakdown → top-20 largest files → recent files → actionable cleanup cards | Files by Google has basic suggestions but nothing this detailed |
| **Recycle Bin with Restore + Auto-Cleanup** | 30-day retention, original path persistence in SharedPreferences, undo support, restore per file | Most competitors don't have a recycle bin at all |
| **Modern Architecture** | Pure Jetpack Compose, Material Design 3, MVVM, Room DB, WorkManager, Coroutines/Flow — zero legacy code | MiXplorer, MT Manager, X-plore still use Activities + XML; Solid Explorer is dated |
| **Privacy-First, No Ads, No Tracking** | All processing on-device, zero network calls, no analytics, no data collection | Amaze is the only other competitor that's fully privacy-respecting (open source), but has fewer features |
| **Built-in PDF Viewer** | Native PdfRenderer with page cache, pinch-to-zoom, double-tap reset, swipe navigation | No file manager competitor has this — they all open PDFs externally |
| **Built-in Audio Player** | ExoPlayer-based with seek bar, play/pause, rewind/forward, auto-release | Very few competitors include this |
| **Video Thumbnail Generation** | Coil + VideoFrameDecoder for video thumbnails without external libraries | Clean implementation that competitors often struggle with |
| **Partial MD5 for Fast Duplicate Detection** | Hash first 4KB + last 4KB + size for O(1) duplicate grouping without reading entire files | Innovative approach — competitors either do full hash (slow) or don't detect duplicates at all |

### Unique Combinations Nobody Else Offers

**Your Files** is the **only** Android file manager that combines:
1. Duplicate finder + smart best-file selection
2. WhatsApp cleaner
3. Image optimizer
4. Storage analyzer with cleanup suggestions
5. Recycle bin with restore
6. Built-in PDF viewer + audio player + text viewer + ZIP browser
7. Material Design 3 + fully offline + no ads

This is a compelling **"smart cleaner + file manager + media viewer"** package that no single competitor offers.

---

## 7. Feature Priority Recommendations

### Tier 1: Must-Have Before Launch

These are essential features that users expect in any file manager:

1. **Show hidden files** — Add a toggle in FileExplorerViewModel + Settings. ~2 hours.
2. **Bookmarks / Favorites** — Add a pinned folders section to the drawer. ~4 hours.
3. **Compress to ZIP** — Basic ZIP creation from selected files. ~6 hours.
4. **Extract archives** — At minimum ZIP extraction; RAR/7z via library. ~8 hours.
5. **Global search** — Query Room DB (already indexed). ~4 hours.
6. **Grid/List view toggle** — Add preference in file browser. ~3 hours.

**Total estimated effort: ~27 hours (~3.5 work days)**

### Tier 2: Important for Competitiveness

These differentiate from mid-tier competitors:

1. **Cloud storage (Google Drive)** — Most requested feature after core ops. ~40 hours.
2. **SMB/LAN browsing** — Enterprise and home network users. ~30 hours.
3. **FTP/SFTP** — Power users. ~25 hours.
4. **Text editor** — Not just viewer. ~15 hours.
5. **Multi-tab browsing** — ~20 hours.
6. **Root access (optional)** — ~20 hours.

**Total estimated effort: ~150 hours (~19 work days)**

### Tier 3: Nice-to-Have

Polish and niche features:

1. **Dual-pane** — Great for tablets. ~30 hours.
2. **AES encryption** — Security differentiator. ~15 hours.
3. **App lock (biometric)** — ~10 hours.
4. **WiFi sharing server** — ~15 hours.
5. **Custom themes** — ~15 hours.

**Total estimated effort: ~85 hours (~11 work days)**

### Tier 4: Future Roadmap

1. **Open source** — Long-term trust builder.
2. **i18n** — Market expansion.
3. **Accessibility** — Compliance and reach.
4. **Cloud sync** — Complex but highly valuable.

---

## 8. Market Positioning Strategy

### Recommended Position: **"The Smart Cleaner File Manager"**

Your Files should NOT compete head-to-head with MiXplorer or Solid Explorer on features like FTP, root, or dual-pane. Instead, it should own a unique niche:

> **Your Files — Clean up your phone, find duplicates, optimize images, browse files, and view media — all in one app. No ads, no tracking, 100% offline.**

### Key Positioning Pillars

| Pillar | Messaging | Evidence |
|---|---|---|
| **Smart Cleaning** | "Automatically find and clean duplicates, old downloads, and WhatsApp junk" | Duplicate finder + BestFileSelector + WhatsApp cleaner + cleanup suggestions |
| **Built-in Media Suite** | "View images, videos, PDFs, listen to music — no external apps needed" | ImageViewer + VideoPlayer + AudioPlayer + PdfViewer + TextViewer + ZipBrowser |
| **Privacy First** | "No ads, no tracking, no internet required. Everything stays on your device." | Zero network calls, on-device processing, privacy policy |
| **Modern Design** | "Material Design 3, dark mode, smooth animations" | Pure Compose, MD3 theme, shimmer states, zero-flash navigation |
| **One-Tap Space Recovery** | "Optimize JPEGs, clean trash, find large files — see exactly how much space you saved" | ImageOptimizer + SavedMemoryTracker + StorageAnalyzer |

### Target Audience

1. **Mainstream Android users** who want a clean, ad-free file manager
2. **Storage-conscious users** who are running out of space and need smart cleanup
3. **WhatsApp-heavy users** who accumulate media and need to clean it
4. **Privacy-conscious users** who distrust ad-supported apps like ES/Cx

### Competitive Angles

| Against | Our Advantage |
|---|---|
| **Files by Google** | Better file operations (clipboard, multi-select), duplicate finder, WhatsApp cleaner, image optimizer, PDF viewer |
| **Solid Explorer** | Free, no ads, built-in cleaning tools, modern MD3 UI, PDF viewer, no network required |
| **MiXplorer** | Modern Compose UI, duplicate finder, WhatsApp cleaner, image optimizer, simpler UX |
| **Cx File Explorer** | Duplicate finder, WhatsApp cleaner, image optimizer, built-in media viewers, cleaner UI |
| **Amaze** | Better cleaning tools, PDF viewer, audio player, more modern UI |
| **ES File Explorer** | No privacy violations, no bloat, ad-free, modern architecture |

---

## Appendix A: Detailed Competitor Notes

### Files by Google (Google LLC)
- **Monetization:** Free, no ads, no in-app purchases
- **Downloads:** ~5B+ (pre-installed on many devices)
- **Rating:** 4.5★
- **Last updated:** Actively maintained (2025)
- **Note:** Simple and reliable but limited. No clipboard operations, no network browsing, no archive support. Focuses on cleanup suggestions and sharing.

### Solid Explorer (Krystian Guliński)
- **Monetization:** $2.99 one-time purchase, 14-day free trial
- **Downloads:** ~1M+
- **Rating:** 4.4★
- **Last updated:** Actively maintained (2025)
- **Note:** Strong encryption, cloud, and network support. Dual-pane UI is praised but criticized as dated. No cleaning tools.

### MiXplorer (Hootan Parsa)
- **Monetization:** Free via XDA Forums; ~$4.99 on Play Store
- **Downloads:** ~10K (Play Store), millions via XDA
- **Rating:** 4.7★ (Play Store)
- **Last updated:** Actively maintained (2025)
- **Note:** The most feature-complete file manager ever built for Android. Supports 20+ cloud providers, all archive formats, root, hex editing, SQLite viewing. Overwhelming for casual users.

### MT Manager (Lin Jin Bin)
- **Monetization:** Free with ads; VIP unlock available
- **Downloads:** ~10M+
- **Rating:** 4.6★
- **Last updated:** Actively maintained (2025)
- **Note:** Unique APK editing capabilities (DEX, ARSC, smali) make it irreplaceable for modding community. Dual-pane. Chinese-first UI.

### Cx File Explorer (Cxinventor)
- **Monetization:** Free, no ads
- **Downloads:** ~10M+
- **Rating:** 4.5★
- **Last updated:** Actively maintained (2025)
- **Note:** Best free alternative with cloud + network support. Clean UI. Frequently recommended on Reddit as "the free Solid Explorer."

### Amaze File Manager (Team Amaze / Vishal Nehra)
- **Monetization:** Free, open source (GPLv3)
- **Downloads:** ~5M+
- **Rating:** 4.2★
- **Last updated:** Sporadically maintained (2025)
- **Note:** Privacy-focused, open source, but F-Droid version hasn't been updated since 2017. Play Store version reportedly no longer fully open source.

### ZArchiver (ZDevs)
- **Monetization:** Free, no ads (donation supported)
- **Downloads:** ~50M+
- **Rating:** 4.7★
- **Last updated:** Actively maintained (2025)
- **Note:** The gold standard for archive management on Android. Supports virtually every archive format including password-protected and multi-part archives.

### ES File Explorer (DO Global — discontinued)
- **Monetization:** Was free with ads + in-app purchases
- **Status:** Removed from Google Play Store in April 2019
- **Note:** The app that defined the category. Had the most comprehensive feature set of any file manager. Removed for click fraud violations by parent company DO Global.

### Root Explorer (Speed Software)
- **Monetization:** $3.99
- **Downloads:** ~100K+
- **Rating:** 4.3★
- **Last updated:** Sporadically maintained
- **Note:** One of the oldest file managers (since 2010). Simple root-focused tool. SQLite editor is a notable feature.

### X-plore File Manager (Lonely Cat Games)
- **Monetization:** Free with ads / Pro unlock
- **Downloads:** ~10M+ (before removal)
- **Status:** Reportedly removed from Google Play Store in 2025
- **Note:** Long-standing dual-pane explorer with WiFi sharing, SQLite viewer, and hex viewer. Dated UI but comprehensive features.

---

## Appendix B: Source File References

### Project Structure

```
com.yourfiles.manager/
├── app/
│   ├── App.kt                          # Application class, DB init, image loader
│   ├── Router.kt                       # Navigation graph, 17 routes
│   └── Constants.kt                    # App-wide constants
│   └── uim3/theme/
│       ├── Theme.kt                    # Material Design 3 theme
│       ├── Color.kt                    # Color definitions
│       └── Type.kt                     # Typography definitions
├── data/
│   ├── model/LocalFile.kt             # Room entity
│   ├── db/
│   │   ├── AppDatabase.kt             # Room database
│   │   └── dao/LocalFilesDao.kt       # Data access object
│   └── repository/LocalFilesRepoImpl.kt
├── domain/
│   ├── model/FileItem.kt              # Presentation model
│   ├── repository/LocalFilesRepo.kt   # Repository interface
│   └── interactors/
│       ├── FileUseCases.kt            # Core file operations
│       ├── HomeUseCases.kt            # Home screen data queries
│       └── BestFileSelector.kt        # Smart duplicate resolution
├── helper/
│   ├── Syncer.kt                      # Background file scanner worker
│   └── UpdateChecksumWorker.kt        # Background MD5 updater
├── presentation/
│   ├── WorkerUIState.kt               # Shared UI state types
│   ├── vm/                            # 11 ViewModels
│   ├── ui/
│   │   ├── MainActivity.kt            # Entry point
│   │   ├── components/
│   │   │   ├── DrawerContent.kt       # Navigation drawer
│   │   │   ├── Constants.kt
│   │   │   └── common/                 # Shared UI components (14 files)
│   │   └── pages/                     # 17 screen composables
└── utils/                              # 7 utility files
    ├── TrashManager.kt
    ├── ImageOptimizer.kt
    ├── StorageHelper.kt
    ├── SavedMemoryTracker.kt
    ├── FileUtil.kt
    ├── UIUtil.kt
    └── DebugUtil.kt
```

### Key Architectural Decisions

| Decision | Implementation | File |
|---|---|---|
| Pure Jetpack Compose | No Activities, no XML layouts — all screens are @Composable functions | All `pages/*.kt` |
| Material Design 3 | Latest Material 3 components (TopAppBar, ModalNavigationDrawer, FilterChip) | `app/uim3/theme/Theme.kt` |
| Zero-transition navigation | EnterTransition.None / ExitTransition.None prevents ghost overlay | `App.kt` |
| LRU folder cache | 20-entry cache for instant back-navigation | `FileExplorerViewModel.kt` |
| Partial MD5 hashing | Head (4KB) + tail (4KB) + size for O(1) duplicate detection | `FileUtil.kt:partialMd5()` |
| Channel-based DB sync | Producer-consumer pattern for non-blocking Room inserts | `FileUseCases.kt:syncAllFilesToDb()` |
| WorkManager foreground service | File scanning with notification for API <31 | `Syncer.kt` |
| SharedPreferences for trash | JSON-serialized records for cross-restart persistence | `TrashManager.kt` |
| Coil + VideoFrameDecoder | Image + video thumbnails in a single image pipeline | `App.kt:initLibraries()` |
| Reactive StateFlow everywhere | Trash info, saved memory, DB queries — all Flow-based | Multiple files |
| FileProvider | Safe URI sharing for APK install and "Open with" | `FileDetailViewerPage.kt` |

---

*End of Report. Generated June 2025.*
