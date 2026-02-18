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

- [ ] App launches and sets as default launcher
- [ ] Home screen renders with app icons
- [ ] App drawer opens and lists all installed apps
- [ ] Tapping app icons launches correct apps
- [ ] Widgets can be added and resized
- [ ] Folders work correctly
- [ ] Gestures trigger configured actions
- [ ] Theme/colors apply correctly

---

## Step 3: Update Product Documentation

Ask Copilot to update these docs to match the packaged build:

| Document | What to Update |
|----------|----------------|
| `README.md` | Version badge, feature list, build instructions |
| `docs/RELEASE-NOTES.md` | Finalize current version entry — features, fixes, known issues |
| `docs/My Launcher User Guide.md` | Ensure all user-facing instructions are accurate |

```
Update README.md, docs/RELEASE-NOTES.md, and docs/My Launcher User Guide.md for the vX.X.X package.
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

---

## Checklist

- [ ] App builds successfully for Android
- [ ] Core functionality verified
- [ ] `README.md` version and features updated
- [ ] `docs/RELEASE-NOTES.md` updated with new version
- [ ] `docs/My Launcher User Guide.md` updated to match current build
- [ ] `docs/chat-history.md` updated
- [ ] `docs/prompts-used.md` updated
