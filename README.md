# Habit & Study Tracker (Android) - v5.3

A modern, offline-first Android application built with **Kotlin** and **Jetpack Compose (Material Design 3)** for daily habit tracking, NEET preparation schedules, interactive time tracking, syllabus completion, and productivity analytics.

---

## 📱 Pre-Built APKs (v5.3 & Version History)

The compiled Android application packages are available directly in the repository, preserving all historical releases:
- **v5.3 Latest Release**: [`apk/habit-tracker-v5.3.apk`](apk/habit-tracker-v5.3.apk)
- **v5.1 Release**: [`apk/habit-tracker-v5.1.apk`](apk/habit-tracker-v5.1.apk)
- **v5 Release**: [`apk/habit-tracker-v5.apk`](apk/habit-tracker-v5.apk)
- **v3 Release**: [`apk/habit-tracker-v3.apk`](apk/habit-tracker-v3.apk)
- **v2 Release**: [`apk/habit-tracker-v2.apk`](apk/habit-tracker-v2.apk)
- **Universal Standard Link**: [`apk/habit-tracker.apk`](apk/habit-tracker.apk)
- **Build Output**: `app/build/outputs/apk/debug/app-debug.apk`
- **Compatibility**: Android 8.0+ (API level 24+)
- **Architecture**: Universal APK
- **Version**: `5.3` (Version Code 8)

---

## 🚀 Release v5.3 - Real-Calendar Month Analytics & Multi-Date Planning

### 1. Real-World Calendar Alignment in Monthly Analytics
- **Accurate Weekday Alignment**: Computed exact day-of-week offsets for the 1st day of every month against the `M`, `T`, `W`, `T`, `F`, `S`, `S` headers.
- **True Calendar Grid**: Inserted alignment spacer cells so that dates match the actual calendar (e.g., September 1, 2026 starts correctly on Tuesday).

### 2. Direct Keyboard Auto-Focus on Add Task
- **Instant Input Readiness**: Tapping "Add Habit / Task" in Tracker or "Plan New Task" in Plan automatically focuses the task title input field and opens the software keyboard immediately.

### 3. Multi-Date Selection in Plan Tab
- **Batch Schedule Across Days**: Easily schedule a planned task across multiple dates in one step using the quick "Today", "Tomorrow", and "+ Select Multiple Dates" interactive calendar picker.
- **Tag Management**: View all chosen target dates with individual chip removal (`X`) and a count indicator on the "Save Plan" action.

---

## 🚀 Release v5.2 - Background Timer & UI Polish

### 1. Continuous Background Time Tracking
- **Foreground Service Integration**: Live chronometer and ongoing notification via `TimerForegroundService` and `TimerManager`.
- **Background Persistence**: Wall-clock synchronization ensures elapsed study and habit intervals continue accurately even when the app is minimized.

### 2. Transparent Multi-Tier Analytics
- **Modern Outline Aesthetic**: Summary cards, day grids, and emoji rating badges across Day, Week, and Month analytics feature transparent backgrounds with subtle borders matching the active dynamic palette.

---

## 🚀 Release v5.1 - Collapsible Event Subtasks in Tracker & Plan

### 1. Collapse / Expand Icon Beside Event Name
- **Interactive Header Controls**: Added dedicated collapse/expand icons (`KeyboardArrowDown` / `KeyboardArrowUp`) directly beside each event name.
- **Live Counter Badges**: Displays completed subtask counts `(X/Y)` next to the expand icon so progress is immediately visible.
- **Bidirectional Status Sync**: Toggle any subtask complete in Tracker or Plan with instant state synchronization.

---

## 🚀 Release v5 - Major Features & Foundation

### 1. Event Subtasks with Granular Date Assignments
- **Flexible Scheduling**: Subtasks can be assigned to specific dates within the event period or set to recur daily across the entire event duration.
- **Daily Tracker Integration**: Today's active event subtasks appear directly in the Daily Tracker under their parent event.

### 2. Collapsible Day Grouping in Plan Screen
- **Chronological Grouping**: Planned tasks are organized by day with expandable/collapsible headers.
- **Task Archiving**: Completed and historic plans can be archived without cluttering active planning views.

### 3. Dedicated Task Presets Architecture
- **Isolated Templates**: Standalone `TaskPreset` entity and DAO decoupled from daily habit routines.
- **Quick Preset Selector**: Save and apply reusable task templates with target durations, study notes, and attached image notes.

### 4. NEET Class 11 & Class 12 Syllabus & Tally Counter
- **NCERT Chapters**: Preloaded with complete Class 11 and Class 12 Physics, Chemistry, and Biology syllabus chapters (Concept, PYQs, Revision).
- **Interactive Tally Counters**: Tactile haptic tally counters for tracking questions solved, study intervals, or revision repetitions.

### 5. Dynamic App Theme Color Picker
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
- **Background Processing**: Android Foreground Service (`TimerForegroundService`)
- **State Management**: StateFlow & `collectAsStateWithLifecycle`
- **Asynchronous Execution**: Kotlin Coroutines & Flow
- **Image Handling**: Coil & Android Photo Picker
- **Navigation**: Jetpack Navigation Compose
- **Theme Persistence**: SharedPreferences via ThemePreferences

---

## 📂 Project Structure

```
├── apk/
│   ├── habit-tracker-v5.3.apk    # Latest v5.3 APK release
│   ├── habit-tracker-v5.1.apk    # Historical v5.1 APK
│   ├── habit-tracker-v5.apk      # Historical v5 APK
│   ├── habit-tracker-v3.apk      # Historical v3 APK
│   ├── habit-tracker-v2.apk      # Historical v2 APK
│   └── habit-tracker.apk         # Universal standard link
├── app/
│   ├── src/main/
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/             # Room Database, DAOs, and Models
│   │   │   ├── ui/
│   │   │   │   ├── components/   # Dialogs, theme picker, calendar pickers
│   │   │   │   ├── screens/      # Tracker, Plan, Analytics screens
│   │   │   │   └── theme/        # Dynamic M3 palettes & typography
│   │   │   └── util/             # Date utilities, TimerManager, Foreground Service
│   │   └── res/                  # Android icons & resources
│   └── build.gradle.kts
├── metadata.json
└── README.md
```

---

## 🔨 How to Build from Source

1. Clone or export this repository.
2. Open in **Android Studio** (Ladybug or newer).
3. Sync Gradle dependencies.
4. Run the app or compile via command line:
   ```bash
   gradle assembleDebug
   ```
   The generated APK will be located in `app/build/outputs/apk/debug/app-debug.apk`.
