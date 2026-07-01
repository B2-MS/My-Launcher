# My Launcher — Chat History

Records of Copilot-assisted development sessions.

---

## Summary

| Metric | Count |
|--------|-------|
| Total sessions | 7 |
| Total prompts | ~35+ |

---

## Session 1 — Project Scaffolding & PRD (February 13, 2026)

**Goal:** Set up the project from scratch and define product requirements.

- Created the Android project with Jetpack Compose, Hilt, DataStore, Room, Coil, Navigation Compose
- Defined the full Product Requirements Document (PRD v1.1) with 6 feature areas and 40+ requirements
- Set up project structure: data layer (models, repositories, preferences), DI, UI layer (screens, components, theme)
- Configured build.gradle.kts with version catalogs, minSdk 29, targetSdk 35
- Created AndroidManifest with `HOME` + `LAUNCHER` intent filters, `QUERY_ALL_PACKAGES` permission
- Implemented core data models: `Tile`, `AppInfo`, `TileGroup`, `GridItem`, `SavedTheme`
- Built `AppRepository` (PackageManager query), `TileRepository` (in-memory CRUD), `PreferencesManager` (DataStore)
- Created `LauncherViewModel` with auto-seeding of 12 default tiles
- Built initial `StartScreen` with tile grid, `AppListScreen` with search and alphabetical grouping
- Built `SettingsScreen` with HSV color picker, opacity slider, bevel controls, dark mode, saved themes
- Created `TileItem` component with tilt animation and bevel modifier
- Set up `LauncherNavHost` with HorizontalPager navigation
- Created doc scaffolding: README, User Guide, Release Notes, PRD, backlog, instructions

## Session 2 — Grid Coordinate System & Tile Placement (February 13–14, 2026)

**Goal:** Replace positional ordering with explicit grid coordinates.

- Migrated from index-based tile ordering to `(gridCol, gridRow)` coordinate system
- Implemented 6-column grid with 4dp gap and 12dp horizontal padding
- Row height equals column width (square cells)
- Auto-placement algorithm: scans top-left to bottom-right for first available cell fitting the tile's span
- Tiles with `gridCol = -1` are auto-assigned positions
- Gaps in the grid render as empty spacer rows
- Updated rendering to use `Box` per row with offset-based tile positioning

## Session 3 — Drag-to-Reorder & Overlap Displacement (February 14, 2026)

**Goal:** Implement full drag-and-drop tile repositioning with overlap handling.

- Added `detectDragGesturesAfterLongPress` for drag initiation
- Drag offset tracked in pixels, converted to grid cell offsets via `cellStepX/Y`
- Target position computed and clamped to grid bounds
- Overlap displacement: when a tile is placed, any overlapping tiles/groups are relocated to the nearest available cell
- `moveTileToGrid()` and `moveGroupToGrid()` functions in TileRepository
- Dragged tile rendered at elevated zIndex with offset translation
- Edit mode auto-enters on drag start

## Session 4 — Tile Resize, Groups, and Edit Mode Polish (February 14–15, 2026)

**Goal:** Add tile resizing, group creation, and edit mode UX.

- `TileSettingsDialog` with width (1–6) and height (1–4) sliders with live preview
- Resize displacement: `setTileSpans()` detects and relocates overlapping tiles after resize
- Size constraint: tiles ≥4 columns require ≥2 rows
- Group creation: `createGroup()` merges two tiles into a group with shared `groupId`
- Group header: `GroupTileItem` showing 2×2 mini-grid of up to 4 app icons
- Group expand/collapse: `toggleGroupExpanded()` and `GroupExpandedContent`
- Expanded group sub-grid: 6-column layout with `(groupCol, groupRow)` coordinates
- Drag-to-reorder within expanded groups
- Size labels (e.g., "2×2") displayed in edit mode
- Unpin button (✕) on each tile in edit mode

## Session 5 — Move vs. Group Differentiation & Group Rename (February 15, 2026)

**Goal:** Fix tile movement always creating groups; add group renaming.

- **Problem:** Dragging a tile to a new position always created a group instead of moving
- **Fix v1 (hover time):** Simple 800ms hover timer — failed because tiles are large and drag naturally lingers
- **Fix v2 (position checkpoint):** 30px movement checkpoint + 800ms timer — failed because timer ran during deceleration
- **Fix v3 (per-frame velocity):** Reset timer on any >2px movement per frame, 1500ms threshold — successfully differentiated fast drag vs. deliberate hold
- Added `renameGroup()` to TileRepository and ViewModel
- Created `GroupRenameDialog.kt` component (AlertDialog with auto-focus text field)
- Added `onGroupEditTap` to GroupTileItem (edit mode tap triggers rename dialog)
- Wired through LauncherNavHost

## Session 6 — Drop Target Outline & Multi-Row Fix (February 15, 2026)

**Goal:** Add visual feedback showing where a tile will land during drag.

- Added `DropTarget` data class with `col`, `row`, `colSpan`, `rowSpan`
- `dropTarget` state updated during `onDrag` to show current target position
- White border outline rendered at target position in both populated and empty grid rows
- **Bug:** Multi-row tile outlines were clipped to single row height
- **Fix:** Added `.wrapContentHeight(align = Alignment.Top, unbounded = true)` to outline Boxes

## Session 7 — Settled-Position Dwell & Grouping Fix (February 15–21, 2026)

**Goal:** Fix grouping being impossible after per-frame velocity fix.

- **Problem:** Per-frame velocity approach (v3) was too sensitive — any finger tremor >2px constantly reset the dwell timer, making it impossible to hold still long enough to create a group
- **Fix v4 (settled-position radius):** Track where finger "settled"; only reset timer when finger drifts more than 50px (~19dp) from settled position. Natural hand tremor within the radius is tolerated.
- Reduced `groupDwellMs` back to 800ms (radius approach makes this safe)
- Added `hoverSettledAt` (Offset) and `hoverSettledTime` (Long) state variables
- Removed per-frame velocity tracking (`frameDist > 2f` check)
- Verified: fast drag = move (elapsed 152ms < 800ms), deliberate hold = group (elapsed 2033ms > 800ms)
- Updated documentation (this session)
