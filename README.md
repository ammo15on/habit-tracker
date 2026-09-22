# Habit & Study Tracker (Android) - v5.1

A modern, offline-first Android application built with **Kotlin** and **Jetpack Compose (Material Design 3)** for daily habit tracking, NEET preparation schedules, time tracking, syllabus completion, and productivity analytics.

---

## 📱 Pre-Built APKs (v5.1 & Version History)

The compiled Android application packages are available in the repository without overwriting or deleting historical versions:
- **v5.1 Latest Release**: `apk/habit-tracker-v5.1.apk`
- **v5 Release**: `apk/habit-tracker-v5.apk`
- **v3 Release**: `apk/habit-tracker-v3.apk`
- **v2 Release**: `apk/habit-tracker-v2.apk`
- **Universal Standard Link**: `apk/habit-tracker.apk`
- **Build Output**: `app/build/outputs/apk/debug/app-debug.apk`
- **Compatibility**: Android 8.0+ (API level 26+)
- **Architecture**: Universal APK
- **Version**: `5.1` (Version Code 6)

---

## 🚀 Release v5.1 - Collapsible Event Subtasks in Tracker & Plan

### 1. Collapse / Expand Icon Beside Event Name
- **Interactive Header Controls**: Added a dedicated collapse/expand icon (`KeyboardArrowDown` / `KeyboardArrowUp`) directly beside each event name in both the **Daily Tracker** and **Plan** screen.
- **On-Demand Subtask Visibility**: Subtasks remain neatly collapsed by default to preserve a compact, uncluttered layout. Tapping the expand icon or event title instantly expands the subtasks section to see all details and interact with individual checkboxes.
- **Live Counter Badges**: Displays a completed subtask counter `(X/Y)` next to the expand icon so progress is immediately visible without needing to expand.
- **Seamless Synchronization**: Toggle any subtask complete in the Tracker or Planner, and the status syncs in real time across the entire app.

---

## 🚀 Release v5 - Major Features & Improvements

### 1. Event Subtasks with Date Assignments & Real-Time Completion
- **Subtasks Inside Events**: Events in the Planner now support granular subtasks with flexible scheduling.
- **Date-Specific or Everyday Recurrence**: Subtasks can be assigned to specific dates within the event period. If no specific dates are selected, the subtask automatically recurs every single day for the entire event duration.
- **Instant Completion Checkbox**: Mark individual event subtasks as complete or pending directly from the Planner and from the Tracker screen for the active day.
- **Daily Tracker Integration**: Today's active event subtasks appear directly in the Daily Tracker under their parent event, synchronized in real time.

### 2. Collapsible Day Grouping in Plan Screen
- **Date-Grouped Task Lists**: All planned tasks are organized chronologically into day sections.
- **Expand & Collapse per Day**: Tap any day header to expand or collapse the day's tasks, making long-term planning clean and easy to scan.
- **Quick Day Counter**: Each day header shows the number of planned tasks at a glance.

### 3. Task Archiving (Replaces Starred Priority)
- **Archive System**: Starred task mechanism replaced with a clean archive flow.
- **Active & Archived Tabs**: The Plan view features dedicated segmented tabs: **All Tasks** and **Archived Tasks**.
- **Planner & Tracker Consistency**: Archived tasks are hidden from the active Plan view (visible only in the Archived tab), while remaining available on their scheduled day in the Daily Tracker.

### 4. Transparent Header & Base Navigation Theming
- **Zero-Visual-Clutter Layout**: The top heading cards, tab rows, and bottom navigation bar use transparent container backgrounds matching the underlying wallpaper or background.
- **Consistent Elevation**: Removed heavy shadows and solid card containers to create an open, unified aesthetic.

### 5. Smooth Gestures & Month Scrolling Optimization
- **Fluid Day Swiping**: Tuned drag velocity thresholds and decoupled recomposition states in the Daily Tracker for ultra-smooth date swipes.
- **100% Smooth Month Scroll**: Replaced nested `LazyVerticalGrid` inside scrollable month cards with memory-efficient chunked rows, completely eliminating scroll stutter and jank.
- **Settled-Page Pager Sync**: Synchronized tabs using `snapshotFlow` and settled page states to ensure stutter-free horizontal swiping between Day, Week, Month, and NEET views.

### 6. Emoji Rating Counts (Replaces "Green Days" Rules)
- **Simplified Rating Overview**: Replaced complex arbitrary "Green Days" thresholds with clear, transparent counts of **Best (😊)**, **Average (😐)**, and **Worst (😞)** ratings.
- **Multi-Tier Overviews**: Summary chips with emoji counts are prominently displayed across Weekly, Monthly, and Daily analytics overviews.
- **Consistent Rating Badges**: Days and calendar cells highlight with emojis and subtle rating badges without heavy solid fills.

---

### 7. Dedicated Task Presets Architecture
- **Isolated Preset Templates**: Task presets are managed as standalone templates (`TaskPreset` Room entity & DAO) decoupled from the active daily tracker database.
- **No Daily Task Pollution**: Creating or editing presets preserves them strictly as reusable templates without injecting them into active daily habit lists.
- **One-Tap "Add to Day"**: Seamlessly instantiate any preset directly into the currently selected date or schedule across custom date ranges.

### 8. NEET Class 11 & Class 12 Syllabus & Tally Counter
- **NCERT Chapters**: Preloaded with complete Class 11 and Class 12 Physics, Chemistry, and Biology syllabus chapters with concept, PYQ, and revision progress.
- **Interactive Tally Counters**: Create and tap counters with tactile haptic feedback for tracking questions solved, study intervals, or daily repetitions.

---

## 🚀 Release v4 - Updates & Improvements
- **Automated SDK Alignment**: Tuned Android 16 (API 36) SDK properties and optimized Gradle configuration parameters.
- **Robust Local Packaging**: Streamlined signing configuration fallback mechanisms to ensure problem-free, predictable debugging and offline APK production.
- **Pre-Built V4 Binary Artifacts**: Added pre-compiled stand-alone binaries for straight-to-device side-loading.

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
│   ├── habit-tracker-v5.apk      # Latest release v5 APK
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
