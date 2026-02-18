# Release Workflow (Detailed)

Detailed release checklist for My Launcher.

---

## Pre-Release

- [ ] All features for this version are complete
- [ ] All P0 bugs resolved
- [ ] Version bumped in `README.md`, `app/build.gradle.kts`, `RELEASE-NOTES.md`
- [ ] User Guide updated for this version

## Build & Test

```bash
./gradlew assembleRelease
```

- [ ] Clean build succeeds
- [ ] Full test checklist passed
- [ ] Export/output features verified

## Documentation

- [ ] `README.md` — version, features, structure current
- [ ] `docs/RELEASE-NOTES.md` — version entry finalized
- [ ] `docs/My Launcher User Guide.md` — matches release build
- [ ] `docs/chat-history.md` — session appended
- [ ] `docs/prompts-used.md` — prompts appended

## Ship

```bash
git add -A
git commit -m "Release vX.X.X - description"
git tag vX.X.X
git push && git push --tags
gh release create vX.X.X --title "My Launcher vX.X.X" --generate-notes
```
