# Quick Package

Fast packaging shortcut for My Launcher.

---

## Steps

1. **Build release:**
   ```bash
   ./gradlew assembleRelease
   ```

2. **Quick smoke test** — launch the built app and verify core functionality.

3. **Update docs:**
   ```
   Update README.md, docs/RELEASE-NOTES.md, and docs/My Launcher User Guide.md for the vX.X.X package.
   ```

4. **Commit and push:**
   ```bash
   git add -A && git commit -m "Package vX.X.X" && git push
   ```
