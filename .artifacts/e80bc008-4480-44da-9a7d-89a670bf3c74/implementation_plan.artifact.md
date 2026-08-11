# Fix WorkManager `StartupException` and `WorkDatabase` Failure

This plan addresses the crash caused by `androidx.work.impl.WorkDatabase` initialization failure, typically due to database corruption or schema migration issues.

## User Review Required

> [!IMPORTANT]
> This change introduces a custom `Application` class. If you already have one that was not detected, please let me know.
> It also disables the default WorkManager initializer in `AndroidManifest.xml` to gain manual control over its lifecycle.

## Proposed Changes

### 1. Build Configuration
- **[libs.versions.toml](file:///C:/Users/Administrator/AndroidStudioProjects/MyApplication7/gradle/libs.versions.toml)**: Added `androidx.work:work-runtime` v2.10.0.
- **[app/build.gradle](file:///C:/Users/Administrator/AndroidStudioProjects/MyApplication7/app/build.gradle)**: Added explicit dependency on `libs.work.runtime`.

### 2. Custom Application Class
- **[NEW] [MyApplication.java](file:///C:/Users/Administrator/AndroidStudioProjects/MyApplication7/app/src/main/java/com/tktkcompany/kakoRaceKeiba/MyApplication.java)**:
    - Implements `Configuration.Provider` for on-demand `WorkManager` initialization.
    - Handles database corruption gracefully in `onCreate()` by detecting `StartupException` or Room errors and deleting the corrupted `androidx.work.workdb` file if necessary.

### 3. Manifest Update
- **[AndroidManifest.xml](file:///C:/Users/Administrator/AndroidStudioProjects/MyApplication7/app/src/main/AndroidManifest.xml)**:
    - Registers `.MyApplication` as the app's entry point.
    - Disables the default `WorkManagerInitializer` using the `androidx.startup.InitializationProvider` configuration.

## Verification Plan

### Automated Tests
- Run `gradlew :app:assembleDebug` to ensure no compilation errors.
- Sync Gradle to verify dependency resolution.

### Manual Verification
- Launch the app and verify it no longer crashes during startup.
- Inspect Logcat for "WorkManager initialization successful" or any logs related to database cleanup if failure was encountered.
