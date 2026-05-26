# Play Store — Assets & Listing Checklist

## 🔑 Credentials (KEEP SAFE — never share)

| Item | Value |
|------|-------|
| Application ID | `com.igarridosi.oroi` |
| Keystore file | `oroi-release.jks` (project root) |
| Key alias | `oroi-release-key` |
| Password | `OroiRelease2025` (see `keystore.properties`) |
| Validity | 10 000 days (~27 years) |

> ⚠️ **Back up `oroi-release.jks` + `keystore.properties` to a safe location outside the repo**
> (Google Drive, USB, password manager). Losing the keystore = cannot update the app ever again.

---

## 📦 Step 4 — Build the signed AAB

In Android Studio:
1. **Build → Generate Signed Bundle / APK**
2. Choose **Android App Bundle**
3. Select existing keystore → browse to `oroi-release.jks`
4. Alias: `oroi-release-key` / enter password
5. Build type: **release**
6. Output: `app/release/app-release.aab`

Or via terminal (once Gradle daemon issue is fixed):
```bash
./gradlew bundleRelease
```

---

## 🎨 Step 6 — Graphic Assets

### Required by Google Play

| Asset | Size | Format | Notes |
|-------|------|--------|-------|
| **App Icon** | 512 × 512 px | PNG, ≤1 MB, no alpha | High-res version of the launcher icon |
| **Feature Graphic** | 1024 × 500 px | PNG or JPG, ≤1 MB | Banner shown at top of store listing |
| **Phone screenshots** | 1080 × 1920 px (min 320px) | PNG or JPG | Minimum 2, maximum 8 |

### Recommended screenshots to take (in this order)

1. **Main screen** — subscription list with several entries visible
2. **Dark mode** — same screen in dark theme, showing the purple palette
3. **Cost carousel** — the monthly/annual/daily cost card
4. **Statistics screen** — donut chart with multiple subscriptions
5. **Widget** — home screen with the Oroi widget visible
6. **Add subscription** — the add flow with the dropdown open

### Tools to create assets

- **Icon 512px**: Export from Android Studio → Image Asset → Launcher Icon, or use Figma/Canva
- **Feature Graphic**: Canva template "Google Play Feature Graphic" (1024×500)
  - Suggestion: dark background (#0C0C14), purple gradient, "oroi" logo centered, tagline "Track your subscriptions"
- **Screenshots**: Take directly from emulator or device via Android Studio → Logcat → Camera icon

---

## 📝 Step 7 — Play Store Listing Copy

### Short description (max 80 chars)
```
Track all your subscriptions. Never miss a renewal.
```

### Full description (max 4000 chars) — suggested

```
oroi is a clean, minimal subscription tracker that helps you stay on top of every 
recurring payment — so you never get surprised by an unexpected charge again.

FEATURES

• Track all your subscriptions in one place — streaming, software, fitness, and more
• See your total monthly, annual, and daily spend at a glance
• Visual donut chart showing where your money goes each month
• Renewal reminders — get notified 2 days before any subscription charges you
• Monthly budget with a live progress bar so you know when you're overspending
• Home screen widget showing upcoming renewals at a glance
• Quick-access cancellation links for popular services
• Export your subscription list as CSV
• Full dark mode and light mode support
• Available in English, Spanish, and Basque

PRIVACY FIRST

oroi works entirely offline. No account required. No data is collected or shared.
Everything stays on your device.

---
Privacy Policy: https://oroi-privacy.netlify.app/privacy-policy.html
```

### Content rating
Complete the IARC questionnaire in Play Console — select:
- No violence, no sexual content, no gambling → **Rating: Everyone (PEGI 3 / Everyone)**

### Category
- **Finance** → Personal Finance

---

## ✅ Play Console Checklist

- [ ] Create app in Google Play Console (paid $25 one-time)
- [ ] Fill in store listing (title, description, screenshots, icon, feature graphic)
- [ ] Upload `app-release.aab`
- [ ] Set Privacy Policy URL: `https://oroi-privacy.netlify.app/privacy-policy.html`
- [ ] Complete content rating questionnaire
- [ ] Set category: Finance
- [ ] Set countries: All countries (or specific ones)
- [ ] Complete data safety form (answer: no data collected/shared)
- [ ] Submit for review (~3–7 days for first submission)
