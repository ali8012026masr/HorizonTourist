# Horizon Tourist (Android)

Native Android mobile application built with **Java + XML Layouts** for the tourist role of the **Horizon** travel platform. The app communicates with the separate Horizon Spring Boot backend service to provide service discovery, cart management, instant checkout, booking management, tour guide hiring with live tracking, group trip coordination with real-time chat, profile security, and an AI travel concierge.

---

## 1. Prerequisites

- **Java Development Kit (JDK)**: OpenJDK 11 or higher (JDK 17/21 compatible)
- **Android SDK**:
  - `compileSdk`: 37
  - `targetSdk`: 37
  - `minSdk`: 24 (Android 7.0 Nougat and above)
- **Build System**: Gradle 8.x with Gradle Wrapper (`.\gradlew.bat` on Windows)
- **IDE**: Android Studio Hedgehog / Iguana / Jellyfish / Ladybug or newer (optional; CLI builds fully supported)

---

## 2. Backend Service Setup

The app connects to the Horizon Spring Boot backend over HTTP/REST.

1. Ensure the Horizon backend service is running on port **`58080`**:
   ```bash
   # From your backend project directory
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=58080"
   # or
   gradle bootRun --args='--server.port=58080'
   ```
2. Verify backend availability by visiting:
   ```
   http://localhost:58080/api/slots
   ```

---

## 3. Network & `BASE_URL` Configuration

The app's API base URL is configured in [`app/build.gradle`](file:///app/build.gradle) under `defaultConfig`:

```groovy
buildConfigField "String", "BASE_URL", "\"http://10.0.2.2:58080/api/\""
```

### A. Android Emulator (Default)
When running in the official Android Studio Emulator, `10.0.2.2` automatically routes to your development PC's `localhost`:
- Base URL: `http://10.0.2.2:58080/api/` (no change required)

### B. Physical Android Device (LAN)
When testing on a physical phone connected over Wi-Fi:
1. Find your development PC's local LAN IP address (e.g. `192.168.1.150`):
   ```powershell
   ipconfig
   ```
2. Update `app/build.gradle`:
   ```groovy
   buildConfigField "String", "BASE_URL", "\"http://192.168.1.150:58080/api/\""
   ```
3. **Backend Host Binding**: Ensure the Spring Boot backend binds to `0.0.0.0` (all interfaces) rather than `127.0.0.1` only. In `application.properties` or `application.yml`:
   ```properties
   server.address=0.0.0.0
   server.port=58080
   ```
4. **Windows Firewall Inbound Rule**:
   Allow inbound TCP traffic on port `58080`:
   ```powershell
   New-NetFirewallRule -DisplayName "Horizon Backend Port 58080" -Direction Inbound -LocalPort 58080 -Protocol TCP -Action Allow
   ```

---

## 4. Test Accounts

- **Pre-seeded Accounts**: Refer to your backend `DataSeeder.java` for pre-configured tourist credentials (e.g., `tourist@example.com` / `password123`).
- **In-App Registration**: You can also register a brand-new tourist account directly from the app:
  1. Open the app and tap **Register** on the login screen.
  2. Enter Full Name, Email, Password (min 6 characters), and optional Phone/Address.
  3. The registration automatically assigns the `TOURIST` role and logs you in.
- **Role Enforcement**: Only users with the `TOURIST` role are permitted to authenticate in this app. Non-tourist roles (guides, service providers, admins) are rejected with an explicit error message.

---

## 5. Architecture Overview

- **Language & UI**: 100% Java 11 with XML layouts.
- **ViewBinding**: Used across 100% of screens (26 Activities, 5 tab Fragments, and custom ViewHolders). `findViewById` is strictly disallowed and unused.
- **Networking**:
  - **Retrofit 2** with **Gson converter**.
  - **OkHttp 4** client configured with:
    - `AuthInterceptor`: Automatically attaches `Bearer <token>` to authenticated endpoints, handles 401 Unauthorized responses by clearing user session and routing to `LoginActivity`.
    - `HttpLoggingInterceptor`: Logs network activity in debug builds.
- **State & Storage**:
  - `SessionManager`: Manages JWT tokens, authenticated tourist profile details, and session state via private `SharedPreferences` (`horizon_tourist_session.xml`).
  - `CartManager`: Manages in-memory and persisted shopping cart slots for multi-service checkout.
- **Lifecycle & Memory Safety**:
  - All asynchronous network callbacks (`ApiCallback<T>`) check `isFinishing()` / `isDestroyed()` (Activities) and `isAdded()` (Fragments) before interacting with UI views.
  - Zero static references to Activity, View, Context, or ViewBinding.
  - All background polling `Handler`s (`GroupChatActivity`, `GuideLocationActivity`, search debounce runnables) are explicitly cancelled in `onPause()` or `onDestroyView()`.
- **Resource Management**:
  - 100% externalized user-visible strings in `res/values/strings.xml`.
  - Material 3 theme and design tokens in `themes.xml` and `colors.xml`.
  - Android 12+ backup and extraction rules (`data_extraction_rules.xml` & `backup_rules.xml`) configured with `android:allowBackup="false"`.

---

## 6. Known Limitations & Architectural Notes

1. **No Remote Image Storage**: The backend does not host or process binary image files. The app renders styled vector drawables, initials-based colored avatar circles, and category iconography without external image loading libraries (Glide/Picasso).
2. **Polling vs Push Notifications / WebSockets**: Real-time features (group chat messages and guide live coordinate tracking) utilize interval polling with lifecycle-aware handlers (3 seconds for chat, 5 seconds for guide GPS location) rather than WebSockets or Firebase Cloud Messaging.
3. **No Offline Cache Mode**: The app operates in direct client-server mode against the live REST API without local database caching (Room/SQLite), ensuring strict data consistency for seat availability and transaction confirmations.

---

## 7. Building & Verifying

### Assemble Debug Build:
```powershell
.\gradlew.bat assembleDebug --no-daemon
```

### Run Static Analysis (Lint):
```powershell
.\gradlew.bat lint --no-daemon
```

### Assemble Release Build:
```powershell
.\gradlew.bat assembleRelease --no-daemon
```
