# My Launcher — Product Requirements Document

**Version:** 1.1  
**Date:** February 14, 2026  
**Status:** Draft  
**Platform:** Android (Google Play Store)

---

## 1. Overview

My Launcher is an Android home-screen replacement (launcher) that faithfully recreates the Windows Phone Metro UI experience on modern Android devices. The app replaces the default Android home screen with a vertically scrolling grid of Live Tiles — interactive, resizable rectangles that display real-time information at a glance. Users can pin apps, contacts, and widgets as tiles, customize tile sizes and positions, choose accent colors, and enjoy the clean, typography-driven design language that defined Windows Phone. My Launcher is built with Jetpack Compose and targets publication on the Google Play Store.

---

## 2. Problem Statement

Windows Phone was discontinued in 2019, yet its Metro/Live Tile interface remains one of the most beloved mobile UX paradigms ever created. Millions of former Windows Phone users — and design enthusiasts — have no way to get that experience on modern hardware. Existing Android launchers that attempt a Metro look are either abandoned, visually inaccurate, or lack critical features like true live tiles, fluid tile rearrangement, and the full suite of customization options (accent colors, tile sizes, transparency). My Launcher fills this gap by delivering a high-fidelity, actively maintained Metro launcher built on modern Android technologies.

---

## 3. Target Users

| Persona | Description |
|---------|-------------|
| **Former Windows Phone User** | Loved the Metro UI and wants that same clean, glanceable, tile-based experience on their current Android phone. Values familiarity, live information at a glance, and a departure from the icon-grid paradigm. |
| **Customization Enthusiast** | Power user who enjoys theming and personalizing their Android device. Attracted to launcher alternatives that offer a unique look and deep layout control. |
| **Minimalist / Productivity User** | Prefers a clutter-free home screen with information-dense tiles over pages of app icons. Values quick access to key info (calendar, weather, notifications) without opening apps. |

---

## 4. Goals & Success Criteria

| Goal | Metric |
|------|--------|
| Faithful Metro UX reproduction | ≥ 90% user-survey score on "feels like Windows Phone" |
| Smooth performance | 60 fps scrolling & tile animations on mid-range devices (Snapdragon 6-series+) |
| Play Store publication | App approved and listed on Google Play Store |
| User retention | ≥ 40% 30-day retention rate |
| User satisfaction | ≥ 4.0 average Play Store rating within first 6 months |
| Tile customization depth | Users can resize, reposition, pin/unpin, and color-theme every tile |

---

## 5. Feature Requirements

### 5.1 Start Screen & Tile Grid

| ID | Requirement | Priority |
|----|-------------|----------|
| SG-01 | Vertically scrolling tile grid as the primary home screen, matching Windows Phone Start screen layout | P0 |
| SG-02 | Support three tile sizes: Small (1×1), Medium (2×2), and Wide (4×2), mirroring Windows Phone tile sizes | P0 |
| SG-03 | Tiles auto-flow into a grid with configurable column count (default 6 columns, matching WP 8.1) | P0 |
| SG-04 | Long-press a tile to enter edit mode — tiles shrink, unpin (X) buttons appear, resize handles shown | P0 |
| SG-05 | Drag-and-drop tile repositioning within the grid while in edit mode | P0 |
| SG-06 | Pinch-to-resize or context-menu resize to cycle through Small → Medium → Wide tile sizes | P1 |
| SG-07 | Support for tile groups / sections separated by named dividers (like WP 8.1 tile groups) | P1 |
| SG-08 | Parallax or subtle motion effect on background wallpaper when scrolling (WP 8.1 style) | P2 |
| SG-09 | Adaptive grid layout for foldable phones and tablets — automatically widen the tile grid and increase column count on larger screens | P0 |

### 5.2 Live Tiles

| ID | Requirement | Priority |
|----|-------------|----------|
| LT-01 | Tiles display live, updating content — flip/slide animations showing info (notifications, counts, previews) | P0 |
| LT-02 | Notification badge counts displayed on tiles for apps with unread notifications | P0 |
| LT-03 | Weather tile (wide): current conditions, temperature, hourly forecast row with per-hour icon/temp/precipitation — matching reference design | P1 |
| LT-04 | Calendar tile: next upcoming event name, time, and location | P1 |
| LT-05 | Photos tile: cycle through recent photos from device gallery | P1 |
| LT-06 | People tile: show contact photos and recent activity for pinned contacts | P2 |
| LT-07 | World Clock tile (wide): display multiple user-configured time zones with city name, current time, AM/PM, and day — supports 2–6 clocks in a single tile | P1 |
| LT-08 | Custom tile content via user-configurable data sources (RSS, etc.) | P2 |

### 5.3 App List

| ID | Requirement | Priority |
|----|-------------|----------|
| AL-01 | Swipe right (or tap "All Apps →" button at bottom-right of Start screen) to access a full alphabetical app list with jump-list letter headers (A, B, C…) | P0 |
| AL-02 | Search bar at top of app list ("Search Apps") for quick app lookup | P0 |
| AL-03 | Long-press an app in the list to pin it as a tile on the Start screen | P0 |
| AL-04 | App list shows each app as a row: accent-colored square icon tile on the left + app name text on the right, single-column layout with dark/transparent background | P0 |
| AL-05 | Letter section headers displayed as large, bold characters separating app groups alphabetically | P0 |
| AL-06 | Recently installed apps highlighted or badged | P2 |

### 5.4 Customization & Theming

| ID | Requirement | Priority |
|----|-------------|----------|
| CT-01 | Accent color picker — applies to all tile backgrounds, highlights, and system text (mirroring WP accent colors) | P0 |
| CT-02 | Support for both Dark and Light theme backgrounds | P0 |
| CT-03 | Per-tile color override — individual tiles can have a custom background color | P1 |
| CT-04 | User-configurable tile transparency — global opacity slider (0%–100%) plus per-tile opacity override, allowing the desktop wallpaper (static or animated) to show through tiles | P0 |
| CT-05 | Custom wallpaper selection for Start screen background — supports static images, animated GIFs, looping video wallpapers, and Android Live Wallpapers | P0 |
| CT-06 | Choice of grid density: 3, 4, or 6 columns | P1 |
| CT-07 | Custom icon packs support for tile icons | P2 |
| CT-08 | Font size adjustment for tile labels | P2 |
| CT-09 | Animated wallpaper renders continuously behind semi-transparent tiles — smooth playback with no frame drops even when tiles are overlaid | P0 |

### 5.5 Navigation & System Integration

| ID | Requirement | Priority |
|----|-------------|----------|
| NS-01 | Set as default launcher via Android system settings | P0 |
| NS-02 | Status bar integration — show system clock, battery, signal in standard Android status bar | P0 |
| NS-03 | Swipe down from top for Android notification shade (standard system behavior) | P0 |
| NS-04 | Action center-style quick settings panel (WP-inspired) accessible via swipe or tile | P2 |
| NS-05 | Support for Android widgets embedded within tiles (bridge to existing widget ecosystem) | P2 |
| NS-06 | Home button returns to Start screen top | P0 |

### 5.6 Settings & Configuration

| ID | Requirement | Priority |
|----|-------------|----------|
| SC-01 | Settings screen for theme, accent color, grid layout, tile animation speed/frequency, tile transparency (global + per-tile), and wallpaper (static / animated / live) | P0 |
| SC-02 | Backup & restore tile layout (local export/import) | P1 |
| SC-03 | Onboarding / first-run tutorial explaining tile customization | P1 |
| SC-04 | About screen with version info, credits, and link to Play Store rating | P0 |
| SC-05 | Battery optimization guidance to keep live tiles updating | P1 |

---

## 6. User Interface

### 6.1 Layout

**Start Screen:**

```
┌──────────────────────────────┐
│ 10:15 🔋 📶                  │  ← Android Status Bar
├──────────────────────────────┤
│ Ellet, Summit Co.      ☀️    │
│ ┌─────────────────────────┐  │
│ │ ☀️ 43°F        Sunny    │  │  ← Weather: Wide tile
│ │ 10AM 11AM 12PM  1PM     │  │    with hourly forecast
│ │ 44°  46°  48°   50°     │  │
│ └─────────────────────────┘  │
│ ┌──────┐ ┌──────┐ ┌──────┐  │
│ │MCAPS │ │Office│ │Outlk  │  │  ← Row 2: 3× Medium
│ │  TC   │ │ 365  │ │ BETA  │  │    full-color icons
│ └──────┘ └──────┘ └──────┘  │
│ ┌──┐┌──┐ ┌──┐┌──┐ ┌──────┐  │
│ │  ││▶️││ │⚙️││GH│ │Teams │  │  ← Row 3: Mixed
│ └──┘└──┘ └──┘└──┘ └──────┘  │
│ ┌──────┐ ┌──────┐ ┌──────┐  │
│ │Edge  │ │M365  │ │ in   │  │  ← Row 4: 3× Medium
│ │      │ │Copilt│ │      │  │
│ └──────┘ └──────┘ └──────┘  │
│ ┌──────┐ ┌──────┐ ┌──────┐  │
│ │Photos│ │Camera│ │Gallry │  │  ← Row 5: 3× Medium
│ └──────┘ └──────┘ └──────┘  │
│ ┌─────────────────────────┐  │
│ │ Redmond  Dubai  London  │  │  ← World Clock: Wide tile
│ │  7:14AM 7:14PM 3:14PM   │  │    multi-timezone
│ │ Schiphol  Hyderabad     │  │
│ │  4:14PM    8:44PM       │  │
│ └─────────────────────────┘  │
│              ↕ scroll        │
│                  All Apps → │  ← Tap/swipe right for App List
└──────────────────────────────┘
```

**App List (swipe right):**

```
┌──────────────────────────────┐
│ 10:15 🔋 📶                  │  ← Android Status Bar
├──────────────────────────────┤
│ ┌─────────────────────── 🔍┐ │
│ │ Search Apps               │ │  ← Search bar
│ └───────────────────────────┘ │
│ A                            │  ← Letter header
│ ┌──┐ Adobe Scan              │
│ │🔴│                         │  ← Colored square icon + name
│ └──┘                         │
│ ┌──┐ Assistant               │
│ │🔵│                         │
│ └──┘                         │
│ ┌──┐ Authenticator           │
│ │🔵│                         │
│ └──┘                         │
│ B                            │  ← Letter header
│ ┌──┐ Bing                    │
│ │🔷│                         │
│ └──┘                         │
│ C                            │  ← Letter header
│ ┌──┐ Calculator              │
│ │🔵│                         │
│ └──┘                         │
│ ┌──┐ Calendar                │
│ │🟪│                         │
│ └──┘                         │
│ ┌──┐ Camera                  │
│ │🔵│                         │
│ └──┘                         │
│              ↕ scroll        │
└──────────────────────────────┘
```

### 6.2 Design Language

- **Design System:** Microsoft Metro / Modern UI — flat, chrome-less, content-forward design with bold typography and solid-color tiles.
- **Typography:** Segoe UI-inspired sans-serif font (use system Roboto with thin/light weights to approximate). Tile labels use light-weight, large-size text positioned at the bottom-left of each tile.
- **Color:** Monochromatic tile backgrounds derived from a single user-chosen accent color. Supports per-tile overrides. Dark theme uses #1A1A1A background; Light theme uses #FFFFFF.
- **Accent Colors:** Predefined palette matching Windows Phone (Cobalt, Cyan, Teal, Emerald, Lime, Yellow, Amber, Mango, Orange, Crimson, Red, Magenta, Mauve, Steel, etc.) plus custom color picker.
- **Animations:** Tile flip (front→back) and slide-up transitions for live content. Tilt effect on tile press (3D perspective tilt toward touch point). Turnstile page transition between Start screen and App List.
- **Spacing:** Consistent 4dp gap between tiles. 12dp margin on left/right screen edges.
- **Icons:** Full-color app icons displayed on tiles, centered above the tile label. In the App List, icons are shown inside accent-colored square backgrounds (WP style). Users may optionally switch to monochrome white icon rendering in Settings for a more traditional WP look.
- **Navigation:** "All Apps →" button anchored at bottom-right of Start screen. Swipe right transitions to the App List with a turnstile animation. Standard Android back gesture and notification shade supported.
- **Platform Conventions:** Follows Android navigation patterns (back gesture, notification shade) while replacing the home screen experience entirely.

---

## 7. Technical Architecture

| Component | Technology |
|-----------|------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Minimum SDK** | Android 10 (API 29) |
| **Target SDK** | Android 15 (API 35) |
| **Tile Layout Engine** | Custom Compose `LazyColumn` + grid layout with drag-and-drop reordering |
| **Live Tile Data** | `NotificationListenerService` for notification content; `WorkManager` for periodic data refresh |
| **Weather Data** | OpenWeatherMap API or similar (free tier) |
| **Persistence** | Room database for tile layout, user preferences, and theme settings |
| **Preferences** | Jetpack DataStore (Preferences) |
| **Image Loading** | Coil (Compose-native image loader) |
| **Animated Wallpaper** | `WallpaperService` for Live Wallpapers; `ExoPlayer` / `Media3` for video wallpapers; animated GIF via Coil GIF decoder |
| **Animations** | Compose Animation APIs (`animateFloatAsState`, `AnimatedContent`, etc.) |
| **App Discovery** | `PackageManager` + `LauncherApps` API for installed app enumeration |
| **Build System** | Gradle (Kotlin DSL) with version catalogs |
| **CI/CD** | GitHub Actions → signed APK/AAB → Play Store upload |
| **Distribution** | Google Play Store (AAB format) |
| **Architecture Pattern** | MVVM with Unidirectional Data Flow |
| **Dependency Injection** | Hilt |

---

## 8. Milestones

| Phase | Scope | Target |
|-------|-------|--------|
| **Alpha** | Start screen tile grid with Small/Medium/Wide tiles, drag-and-drop editing, accent color theming, tile transparency, wallpaper support (static + animated), app list with search, set-as-default-launcher | TBD |
| **Beta** | Live tile content (notifications, weather, calendar, photos), tile transparency, per-tile colors, backup/restore, onboarding tutorial | TBD |
| **v1.0** | Polish, performance optimization, Play Store listing assets (screenshots, description, feature graphic), Play Store publication | TBD |
| **v1.1+** | People tile, action center, Android widget embedding, custom icon packs, RSS tiles | TBD |

---

## 9. Open Questions

| # | Question | Status | Answer |
|---|----------|--------|--------|
| 1 | Should we support tablets / foldables with wider grid layouts in v1, or defer to a later release? | Resolved | Yes — support wider adaptive layouts for foldables and tablets in v1. Added as SG-09 (P0). |
| 2 | Which weather API to use (OpenWeatherMap free tier has limits)? | Resolved | OpenWeatherMap — start with the free tier; evaluate upgrading if rate limits become an issue. |
| 3 | Do we need `QUERY_ALL_PACKAGES` permission or can we use `<queries>` intent filters for app discovery? | Resolved | Use `QUERY_ALL_PACKAGES` permission. As a launcher, this is a valid use case and should be approved by Google Play policy. |
| 4 | Should live tile animation frequency be user-configurable or fixed (e.g., flip every 5 seconds)? | Resolved | User-configurable — add an animation frequency setting (e.g., 3s / 5s / 10s / 30s / off) to Settings. |
| 5 | Monetization model: free with ads, freemium (free + paid Pro unlock), or paid upfront? | Resolved | Paid upfront — no ads. Single purchase price on the Play Store. |
| 6 | Should the app include a WP-style lock screen replacement, or defer that to a future version? | Resolved | Deferred — added to the backlog (see docs/backlog.md). |
| 7 | Minimum Android version — API 29 (Android 10) covers ~95% of devices; is that low enough? | Resolved | Yes — API 29 (Android 10) is confirmed as the minimum SDK. |
