# Horizon Tourist Android App - Build Plan and Antigravity CLI Prompts

## 0. How to use this file
1. Start the backend first (PostgreSQL `horizon` DB, Spring Boot on port 58080; `DataSeeder` gives test data). Confirm `http://localhost:58080/api/slots/search` returns JSON in a browser.
2. Open a terminal in `C:\Users\MT\OneDrive\Documents\JAVA\1295200\HorizonTourist` and start Antigravity CLI there.
3. Paste prompts one phase at a time, in order. Do not paste the next prompt until the current phase builds clean and you have run it on the emulator.
4. Commit to git after each green phase (`git init` first if needed). If a phase goes wrong, you can roll back.
5. If the CLI does not auto-load `AGENTS.md`, copy it to `GEMINI.md` or paste its content at the start of the session (check your CLI docs; I could not verify which file it reads).

## 1. Architecture decisions (fixed, so the model does not improvise)
| Topic | Decision | Reason |
|---|---|---|
| Language/UI | Java, XML, ViewBinding | Requested; ViewBinding removes id typos |
| Networking | Retrofit 2 + Gson + OkHttp | Standard, few moving parts |
| Structure | Activities for auth and detail screens; one `MainActivity` with BottomNavigationView and Fragments for tabs | Simple, no Navigation-component or SafeArgs plugin risk |
| State | No ViewModel/LiveData/Room. Fragments call Retrofit directly, `SessionManager` in SharedPreferences | Fewer generated-code errors |
| Dates | Strings, no `java.time` | minSdk 24, avoids desugaring setup |
| Images | None (backend has no image support) | Use icons/category chips |
| Map | Open guide location in Google Maps via `geo:` intent; osmdroid is an optional stretch | No API key needed |
| Push | Poll notifications on resume plus pull-to-refresh | No FCM setup |
| Token | Send `Authorization` + `x-refresh-token`; save `x-access-token` from responses | Matches backend auto-refresh |

## 2. Screens (mirrors the Angular tourist routes)
| Web route | Android screen |
|---|---|
| auth-gateway | LoginActivity, RegisterActivity, Forgot/ResetPasswordActivity |
| dashboard | HomeFragment |
| slots, slots/:id | SlotsFragment (search + filters), SlotDetailActivity |
| cart, payment, booking-confirmation | CartActivity, CheckoutActivity, BookingConfirmationActivity |
| bookings | BookingsFragment, BookingDetailActivity |
| guides, guides/:id, guides/request/:id | GuidesFragment, GuideDetailActivity, RequestGuideActivity, MyGuideBookingsActivity |
| tracking/live | GuideLocationActivity |
| ratings | RateServiceActivity, RateGuideActivity |
| group/* | GroupsFragment, CreateGroupActivity, JoinGroupActivity, GroupDetailActivity (members + chat) |
| profile | ProfileFragment, EditProfileActivity, ChangePasswordActivity, NotificationsActivity |
| ai-concierge | AiConciergeActivity |

Bottom tabs: Home, Explore (slots), Guides, Bookings, Profile. Groups, notifications, AI and cart are reached from Home and Profile.

## 3. Phases
| # | Phase | Done when |
|---|---|---|
| 1 | Foundation: Gradle, theme, manifest, network layer, models, session | Debug build succeeds, app launches to a splash router |
| 2 | Auth flow | Register, login, forgot/reset, logout all work against backend |
| 3 | Main shell + Home | Bottom nav works, Home shows profile summary, unread count, quick actions |
| 4 | Slots, cart, checkout, confirmation | Search, filter, book, pay end to end |
| 5 | Bookings, cancel, rate service | List, detail, cancel, rating work |
| 6 | Guides, guide booking, rate guide, location | Browse, request, pay, rate, open location |
| 7 | Groups + chat | Create, join by code, members, chat, mark paid, link booking |
| 8 | Profile, notifications, change password | All work; 401 handling verified |
| 9 | AI concierge | Chat, action buttons for each `actionType` |
| 10 | Hardening audit | Clean build + lint, all flows re-tested, README |

## 4. Prompts

### Prompt 0 - Orientation (paste first, every new session)
```
You are building the Horizon Tourist Android app in this project (Java, XML, Gradle Groovy DSL, package com.example.horizontourist).
Read these files fully before doing anything: AGENTS.md, docs/API_CONTRACT.md, docs/BUILD_PLAN.md, app/build.gradle, build.gradle, settings.gradle, gradle/libs.versions.toml, app/src/main/AndroidManifest.xml, and everything under app/src/main/res/values.
Then reply with: (1) a 10-line summary of the rules you must follow, (2) the list of files that currently exist under app/src/main/java, (3) anything in AGENTS.md or the API contract that is unclear or conflicts. Do not write or change any code in this step.
```

### Prompt 1 - Foundation
```
PHASE 1: Foundation. Follow AGENTS.md strictly.

Gradle:
- In gradle/libs.versions.toml add Retrofit 2.11.0, converter-gson 2.11.0, OkHttp 4.12.0 and logging-interceptor 4.12.0, recyclerview, swiperefreshlayout, cardview. Keep existing entries. Use the newest stable versions compatible with the existing AGP, and tell me which you chose.
- In app/build.gradle: enable buildFeatures { viewBinding true; buildConfig true }, add the dependencies via the catalog, and add buildConfigField "String" "BASE_URL" for debug = "\"http://10.0.2.2:58080/api/\"" and release = "\"https://api.horizon.example.com/api/\"".
- Do not change AGP, compileSdk, minSdk or targetSdk.

Manifest and resources:
- Add INTERNET and ACCESS_NETWORK_STATE permissions, android:usesCleartextTraffic via res/xml/network_security_config.xml that allows cleartext to 10.0.2.2, localhost and private LAN ranges for debug.
- Create a Material 3 theme (Theme.Material3.Light.NoActionBar parent), colors.xml with a travel palette (deep blue primary, teal secondary), a ic_launcher-safe change only to the theme, strings.xml with app_name "Horizon Tourist".
- Delete the default MainActivity content for now and keep a compilable MainActivity placeholder that will be replaced in Phase 3 (a bare activity with an empty layout is fine).

Code (package com.example.horizontourist):
- data/model: create ALL request and response POJOs from docs/API_CONTRACT.md (LoginRequest, LoginResponse, RegisterTouristRequest, Tourist, UpdateTouristRequest, ServiceSlot, Booking, BookingRequest, StatusRequest, Payment, PaymentRequest, Guide, GuideAvailability, GuideBooking, GuideBookingRequest, GuideRating, GuideRatingRequest, ServiceRating, ServiceRatingRequest, Group, GroupMember, GroupMessage, GroupCreateRequest, JoinGroupRequest, GroupBookingRequest, MessageRequest, Location, Notification, CountResponse, MessageResponse, ChangePasswordRequest, ForgotPasswordRequest, ResetPasswordRequest, AiChatRequest, AiChatMessage, AiChatResponse, ErrorResponse). Public fields or getters/setters, Gson annotations per the "Known unknowns" section (alternate names for isAvailable, isNegotiated, read). Money is double. Dates are String. Put enum values as String constants in a class named Constants.
- data/api/ApiService: a Retrofit interface with one method for EVERY endpoint in the contract, returning retrofit2.Call<T> (Call<Void> for 204).
- data/api/ApiClient: singleton building Retrofit with BuildConfig.BASE_URL, OkHttp with 30s timeouts, HttpLoggingInterceptor (BODY only in debug), and AuthInterceptor.
- data/api/AuthInterceptor: adds Authorization: Bearer <access> and x-refresh-token headers when a session exists (skip for /auth/login, /auth/register/tourist, /auth/forgot-password, /auth/reset-password); after each response, if header x-access-token is present, save it to SessionManager; if the response code is 401 on a non-auth path, clear the session and broadcast a local event so the UI can route to login (implement with a static listener in ApiClient that MainActivity/BaseActivity registers, main-thread posted).
- data/api/ApiCallback<T>: an abstract retrofit2.Callback wrapper with onSuccess(T), onError(String message, int code); parses ErrorResponse JSON from errorBody, uses friendly messages for IOException ("No connection to server"), timeouts, and 423 ("Account locked"). Never crash on empty or malformed bodies.
- util/SessionManager: SharedPreferences store for accessToken, refreshToken, touristId, fullName, email; isLoggedIn(), save(LoginResponse), updateAccessToken(), clear().
- util/Formatters: formatMoney(double) (Bangladeshi taka style "BDT 1,200"), formatDateTime(String iso) and formatDate(String iso) with safe fallback to the raw string, capitalizeEnum("MOBILE_BANKING") -> "Mobile banking".
- util/Validators: email, password (>= 6), non-empty.
- Application subclass HorizonApp that initializes SessionManager and ApiClient, declared in the manifest.
- ui/SplashActivity (launcher): no UI logic beyond routing to LoginActivity if not logged in else MainActivity. Since Login does not exist yet, create minimal compilable stubs LoginActivity and MainActivity that the next phases will replace.

Verify: run .\gradlew.bat assembleDebug, fix every error and warning about missing symbols, then run .\gradlew.bat lint and fix errors. Report the final BUILD SUCCESSFUL line and a file tree of what you created. Do not start Phase 2.
```

### Prompt 2 - Auth
```
PHASE 2: Authentication. Follow AGENTS.md. Do not modify Phase 1 files except for minimal fixes and list any you touch.

Build fully working, validated, polished screens with Material TextInputLayout:
- LoginActivity: email, password (with show/hide toggle), Login button with progress state (disable button while loading), links to Register and Forgot password. On success: if LoginResponse.role is not "TOURIST" show "This app is for tourists only" and do not save session; else SessionManager.save, go to MainActivity, clear back stack. Map 401 -> "Invalid email or password", 423 -> "Account locked, try again later", others -> server message.
- RegisterActivity: fullName, email, phone (optional), address (optional), password, confirm password; client validation matching the contract; on 201 show success message and return to Login with the email prefilled; on 409 show "Email already registered" on the email field; show per-field errors from ErrorResponse.errors when present.
- ForgotPasswordActivity: email -> POST forgot-password -> show the returned message; button then leads to ResetPasswordActivity.
- ResetPasswordActivity: token field (user pastes the emailed token) + new password + confirm; on success go to Login. Also handle an incoming deep link horizonTourist://reset?token=... by prefilling token (add the intent-filter).
- SplashActivity routing: logged in -> MainActivity else LoginActivity.
- Logout helper in SessionManager/BaseActivity that clears session and returns to Login with FLAG_ACTIVITY_CLEAR_TASK.
- Create BaseActivity with the global 401 listener registered in onStart and unregistered in onStop, redirecting to Login with a Toast "Session expired".
- Handle keyboard (adjustResize, ScrollView roots), rotation (no crash, fields preserved via onSaveInstanceState or android:configChanges is NOT allowed; rely on default state saving), and double-tap protection.

Add all strings to strings.xml. Declare all activities in the manifest. Verify with .\gradlew.bat assembleDebug and lint; fix everything. Then give me a manual test checklist (10 steps) for the emulator. Do not start Phase 3.
```

### Prompt 3 - Shell and Home
```
PHASE 3: Main shell and Home. Follow AGENTS.md.

- Replace MainActivity with a real one extending BaseActivity: activity_main.xml with a FragmentContainerView-free design (FrameLayout container) plus BottomNavigationView (menu res/menu/bottom_nav.xml with Home, Explore, Guides, Bookings, Profile and vector icons you create in res/drawable). Use FragmentManager show/hide or replace with tag-based fragment reuse so tab switches keep state, and save the selected tab across rotation.
- Create fragments with their own layouts, all extending a small BaseFragment (helpers: showLoading, showError, showEmpty, toast, isSafe()): HomeFragment (fully built now), and simple but complete placeholders ONLY for SlotsFragment, GuidesFragment, BookingsFragment, ProfileFragment that show a title and an empty-state view, to be replaced in later phases. (This is the one place placeholders are allowed.)
- HomeFragment: greeting with the tourist name, loyalty points card (GET /tourists/{id}), unread notification badge (GET notifications unread-count), grid of quick-action cards: Browse services, Find a guide, My bookings, My groups, Cart, AI Concierge. Cards for screens not built yet must start the target Activity class if it exists, otherwise show a Toast "Coming soon" via a single helper so Phase-N prompts only need to swap the target. SwipeRefreshLayout to refresh.
- Toolbar-style header with the app name and a cart icon with item count from util/CartManager (create CartManager now: in-memory + SharedPreferences persistence of cart items as JSON via Gson; a CartItem holds slot id, title, price, quantity fixed at 1 per slot, addedAt; methods add, remove, clear, getItems, getTotal, contains, count).

Verify with assembleDebug and lint. Provide a manual test checklist. Do not start Phase 4.
```

### Prompt 4 - Slots, cart, checkout
```
PHASE 4: Service slots, cart, checkout. Follow AGENTS.md.

Before coding, open docs/API_CONTRACT.md and note: booking is POST /bookings {touristId, serviceSlotId}; payment is POST /payments {bookingId, amount, method}; a 409 means the slot is unavailable.

- Replace SlotsFragment: search bar (locationName/destination text), filter bottom sheet or dialog with category (ServiceCategory chips, single select plus "All"), origin, destination, min/max price, date from/to (MaterialDatePicker; convert to ISO date-time strings like 2026-10-01T00:00:00), Apply and Clear. RecyclerView of cards showing category icon, "origin -> destination" or locationName, provider name with rating, start/end time, price, seats left with a color chip, status. Only OPEN slots have an enabled action. Pull-to-refresh, empty state, error state with Retry, debounce on text search (400 ms).
- SlotDetailActivity (opened with EXTRA slot id, loads GET /slots/{id}): all fields, provider rating list from GET /ratings/provider/{providerId} (show average and comments, empty state), "Add to cart" (disabled if not OPEN or 0 seats or already in cart) and "Book now".
- CartActivity: list from CartManager with remove, total, empty state, "Checkout". Refresh each slot from the server on open and flag items that are no longer OPEN or changed price.
- CheckoutActivity: summary, payment method selector (CARD, MOBILE_BANKING, CASH), Pay button. For each cart item sequentially: POST /bookings then POST /payments with the booking's totalAmount; track per-item result; on partial failure show exactly which items failed and keep only failed ones in the cart; never double-book an item that already succeeded (store created booking ids in memory for this checkout and retry only the payment when the booking exists but payment failed). Disable UI while running.
- BookingConfirmationActivity: shows booking ids, totals, statuses; buttons "View my bookings" (go to MainActivity with Bookings tab selected) and "Continue exploring".
- "Book now" from SlotDetail skips the cart and goes straight to Checkout with that single item.
- Wire HomeFragment cards (Browse services, Cart) and the header cart icon to the real activities.

Verify with assembleDebug and lint. Provide a manual test checklist covering a full booking and a 409 case. Do not start Phase 5.
```

### Prompt 5 - Bookings and ratings
```
PHASE 5: My bookings, cancel, rate service. Follow AGENTS.md.

- Replace BookingsFragment with two tabs (TabLayout + ViewPager2 is NOT allowed to avoid extra dependencies; use two MaterialButtonToggle-style segmented buttons switching between "Services" and "Guides" lists in the same fragment). Services list from GET /bookings/tourist/{id}, guides list from GET /guide-bookings/tourist/{id} (guide-booking rows are completed in Phase 6; here just render the list read-only with guide name resolved via GET /guides/{id} cached in a map, status chip, date, price).
- Each booking card: id, slot summary (fetch GET /slots/{id} lazily, cache in a map, show placeholder text while loading), booking date, total, status chip color per BookingStatus, payment status chip.
- BookingDetailActivity: booking details, slot info, payments list from GET /payments/booking/{bookingId}, actions: Cancel (only PENDING or CONFIRMED; confirmation MaterialAlertDialog; PUT /bookings/{id}/status {status: "CANCELLED"}; handle 409), Pay now (if paymentStatus UNPAID and status not CANCELLED -> payment method dialog -> POST /payments), Rate service (only when status COMPLETED).
- RateServiceActivity: star RatingBar 1-5, comment, POST /ratings/service {bookingId, touristId, providerId (from the slot), score, comment}; handle 409/400 (already rated) with a friendly message.
- Filter chips on the list: All, Pending, Confirmed, Completed, Cancelled (client-side).

Verify with assembleDebug and lint. Provide a manual test checklist. Do not start Phase 6.
```

### Prompt 6 - Guides
```
PHASE 6: Guides, guide bookings, rating, live location. Follow AGENTS.md.

- Replace GuidesFragment: toggle "Available now" (GET /guides?available=true/false), search by name/location/language (client-side), sort by rating or price, cards with name, rating stars and count, experience years, languages chips, default price, "Negotiable" badge, verification badge. Pull-to-refresh, empty and error states.
- GuideDetailActivity: GET /guides/{id} + GET /guide-availability/guide/{id} (list OPEN slots by date/time/location/notes) + GET /ratings/guide/{id}. Buttons: "Request this guide" and, only if the tourist has an ACCEPTED guide booking with this guide, "View live location".
- RequestGuideActivity: choose an availability (spinner/list of OPEN ones) or a custom date (MaterialDatePicker, not in the past), price field prefilled with defaultPrice (editable only if guide.negotiable; if edited and differs then isNegotiated = true), POST /guide-bookings. On success go to MyGuideBookingsActivity.
- MyGuideBookingsActivity (also embedded list source for BookingsFragment's Guides segment; make BookingsFragment's guide segment tappable and open this detail flow): status chips per GuideBookingStatus, actions per status: ACCEPTED and not paymentReceived -> Pay (POST /payments {guideBookingId, amount: agreedPrice, method}); ACCEPTED -> View location; COMPLETED -> Rate guide. REQUESTED/DECLINED are read-only. Tourist cannot cancel a guide booking (no endpoint) so do not show a cancel button.
- RateGuideActivity: stars + comment, POST /ratings/guide {guideBookingId, score, comment}; handle duplicates.
- GuideLocationActivity: GET /guides/{id}/location, show lat/lng, last updated, guide name; buttons "Open in Maps" (geo:lat,lng?q=lat,lng(Guide name) intent with a resolveActivity/try-catch fallback to a browser https://www.google.com/maps/search/?api=1&query=lat,lng) and "Refresh"; auto-refresh every 15 s while visible (Handler, stopped in onPause); 403/404 -> friendly message that tracking is available only after the guide accepts.
- Wire the Home card "Find a guide".

Verify with assembleDebug and lint. Provide a manual test checklist. Do not start Phase 7.
```

### Prompt 7 - Groups and chat
```
PHASE 7: Groups and group chat. Follow AGENTS.md.

- GroupsFragment is not a tab; create GroupsActivity opened from Home ("My groups"): list from GET /groups/tourist/{id} with name, member count, join code (tap to copy with ClipboardManager), linked booking indicator. FAB opens a bottom choice: Create group / Join group.
- CreateGroupActivity: name -> POST /groups {groupName, createdByTouristId}; then show the join code with Copy and Share (ACTION_SEND text) buttons.
- JoinGroupActivity: join code (uppercase, trimmed) -> POST /groups/join; 404 -> "Invalid join code".
- GroupDetailActivity: GET /groups/{id}; header with name and join code; members list (GET /groups/{id}/members) with name, status, amountOwed, paid chip; actions: "Mark my share paid" (PUT /groups/{id}/members/{touristId}/paid for own id), "Leave group" (DELETE /groups/{id}/members/{myId}, confirm dialog), "Link a booking" (creator only; choose from my PENDING/CONFIRMED bookings via GET /bookings/tourist/{id}, POST /groups/{id}/booking), and a Chat section or button opening GroupChatActivity.
- GroupChatActivity: GET /groups/{id}/messages, RecyclerView with bubbles (own messages right-aligned by senderId == myId), input + send (POST /groups/{id}/messages {touristId: myId, message}), auto-scroll to bottom, poll every 5 s while the screen is visible (Handler stopped in onPause/onStop), avoid duplicate rendering by diffing on message id, disable send on blank text, handle 403 (removed from group).
- Wire Home "My groups".

Verify with assembleDebug and lint. Provide a manual test checklist that uses two emulator/user accounts. Do not start Phase 8.
```

### Prompt 8 - Profile, notifications, security
```
PHASE 8: Profile, notifications, account. Follow AGENTS.md.

- Replace ProfileFragment: avatar circle with initials (drawn view, no image), name, email, phone, address, loyalty points, status. Rows: Edit profile, Change password, Notifications (with unread badge), My groups, Ratings I gave (not required), Logout (confirm dialog), Delete account (double confirm; DELETE /tourists/{id}; clear session on 204).
- EditProfileActivity: fullName, phone, address -> PUT /tourists/{id}; update SessionManager name; show success and return; refresh the profile on return.
- ChangePasswordActivity: current, new, confirm -> PUT /auth/change-password. The backend invalidates old tokens after a password change, so after success clear the session and send the user to Login with a message "Password changed, please log in again".
- NotificationsActivity: GET /notifications/user/{id}, unread items bold with a dot, tap marks read (PUT /notifications/{id}/read) and updates the row optimistically, pull-to-refresh, empty state, and a menu action "Mark all as read" that loops over unread items.
- Verify global 401 handling end to end: describe how you tested by temporarily forcing an invalid token and confirm the app returns to Login without crashing.

Verify with assembleDebug and lint. Provide a manual test checklist. Do not start Phase 9.
```

### Prompt 9 - AI concierge
```
PHASE 9: AI Concierge. Follow AGENTS.md.

AiConciergeActivity: chat UI with message bubbles (user right, assistant left), text input (max 1000 chars with counter), send button, typing indicator while waiting. Keep history in memory as a list of {role: "user"|"model", text}; send only the last 12 turns (POST /ai/chat {userMessage, history}). Handle 429 ("Too many requests, wait a minute"), 403, 400 and network errors as assistant-style error bubbles without adding them to history.

Handle actionType from the response:
- FILTER_SLOTS: parse actionPayloadJson with Gson into a Map; show a button under the reply "Show matching services" that opens MainActivity on the Explore tab with the payload keys (destination, origin, category, maxPrice, minPrice, locationName) passed as intent extras and applied as filters in SlotsFragment (add that intent-driven filter support to SlotsFragment).
- RECOMMEND_BUNDLE: payload {slotId, guideId}; show two mini cards (fetch GET /slots/{slotId} and GET /guides/{guideId}) with buttons to open SlotDetailActivity and GuideDetailActivity.
- RECOMMEND_GROUP_BUNDLE: same as bundle plus a button to Create group (opens CreateGroupActivity).
- NONE: text only.
Wrap all JSON parsing in try/catch and fall back to text-only replies. Payload values can be numbers or strings; handle both. Add the floating entry point on Home ("AI Concierge" card) and wire it.

Verify with assembleDebug and lint. Provide a manual test checklist. Do not start Phase 10.
```

### Prompt 10 - Hardening audit
```
PHASE 10: Hardening and audit. Do not add features.

1. Run .\gradlew.bat clean assembleDebug lint and fix every error. Review lint warnings and fix all of severity Error and any HardcodedText, MissingTranslation, UnusedResources that are real.
2. Audit every file you created against AGENTS.md rules. Produce a table: rule number -> pass/fail -> fixes made.
3. Verify programmatically with grep: every Activity class in src/main/java extending Activity/AppCompatActivity is in AndroidManifest.xml; every R.string used exists; no findViewById; no java.time imports; no TODO or "Coming soon" left except intentionally documented ones.
4. Null-safety pass: for every Retrofit callback ensure body null checks, view access guarded by isAdded()/isFinishing(), and no static references to Activities/Views (memory leaks); Handlers stopped in onPause/onStop.
5. Configuration: rotation on every screen must not crash; check that lists survive rotation or reload gracefully.
6. Add res/xml/data_extraction_rules and backup rules check to remove lint backup warnings; set android:allowBackup="false".
7. Write README.md with: prerequisites, how to run the backend, how to change BASE_URL (emulator vs real device using the PC LAN IP, and the need for the backend to listen on 0.0.0.0 and Windows Firewall to allow port 58080), test accounts (tell me to fill in from DataSeeder), architecture overview, and known limitations (no image support, polling instead of push, no offline mode).
8. Build a release variant with .\gradlew.bat assembleRelease (unsigned is fine) to confirm no minify/resource errors; if minify is off leave it off.
Report final results with the command output summary lines.
```

## 5. Repair prompt (use whenever a build fails)
```
The build failed. Do not rewrite the project. Run .\gradlew.bat assembleDebug --stacktrace yourself, read the FIRST error only, open the exact file and line it names, and fix the root cause with the smallest possible edit. Then rebuild. Repeat until BUILD SUCCESSFUL. Do not silence errors with @SuppressWarnings, do not delete features to make it compile, and do not change dependency versions unless the error proves a version conflict (then tell me why). Report each error and the fix.
```

## 6. Bug-fix prompt (runtime/behavior problems)
```
Bug: <describe screen, steps, expected, actual>. Logcat excerpt: <paste the few relevant lines>.
Reproduce by reading the code path (fragment/activity -> ApiService call -> model class -> layout). Identify the root cause before editing. Check the JSON field names in docs/API_CONTRACT.md against the Gson model (field name or @SerializedName mismatch is the most likely cause of null values). Make a minimal fix, rebuild, and tell me exactly how to verify it.
```

## 7. Checks that Antigravity cannot do for you
- **Backend gaps**: the backend does not check that `touristId` in booking, payment, guide-booking, rating and notification calls matches the logged-in user. The app sends the correct id, but that is a server-side hole worth fixing before production.
- **Real device**: the backend CORS setting does not matter, but the server must listen on the LAN and Windows Firewall must allow port 58080.
- **Password reset email**: needs `MAIL_USERNAME`/`MAIL_PASSWORD` env vars on the backend. The reset link points to the web app, so the pasted-token screen (or the `horizonTourist://reset` deep link) is the Android path.
- **Wire-name check**: before Phase 2, open `http://localhost:58080/api/guides` in a browser and confirm whether the boolean fields are `isAvailable` or `available`; the alternates in the models cover both, but confirm.
- **Model output**: the CLI model can still make mistakes. The build gate after each phase and your emulator test are the real safety net, so never batch phases.
