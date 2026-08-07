# Dependency Update and Project Cleanup Plan

The project has been updated with a large number of dependencies, including AGP 9.3.1 and Gradle 9.6.1. While the project initially reported a successful build via tools, manual verification revealed an environment conflict and several deprecation warnings. This plan aims to resolve these issues and clean up the build configuration.

## User Review Required

> [!IMPORTANT]
> **Environment Variable Conflict**: AGP 9.3.1 is strict about having only one of `ANDROID_PREFS_ROOT` or `ANDROID_USER_HOME` set. Currently, both are set to the same path. I will need to ensure the build environment is consistent. In my local shell, I can work around this by unsetting `ANDROID_PREFS_ROOT`.

> [!NOTE]
> **targetSdk Update**: I will update `targetSdk` to 37 to match the `compileSdk 37` requested, ensuring consistency with the latest SDK.

## Proposed Changes

### Build Configuration

#### [MODIFY] [gradle.properties](file:///C:/Users/Administrator/AndroidStudioProjects/MyApplication7/gradle.properties)
- Remove deprecated and unsupported project options introduced by AGP 9.3.1.
- Enable `android.dependency.excludeLibraryComponentsFromConstraints` as recommended for performance.

#### [MODIFY] [app/build.gradle](file:///C:/Users/Administrator/AndroidStudioProjects/MyApplication7/app/build.gradle)
- Update `targetSdk` to 37.
- Clean up duplicate dependency declarations.
- Standardize dependency aliases to remove confusing "v130" or "v2300" suffixes that no longer match the resolved versions.

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Administrator/AndroidStudioProjects/MyApplication7/gradle/libs.versions.toml)
- Ensure all requested versions are accurately represented.
- Add missing versions for artifacts mentioned by the user but not explicitly in the catalog.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` (with environment workaround if necessary) to verify the project builds.
- Run `./gradlew help` to ensure the configuration phase is clean.

### Manual Verification
- Verify that the `app/build.gradle` file is clean and free of duplicates.
