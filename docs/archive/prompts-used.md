# My Launcher — Prompts Used

Archive of every prompt/request made during Copilot development sessions.

---

## Summary

| Metric | Count |
|--------|-------|
| Total sessions | 7 |
| Total prompts | ~35+ |

---

## Session 1 — Project Scaffolding & PRD

1. Initial project creation prompt — create Android launcher app with Metro/Windows Phone UI
2. Define PRD with feature requirements, target users, goals, UI specs
3. Set up Kotlin/Compose project structure with Hilt, DataStore, Navigation
4. Create data models (Tile, AppInfo, TileGroup)
5. Build repositories (AppRepository, TileRepository)
6. Create preferences manager with DataStore
7. Build Start Screen with tile grid
8. Build App List screen with search and alphabetical grouping
9. Build Settings screen with color picker, opacity, bevel, dark mode, themes
10. Create tile component with tilt animation and bevel effect
11. Set up navigation with HorizontalPager
12. Create documentation scaffolding (README, User Guide, Release Notes, PRD, backlog)

## Session 2 — Grid Coordinate System

13. Migrate from index-based tile ordering to explicit grid coordinates
14. Implement auto-placement algorithm for tiles without coordinates
15. Update grid rendering to use coordinate-based Box positioning

## Session 3 — Drag & Drop

16. Implement drag-to-reorder with long-press gesture detection
17. Add overlap displacement when tiles are dropped on occupied cells
18. Implement moveTileToGrid and moveGroupToGrid in TileRepository

## Session 4 — Tile Resize & Groups

19. Create Tile Settings Dialog with width/height sliders
20. Implement resize with overlap displacement
21. Implement group creation (create group from two tiles)
22. Build group header component with 2×2 icon preview
23. Add expand/collapse for groups with sub-grid layout
24. Add drag-to-reorder within expanded groups

## Session 5 — Move vs. Group & Rename

25. "Dragging an app to a new location between other apps is not working, it still creates a group every time"
26. Fix: Implement hover-time dwell tracking (v1 — 800ms timer)
27. "Dragging an app to a new location between other apps is not working, it still creates a group every time - FIX this and test it"
28. Fix: Switch to per-frame velocity detection (v3 — 1500ms threshold, >2px frame movement resets timer)
29. "need to allow the ability to rename groups"
30. Implement group rename: repository method, dialog component, edit mode wiring

## Session 6 — Drop Target Outline

31. "can we add a target outline when hovering a tile at a new location so you know where it it going to be placed?"
32. Implement DropTarget data class and white border outline rendering
33. "It's not the correct size, I just drug a 2 x 2 tile and the outline was a one by two outline"
34. Fix: Add wrapContentHeight(unbounded = true) for multi-row outline rendering

## Session 7 — Fix Grouping & Documentation

35. "Moving an app to a new location now works fine, but I can no longer add an app to a group or create a group"
36. Diagnose: per-frame velocity (>2px) is too sensitive to finger tremor, constantly resets dwell timer
37. Fix: Replace with settled-position radius approach (50px radius, 800ms dwell)
38. Verify via adb gesture testing and logcat: fast drag = move (152ms), deliberate hold = group (2033ms)
39. "OK, let's make sure all of the markdown documents are updated for the user guide, read me, release, notes, chat, history, and prompt history"
40. Update all documentation files to reflect actual implemented features
