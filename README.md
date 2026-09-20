# Habit & Study Tracker (Android) - v3

A modern, offline-first Android application built with **Kotlin** and **Jetpack Compose (Material Design 3)** for daily habit tracking, NEET preparation schedules, time tracking, syllabus completion, and productivity analytics.

---

## 📱 Pre-Built APK (v3)

The compiled Android application package with suffix `v3` is ready in the repository:
- **v3 APK Location**: `apk/habit-tracker-v3.apk`
- **Standard Link**: `apk/habit-tracker.apk`
- **Build Output**: `app/build/outputs/apk/debug/app-debug.apk`
- **Compatibility**: Android 8.0+ (API level 26+)
- **Architecture**: Universal APK
- **Version**: `3.0-v3` (Version Code 3)

---

## 🚀 Release v3 - Major Updates & Improvements

### 1. Date-Specific Scheduling & Bidirectional Sync
- **Strict Date Isolation**: Tasks and plans scheduled for a specific date appear strictly on that chosen date in the Tracker view, resolving multi-day bleed.
- **Bidirectional Plan & Tracker Synchronization**: Tasks scheduled in the Planner appear seamlessly in the Daily Tracker when navigating to that date, and tasks created in the Tracker are synchronized into the calendar plan.
- **Completion Sync**: Marking a task complete in the Daily Tracker synchronizes its completed status in the Planner calendar view.

### 2. Default Date on Task Creation
- **Targeted Day by Default**: Creating a task now defaults to "This Day Only" matching the date currently being viewed, avoiding unwanted default recurrence across everyday routines.
- **Flexible Recurrence**: One-tap toggle to expand into custom recurring days or "Everyday".

### 3. Refined Starred Priority Styling
- **Subtle Visual Hierarchy**: Removed the harsh yellow container background on starred tasks, replacing it with an elegant star icon indicator and clean outline borders that match the selected app theme.

### 4. Day Tasks Inspection in Analytics
- **Interactive Day Inspector**: Tapping any day row or dot in the Day, Week, or Month analytics opens an instant dialog detailing all habits and tasks for that date, complete with status, logged duration vs target, and a direct "Open in Tracker" action.

### 5. Swipe Gestures for Date Navigation
- **Fluid Horizontal Swiping**: Swipe left or right anywhere on the Tracker screen to navigate backward or forward between dates, backed by responsive haptic feedback.

### 6. NEET Class 11 & Class 12 Syllabus
- **Comprehensive NCERT Chapters**: Preloaded with complete Class 11 and Class 12 Physics, Chemistry, and Biology syllabus chapters.
- **3-Pillar Progress Tracking**: Independent checkboxes for *Concept Completed*, *PYQs Done*, and *Revision Done*.

### 7. Haptic Feedback Integration
- **Tactile Responses**: Added tactile vibration feedback for task creation, timer starts/stops, task completions, rating changes, tally counter taps, and date swipes.

### 8. Dynamic App Theme Color Picker
- **Theme Palette Selection**: Accessible via the hamburger button positioned directly above the floating add button.
- **Color Themes**:
  - 💛 Yellow
  - 🏆 Golden
  - 🖤 Deep Black (AMOLED)
  - 🩶 Sleek Grey
  - 🌿 Emerald Green
  - 🌊 Ocean Blue
  - 💜 Royal Purple

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Programming Language**: Kotlin (100%)
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Local Persistence**: Jetpack Room Database (SQLite) v5
- **State Management**: StateFlow & `collectAsStateWithLifecycle`
- **Asynchronous Execution**: Kotlin Coroutines
- **Image Handling**: Coil & Android Photo Picker
- **Navigation**: Jetpack Navigation Compose
- **Theme Persistence**: SharedPreferences via ThemePreferences

---

## 📂 Project Structure

```
├── apk/
│   ├── habit-tracker-v3.apk      # Latest release v3 APK
│   └── habit-tracker.apk         # Universal download link
├── app/
│   ├── src/main/
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/             # Room Database, DAOs, and Models
│   │   │   ├── ui/
│   │   │   │   ├── components/   # Dialogs, theme picker, task rows
│   │   │   │   ├── screens/      # Tracker, Plan, Analytics screens
│   │   │   │   └── theme/        # Dynamic M3 palettes & typography
│   │   │   └── util/             # Date utilities & Theme preferences
│   │   └── res/                  # Android icons & resources
│   └── build.gradle.kts
├── metadata.json
└── README.md
```

---

## 🔨 How to Build from Source

1. Clone or export this repository.
2. Open in **Android Studio Ladybug** or newer.
3. Sync Gradle dependencies.
4. Run the app directly or compile via command line:
   ```bash
   gradle assembleDebug
   ```
   The APK output will be located in `app/build/outputs/apk/debug/app-debug.apk`.
