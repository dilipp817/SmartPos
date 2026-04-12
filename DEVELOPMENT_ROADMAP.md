# 🗓️ SmartPos Development Roadmap

**Last Updated:** April 12, 2026
**API Contract:** ✅ Locked — 28 endpoints (100/100 review, April 4, 2026)
**Multi-outlet update:** April 11, 2026 — `restaurantId` in all auth responses

---

## ✅ Phase 0: Architecture Foundation (COMPLETED)
- [x] Clean architecture setup (multi-module: app, core, data, domain, feature/food, ui-components)
- [x] Hilt DI configuration
- [x] Result & UiState patterns
- [x] Repository pattern
- [x] Use cases layer
- [x] Basic navigation
- [x] Type-safe error handling
- [x] Build: SUCCESSFUL ✅

---

## ✅ Phase 1: Design System & Components (COMPLETED)
- [x] Material 3 Theme — `Color.kt`, `Type.kt`, `Theme.kt`
- [x] Button Components — `Buttons.kt`, `buttons/PrimaryButton.kt`
- [x] Card Components — `Cards.kt`, `cards/CardComponents.kt`
- [x] Input / Text Fields — `InputFields.kt`, `fields/TextFieldComponents.kt`
- [x] Dialog Components — `dialogs/DialogComponents.kt`
- [x] Loading / Skeleton — `loaders/LoaderComponents.kt`
- [x] List Components — `list/ListItemComponents.kt`, `InfiniteScroll.kt`
- [x] Layout Components — `layout/LayoutComponents.kt`
- [x] Badge Components — `badges/BadgeComponents.kt`

---

## ✅ Phase 2: Food Browsing & Local Cart (COMPLETED)

### 2.1 Food List Screen ✅
- [x] `FoodScreen.kt` — paginated food grid
- [x] `FoodGridCard.kt`, `FoodGridSection.kt`
- [x] Search integrated via `SearchFilterPanel.kt` (panel inside FoodList — not a separate screen)
- [x] `SortDialog.kt` — sort by price/name asc/desc
- [x] Filters — category, vegetarian, spicy
- [x] `FoodViewModel.kt` + `FoodUiState.kt`
- [x] `GetFoodsPaginatedUseCase`, `SearchFoodsPaginatedUseCase`, `SearchFoodsUseCase`

### 2.2 Food Detail Screen ✅
- [x] `FoodDetailScreen.kt`
- [x] `FoodDetailViewModel.kt` — uses `GetFoodByIdUseCase`
- [x] `FoodDetailRoute.kt` — navigation wired up
- [x] Add To Cart action

### 2.3 Local Cart (Room-based) ✅
> ⚠️ Cart is **LOCAL ONLY** — there is no backend cart API.
> Items are stored in Room DB. Checkout = `POST /orders` to the backend.

- [x] `CartItem` domain model
- [x] `CartRepository` interface + `CartRepositoryImpl` (Room)
- [x] Cart use cases: `AddToCartUseCase`, `GetCartUseCase`, `IncreaseCartQuantityUseCase`, `DecreaseCartQuantityUseCase`, `ClearCartUseCase`
- [x] `CartSummaryFooter.kt` — shows item count + running total

---

## 🔐 Phase 3: Authentication & Session Management (NEXT — Week 1)

> ⚠️ **Must complete before Phase 4.**
> Every table / order / payment API call requires `restaurantId` obtained from login.

### 3.1 Login Screen ✅
- [x] `LoginScreen.kt` — username + password, polished tablet UI
- [x] `LoginViewModel.kt` — uses `LoginUseCase`
- [x] Include optional `deviceId` + `deviceType` in login request
  - `deviceId` = `Settings.Secure.ANDROID_ID` (stable per device, read in `LoginViewModel`)
  - `deviceType` = `"tablet"` (hardcoded — this app targets tablet POS counters only)
  - Backend stores which physical device is logged in (for multi-counter analytics)
- [x] Show loading / error states
- [x] On success → navigate to Home

**API:** `POST /api/v1/auth/login`
```json
{
  "username": "counter_1",
  "password": "password123",
  "deviceId": "tablet-counter-1",
  "deviceType": "tablet"
}
```

### 3.2 Session Storage ✅

> ⚠️ **`restaurantId` MUST come from the login response — NEVER hardcode it.**
> Save it to `SessionDataStore` immediately after login.
> All restaurant-scoped API calls use this saved value via `GetRestaurantIdUseCase`.

- [x] Save `{ token, restaurantId, role, userId }` to `SessionDataStore` on login (already exists)
- [x] `GetRestaurantIdUseCase` — inject in every ViewModel that calls a restaurant-scoped API
- [x] Token persists across app restarts (DataStore already set up)

### 3.3 Session Recovery on App Start

> If a token exists in DataStore but `restaurantId` is missing locally:
> Call `GET /auth/me` → extract `restaurantId` → save. **Do NOT force re-login.**

- [ ] `RecoverSessionUseCase` (scaffolded — wire it into app startup)
- [ ] `MainViewModel`: on app start → token present? → recover session → navigate to Home
- [ ] Token expired / invalid → navigate to Login

### 3.4 Logout
- [ ] `LogoutUseCase` — clears all session data from DataStore
- [ ] Navigate to `LoginScreen` on logout

### 3.5 Role-Aware UI (enforcement at API level in v2)

| Role | Show Cancel Order | Show Apply Discount | Show Manage Menu |
|------|:-----------------:|:-------------------:|:----------------:|
| `staff` | ❌ | ❌ | ❌ |
| `manager` | ✅ | ✅ | ✅ |
| `admin` | ✅ | ✅ | ✅ |
| `super_admin` | ✅ | ✅ | ✅ |

> Read role from `ObserveSessionUseCase` to show/hide controls.

### Multi-Counter Setup (no extra code needed)

Each billing counter = one user account. All counters at one outlet share the same `restaurantId`:
```
counter_1 / password → restaurantId: 1 → Counter 1 tablet
counter_2 / password → restaurantId: 1 → Counter 2 tablet
kitchen   / password → restaurantId: 1 → Kitchen display
```

> ⚠️ There is **no Registration API** and no Password Recovery API.
> User accounts are created by admins directly on the backend.
> The app handles login + session only.

**Time Estimate**: 2–3 days

---

## 🪑 Phase 4: Table Management (Week 2)

> Requires Phase 3 complete (`restaurantId` available from session).

### 4.1 Table List / Selection Screen
- [ ] `GET /restaurants/{restaurantId}/tables` — all tables
- [ ] `GET /restaurants/{restaurantId}/tables/available` — available tables
  - Support `?capacity=4` to filter by minimum seating
- [ ] `GET /restaurants/{restaurantId}/tables/occupied` — occupied tables view
- [ ] `GET /restaurants/{restaurantId}/tables/count/available` — count for header badge
- [ ] Table status chips: `AVAILABLE` | `OCCUPIED` | `RESERVED` | `CLEANING` | `MAINTENANCE`
- [ ] Tap available table → proceed to Create Order (Phase 5)

### 4.2 Table Status Update
- [ ] `PATCH /restaurants/{restaurantId}/tables/{id}/status?newStatus=CLEANING`
- [ ] Free table after payment: `?newStatus=AVAILABLE` (called from Payment screen)
- [ ] Optimistic locking: handle `409 CONFLICT` → re-fetch table (new `version`) → retry

### 4.3 Table CRUD (Admin / Manager)
- [ ] `POST /restaurants/{restaurantId}/tables` — create table
- [ ] `PUT /restaurants/{restaurantId}/tables/{id}` — update tableNumber / capacity
- [ ] `DELETE /restaurants/{restaurantId}/tables/{id}` — remove table

**Files to create:**
```
feature/table/
  TableViewModel.kt
  TableUiState.kt
  TableListScreen.kt
  TableGridCard.kt
  TableRoute.kt
```

**Time Estimate**: 2–3 days

---

## 📋 Phase 5: Order Flow (Week 3)

> Core POS flow: Cart → Create Order → Manage Items → Update Status → KDS

### 5.1 Create Order from Local Cart

> Called from `CartSummaryFooter` after user selects a table.
> On success — **clear local cart**.

- [ ] `POST /restaurants/{restaurantId}/orders`
  ```json
  {
    "tableId": 1,
    "orderType": "DINE_IN",
    "items": [{ "foodId": 3, "quantity": 2, "specialRequests": "Extra cheese" }],
    "notes": "Birthday table"
  }
  ```
  - `orderType`: `DINE_IN` | `TAKEAWAY` | `DELIVERY`
  - Backend auto-sets table → `OCCUPIED`
  - Food price **captured at order time** — future price changes don't affect this order
- [ ] Handle `409 CONFLICT` (table already occupied — prompt re-select)
- [ ] `ClearCartUseCase` after successful order creation
- [ ] Navigate to Order Detail screen

### 5.2 Order List Screen
- [ ] `GET /restaurants/{restaurantId}/orders` — all orders
- [ ] `GET /restaurants/{restaurantId}/orders/active` — not DELIVERED or CANCELLED
- [ ] `GET /restaurants/{restaurantId}/orders/status/{status}` — filter by status
- [ ] `GET /restaurants/{restaurantId}/orders/count/pending` — badge in nav bar
- [ ] `GET /restaurants/{restaurantId}/orders/search?q=ORD-001` — search by order/table number

**Order Status Flow:**
```
PENDING → IN_PROGRESS → COMPLETED → DELIVERED   (normal)
PENDING → HOLD → PENDING / CANCELLED            (paused)
IN_PROGRESS → CANCELLED                         (abort)
```

### 5.3 Order Detail Screen
- [ ] `GET /restaurants/{restaurantId}/orders/{orderId}` — full order with items + `version`
- [ ] `PATCH /restaurants/{restaurantId}/orders/{orderId}/status { "status": "IN_PROGRESS" }`
- [ ] `POST /restaurants/{restaurantId}/orders/{orderId}/items` — add item (PENDING or HOLD only)
- [ ] `PUT /restaurants/{restaurantId}/orders/{orderId}/items/{itemId}` — update qty / special requests
  - ⚠️ Blocked if item is `READY`, `SERVED`, or `CANCELLED` (returns 400)
- [ ] `DELETE /restaurants/{restaurantId}/orders/{orderId}/items/{itemId}` — remove item
- [ ] `DELETE /restaurants/{restaurantId}/orders/{orderId}` — cancel entire order
- [ ] Handle `409 CONFLICT` → re-fetch order (new `version`) → retry

### 5.4 Kitchen Display Screen (KDS)

> Item-level status drives kitchen workflow. UI enforces forward-only progression.

- [ ] `PATCH /restaurants/{restaurantId}/orders/{orderId}/items/{itemId}/status?newStatus=IN_PROGRESS`
- [ ] Item status flow: `PENDING` → `IN_PROGRESS` → `READY` → `SERVED` | `CANCELLED`
- [ ] KDS view: orders grouped by table, items colour-coded by status
- [ ] Items in `READY`, `SERVED`, `CANCELLED` — disable all edit controls (server-locked)

**Item Status Rules:**
```
PENDING     → IN_PROGRESS, CANCELLED
IN_PROGRESS → READY, CANCELLED
READY       → SERVED               (final — table pickup)
SERVED      → (locked)
CANCELLED   → (locked)
```

**Files to create:**
```
feature/order/
  OrderViewModel.kt
  OrderUiState.kt
  OrderListScreen.kt
  OrderDetailScreen.kt
  OrderItemCard.kt
  KitchenDisplayScreen.kt
  OrderRoute.kt
```

**Time Estimate**: 5–7 days

---

## 🧾 Phase 6: Billing & Payment (Week 4)

> Core POS flow: Generate Bill → Process Payment → Free Table

### 6.1 Generate Bill

> ⚠️ **NEVER calculate tax on the app.** Always use `generate-bill`.
> Backend auto-computes: `subtotal`, `cgstAmount` (9%), `sgstAmount` (9%), `taxAmount` (18%), `totalAmount`.

- [ ] `POST /restaurants/{restaurantId}/orders/{orderId}/generate-bill?discount=0.00`
  - `discount` is optional (default 0) — absolute rupee amount e.g. `?discount=50.00`
  - Returns `409 CONFLICT` if a bill already exists for this order
  - `billNumber` auto-generated: `BILL-{restaurantId}-{yyyyMMdd}-{seq}`
- [ ] Display: `subtotal` + `cgst (9%)` + `sgst (9%)` − `discount` = `totalAmount`

### 6.2 Bill Detail & Status
- [ ] `GET /bills/{id}` — view full bill with items
- [ ] `GET /bills/number/{billNumber}` — fetch by bill number

| Bill Status | Meaning |
|-------------|---------|
| `ISSUED` | Bill generated, no payment yet |
| `PARTIAL` | At least one payment made, total not yet reached |
| `PAID` | Cumulative payments ≥ totalAmount — **auto-set by backend** |
| `CANCELLED` | Bill voided |

> ⚠️ Never set `PAID` manually — the backend does it automatically when payment succeeds.

- [ ] `PATCH /bills/{id}/cancel` — cancel bill (only when `ISSUED`)

### 6.3 Payment Screen

> Payment is created as `PENDING`. Confirm it with `/process`.
> Use `referenceNumber` for idempotency — retrying with the same `referenceNumber` after a
> network drop returns the existing payment if it already succeeded. **No double charge.**

- [ ] `POST /payments`
  ```json
  {
    "billId": 1,
    "orderId": 1,
    "paymentMethod": "CARD",
    "amount": 705.64,
    "referenceNumber": "REF-20260412-001",
    "transactionId": "TXN-HDFC-9823741",
    "notes": "Paid via Visa"
  }
  ```
  - Methods: `CASH` | `CARD` | `UPI` | `WALLET`
  - For `CASH`: display `changeAmount` (amount returned to customer — server-managed)
- [ ] `PATCH /payments/{id}/process` — confirm SUCCESS → bill auto-set to `PAID`
- [ ] `PATCH /payments/{id}/status { "status": "SUCCESS" }` — full lifecycle control
- [ ] After payment confirmed: `PATCH /tables/{tableId}/status?newStatus=AVAILABLE`

### 6.4 Payment History & Refund
- [ ] `GET /payments/bill/{billId}` — all payments for a bill
- [ ] `GET /payments/order/{orderId}` — all payments for an order
- [ ] `PATCH /payments/{id}/refund` — refund (`SUCCESS` → `REFUNDED`)

**Payment Status Flow:**
```
PENDING → SUCCESS   (confirmed → bill auto-PAID)
PENDING → FAILED    (declined / error)
SUCCESS → REFUNDED  (refund issued)
```

**Files to create:**
```
feature/billing/
  BillViewModel.kt
  BillingScreen.kt
  PaymentScreen.kt
  BillSummaryCard.kt
  BillingRoute.kt
```

**Time Estimate**: 4–5 days

---

## 🏢 Phase 7: Restaurant & Category Management (Week 5)

### 7.1 Restaurant Details
- [ ] `GET /restaurants/{restaurantId}` — load on app start
  - Store: `name`, `taxRate`, `currency`, `settings.enableTips`, `settings.autoPrintBill`
- [ ] `PATCH /restaurants/{restaurantId}` — update settings (admin / manager only)

### 7.2 Category Management
- [ ] `GET /categories?restaurantId={id}` — list (already used in food filter)
- [ ] `POST /categories?restaurantId={id}` — create category
- [ ] `PUT /categories/{id}` — update category
- [ ] `DELETE /categories/{id}` — delete category
- [ ] `GET /categories/{id}/foods` — foods in a category (paginated)

### 7.3 Settings Screen
- [ ] User profile display (username, email, role — from session)
- [ ] Theme selection (light / dark)
- [ ] Logout

**Time Estimate**: 3–4 days

---

## 📊 Phase 8: Reports & Analytics (Week 6)

### 8.1 Daily Sales Report
- [ ] `GET /restaurants/{restaurantId}/orders/range?startDate=...&endDate=...`
- [ ] Total revenue, order count, average order value
- [ ] Top-selling food items

### 8.2 Order History
- [ ] Date range picker
- [ ] Filter by status (`DELIVERED`, `CANCELLED`)
- [ ] Bill summary per order

**Time Estimate**: 3–4 days

---

## 🚀 Phase 9: Advanced Features (Week 7+)

### 9.1 Real-Time Features
- WebSocket integration for live table & order status
- Push notifications for order status changes
- Real-time KDS updates across kitchen displays

### 9.2 Offline Mode
- Queue order creation when offline
- Sync queue when connection restored
- Conflict resolution using `version` field (optimistic locking already in domain models)

### 9.3 Admin Dashboard
- User / staff management
- Inventory tracking
- Multi-store / super_admin view (`restaurantId = null` user can access all outlets)

**Time Estimate**: 10+ days

---

## ⚠️ Key Business Rules (Agreed with Backend)

| Rule | Detail |
|------|--------|
| **`restaurantId` source** | ALWAYS from login response — NEVER hardcode |
| **Tax calculation** | Server-side ONLY via `generate-bill` — NEVER calculate client-side |
| **Price capture** | Food price locked at order creation — future price changes don't affect existing orders |
| **Cart** | Local-only (Room DB) — no backend cart API exists |
| **One bill per order** | `409 CONFLICT` if bill already exists for this order |
| **Idempotent payments** | Unique `referenceNumber` per attempt — duplicate SUCCESS returns existing payment |
| **Table auto-occupy** | `POST /orders` automatically sets table → `OCCUPIED` |
| **Bill auto-pay** | `PATCH /payments/{id}/process` (SUCCESS) → backend auto-sets bill → `PAID` |
| **BillStatus PARTIAL** | Auto-set when payment < totalAmount; reaches PAID when cumulative ≥ total |
| **Optimistic locking** | `version` on Order & Table — `409 CONFLICT` on concurrent update → re-fetch + retry |
| **Item locking** | Items in `READY` / `SERVED` / `CANCELLED` are server-locked — edits return `400` |
| **OrderType values** | `DINE_IN` / `TAKEAWAY` / `DELIVERY` — backend confirmed (ignore `OFFLINE`/`ONLINE` in older doc) |
| **No Registration API** | Login / Me / Validate only — no sign-up or password recovery endpoints exist |
| **Session recovery** | Token exists but `restaurantId` missing → `GET /auth/me` — do NOT force re-login |
| **Device tracking** | Optional `deviceId` + `deviceType` in login — backend logs per-device activity |
| **Free table manually** | After payment, app must call `PATCH /tables/{id}/status?newStatus=AVAILABLE` — not automatic |

---

## 🔄 Complete POS Transaction Flow

```
1. App Start
   Token in DataStore? → RecoverSessionUseCase → GET /auth/me → restore restaurantId
   No token → LoginScreen

2. Login
   POST /auth/login { username, password, deviceId?, deviceType? }
   Save: { token, restaurantId, role } to DataStore

3. Load App Data
   GET /categories?restaurantId=1
   GET /foods?restaurantId=1

4. Browse Menu → add to local cart (Room DB)
   CartSummaryFooter shows item count + running total

5. Select Table
   GET /restaurants/1/tables/available?capacity=2
   User taps an available table

6. Create Order  (clears local cart)
   POST /restaurants/1/orders { tableId, orderType: "DINE_IN", items[], notes? }
   → Table auto-set to OCCUPIED
   → ClearCartUseCase called after success

7. Kitchen Display (KDS)
   PATCH /restaurants/1/orders/{id}/items/{itemId}/status?newStatus=IN_PROGRESS
   PATCH /restaurants/1/orders/{id}/items/{itemId}/status?newStatus=READY

8. Update Order Status
   PATCH /restaurants/1/orders/{id}/status { "status": "COMPLETED" }

9. Generate Bill  (all tax auto-calculated by backend)
   POST /restaurants/1/orders/{id}/generate-bill?discount=0
   Response: subtotal + 9% CGST + 9% SGST = totalAmount

10. Process Payment
    POST /payments { billId, orderId, paymentMethod, amount, referenceNumber }
    PATCH /payments/{id}/process
    → Bill auto-set to PAID

11. Free Table
    PATCH /restaurants/1/tables/{tableId}/status?newStatus=AVAILABLE
```

---

## 📅 Updated Timeline

| Week | Phase | Focus | Status |
|------|-------|-------|--------|
| Done | 0 | Architecture | ✅ DONE |
| Done | 1 | Design System | ✅ DONE |
| Done | 2 | Food Browsing & Local Cart | ✅ DONE |
| W1 | 3 | Authentication & Session | 🔄 IN PROGRESS |
| W2 | 4 | Table Management | TODO |
| W3 | 5 | Order Flow + KDS | TODO |
| W4 | 6 | Billing & Payment | TODO |
| W5 | 7 | Restaurant & Category Management | TODO |
| W6 | 8 | Reports & Analytics | TODO |
| W7+ | 9 | Advanced (Real-time, Offline, Admin) | TODO |

**Total Estimated Timeline**: 7 weeks to full MVP

---

## 🔄 Feature Development Checklist

For **each new feature**, follow this checklist:

- [ ] **Domain Layer**
  - [ ] Model (most already scaffolded)
  - [ ] Repository interface
  - [ ] Use cases

- [ ] **Data Layer**
  - [ ] DTO + mapper
  - [ ] Room entity + DAO (if local storage needed)
  - [ ] API service interface (most already exist)
  - [ ] Repository implementation

- [ ] **Presentation Layer**
  - [ ] UiState sealed class
  - [ ] ViewModel
  - [ ] Screen composable(s)
  - [ ] Navigation route

- [ ] **Testing**
  - [ ] Unit tests: ViewModel + Use cases (target 80%+ coverage)
  - [ ] UI tests: screen states
  - [ ] Manual test on tablet in landscape

- [ ] **Documentation**
  - [ ] Update this roadmap (mark items complete)
  - [ ] Update `docs/API_REFERENCE.md` if anything changes

---

## 💡 Pro Tips

✅ **`restaurantId` from session always** — `GetRestaurantIdUseCase` in every ViewModel
✅ **Never calculate tax** — call `POST generate-bill`, display what comes back
✅ **Handle `409 CONFLICT`** — re-fetch the resource (Order / Table), get new `version`, retry
✅ **Unique `referenceNumber` per payment attempt** — e.g. `REF-{timestamp}-{UUID}`
✅ **Test on real tablet in landscape orientation**
✅ **Follow domain → data → feature layer order** when building each feature

---

## ❓ Common Questions

**Q: How do I get `restaurantId` in a ViewModel?**
A: Inject `GetRestaurantIdUseCase` — it reads from `SessionDataStore`. Never pass it as a hardcoded constant.

**Q: Should I calculate GST on the app?**
A: No. Call `POST /orders/{id}/generate-bill`. The backend returns the complete bill with `cgstAmount`, `sgstAmount`, and `totalAmount` already computed.

**Q: Two tablets updated the same order simultaneously — now what?**
A: The second update gets `409 CONFLICT`. Re-fetch the order (grab the new `version`), apply the change again, and retry.

**Q: What is `referenceNumber` for payments?**
A: A unique string you generate per payment attempt (e.g. `REF-20260412-001`). If the network drops mid-request and you retry with the same `referenceNumber`, and the first attempt already succeeded, the server returns the existing payment — no duplicate charge.

**Q: Is there a registration or password reset screen to build?**
A: No. Only `POST /auth/login`, `GET /auth/me`, and `POST /auth/validate` exist. User accounts are created by admins on the backend directly.

**Q: What is `BillStatus.PARTIAL`?**
A: Auto-set by the backend when at least one payment is made but the total hasn't been reached yet (e.g. split bill). Transitions to `PAID` automatically once cumulative payments ≥ `totalAmount`.

**Q: When does the table become free after payment?**
A: It doesn't happen automatically. After payment succeeds, explicitly call `PATCH /tables/{id}/status?newStatus=AVAILABLE`.

---

## 📚 Reference Documents

| File | Purpose |
|------|---------|
| `docs/API_REFERENCE.md` | Complete 28-endpoint API contract (locked) |
| `README.md` | Project overview and quick start |

---

**Current focus: Phase 3 — Authentication & Session Management 🚀**
