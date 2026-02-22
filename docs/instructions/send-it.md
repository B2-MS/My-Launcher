# Release Workflow

Complete release: build, package, verify documentation, and push to GitHub.

---

## Step 1: Update Version Files

Before releasing, update version numbers in:

| File | What to Update |
|------|----------------|
| `README.md` | Version badge |
| `docs/RELEASE-NOTES.md` | Add new version section at top |
| `app/build.gradle.kts` | `versionCode` and `versionName` |

---

## Step 2: Update Product Documentation

Before releasing, ask Copilot to update:

| Document | What to Update |
|----------|----------------|
| `README.md` | Version badge, feature list, project structure |
| `docs/RELEASE-NOTES.md` | Finalize current version entry — features, fixes, known issues |
| `docs/My Launcher User Guide.md` | Ensure all instructions match the release build |
| `docs/backlog.md` | Add new deferred features or remove items that were implemented |

```
Update README.md, docs/RELEASE-NOTES.md, docs/My Launcher User Guide.md, and docs/backlog.md for the vX.X.X release.
```

---

## Step 3: Build and Package

```bash
# Android release build
./gradlew assembleRelease

# Signed APK will be at app/build/outputs/apk/release/
```

### Test the Build

Run through the core test checklist:

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

## Step 4: Push to GitHub

```bash
git add -A
git status
git commit -m "Release vX.X.X - brief description of changes"
git push
```

---

## Step 5: Optional GitHub Release

```bash
gh release create vX.X.X --title "My Launcher vX.X.X" --generate-notes
```

---

## Output Files

| File | Location |
|------|----------|
| APK (Android) | `app/build/outputs/apk/release/app-release.apk` |
| Git | Committed and pushed to GitHub |
