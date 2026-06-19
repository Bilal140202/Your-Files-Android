# Research: Phase 6 — Integration Testing & Release

## Research Date: 2026-06-19
## Status: COMPLETE

---

## 1. Build Environment Verification

### Prerequisites Check
```bash
# JDK
echo $JAVA_HOME  # /home/z/jdk-21
/home/z/jdk-21/bin/java -version  # openjdk 21.0.2

# Android SDK
echo $ANDROID_HOME  # /home/z/android-sdk
ls /home/z/android-sdk/platforms/android-36/
ls /home/z/android-sdk/build-tools/36.0.0/

# Gradle
./gradlew --version  # 8.13
```

### Known Build Issues (from previous sessions)
- JDK 21 required (not JRE) — FIXED (installed at /home/z/jdk-21)
- Android SDK platforms;android-36 required — FIXED
- Build tools 36.0.0 required — FIXED
- `cmdlinetools.zip` was in git history — FIXED (removed in branch setup)
- `upload/` directory contained secrets — FIXED (removed in branch setup)

---

## 2. Test Infrastructure

### Current Tests
- `ExampleUnitTest.kt`: `assertEquals(4, 2 + 2)` — BOILERPLATE
- `ExampleInstrumentedTest.kt`: checks package name — BOILERPLATE

### Unit Test Opportunity (Phase 6)
For this phase, we DON'T write new unit tests (that's a separate effort). We do MANUAL testing with the 74-case checklist.

### Future Test Infrastructure (not in scope)
- JUnit 5 + MockK for VM testing
- Compose UI testing with `createComposeRule()`
- Espresso for integration testing
- CI/CD with GitHub Actions

---

## 3. APK Distribution

### Telegram
- Bot token: available (from session context)
- Chat ID: available (from session context)
- Send command: `curl -F document=@app-debug.apk "https://api.telegram.org/bot<TOKEN>/sendDocument?chat_id=<CHAT_ID>"`

### GitHub Release
- Create release with tag v2.0.0
- Upload release APK
- Paste changelog from Phase 6 plan

---

## 4. Merge Strategy

### Branch State After All Phases
```
main (untouched since branch creation)
  └── feature/phase-implementation
        ├── phase-1: fix critical bugs
        ├── phase-2: toolbar and organiser
        ├── phase-3: storage analyzer rewrite
        ├── phase-4: features and UX
        ├── phase-5: cleanup and security
        └── phase-6: testing and release
```

### Merge Process
```bash
# 1. On feature/phase-implementation:
git log --oneline  # verify 6+ clean commits

# 2. Update main:
git checkout main
git pull origin main
git merge feature/phase-implementation --no-ff  # creates merge commit

# 3. Push:
git push origin main

# 4. Tag:
git tag -a v2.0.0 -m "v2.0.0 - Major update with ES-style analyzer"
git push origin v2.0.0

# 5. Cleanup:
git branch -d feature/phase-implementation
git push origin --delete feature/phase-implementation
```

### Rollback Plan
If merge causes issues:
```bash
git checkout main
git reset --hard v1.0.0  # or the commit before merge
git push origin main --force
```

---

## 5. Post-Release Monitoring

### What to Check
- GitHub Actions (if configured) pass
- No crash reports in Google Play Console (when published)
- User feedback on issues

### Immediate Follow-ups (not in this phase)
- Write actual unit tests for new code
- Add CI/CD pipeline
- Internationalize new strings (add to ja/ko/zh translations)
- Performance profiling on low-end devices