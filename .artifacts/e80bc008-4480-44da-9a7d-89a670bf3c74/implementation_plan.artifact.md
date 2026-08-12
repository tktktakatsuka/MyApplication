# Edge-to-Edge and R8 Optimization Refinement Plan

Address the ongoing Play Console warnings regarding Edge-to-Edge inconsistencies, deprecated cutout parameters, and R8 optimization.

## User Review Required

> [!IMPORTANT]
> The warnings you received often appear in the Play Console for releases that were built *before* recent fixes or due to internal behavior of Google's own SDKs (AdMob/Play Core). This plan ensures the app uses the absolute latest standards to minimize these reports.

## Proposed Changes

### 1. Build Configuration & Dependencies
- **[libs.versions.toml](file:///C:/Users/Administrator/AndroidStudioProjects/MyApplication7/gradle/libs.versions.toml)**:
    - Update core dependencies to the latest stable versions.
    - Explicitly add new split Play libraries (`app-update`, `review`) to ensure the old monolithic `play-core` is completely replaced.
- **[app/build.gradle](file:///C:/Users/Administrator/AndroidStudioProjects/MyApplication7/app/build.gradle)**:
    - Ensure `targetSdk` is set to **35** (Android 15) to trigger the correct Edge-to-Edge defaults.
    - Clean up redundant dependency declarations.

### 2. Edge-to-Edge UI Refinement
- **[MainActivity.java](file:///C:/Users/Administrator/AndroidStudioProjects/MyApplication7/app/src/main/java/com/tktkcompany/kakoRaceKeiba/MainActivity.java)**:
    - Improve inset handling using `ViewCompat.setOnApplyWindowInsetsListener`. This ensures that even when the app is edge-to-edge, the bottom navigation bar and top action bar don't overlap with system UI (gestures, clock, etc.).
- **[activity_main.xml](file:///C:/Users/Administrator/AndroidStudioProjects/MyApplication7/app/src/main/res/layout/activity_main.xml)**:
    - Remove `android:fitsSystemWindows="true"` and hardcoded padding from the root layout. This allows the background to bleed truly to the edges while we handle the "safe zones" programmatically.

### 3. R8 Optimization & ProGuard
- **[proguard-rules.pro](file:///C:/Users/Administrator/AndroidStudioProjects/MyApplication7/app/proguard-rules.pro)**:
    - Add `-repackageclasses` and `-allowaccessmodification` to further optimize the release build.
    - Verify that all libraries (ThreeTenABP, Jsoup, etc.) have precise "keep" rules to allow for maximum shrinking without breaking.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleRelease` to verify that the optimized build still functions correctly.
- Compare APK size before and after these changes.

### Manual Verification
- Deploy the **Release** build to an Android 15 device/emulator.
- Check that the navigation bar and status bar are transparent and do not obscure content.
- Verify that AdMob ads still load correctly in the new cutout mode.
