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

- [ ] App launches and sets as default launcher
- [ ] Home screen renders with app icons
- [ ] App drawer opens and lists all installed apps
- [ ] App search filters results correctly
- [ ] Tapping an app icon launches the correct app
- [ ] Long-press shows app options (uninstall, info, remove)
- [ ] Widgets can be added and resized
- [ ] Folders can be created by dragging apps together
- [ ] Swipe gestures trigger configured actions
- [ ] Wallpaper displays correctly behind launcher
- [ ] Theme/colors apply across all screens
- [ ] Back button / home gesture returns to home screen

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
