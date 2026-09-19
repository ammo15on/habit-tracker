# Habit & Study Tracker (Android) - v2

A modern, offline-first Android application built with **Kotlin** and **Jetpack Compose (Material Design 3)** for daily habit tracking, NEET preparation schedules, time tracking, and productivity analytics.

---

## 📱 Pre-Built APK (v2)

The compiled Android application package with suffix `v2` is ready in the repository:
- **v2 APK Location**: `apk/habit-tracker-v2.apk`
- **Standard Link**: `apk/habit-tracker.apk`
- **Build Output**: `app/build/outputs/apk/debug/app-debug.apk`
- **Compatibility**: Android 8.0+ (API level 26+)
- **Architecture**: Universal APK
- **Version**: `2.0-v2` (Version Code 2)

---

## 🚀 Release v2 - New Features & Updates

### 1. Default Tasks & Quick Presets
- **Add as Default Task**: Toggle when creating tasks to designate recurring baseline routines.
- **Default Task Management**: Dedicated management dialog to edit, reorder, or delete default templates.
- **NEET Quick-Add Presets**: One-tap preloaded routines including:
  - *Physics Revision* (60m)
  - *Biology NCERT Deep Read* (90m)
  - *Chemistry Problem Practice* (60m)
  - *Mock Test & Error Analysis* (180m)
  - *Formula & Flashcard Review* (30m)
  - *Daily Exercise & Walk* (30m)
- **Visual Badges**: Star icon (`⭐`) and default task indicator chips.

### 2. Custom Task Frequency & Schedule
- **Flexible Scheduling**: Customize task frequency between "Everyday" or specific days of the week (Mon, Tue, Wed, Thu, Fri, Sat, Sun).
- **Edit Frequency**: Full support for updating task frequencies anytime via the Edit Task dialog.

### 3. Task Notes & Photo Attachments
- **Notes Field**: Attach chapter references, syllabus notes, formulas, or reminders.
- **Photo Picker Integration**: Uses Android's zero-permission photo picker (`PickVisualMedia`) to safely attach study diagrams, question snapshots, or notes.
- **Internal Storage Caching**: Attached images are securely stored in the app's internal cache directory.
- **Image Previews**: Thumbnail preview directly on the habit card with full-screen zoom dialog.

### 4. Planner with Starred Priority & Upcoming Days Counter
- **Priority Star System**: Star critical study tasks directly when creating or reviewing planned items.
- **Upcoming Starred Banner**: Dynamic dashboard banner displaying the total number of starred tasks scheduled across upcoming days.
- **Starred Only Filter**: Quick filter chip to focus strictly on high-priority planned sessions.
- **Jump to Task**: Instant jump action button to take any planned task directly into live tracking.

### 5. Compact Day Rating Bar
- **Minimalist Design**: Streamlined the 1–5 day rating dots (😭, 😔, 😐, 😊, 🤩) into a clean, floating bar above the bottom navigation bar.
- **Removed Heavy Containers**: High-contrast, spacious, and accessible touch targets without bulky boxes.

### 6. Interactive Timer & Time Editing
- **Live Stopwatch**: Real-time timer with play/pause controls per task.
- **Direct Time Editor**: Quick modal to manually adjust logged minutes or change targets.

### 7. NEET Mock Test Score Tracking & Analytics
- **Test Score Logger**: Track marks across Physics, Chemistry, and Biology (out of 720).
- **Comprehensive Analytics**: Study streaks, daily completion rates, and historical trends.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Programming Language**: Kotlin (100%)
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Local Persistence**: Jetpack Room Database (SQLite)
- **State Management**: StateFlow & `collectAsStateWithLifecycle`
- **Asynchronous Execution**: Kotlin Coroutines
- **Image Handling**: Coil & Android Photo Picker
- **Navigation**: Jetpack Navigation Compose

---

## 📂 Project Structure

```
├── apk/
│   └── habit-tracker.apk         # Compiled standalone APK
├── app/
│   ├── src/main/
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/             # Room Database, DAOs, and Models
│   │   │   ├── ui/
│   │   │   │   ├── components/   # Dialogs, rating bar, cards
│   │   │   │   ├── screens/      # Tracker, Plan, Analytics screens
│   │   │   │   └── theme/        # Material 3 colors & typography
│   │   │   └── util/             # Date utilities & image storage
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
