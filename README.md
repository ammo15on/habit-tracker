# Habit & Study Tracker (Android) - v6.2

A modern, offline-first Android application built with **Kotlin** and **Jetpack Compose (Material Design 3)** for daily habit tracking, NEET preparation schedules, interactive time tracking, syllabus completion, home screen widget, progressive AI study coaching, and productivity analytics.

---

## 📱 Pre-Built APKs (v6.2 & Version History)

The compiled Android application packages are available directly in the repository, preserving all historical releases:
- **v6.2 Latest Release**: [`apk/habit-tracker-v6.2.apk`](apk/habit-tracker-v6.2.apk)
- **v6.1 Release**: [`apk/habit-tracker-v6.1.apk`](apk/habit-tracker-v6.1.apk)
- **v6.0 Release**: [`apk/habit-tracker-v6.apk`](apk/habit-tracker-v6.apk)
- **v5.4 Release**: [`apk/habit-tracker-v5.4.apk`](apk/habit-tracker-v5.4.apk)
- **v5.3 Release**: [`apk/habit-tracker-v5.3.apk`](apk/habit-tracker-v5.3.apk)
- **v5.1 Release**: [`apk/habit-tracker-v5.1.apk`](apk/habit-tracker-v5.1.apk)
- **v5 Release**: [`apk/habit-tracker-v5.apk`](apk/habit-tracker-v5.apk)
- **v3 Release**: [`apk/habit-tracker-v3.apk`](apk/habit-tracker-v3.apk)
- **v2 Release**: [`apk/habit-tracker-v2.apk`](apk/habit-tracker-v2.apk)
- **Universal Standard Link**: [`apk/habit-tracker.apk`](apk/habit-tracker.apk)
- **Build Output**: `app/build/outputs/apk/debug/app-debug.apk`
- **Compatibility**: Android 8.0+ (API level 24+)
- **Architecture**: Universal APK
- **Version**: `6.2` (Version Code 12)

---

## 🚀 Release v6.2 - UI Styling Consistency, Wallpaper Opacity, Scroll-Hide Bottom Nav, Edge-Swipe Menu, Text Scaling & Progressive AI Chat

### 1. Settings & Hub Clean Glass Styling (No Cloudy Boxes)
- **Unified Transparent Glass**: Completely redesigned the **Settings & Hub** drawer and dialog pages (`Past Tasks`, `Theme & Styling`, `Data Backup`, `Presets`) to follow the app's clean obsidian glassmorphism with delicate slate borders (`BorderStroke(1.dp, outlineVariant)`), eradicating all cloudy white/black box fills.
- **Version Identifier**: Updated visual branding badges and metadata to **v6.2**.

### 2. Wallpaper Opacity & Transparency Control
- **Direct Wallpaper Transparency**: The UI transparency effect slider now directly controls the opacity/alpha of your custom background wallpaper image alongside container translucency, giving full control over image visibility.

### 3. Scroll-Aware Auto-Hiding Bottom Menu
- **Maximized Vertical Space**: Added root-level `NestedScrollConnection` and animated transitions (`slideInVertically` / `slideOutVertically`) that hide the bottom navigation bar on downward scrolling across all screens (`Tracker`, `Plan`, `Detail`) and reveal it on upward scroll.

### 4. Edge-Swipe Gesture for Hamburger Menu
- **Universal Left-Edge Swipe**: Users can swipe inwards from the left screen edge on ANY screen to instantly open the Settings & Hub menu.

### 5. Settings Text Size Control Function
- **Dynamic Text Scaling**: Added a dedicated **Text Size & Font Scale** controller in Settings with an interactive slider (80% to 135%), quick preset chips (*Small 85%*, *Default 100%*, *Large 115%*, *XL 130%*), and a live preview card. Scaling applies seamlessly across all screens and persists across app restarts.

### 6. Progressive Magnifying Glass AI Chat & Personalized Suggestions
- **Expanding Search Bar**: Replaced the static full-width chat input with a compact magnifying glass icon on the bottom left that smoothly expands in width as you type.
- **Same-Line Dynamic Suggestions**: In the exact same line alongside the magnifying glass, dynamic suggestion chips offer **personalized prompts derived from real student data** (uncompleted chapters, mock score breakdowns, subject time ratios) and universal high-yield NEET strategies.
- **Keyboard Handling & Draft Preservation**: The chat bar moves up above the software keyboard using `imePadding()`, and typed text is preserved as a draft across screen navigation and app pauses.

---

## 🚀 Release v6.1 - Fixed Widget, Swipe Across Tabs, Clean Transparent UI, Scroll-Hide Tabs & Max FPS

### 1. Fixed Home Screen Widget
- **RemoteViews Inflation Fix**: Fixed the widget failure by replacing unsupported XML layout tags with RemoteViews-compliant FrameLayout separators.
- **Immediate Data Sync & Launch Handler**: Tapping the widget opens the app directly into the NEET Detail screen.

### 2. Removed Pin Button & Pin Text
- **Clean Interface**: Completely removed the "Pin Widget" text and action button from the NEET and Analytics sections; users can add the widget directly from their phone's home screen widget picker.

### 3. Swipe Across Tabs Feature
- **Horizontal Pager Integration**: Added smooth horizontal gesture swiping between the 3 main detail tabs (Timeline, NEET, and Analytics), fully synchronized with the tab navigation bar.

### 4. Eliminated Whitish / Blackish Tint Boxes & True UI Transparency
- **Unified Clean Styling**: Replaced cloudy white/black card fills across Tracker, Plan, NEET, and Analytics screens with clean transparent containers and thin slate borders, matching the beloved Timeline tab aesthetic.
- **Disabled Artificial M3 Surface Tint**: Removed Material 3's automatic surface tint overlay, allowing custom background colors and wallpaper transparency to shine through without milky washed-out boxes.

### 5. Scroll-Aware Auto-Hiding Top Navigation
- **Maximized Content Space**: Positioned the tab bar and header at the top of the page with smooth scroll detection—tucking away when scrolling down and revealing when scrolling up.

### 6. Boosted Display Refresh Rate (Max FPS)
- **Fluid High-FPS Mode**: Dynamically configures window attributes to run at the device's highest supported display refresh rate (90Hz / 120Hz / 144Hz) for buttery-smooth scrolling and animations.

---

## 🚀 Release v6.0 - NEET Home Screen Widget, UI Refinements & Analytics

### 1. NEET Home Screen Widget
- **Live Syllabus Overview**: Dedicated Glance / RemoteViews AppWidget displaying live completion metrics for Chapters, PYQs, and NCERT read progress out of total milestones.
- **One-Tap Quick Launch**: Direct tap interaction on widget to jump straight to NEET details and tracker.

### 2. Streamlined Detail Page Cycling
- **Tab-Style Time Horizon Switching**: Simplified period cycling to clean, direct tab-click switching (Day / Week / Month) eliminating cumbersome multiple selection controls.

### 3. Hamburger Button Visual Polish
- **Removed White Halo / Circle**: Cleaned up the navigation drawer trigger icon button styling to blend seamlessly with custom theme background colors and transparency.

### 4. Comprehensive Productivity Analytics
- **Multi-Faceted Progress Metrics**: Refined analytics dashboard featuring completion breakdowns, subject progress, streak counts, and streamlined AI recommendations without clutter.

---

---

## 🚀 Release v5.4 - Hex Color Themes, Alarms & Reminders, Goal Milestones & Full-Screen Viewer

### 1. Hexadecimal Theme System with Transparency
- **Complete Hex Palette**: Integrated a 90-swatch Hexadecimal Colour Chart for UI elements, background, and text colors.
- **Custom Hex Code Input**: Type or paste any 6-digit hex code with instant swatch preview and validation.
- **Glassmorphism Preview**: Real-time transparent card effect preview and custom wallpaper support.

### 2. Task Alarms & Sound Notifications
- **Exact Alarms**: Schedule reminder times for tasks directly from Add Task and Edit Task dialogs.
- **Audio & Haptic Alerts**: High-priority notifications with sound and vibration powered by Android's exact alarm scheduler.

### 3. Future Goal Countdown
- **Target Date Tracking**: Set milestone goals with future dates from Tracker and Plan screens.
- **Clean Inline Days Indicator**: Displays remaining days count directly on the right side of Habits & Tasks in the Tracker.

### 4. Full-Screen Image Viewer
- **Modal Viewer**: Tap any image attachment or note thumbnail to open an interactive full-screen image viewer with pinch-to-zoom and pan.

### 5. Presets Experience in Hamburger Menu
- **Streamlined Add Task**: Presets consolidated into the Hamburger Menu.
- **Continuous Addition**: "Add to Day" keeps the presets dialog open for rapid multi-task creation with instant checkmark feedback.

### 6. Modernized Time Tracking Notification
- **Refined Foreground Notification**: Enhanced layout with elapsed chronometer time, clean action buttons, and high visibility.

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
│   ├── habit-tracker-v6.2.apk    # Latest v6.2 APK release
│   ├── habit-tracker-v6.1.apk    # Historical v6.1 APK
│   ├── habit-tracker-v6.apk      # Historical v6.0 APK
│   ├── habit-tracker-v5.4.apk    # Historical v5.4 APK
│   ├── habit-tracker-v5.3.apk    # Historical v5.3 APK
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
