# BacheBazi (com.bachebazi.app)

Native Android app (Kotlin, Jetpack Compose, Material 3). Persian/RTL, dark blue theme, fully offline, no permissions.

## Build a debug APK (Android Studio)
1. Install Android Studio (current stable). 2. File > Open > select this `BacheBazi` folder.
3. Wait for "Gradle sync" to finish (first sync downloads dependencies; internet needed once).
   If asked to install the Android SDK / platform 34, accept.
4. Build > Build Bundle(s) / APK(s) > Build APK(s).
   Output: `app/build/outputs/apk/debug/app-debug.apk`

## Preview on emulator
Device Manager > Create Device (e.g. Pixel 7, API 34) > press the green Run button.

## Install on a real phone
Phone: Settings > About phone > tap "Build number" 7 times > Developer options > enable USB debugging.
Connect by USB, accept the prompt, choose the phone in Android Studio and press Run.
Or copy `app-debug.apk` to the phone, open it, and allow "install unknown apps" for your file manager.

## Rebuild after changes
Repeat step 4 (or press Run). Command line: `./gradlew assembleDebug` (needs the Gradle wrapper that Android Studio creates).

## Unit tests
`./gradlew testDebugUnitTest` or right-click `RandomizerTest` in Android Studio > Run.
