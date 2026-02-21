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

```
Update README.md, docs/RELEASE-NOTES.md, and docs/My Launcher User Guide.md to reflect any changes made in this session.
```

---

## Step 4: Update Session Documentation

Ask Copilot to update the session documentation:

### Chat History
```
Review our entire conversation and APPEND a summary to docs/chat-history.md

Use this format:
---
## Session: [Descriptive Topic Title]
**Date:** [Date]

### Prompts
1. [First prompt - summarized]
2. [Second prompt - summarized]

### Outcomes
- [What was built/changed/fixed]
- [Key files modified]
---

APPEND to the END of the file. Do not overwrite.
```

### Prompts Used
```
Review our entire conversation and APPEND to docs/prompts-used.md

Extract EVERY prompt/request I made - use my EXACT words, not summaries.

Format:
## Session: [Date] - [Topic] (vX.X.X)
1. "[exact prompt 1]"
2. "[exact prompt 2]"

Then UPDATE the Summary section counts at the top of the file.
```
