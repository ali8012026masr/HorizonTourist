# Horizon Backend API Contract (tourist scope only)

Source of truth: Spring Boot backend (`horizon.example.demo`). Copied here so the Android agent needs no access to the web project.

## Connection
- Emulator base URL: `http://10.0.2.2:58080/api/` (trailing slash required by Retrofit). Real device: `http://<PC-LAN-IP>:58080/api/`.
- Plain HTTP, so the app needs a `network_security_config` allowing cleartext for dev hosts, plus `INTERNET` permission.
- CORS does not apply to native apps.
- All error bodies: `{ "timestamp": "...", "status": 409, "message": "...", "errors": { "field": "msg" } }` (`errors` optional).
- Status codes: 400 validation, 401 bad credentials/token, 403 forbidden, 404 not found / invalid join code, 409 conflict (duplicate email, slot unavailable, bad status transition), 423 account locked, 429 AI rate limit, 503 email failure.

## Auth model (stateless JWT)
- `POST /auth/login` returns `accessToken` (15 min) and `refreshToken` (1 year).
- Send both on every authenticated request: `Authorization: Bearer <access>` and `x-refresh-token: <refresh>`.
- If the access token is expired but the refresh token is valid, the server still authenticates the request and returns a fresh access token in the response header `x-access-token`. The client must read that header on every response and overwrite the stored access token.
- Role string for tourists is `TOURIST`. Only `TOURIST` role users may use this app; reject other roles at login with a clear message.
- The logged-in tourist id is `LoginResponse.roleDetails.id` (also `LoginResponse.id`). Use it as `touristId` in all calls.
- The backend does not enforce ownership on bookings, payments, guide-bookings, ratings and notifications. The client must always send the logged-in user's own id.

## Public endpoints (no token)
| Method | Path | Notes |
|---|---|---|
| POST | `/auth/register/tourist` | body `{fullName, email, password, phone?, address?}` -> 201 TouristResponse |
| POST | `/auth/login` | body `{email, password}` -> LoginResponse |
| POST | `/auth/forgot-password` | body `{email}` -> `{message}` |
| POST | `/auth/reset-password` | body `{token, newPassword}` (min 6) -> `{message}` |
| GET | `/slots/search` | query: `category, origin, destination, locationName, dateFrom, dateTo` (ISO date-time), `minPrice, maxPrice` -> ServiceSlot[] |
| GET | `/slots/{id}` | ServiceSlot |
| GET | `/guide-availability/search` | query: `dateFrom, dateTo` (ISO date), `location` -> GuideAvailability[] |
| GET | `/guide-availability/guide/{guideId}` | GuideAvailability[] |
| GET | `/guides` | query `available` (bool, default true) -> Guide[] |
| GET | `/guides/{id}` | Guide |
| GET | `/ratings/guide/{guideId}` | GuideRating[] |
| GET | `/ratings/provider/{providerId}` | ServiceRating[] |

## Authenticated endpoints
| Method | Path | Body -> Response |
|---|---|---|
| PUT | `/auth/change-password` | `{currentPassword, newPassword(min 6)}` -> `{message}` |
| GET | `/tourists/{id}` | Tourist (self) |
| PUT | `/tourists/{id}` | `{fullName?, phone?, address?}` -> Tourist |
| DELETE | `/tourists/{id}` | 204 |
| POST | `/bookings` | `{touristId, serviceSlotId}` -> 201 Booking |
| GET | `/bookings/tourist/{touristId}` | Booking[] |
| PUT | `/bookings/{id}/status` | `{status}` -> Booking (tourist uses `CANCELLED`) |
| POST | `/payments` | `{bookingId?, guideBookingId?, amount(>0), method}` -> 201 Payment |
| GET | `/payments/{id}` | Payment |
| GET | `/payments/booking/{bookingId}` | Payment[] |
| POST | `/guide-bookings` | `{touristId, tourGuideId, guideAvailabilityId?, scheduleDate(yyyy-MM-dd), agreedPrice, isNegotiated}` -> 201 GuideBooking |
| GET | `/guide-bookings/tourist/{touristId}` | GuideBooking[] |
| POST | `/ratings/guide` | `{guideBookingId, score 1-5, comment?}` -> 201 GuideRating |
| POST | `/ratings/service` | `{bookingId, touristId, providerId, score 1-5, comment?}` -> 201 ServiceRating |
| POST | `/groups` | `{groupName, createdByTouristId}` -> 201 Group |
| POST | `/groups/join` | `{joinCode, touristId}` -> Group |
| GET | `/groups/{id}` | Group (members only) |
| GET | `/groups/tourist/{touristId}` | Group[] |
| GET | `/groups/{id}/members` | GroupMember[] |
| DELETE | `/groups/{id}/members/{touristId}` | 204 |
| POST | `/groups/{id}/booking` | `{bookingId}` -> Group |
| PUT | `/groups/{id}/members/{touristId}/paid` | GroupMember |
| GET | `/groups/{id}/messages` | GroupMessage[] |
| POST | `/groups/{id}/messages` | `{touristId, message}` -> 201 GroupMessage (touristId must equal caller) |
| GET | `/guides/{id}/location` | Location (only for a tourist with an ACCEPTED guide booking) |
| GET | `/notifications/user/{userId}` | Notification[] |
| GET | `/notifications/user/{userId}/unread-count` | `{count}` |
| PUT | `/notifications/{id}/read` | Notification |
| POST | `/ai/chat` | TOURIST only. `{userMessage(<=1000 chars), history:[{role:"user"|"model", text}] (<=12)}` -> `{replyText, actionType, actionPayloadJson}` |

AI details: `actionType` is one of `FILTER_SLOTS`, `RECOMMEND_BUNDLE`, `RECOMMEND_GROUP_BUNDLE`, `NONE`. `actionPayloadJson` is a JSON string, for example `{"destination":"..","maxPrice":..}` or `{"slotId":..,"guideId":..}`. Limit: 10 requests per minute per tourist (429).

## Response shapes
- **LoginResponse**: `{id, fullName, email, role, roleDetails(Tourist), accessToken, refreshToken, expiresInSeconds}`
- **Tourist**: `{id, fullName, email, phone, address, status, loyaltyPoints}`
- **ServiceSlot**: `{id, providerId, providerBusinessName, category, origin, destination, locationName, startDateTime, endDateTime, capacity, availableSeats, price, status, providerRatingAvg, providerRatingCount}`
- **Booking**: `{id, touristId, serviceSlotId, bookingDate, status, totalAmount, paymentStatus}`
- **Payment**: `{id, bookingId, guideBookingId, amount, method, status, transactionDate}`
- **Guide**: `{id, fullName, email, phone, address, status, nationalId, bio, ratingAvg, ratingCount, isAvailable, verificationStatus, rejectionReason, languages[], location, experienceYears, defaultPrice, negotiable}`
- **GuideAvailability**: `{id, tourGuideId, tourGuideName, date, startTime, endTime, location, notes, status, tourGuideRatingAvg, tourGuideRatingCount}`
- **GuideBooking**: `{id, touristId, tourGuideId, guideAvailabilityId, scheduleDate, agreedPrice, isNegotiated, status, paymentReceived}`
- **GuideRating**: `{id, guideBookingId, score, comment, ratedDate}`
- **ServiceRating**: `{id, bookingId, touristId, providerId, score, comment, ratedDate}`
- **Group**: `{id, groupName, createdByTouristId, joinCode, bookingId, createdAt, members: GroupMember[]}`
- **GroupMember**: `{id, touristId, touristName, status, joinedAt, amountOwed, paid}`
- **GroupMessage**: `{id, senderId, senderName, message, sentAt}`
- **Location**: `{tourGuideId, latitude, longitude, updatedAt}`
- **Notification**: `{id, message, type, read, createdAt}`

## Enums (send/receive as strings)
- BookingStatus: PENDING, CONFIRMED, CANCELLED, COMPLETED
- PaymentStatus (booking.paymentStatus): UNPAID, PAID, REFUNDED
- PaymentTxStatus: PENDING, SUCCESS, FAILED, REFUNDED
- PaymentMethod: CARD, MOBILE_BANKING, CASH
- GuideBookingStatus: REQUESTED, ACCEPTED, DECLINED, COMPLETED
- ServiceCategory: BUS, MICROBUS, LAUNCH, TRAIN, AIRPLANE, SHIP, HOTEL, RESORT, CONVENTION_CENTER, BUFFET, AMUSEMENT_PARK
- SlotStatus: OPEN, FULL, CLOSED, CANCELLED
- GroupMemberStatus: INVITED, JOINED, DECLINED
- UserStatus: ACTIVE, PENDING_VERIFICATION, SUSPENDED, REVOKED
- AvailabilityStatus: OPEN, BOOKED, CANCELLED

## Known unknowns (agent must handle defensively)
1. Boolean wire names. Fields `isAvailable`, `isNegotiated` may serialize as `available` / `negotiated`. In Java models use `@SerializedName(value = "isAvailable", alternate = {"available"})` and the same pattern for `isNegotiated`/`negotiated`; `read` in Notification may also arrive as `isRead`.
2. Date/time values are ISO-8601 strings. Keep them as `String` in models and format for display with `SimpleDateFormat`/`String` handling (no `java.time`, minSdk is 24).
3. Money (`BigDecimal`) arrives as JSON numbers. Use `double` in models.
4. No image upload exists in the backend. The app must not show remote images; use icons and category placeholders.
