# Carpool App — Nanded ↔ Hyderabad

Spring Boot backend for a route-based carpooling app: car owners post rides,
passengers pick a specific seat and request it, owners accept/reject, and
live location is broadcast over WebSocket while a ride is ongoing.

## Requirements
- Java 17+
- Maven 3.8+
- MySQL 8 running locally (or update `application.properties`)

## Setup

1. Set the database credentials and a private JWT signing key in your shell.
   Do not commit these values:

   ```powershell
   $env:DB_USERNAME = "root"
   $env:DB_PASSWORD = "<your MySQL password>"
   $bytes = New-Object byte[] 32
   $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
   $rng.GetBytes($bytes)
   $rng.Dispose()
   $env:APP_JWT_SECRET = [Convert]::ToBase64String($bytes)
   $env:SMS_PROVIDER = "MOCK"
   ```

   The database (`carpool_db`) is auto-created on first run. `DB_URL` is
   optional and defaults to the local MySQL URL.

2. Build and run from the project directory using Java 17:
   ```
   mvn spring-boot:run
   ```
   The app starts on `http://localhost:8080`.

3. Tables are auto-created/updated by Hibernate (`ddl-auto=update`) —
   no manual SQL needed for local dev.

Ride dates are stored and returned as `DD/MM/YY` (for example `15/09/26`).
On startup, existing MySQL `DATE` values are copied into the new formatted
column; the old column is renamed to `ride_date_iso_legacy` and retained as
nullable legacy data. Ride times remain in 24-hour format in the API and
database, and are displayed in the browser with AM/PM.

## Trying it out (example flow with curl)

```bash
# 1. Sign up two users: an owner and a passenger
curl -X POST http://localhost:8080/api/auth/signup -H "Content-Type: application/json" \
  -d '{"name":"Owner Rao","phone":"9990001111"}'

curl -X POST http://localhost:8080/api/auth/signup -H "Content-Type: application/json" \
  -d '{"name":"Passenger Iqbal","phone":"9990002222"}'

# copy the "token" from the owner's signup response into $OWNER_TOKEN, and
# the passenger's into $PASSENGER_TOKEN for the calls below

# 2. Owner adds a car
curl -X POST http://localhost:8080/api/cars -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"model":"Swift Dzire","numberPlate":"MH26AB1234","totalSeats":4}'

# 3. Owner posts a ride (use the car id returned above)
curl -X POST http://localhost:8080/api/rides -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"carId":1,"origin":"Nanded","destination":"Hyderabad","pickupPoint":"Nanded Bus Stand","rideDate":"15/09/26","rideTime":"06:00:00","pricePerSeat":600}'

# 4. Anyone can browse upcoming rides (no auth — this is what your portfolio widget calls)
curl http://localhost:8080/api/rides/upcoming

# 5. Anyone can view a ride's seat map; passengers authenticate to request a seat
curl http://localhost:8080/api/rides/1

curl -X POST http://localhost:8080/api/rides/1/seats/1/request \
  -H "Authorization: Bearer $PASSENGER_TOKEN"

# 6. Owner accepts the request (use the request id returned above)
curl -X PATCH http://localhost:8080/api/requests/1/accept \
  -H "Authorization: Bearer $OWNER_TOKEN"
```

## Live location

- Owner's client connects to `ws://localhost:8080/ws` (SockJS/STOMP) and
  sends location frames to `/app/rides/{rideId}/location` with body
  `{"lat":19.15,"lng":77.32}`.
- Any subscriber to `/topic/rides/{rideId}/location` (e.g. a passenger's app)
  receives live updates.
- If you'd rather not wire up STOMP on the frontend yet, poll
  `GET /api/rides/{rideId}/location` instead — same underlying data.

## What's implemented
- JWT auth (signup/login)
- Cars with auto-generated seat layout
- Ride posting, search by route+date, ride detail with live seat map
- Public unauthenticated `/api/rides/upcoming` endpoint (for your portfolio site)
- Seat-specific booking requests with a 10-minute hold that auto-expires
  (see `SeatHoldExpiryScheduler`) so seats don't get stuck if an owner
  never responds
- Accept/reject/cancel flow
- Live location via WebSocket (STOMP) + REST polling fallback
- Ratings (post-ride, updates the rated user's average)

## Frontend

`frontend/index.html` is a single self-contained HTML/CSS/JS file (no build
step) that exercises every backend feature: signup/login, adding a car,
posting a ride, browsing/searching, a clickable seat map for booking a
specific seat, accepting/rejecting requests, live location (via WebSocket,
with a REST-polling fallback), ratings, and the public "upcoming rides"
portfolio widget preview.

The homepage refreshes available rides automatically and hides the ride-list
sections when there are no active rides. Expired rides remain in the database
for booking/history integrity but are not shown as available rides.

To use it:
1. Start the backend (`mvn spring-boot:run`) — it must be running on
   `http://localhost:8080` (or update the "API base" field at the top of
   the page to match).
2. Just open `frontend/index.html` directly in a browser — double-click it,
   no server needed.
3. The CORS config in the backend (`CorsConfig.java`) already allows
   requests from a `file://` origin, so this works out of the box.

With `SMS_PROVIDER=MOCK`, the OTP is printed to the backend console. The OTP
is never returned by the API or filled into the browser automatically.

For real SMS, set `SMS_PROVIDER` to `TWILIO`, `MSG91`, or `FAST2SMS` and provide
the corresponding credentials. MSG91 requires `MSG91_AUTH_KEY` and
`MSG91_TEMPLATE_ID`; Fast2SMS requires `FAST2SMS_API_KEY` and `FAST2SMS_OTP_ID`.
The provider account must have an approved OTP template. Missing or rejected
provider configuration causes the OTP request to fail instead of reporting a
false success.

Flow to try: sign up as an owner → add a car → post a ride → open a second
browser tab/profile, sign up as a passenger → search rides → choose a ride to
open its booking screen → select an available seat and submit the request.
The seat is held while the owner reviews it. In the owner's tab, accept or
reject the request from **My Rides & Requests**; the passenger can track the
request status in the same section.

## What's still worth adding
- Refresh tokens / token expiry handling on the frontend
- Rate limiting on `/api/auth/**`
- Push notifications (new ride on your route, request accepted)
- Payment integration (UPI deep link is enough for v1)
- A proper index on `(origin, destination, ride_date)` once there's real data volume
