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

```
Update README.md, docs/RELEASE-NOTES.md, and docs/My Launcher User Guide.md for the vX.X.X release.
```

---

## Step 3: Update Session Documentation

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

## Step 4: Build and Package

```bash
# Android release build
./gradlew assembleRelease

# Signed APK will be at app/build/outputs/apk/release/
```

---

## Step 5: Push to GitHub

```bash
git add -A
git status
git commit -m "Release vX.X.X - brief description of changes"
git push
```

---

## Step 6: Optional GitHub Release

```bash
gh release create vX.X.X --title "My Launcher vX.X.X" --generate-notes
```

---

## Output Files

| File | Location |
|------|----------|
| APK (Android) | `app/build/outputs/apk/release/app-release.apk` |
| Git | Committed and pushed to GitHub |
