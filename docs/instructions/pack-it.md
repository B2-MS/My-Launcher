# Packaging Workflow

Build and package My Launcher for distribution.

---

## Step 1: Build the App

```bash
# Android release build
./gradlew assembleRelease
```

---

## Step 2: Test the Build

1. Launch the built app
2. Run through the core test checklist:

**Start Screen**
- [ ] App launches and displays the tile grid
- [ ] Tiles show correct app icons and names
- [ ] Tapping a tile launches the correct app
- [ ] Vertical scrolling works when many tiles are present
- [ ] Wallpaper is visible behind semi-transparent tiles

**Edit Mode**
- [ ] Long-press enters edit mode (tiles shrink, size labels appear)
- [ ] Unpin (✕) buttons remove tiles
- [ ] Tap tile opens Tile Settings Dialog (width/height sliders, live tile toggle)
- [ ] Drag & drop moves tiles to new positions with drop target outline
- [ ] "Done" button exits edit mode

**Tile Groups**
- [ ] Drag tile onto another + hold ~1s creates a group
- [ ] Tap group expands/collapses member tiles
- [ ] Tap group in edit mode opens rename dialog
- [ ] Drag tile out of expanded group ungroups it

**App List**
- [ ] Swipe right or tap "All Apps →" opens alphabetical app list
- [ ] Search filters apps in real time
- [ ] Tap launches app, long-press pins to Start Screen

**Settings**
- [ ] Accent color picker changes tile colors globally
- [ ] Tile transparency slider works (0%–100%)
- [ ] Bevel toggle and depth slider adjust glass effect
- [ ] Dark mode toggle applies to Settings & App List
- [ ] Theme save / apply / delete works

---

## Step 3: Update Product Documentation

Ask Copilot to update these docs to match the packaged build:

| Document | What to Update |
|----------|----------------|
| `README.md` | Version badge, feature list, build instructions |
| `docs/RELEASE-NOTES.md` | Finalize current version entry — features, fixes, known issues |
| `docs/My Launcher User Guide.md` | Ensure all user-facing instructions are accurate |
| `docs/backlog.md` | Add new deferred features or remove items that were implemented |

```
Update README.md, docs/RELEASE-NOTES.md, docs/My Launcher User Guide.md, and docs/backlog.md for the vX.X.X package.
```

---

## Checklist

- [ ] App builds successfully for Android
- [ ] Core functionality verified
- [ ] `README.md` version and features updated
- [ ] `docs/RELEASE-NOTES.md` updated with new version
- [ ] `docs/My Launcher User Guide.md` updated to match current build
- [ ] `docs/backlog.md` reviewed and updated
