# My Launcher

**Version:** 0.1.0-alpha  
**Platform:** Android 10+ (API 29)  
**Status:** In Development (Alpha)

---

## What is My Launcher?

My Launcher is an Android home-screen replacement that faithfully recreates the Windows Phone Metro UI on modern Android devices. It replaces the default home screen with a vertically scrolling grid of resizable, customizable tiles — delivering the clean, typography-driven design language that defined Windows Phone.

Built entirely with Jetpack Compose and modern Android architecture (Hilt, DataStore, MVVM).

---

## Features

### Start Screen
- **Metro-style tile grid** — 6-column vertically scrolling grid of resizable tiles
- **Freely resizable tiles** — 1–6 columns wide, 1–4 rows tall (Small, Medium, Wide, Large, and everything in between)
- **Drag & drop** — long-press to drag tiles to new positions; overlapping tiles are auto-displaced
- **Drop target outline** — white border preview shows where a dragged tile will land
- **Tilt-on-press animation** — 3D tilt effect when touching a tile
- **Glass bevel effect** — configurable gradient overlay giving tiles a polished, glass-like appearance
- **Wallpaper transparency** — system wallpaper visible behind semi-transparent tiles

### Tile Groups
- **Dwell-to-group** — drag a tile onto another and hold ~1 second to create a group
- **Add to existing groups** — drag tiles onto group headers
- **Expand/collapse** — tap a group to reveal member tiles in a sub-grid
- **Rearrange within groups** — long-press to drag tiles inside an expanded group
- **Drag-out ungrouping** — drag a tile outside an expanded group to remove it
- **Auto-dissolve** — groups with only 1 tile remaining are automatically ungrouped
- **Rename groups** — tap a group in edit mode to rename it

### Edit Mode
- **Long-press** any tile to enter edit mode
- Tiles shrink with size labels (e.g., "2×2") and unpin (✕) buttons
- Tap a tile → **Tile Settings Dialog** (width/height sliders, live tile toggle)
- Tap a group → **Group Rename Dialog**
- **"Done" button** to exit

### App List
- **Alphabetical grouping** with letter section headers (A, B, C…)
- **Real-time search** — filter apps as you type
- **Tap to launch**, **long-press to pin** as a new tile on the Start Screen
- Accessible via swipe-right or **"All Apps →"** button

### Customization & Theming
- **HSV color picker** — full-spectrum accent color selection with live hex preview
- **Tile transparency slider** — 0% (transparent) to 100% (opaque)
- **Bevel toggle + depth slider** — Subtle to Deep glass effect
- **Dark / Light mode** toggle
- **Live tile animation interval** — 3s, 5s, 10s, 30s, or Off
- **Save / Apply / Delete themes** — capture and restore full tile layouts with settings

### Navigation & System Integration
- **HorizontalPager** — swipe between Start Screen and App List
- **Set as default launcher** — registered as `HOME` + `LAUNCHER` activity
- **Edge-to-edge** — status bar and navigation bar integration
- **System wallpaper** — visible behind transparent tiles via `windowShowWallpaper`

---

## Getting Started

### Requirements

- Android 10+ (API 29) device or emulator
- Android Studio Ladybug (2024.2) or later
- JDK 17+

### Build & Run

```bash
# Clone the repo
git clone <repo-url>
cd "My Launcher/app"

# Build and install on connected device
./gradlew installDebug
```

Or open the `app/` directory in Android Studio, select the **app** module, choose a device, and press **▶ Run**.

### Set as Default Launcher

After installing, press your device's Home button and select **My Launcher** → **Always**.

---

## Project Structure

```
My Launcher/
├── app/                              # Gradle project root
│   ├── build.gradle.kts              # Root build config
│   ├── settings.gradle.kts           # Project settings
│   ├── gradle/
│   │   └── libs.versions.toml        # Version catalog
│   └── app/                          # App module
│       ├── build.gradle.kts          # App module config
│       └── src/main/
│           ├── AndroidManifest.xml
│           └── java/com/mylauncher/
│               ├── MyLauncherApp.kt            # Hilt application
│               ├── data/
│               │   ├── model/                  # Data classes (Tile, AppInfo, etc.)
│               │   ├── preferences/            # DataStore preferences
│               │   └── repository/             # App & Tile repositories
│               ├── di/
│               │   └── AppModule.kt            # Hilt DI module
│               └── ui/
│                   ├── MainActivity.kt         # Single activity, edge-to-edge
│                   ├── navigation/             # NavHost, Routes
│                   ├── viewmodel/              # LauncherViewModel
│                   ├── screens/                # StartScreen, AppList, Settings
│                   ├── components/             # TileItem, GroupTile, Dialogs
│                   └── theme/                  # Theme, Typography
└── docs/
    ├── My Launcher PRD.md            # Product Requirements Document
    ├── My Launcher User Guide.md     # User guide
    ├── RELEASE-NOTES.md              # Release notes
    ├── backlog.md                    # Deferred features
    ├── images/                       # Screenshots for User Guide
    ├── archive/                      # Historical session docs
    └── instructions/                 # Workflow triggers
```

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI Framework | Jetpack Compose (BOM 2024.12.01) |
| Architecture | MVVM + Unidirectional Data Flow |
| DI | Hilt 2.53.1 |
| Navigation | Navigation Compose 2.8.5 |
| Preferences | Jetpack DataStore |
| Image Loading | Coil 2.7.0 |
| Min SDK | 29 (Android 10) |
| Target SDK | 35 (Android 15) |
| Build | Gradle (Kotlin DSL) + Version Catalogs |

---

## Documentation

| Document | Description |
|----------|-------------|
| [User Guide](docs/My%20Launcher%20User%20Guide.md) | How to use My Launcher |
| [PRD](docs/My%20Launcher%20PRD.md) | Product requirements & roadmap |
| [Release Notes](docs/RELEASE-NOTES.md) | Version history |
| [Backlog](docs/backlog.md) | Deferred feature ideas |

---

## License

TBD

## Contributing

TBD
