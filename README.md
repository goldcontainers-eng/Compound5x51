# Compound 5×5 Pro

Native Android workout tracker for a Smith machine, cable pulldown/row station and dumbbells.

## Added in this version
- Today’s Workout home screen
- Automatic weekend rest-day screen
- Dark/light mode toggle
- Large gym-friendly set buttons
- Previous working weight display
- Personal-best tracking
- Automatic next-weight target after successful completion
- 2-minute rest timer
- Workout history
- Full Monday-Friday program preloaded

## Build the APK
1. Open this folder in Android Studio.
2. Allow Gradle to sync.
3. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Android Studio will show a notification with **Locate**. The APK is usually under `app/build/outputs/apk/debug/app-debug.apk`.

The app stores workout data locally on the phone.

## Phone-only APK build (no Android Studio)

This project includes `.github/workflows/build-apk.yml`.

From an Android phone:
1. Create a GitHub account if needed.
2. Create a new repository.
3. Upload the project files to the repository so `app`, `build.gradle.kts`, `settings.gradle.kts`, and `.github` are at the repository root.
4. Open the repository's **Actions** tab.
5. Open **Build Android APK** and tap **Run workflow**.
6. When the run finishes, open it and download the **Compound5x5-APK** artifact.
7. Extract the downloaded artifact ZIP; inside is `app-debug.apk`.
8. Tap the APK on your phone and allow installation from that browser/files app when Android prompts you.

The APK is a debug build intended for personal installation/testing.
