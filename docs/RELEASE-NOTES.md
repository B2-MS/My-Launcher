# My Launcher — Release Notes

---

## v0.1.0-alpha — (February 21, 2026)

**Initial alpha build — Metro UI launcher for Android.**

### Start Screen & Tile Grid
- 6-column vertically scrolling Metro-style tile grid
- Freely resizable tiles: 1–6 columns wide, 1–4 rows tall
- Tiles auto-seeded from first 12 installed apps on initial launch
- Coordinate-based grid layout with 4dp gaps and 12dp edge margins
- Tap a tile to launch its associated app
- System wallpaper visible behind tiles via transparent window background

### Drag & Drop
- Long-press any tile to enter edit mode and begin dragging
- Drag tiles to any grid position; overlapping tiles are automatically displaced
- White outline drop-target indicator shows where a tile will land during drag
- Multi-row/multi-column tiles render correctly sized outlines

### Tile Groups
- Dwell-to-group: drag a tile over another and hold still for ~800ms to create a group
- Settled-position radius approach tolerates natural hand tremor (~20px)
- Add tiles to existing groups via the same dwell gesture
- Group header displays 2×2 mini-grid preview of up to 4 app icons, plus name and count
- Tap group to expand/collapse; expanded view shows member tiles in a 6-column sub-grid
- Long-press to rearrange tiles within an expanded group
- Drag a tile out of an expanded group to ungroup it
- Groups with only 1 remaining tile are auto-dissolved
- Rename groups via dialog in edit mode

### Edit Mode
- Long-press triggers edit mode: tiles shrink (0.92× scale), size labels shown (e.g., "2×2")
- Unpin (✕) button on each tile to remove it from the Start Screen
- Tap tile → Tile Settings Dialog (width/height sliders with live preview, live tile toggle)
- Tap group → Group Rename Dialog
- "Done" button to exit edit mode

### App List
- Alphabetically grouped list with letter section headers (A–Z)
- Real-time search bar filters apps by name
- Tap to launch app
- Long-press to pin app as a 2×2 tile on Start Screen (toast confirmation)
- Accessible via swipe-right (HorizontalPager) or "All Apps →" button

### Customization & Theming
- Full HSV accent color picker: 2D saturation/value box + hue slider + live hex preview
- Global tile transparency slider (0%–100%)
- Glass bevel effect toggle with configurable depth (Subtle → Deep)
- Dark / Light mode toggle for Settings and App List
- Live tile animation interval selector: 3s / 5s / 10s / 30s / Off
- Save Current Layout as named theme; Apply or Delete saved themes

### Visual Effects
- Tilt-on-press animation (3° X-axis rotation with 12f camera distance)
- Glass bevel modifier (highlight top-left, shadow bottom-right gradients)
- Metro-inspired typography (SansSerif, light/thin weights, generous sizing)
- Edge-to-edge layout with status bar integration

### Architecture
- Kotlin + Jetpack Compose (BOM 2024.12.01)
- MVVM with Unidirectional Data Flow
- Hilt dependency injection
- Jetpack DataStore for preferences
- Navigation Compose with HorizontalPager
- In-memory tile storage (Room declared but not yet wired)

### Known Limitations
- Tile data is in-memory only — restarting the app seeds default tiles from installed apps (Room persistence is declared but not yet implemented)
- Live tile content is stubbed — the toggle exists but no data sources are connected yet
- Per-tile color and opacity overrides exist in the data model but are not exposed in the UI
- No notification badge support yet
- No weather, calendar, or photo live tile types yet
- No onboarding / tutorial flow
- No backup/restore export (theme save/apply is local only)

---

*For the full product roadmap, see [My Launcher PRD.md](My%20Launcher%20PRD.md).*
