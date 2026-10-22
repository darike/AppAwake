# ⚡ AppAwake - Android Keep-Alive Accessibility Utility

> **Native Android App (Kotlin + Jetpack Compose Material 3)**  
> User ki chuni hui apps (jaise Instagram, WhatsApp, InDrive, Trading apps) ko detect karti hai aur screen/system ko awake aur active rakhti hai.

---

## ✨ Features (Khususiyaat)

1. **🚀 Zero-Lag Modern UI/UX (Jetpack Compose Material 3):**
   * High performance `LazyColumn` with stable keys.
   * Asynchronous background app scanning & fast live search filtering.
   * Sleek dark theme with OLED slate and neon cyan accents.
   * Filter chips: *All Apps, Selected, Social, Rider / Delivery, Games & Media*.

2. **🛡️ System Accessibility Engine (`KeepAliveAccessibilityService`):**
   * Real-time foreground app monitoring via Android Accessibility OS events (`TYPE_WINDOW_STATE_CHANGED`).
   * **Prevent Screen Sleep:** Jab tak selected app screen par hai, screen lock ya dim nahi hogi (`WakeLock` + Invisible overlay).
   * **Ghost Touch (Anti-Idle Gestures):** Safe screen area par periodic harmless micro-touches inject karta hai taake Instagram, TikTok ya driver apps idle hokar band na hon.
   * **Auto-Relaunch (Optional):** Target app crash ya minimize hone par auto-reopen.

3. **☁️ GitHub Actions CI/CD (Cloud APK Builder):**
   * Jaise hi aap yeh code GitHub par push karenge, GitHub cloud par automatically APK build karega.
   * Aap bina Android Studio khole direct GitHub se ready-to-install `.apk` download kar sakte hain.

---

## 📂 Project Structure

```text
E:\AppAwake\
├── .github/workflows/
│   └── build-apk.yml               # GitHub Actions Auto-Build APK
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       │   └── xml/accessibility_service_config.xml
│       └── java/com/appawake/app/
│           ├── MainActivity.kt
│           ├── data/
│           │   ├── AppModel.kt
│           │   └── AppPreferencesRepository.kt
│           ├── service/
│           │   └── KeepAliveAccessibilityService.kt
│           └── ui/
│               ├── theme/ (Color.kt, Theme.kt, Type.kt)
│               ├── components/ (ServiceStatusCard, AppListItem, SettingsSheet)
│               └── screens/ (MainViewModel, MainScreen)
├── gradle/
└── gradlew & gradlew.bat
```

---

## 🚀 GitHub Par Push Karne Ka Tareeqa (To Get APK)

Aap apne terminal ya CMD se **`E:\AppAwake`** folder mein jakar yeh commands chalayein:

```bash
cd /mnt/e/AppAwake   # ya Windows CMD mein: cd E:\AppAwake

git init
git add .
git commit -m "Initial commit: AppAwake Accessibility keep-alive utility"
git branch -M main
git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/<YOUR_REPO_NAME>.git
git push -u origin main
```

### APK Kahan Se Download Hogi?
1. Apni GitHub repository open karein.
2. Upar **"Actions"** tab par click karein.
3. **"Build Android APK"** workflow run hota dikhega (~2-3 minutes).
4. Run complete hone ke baad neeche **Artifacts** section mein **`AppAwake-Debug-APK`** milega.
5. Us par click karein aur APK direct download kar ke apne phone mein install kar lein!

---

## 📱 Mobile Mein Kaise Setup Karein?

1. APK install karein aur App open karein.
2. Upar diye gaye **"Grant Accessibility Permission"** button par tap karein.
3. Phone ki Settings mein jakar **AppAwake** ko **Allow / Turn ON** kar dein.
4. List mein se **Instagram**, InDrive, ya jo bhi app awake rakhni ho usay **ON** kar dein.
5. Enjoy! Jab bhi wo app open hogi, phone ki screen lock nahi hogi aur app active rahegi!
