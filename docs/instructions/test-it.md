# Testing Workflow

Quick rebuild and deploy for testing My Launcher during development.

---

## Step 1: Open in Android Studio & Build

1. Open the project in Android Studio:
   ```bash
   open . -a "Android Studio"
   ```

2. Select the **app** module and a connected device or emulator in the toolbar.

3. Press **▶ Run** (or **⌃R**) to build and deploy the app.

4. Check the Build output window for any errors or warnings. Fix before proceeding.

> **Tip:** You can also build from the terminal if preferred:
> ```bash
> # Terminal build (debug)
> ./gradlew assembleDebug
>
> # Terminal build + run
> ./gradlew installDebug
> ```

---

## Step 2: Run and Test

With the app running on a device or emulator, verify:

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

After testing, ask Copilot to update these docs if any features changed:

| Document | What to Update |
|----------|----------------|
| `README.md` | Feature list, project structure, version badge |
| `docs/RELEASE-NOTES.md` | Add or amend current version entry with new/changed features and known issues |
| `docs/My Launcher User Guide.md` | Update instructions, screenshots, or sections affected by changes |
| `docs/backlog.md` | Add new deferred features or remove items that were implemented |

```
Update README.md, docs/RELEASE-NOTES.md, docs/My Launcher User Guide.md, and docs/backlog.md to reflect any changes made in this session.
```
