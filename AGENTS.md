# Horizon Tourist (Android) - agent rules

Native Android app, Java + XML views, for the tourist role of the Horizon travel platform. Backend is a separate Spring Boot service; the full contract is in `docs/API_CONTRACT.md`. Build plan and prompts are in `docs/BUILD_PLAN.md`.

## Project facts
- Package/namespace/applicationId: `com.example.horizontourist`
- minSdk 24, targetSdk 37, compileSdk release(37), Java 11, Groovy Gradle (`build.gradle`), version catalog at `gradle/libs.versions.toml`
- Use `.\gradlew.bat` on Windows. Never switch to Kotlin, Kotlin DSL, Compose, Hilt, Room or Coroutines.

## Hard rules
1. Java only. XML layouts only. ViewBinding for every screen (no `findViewById`).
2. Dependencies: Retrofit 2 + Gson converter, OkHttp + logging-interceptor, Material Components, RecyclerView, SwipeRefreshLayout, ConstraintLayout, CardView. Add nothing else without asking. Put versions in `gradle/libs.versions.toml`.
3. Before using any library API, check the actual version added in the catalog. Do not invent methods.
4. Every class you reference must exist. Every `R.id.*`, `R.string.*`, `R.layout.*`, `R.drawable.*` you reference must exist. Every activity must be declared in `AndroidManifest.xml`.
5. All user-visible text goes in `res/values/strings.xml`. Colors in `colors.xml`, theme in `themes.xml`.
6. Network calls go through `ApiClient` / `ApiService` only. Callbacks must check `isAdded()` (fragments) or `isFinishing()`/`isDestroyed()` (activities) before touching views.
7. Handle every failure path: no network, timeout, 4xx/5xx with server `message`, 401 (clear session, go to login), empty lists (show empty-state view), loading (ProgressBar).
8. Only `TOURIST` role may log in.
9. Always send the logged-in tourist's own id as `touristId`; never let the user type it.
10. Keep dates as `String` from the API. No `java.time`.
11. No image loading library. No remote images (backend has no image support).
12. After every phase run `.\gradlew.bat assembleDebug` and `.\gradlew.bat lint`. Fix all errors before you say the phase is done. Show the final build result line.
13. Do not rewrite files from earlier phases unless the current phase needs a specific change; then make a minimal edit.
14. Do not create placeholder or TODO screens. Every screen listed in a phase must be fully wired to the API.

## Package layout
```
com.example.horizontourist
  data/model     request + response POJOs (Gson)
  data/api       ApiService, ApiClient, AuthInterceptor, ApiError helper, ApiCallback
  util           SessionManager, CartManager, Formatters, Validators
  ui/auth        LoginActivity, RegisterActivity, ForgotPasswordActivity, ResetPasswordActivity
  ui/main        MainActivity + tab fragments
  ui/slots       slot list, slot detail, cart, checkout
  ui/bookings    my bookings, booking detail, rate service
  ui/guides      guide list, guide detail, request guide, my guide bookings, rate guide, live location
  ui/groups      groups list, create, join, group detail + chat
  ui/profile     profile, edit profile, change password, notifications
  ui/ai          AI concierge
```
