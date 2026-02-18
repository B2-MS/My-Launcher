# Testing Workflow (Detailed)

Detailed testing steps for My Launcher.

---

## Build

```bash
./gradlew assembleDebug
```

- [ ] Build completes with zero errors
- [ ] Build completes with zero warnings (or only known/accepted warnings)

## Launch

```bash
./gradlew installDebug
```

- [ ] App launches without crashes
- [ ] Initial screen renders correctly

## Core Test Checklist

- [ ] App launches and sets as default launcher
- [ ] Home screen renders with app icons
- [ ] App drawer opens and lists all installed apps
- [ ] App search filters results correctly
- [ ] Tapping an app icon launches the correct app
- [ ] Long-press shows app options
- [ ] Widgets can be added and resized
- [ ] Folders can be created and managed
- [ ] Swipe gestures trigger configured actions
- [ ] Wallpaper displays correctly
- [ ] Theme/colors apply across all screens
- [ ] Home gesture returns to launcher

## Edge Cases

- [ ] No apps installed (fresh device)
- [ ] Very large number of installed apps (200+)
- [ ] App install/uninstall while launcher is running
- [ ] Orientation change (if supported)
- [ ] Split-screen / multi-window mode

## Performance

- [ ] Responsive UI during scrolling and transitions
- [ ] No memory leaks (check Android Profiler)
- [ ] Acceptable CPU and battery usage
- [ ] Smooth 60fps animations
- [ ] Acceptable CPU usage
