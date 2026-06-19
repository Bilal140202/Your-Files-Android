# Research: Phase 5 — Dead Code Cleanup & Security

## Research Date: 2026-06-19
## Status: COMPLETE

---

## 1. Dead Code Verification Commands

These commands MUST be run during Phase 5 implementation before deleting anything:

```bash
# Legacy theme
rg "com\.yourfiles\.manager\.app\.ui\." --type kotlin
# Expected: zero results (if nothing references the old theme package)

# HomePage
rg "HomePage|HomeComposable" --type kotlin
# Expected: only results in the file itself (not in Router.kt or elsewhere)

# ScanningComposable
rg "ScanningComposable" --type kotlin
# Expected: only in HomePage.kt

# ScanResultComposable
rg "ScanResultComposable" --type kotlin
# Expected: only in HomePage.kt

# HomeVM
rg "HomeVM" --type kotlin
# Expected: only in HomePage.kt

# WorkerUIState
rg "WorkerUIState" --type kotlin
# Expected: only in HomeVM.kt and HomePage.kt

# StorageUiState
rg "StorageUiState" --type kotlin
# Expected: only in HomeVM.kt

# ncdl
rg "ncdl" --type kotlin --type java
# Expected: zero results

# activity_main.xml
rg "activity_main" --type xml --type kotlin --type java
# Expected: only in AndroidManifest.xml (if referenced) or zero

# connect_account_fragment
rg "connect_account_fragment" --type xml --type kotlin
# Expected: zero results

# app_navigation.xml
rg "app_navigation" --type xml --type kotlin
# Expected: zero results

# Glide
rg "glide|Glide|GlideImage" --type kotlin -i
# Expected: zero results (verify Coil-only usage)

# MediaItem
rg "MediaItem" --type kotlin
# Expected: verify if used by any screen

# UpdateChecksumWorker
rg "UpdateChecksumWorker" --type kotlin
# Expected: verify if scheduled or used
```

---

## 2. MainActivity.kt Analysis

Need to verify: does `MainActivity.kt` use `activity_main.xml` layout?

From the codebase, `MainActivity.kt` is a Compose activity. It likely uses:
```kotlin
setContent { YourFilesApp() }
```

If it uses `setContentView(R.layout.activity_main)` → we need to keep the XML.
If it uses `setContent {}` → we can delete `activity_main.xml`.

### AndroidManifest.xml Activity Declaration
```xml
<activity
    android:name=".presentation.ui.MainActivity"
    android:exported="true"
    android:theme="@style/Theme.YourFiles">
```

No `android:layout` attribute → activity doesn't specify a layout in manifest.

---

## 3. Dependency Audit

### Build Dependencies (from app/build.gradle)

| Dependency | Used By | Verdict |
|-----------|---------|---------|
| Room 2.8.4 | AppDatabase, DAOs | KEEP |
| Compose BOM 2025.12.00 | Entire UI | KEEP |
| Navigation Compose 2.9.6 | Router.kt | KEEP |
| Coil 3.3.0 (+ video) | Thumbnails, AsyncImage | KEEP |
| Glide 5.0.5 | ???? | VERIFY → likely REMOVE |
| ExoPlayer 2.19.1 | VideoPlayer.kt | KEEP |
| Work Manager 2.11.0 | ReadFileWorker (initial scan) | KEEP |
| Paging 3 3.3.6 | ???? | VERIFY → likely REMOVE |
| Lifecycle 2.10.0 | VMs, Compose | KEEP |
| Coroutines 1.10.2 | Everything | KEEP |
| Core KTX, Activity Compose | Standard | KEEP |
| Material 3 | UI | KEEP |
| DataStore | ???? | VERIFY |

### Glide Verification
```bash
rg "import com\.bumptech\.glide|import github\.bumptech\.glide" --type kotlin
```
If zero results → REMOVE.

### Paging Verification
```bash
rg "import androidx\.paging|Pager|PagingData|LazyPagingItems" --type kotlin
```
If zero results → REMOVE.

---

## 4. Signing Credentials Migration

### Current (app/build.gradle lines ~25-29)
```groovy
signingConfigs {
    release {
        storeFile file('yourfiles-release.jks')
        storePassword 'yourfiles_2024'
        keyAlias 'yourfiles'
        keyPassword 'yourfiles_2024'
    }
}
```

### Target
```groovy
def keystoreProps = new Properties()
def propsFile = rootProject.file("local.properties")
if (propsFile.exists()) {
    keystoreProps.load(new FileInputStream(propsFile))
}

signingConfigs {
    release {
        storeFile file(keystoreProps['KEYSTORE_FILE'] ?: 'yourfiles-release.jks')
        storePassword keystoreProps['KEYSTORE_PASSWORD'] ?: ''
        keyAlias keystoreProps['KEY_ALIAS'] ?: ''
        keyPassword keystoreProps['KEY_PASSWORD'] ?: ''
    }
}
```

### local.properties Addition
```properties
KEYSTORE_FILE=yourfiles-release.jks
KEYSTORE_PASSWORD=yourfiles_2024
KEY_ALIAS=yourfiles
KEY_PASSWORD=yourfiles_2024
```

### Verify .gitignore
```bash
rg "local.properties" .gitignore
```
Must be present (it is in standard Android .gitignore).

---

## 5. ProGuard Rules

### Room
```proguard
-keep class com.yourfiles.manager.data.model.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
```

### Coil
```proguard
-dontwarn coil3.**
```

### ExoPlayer
```proguard
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**
```

### Gson (if used for favorites/any JSON)
```proguard
-keep class com.yourfiles.manager.utils.** { *; }
-keepattributes Signature
```

### General Android
```proguard
-keepattributes *Annotation*
-keep class kotlinx.coroutines.** { *; }
```

---

## 6. Risk Assessment

| Action | Risk | Mitigation |
|--------|------|-----------|
| Delete legacy theme | LOW | Grep for all references first |
| Delete HomePage chain | LOW | Grep confirmed not in Router |
| Delete ncdl.java | NONE | Empty file |
| Delete XML layouts | LOW | Grep for references |
| Remove Glide | MEDIUM | Must verify zero imports |
| Remove Paging | LOW | Must verify zero imports |
| Move signing creds | LOW | Fallback to empty strings |
| ProGuard rules | MEDIUM | Test release build thoroughly |