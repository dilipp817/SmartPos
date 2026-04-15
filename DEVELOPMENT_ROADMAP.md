# 🗓️ SmartPos Development Roadmap

**Last Updated:** April 15, 2026
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

### 3.3 Session Recovery on App Start ✅

> If a token exists in DataStore but `restaurantId` is missing locally:
> Call `GET /auth/me` → extract `restaurantId` → save. **Do NOT force re-login.**

- [x] `RecoverSessionUseCase` (scaffolded — wire it into app startup)
- [x] `MainViewModel`: on app start → token present? → recover session → navigate to Home
- [x] Token expired / invalid → navigate to Login

### 3.4 Logout ✅
- [x] `LogoutUseCase` — clears all session data from DataStore
- [x] Navigate to `LoginScreen` on logout

### 3.5 Role-Aware UI ✅ (enforcement at API level in v2)

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

### 4.1 Table List / Selection Screen ✅
- [x] `GET /restaurants/{restaurantId}/tables` — all tables
- [x] `GET /restaurants/{restaurantId}/tables/available` — available tables
  - Support `?capacity=4` to filter by minimum seating
- [x] `GET /restaurants/{restaurantId}/tables/occupied` — occupied tables view
- [x] `GET /restaurants/{restaurantId}/tables/count/available` — count for header badge
- [x] Table status chips: `AVAILABLE` | `OCCUPIED` | `RESERVED` | `CLEANING` | `MAINTENANCE`
- [x] Tap available table → proceed to Create Order (Phase 5)

### 4.2 Table Status Update ✅
- [x] `PATCH /restaurants/{restaurantId}/tables/{id}/status?newStatus=CLEANING`
- [x] Free table after payment: `?newStatus=AVAILABLE` (called from Payment screen)
- [x] Optimistic locking: handle `409 CONFLICT` → re-fetch table (new `version`) → retry

### 4.3 Table CRUD (Admin / Manager) ✅
- [x] `POST /restaurants/{restaurantId}/tables` — create table
- [x] `PUT /restaurants/{restaurantId}/tables/{id}` — update tableNumber / capacity / floor
- [x] `DELETE /restaurants/{restaurantId}/tables/{id}` — remove table
- [x] Role-gated: FAB "Add Table" + card ⋮ overflow (Edit / Delete) visible to manager / admin / super_admin only
- [x] `canManageTables` added to `RolePermissions` — driven by `ObserveRolePermissionsUseCase`

---

## 📋 Phase 5: Order Flow (Week 3)

> Core POS flow: Cart → Create Order → Manage Items → Update Status → KDS

### 5.1 Create Order from Local Cart ✅

> Called from `CartSummaryFooter` after user selects a table.
> On success — **clear local cart**.

- [x] `POST /restaurants/{restaurantId}/orders`
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
- [x] Handle `409 CONFLICT` → mapped to `HttpConflictException` in data layer → "Re-select Table" banner shown
- [x] `ClearCartUseCase` called after successful order creation
- [x] Navigate to Order Detail screen (stub — full detail in Phase 5.3)
- [x] New module `feature:order` created with `CreateOrderScreen`, `CreateOrderViewModel`, `CreateOrderRoute`
- [x] `OrderRepository` + `CreateOrderUseCase` + `GetTableByIdUseCase` added to domain
- [x] `OrderMappers`, `OrderDao`, `OrderRepositoryImpl` added to data layer
- [x] `Screen.CreateOrder` + `Screen.OrderDetail` (stub) wired in `NavHost`

### 5.2 Order List Screen ✅
- [x] `GET /restaurants/{restaurantId}/orders` — all orders
- [x] `GET /restaurants/{restaurantId}/orders/active` — not DELIVERED or CANCELLED
- [x] `GET /restaurants/{restaurantId}/orders/status/{status}` — filter by status
- [x] `GET /restaurants/{restaurantId}/orders/count/pending` — badge in nav bar
- [x] `GET /restaurants/{restaurantId}/orders/search?q=ORD-001` — search by order/table number

**Order Status Flow:**
```
PENDING → IN_PROGRESS → COMPLETED → DELIVERED   (normal)
PENDING → HOLD → PENDING / CANCELLED            (paused)
IN_PROGRESS → CANCELLED                         (abort)
```

### 5.3 Order Detail Screen ✅
- [x] `GET /restaurants/{restaurantId}/orders/{orderId}` — full order with items + `version`
- [x] `PATCH /restaurants/{restaurantId}/orders/{orderId}/status { "status": "IN_PROGRESS" }`
- [x] `POST /restaurants/{restaurantId}/orders/{orderId}/items` — add item (PENDING or HOLD only)
- [x] `PUT /restaurants/{restaurantId}/orders/{orderId}/items/{itemId}` — update qty / special requests
  - ⚠️ Blocked if item is `READY`, `SERVED`, or `CANCELLED` (returns 400)
- [x] `DELETE /restaurants/{restaurantId}/orders/{orderId}/items/{itemId}` — remove item
- [x] `DELETE /restaurants/{restaurantId}/orders/{orderId}` — cancel entire order
- [x] Handle `409 CONFLICT` → re-fetch order (new `version`) → retry

### 5.4 Kitchen Display Screen (KDS) ✅

> Item-level status drives kitchen workflow. UI enforces forward-only progression.

- [x] `PATCH /restaurants/{restaurantId}/orders/{orderId}/items/{itemId}/status?newStatus=IN_PROGRESS`
- [x] Item status flow: `PENDING` → `IN_PROGRESS` → `READY` → `SERVED` | `CANCELLED`
- [x] KDS view: orders grouped by table, items colour-coded by status
- [x] Items in `READY`, `SERVED`, `CANCELLED` — disable all edit controls (server-locked)

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

## ✅ Phase 6: Billing & Payment (Week 4) — COMPLETED

> Core POS flow: Generate Bill → Process Payment → Free Table

### 6.1 Generate Bill ✅

> ⚠️ **NEVER calculate tax on the app.** Always use `generate-bill`.
> Backend auto-computes: `subtotal`, `cgstAmount` (9%), `sgstAmount` (9%), `taxAmount` (18%), `totalAmount`.

- [x] `POST /restaurants/{restaurantId}/orders/{orderId}/generate-bill?discount=0.00`
  - `discount` is optional (default 0) — absolute rupee amount e.g. `?discount=50.00`
  - Returns `409 CONFLICT` if a bill already exists for this order
  - `billNumber` auto-generated: `BILL-{restaurantId}-{yyyyMMdd}-{seq}`
- [x] Display: `subtotal` + `cgst (9%)` + `sgst (9%)` − `discount` = `totalAmount`

### 6.2 Bill Detail & Status ✅
- [x] `GET /bills/{id}` — view full bill with items
- [x] `GET /bills/number/{billNumber}` — fetch by bill number

| Bill Status | Meaning |
|-------------|---------|
| `ISSUED` | Bill generated, no payment yet |
| `PARTIAL` | At least one payment made, total not yet reached |
| `PAID` | Cumulative payments ≥ totalAmount — **auto-set by backend** |
| `CANCELLED` | Bill voided |

> ⚠️ Never set `PAID` manually — the backend does it automatically when payment succeeds.

- [x] `PATCH /bills/{id}/cancel` — cancel bill (only when `ISSUED`)

### 6.3 Payment Screen ✅

> Payment is created as `PENDING`. Confirm it with `/process`.
> Use `referenceNumber` for idempotency — retrying with the same `referenceNumber` after a
> network drop returns the existing payment if it already succeeded. **No double charge.**

- [x] `POST /payments`
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
- [x] `PATCH /payments/{id}/process` — confirm SUCCESS → bill auto-set to `PAID`
- [x] `PATCH /payments/{id}/status { "status": "SUCCESS" }` — full lifecycle control
- [x] After payment confirmed: `PATCH /tables/{tableId}/status?newStatus=AVAILABLE`

### 6.4 Payment History & Refund ✅
- [x] `GET /payments/bill/{billId}` — all payments for a bill
- [x] `GET /payments/order/{orderId}` — all payments for an order
- [x] `PATCH /payments/{id}/refund` — refund (`SUCCESS` → `REFUNDED`)

**Payment Status Flow:**
```
PENDING → SUCCESS   (confirmed → bill auto-PAID)
PENDING → FAILED    (declined / error)
SUCCESS → REFUNDED  (refund issued)
```

**Files created:**
```
domain/repository/BillRepository.kt
domain/repository/PaymentRepository.kt
domain/usecase/BillUseCases.kt
domain/usecase/PaymentUseCases.kt
domain/usecase/FreeTableUseCase  (added to TableUseCases.kt)
data/mapper/BillMappers.kt
data/mapper/PaymentMappers.kt
data/repository/BillRepositoryImpl.kt
data/repository/PaymentRepositoryImpl.kt
feature/billing/build.gradle.kts
feature/billing/BillingUiState.kt
feature/billing/PaymentUiState.kt
feature/billing/BillingViewModel.kt
feature/billing/PaymentViewModel.kt
feature/billing/BillSummaryCard.kt
feature/billing/BillingScreen.kt
feature/billing/PaymentScreen.kt
feature/billing/BillingRoute.kt
```

---

## ✅ Phase 7: Restaurant & Category Management (Week 5)

### 7.1 Restaurant Details ✅
- [x] `GET /restaurants/{restaurantId}` — load on app start
  - Store: `name`, `taxRate`, `currency`, `settings.enableTips`, `settings.autoPrintBill`
- [x] `PATCH /restaurants/{restaurantId}` — update settings (admin / manager only)

**Files created:**
```
domain/model/Restaurant.kt              (updated: added RestaurantSettings, UpdateRestaurantSettingsRequest)
domain/repository/RestaurantRepository.kt
domain/usecase/RestaurantUseCases.kt    (GetRestaurantUseCase, ObserveRestaurantUseCase, UpdateRestaurantSettingsUseCase)
data/remote/dto/RestaurantDto.kt        (RestaurantDto, RestaurantSettingsDto, UpdateRestaurantRequest)
data/remote/RestaurantApiService.kt
data/mapper/RestaurantMappers.kt
data/local/RestaurantDataStore.kt       (DataStore cache: name, taxRate, currency, settings flags)
data/repository/RestaurantRepositoryImpl.kt  (network-first + cache-fallback)
```
**Wired:**
- `RepositoryModule` binds `RestaurantRepository`
- `NetworkModule` provides `RestaurantApiService`
- `AuthRepositoryImpl.clearSession()` now also clears `RestaurantDataStore`
- `MainViewModel` calls `GetRestaurantUseCase` on every cold app start (after session recovery)

### 7.2 Category Management ✅
- [x] `GET /categories?restaurantId={id}` — list (already used in food filter)
- [x] `POST /categories?restaurantId={id}` — create category (🔴 admin only — server enforces 403)
- [x] `PUT /categories/{id}` — update category (🔴 admin only)
- [x] `DELETE /categories/{id}` — delete category (🔴 admin only)
- [x] `GET /categories/{id}/foods` — foods in a category (paginated)

**Files created:**
```
domain/model/Category.kt
domain/repository/CategoryRepository.kt
domain/usecase/CategoryUseCases.kt    (GetCategoriesUseCase, ObserveCategoriesUseCase,
                                       GetCategoryByIdUseCase, GetFoodsByCategoryUseCase,
                                       CreateCategoryUseCase, UpdateCategoryUseCase, DeleteCategoryUseCase)
data/mapper/CategoryMappers.kt
data/repository/CategoryRepositoryImpl.kt  (in-memory cache; sorted by displayOrder then name;
                                             write ops refresh cache on success)
```
**Already existed (no changes needed):**
```
data/remote/CategoryApiService.kt     (all 6 endpoints already defined)
data/remote/dto/CategoryDto.kt        (CategoryDto + CreateCategoryRequest)
data/di/NetworkModule.kt              (provideCategoryApiService already present)
```
**Wired:**
- `RepositoryModule` binds `CategoryRepository → CategoryRepositoryImpl`
- Admin-only mutations gated by `RolePermissions.canManageMenu` (enforced server-side with 403)

### 7.3 Settings Screen ✅
- [x] User profile display (username, email, role chip — from session)
- [x] Restaurant / outlet info (name, currency — from RestaurantDataStore cache)
- [x] Theme selection (light / dark — persisted in AppPrefsDataStore)
- [x] Logout with confirmation dialog

**Files created:**
```
data/local/AppPrefsDataStore.kt          (DataStore<Preferences>: isDarkTheme boolean)
app/settings/SettingsUiState.kt          (profile, restaurant, isDarkTheme, isLoggingOut)
app/settings/SettingsViewModel.kt        (observes session + restaurant + theme prefs)
app/settings/SettingsScreen.kt           (Profile / Outlet / Appearance / Logout sections)
app/settings/SettingsRoute.kt
```
**Updated:**
- `ui-components/Theme.kt`: added `SmartPosDarkColorScheme` (warm dark palette);
  `SmartPosTheme` now accepts `darkTheme: Boolean` param (defaults to system preference)
- `MainViewModel.kt`: injects `AppPrefsDataStore`; exposes `isDarkTheme: StateFlow<Boolean>`
- `MainActivity.kt`: observes `isDarkTheme` from `MainViewModel`, passes to `SmartPosTheme`
- `NavHost.kt`: `Screen.Settings` wired to `SettingsRoute`

**Time Estimate**: 3–4 days

---

## ✅ Phase 8: Reports & Analytics (Week 6) — COMPLETED

### 8.1 Daily Sales Report ✅
- [x] `GET /restaurants/{restaurantId}/orders/range?start_date=...&end_date=...`
- [x] Total revenue (DELIVERED orders), order count, average order value
- [x] Top-selling food items (ranked by quantity, top 10)
- [x] Date range picker (start + end date, default = today)
- [x] Refresh button

### 8.2 Order History ✅
- [x] Date range picker (default = today)
- [x] Filter chips: All | Delivered | Cancelled (client-side, no re-fetch)
- [x] Bill summary per order: subtotal, tax, total, status badge
- [x] Pull-to-refresh

**Files created:**
```
domain/model/SalesReport.kt                     (SalesReport + TopSellingItem data classes)
domain/repository/OrderRepository.kt            (+ getOrdersByDateRange)
domain/usecase/OrderUseCases.kt                 (+ GetOrdersByDateRangeUseCase, GetSalesReportUseCase)
data/local/dao/OrderDao.kt                      (+ getOrdersByDateRange ISO-8601 range query)
data/repository/OrderRepositoryImpl.kt          (+ getOrdersByDateRange impl + cache fallback with items)
feature/reports/build.gradle.kts
feature/reports/src/main/AndroidManifest.xml
feature/reports/SalesReportUiState.kt           (+ date helpers: todayStartMs, toIsoDateString, toDisplayDate)
feature/reports/SalesReportViewModel.kt
feature/reports/SalesReportScreen.kt            (DatePickerDialog, 3 metric cards, top-items table)
feature/reports/SalesReportRoute.kt
feature/reports/OrderHistoryUiState.kt          (OrderHistoryFilter enum + applyFilter ext fn)
feature/reports/OrderHistoryViewModel.kt
feature/reports/OrderHistoryScreen.kt           (DatePickerDialog, FilterChip row, PullToRefreshBox)
feature/reports/OrderHistoryRoute.kt
```
**Wired:**
- `settings.gradle.kts`: `include(":feature:reports")`
- `app/build.gradle.kts`: `implementation(project(":feature:reports"))`
- `Navigation.kt`: `Screen.SalesReport` + `Screen.OrderHistory`
- `NavHost.kt`: two new `composable()` entries
- `AppDrawerContent.kt`: "Reports" drawer item (BarChart icon); active on both report routes

---

## ✅ Phase 9: Advanced Features (Week 7+)

### ✅ 9.1 Real-Time Features (COMPLETED — April 14, 2026)
- [x] WebSocket integration for live table & order status
  - `SmartPosWebSocketManager` — OkHttp WS, Bearer auth, exponential back-off (1 s → 30 s cap), 30 s PING keepalive
  - `RealTimeRepositoryImpl` — Room-cached events + notification triggers on every inbound message
  - `ConnectRealTimeUseCase` / `DisconnectRealTimeUseCase` — lifecycle managed by `MainViewModel`
- [x] Push notifications for order status changes
  - 3 notification channels: **New Orders** (HIGH), **Items Ready** (DEFAULT), **Order Status** (DEFAULT)
  - `SmartPosNotificationManager` — branded `ic_notification` POS-receipt icon
  - Runtime `POST_NOTIFICATIONS` permission request on Android 13+ in `MainActivity`
- [x] Real-time KDS updates across kitchen displays
  - `KitchenDisplayViewModel` — WS primary + 60 s polling fallback when socket is unavailable
  - `KitchenDisplayScreen` — `KitchenConnectionBadge`: ⚡ **LIVE** (green) / ⚠ **Reconnecting…** (amber)
  - `OrderDetailViewModel` — `observeRealTimeEvents()` keeps item statuses live; mutation-safe guard prevents optimistic-state overwrite
  - `OrderViewModel` — connection state observed; `OrderListScreen` shows animated amber banner when not CONNECTED
  - `TableViewModel` — `observeRealTimeEvents()` replaces table in-place on every `TABLE_UPDATED` event

### ✅ 9.2 Offline Mode (COMPLETED — April 14, 2026)
- [x] **ConnectivityMonitor** (`data/device/`) — `ConnectivityManager.NetworkCallback` → `StateFlow<Boolean>`; registered for process lifetime
- [x] **ConnectivityRepository** (domain interface) + **ConnectivityRepositoryImpl** (data) — exposes `observeIsOnline(): Flow<Boolean>` + `isCurrentlyOnline(): Boolean`
- [x] **PendingOrderEntity** + **PendingOrderDao** (`pending_orders` table) — queued payload with status `PENDING` → `SYNCING` → deleted (success) | `FAILED` (409)
- [x] **OfflineQueueRepository** (domain interface) + **OfflineQueueRepositoryImpl** (data) — `enqueue()` serialises cart to JSON, `scheduleSyncIfNeeded()` schedules WorkManager job
- [x] **SyncWorker** (`@HiltWorker`, WorkManager) — drains queue on `CONNECTED` constraint; 409 → marks `FAILED`; transient error → `Result.retry()`
- [x] **DB migration 8 → 9** — adds `pending_orders` table
- [x] **`OrderRepositoryImpl.createOrder()`** — offline path: enqueues + schedules + returns `OfflineQueuedException`; online path unchanged
- [x] **`OfflineQueuedException`** added to `domain/common/Result.kt`
- [x] **`CreateOrderViewModel`** — observes connectivity (`isOffline` banner), catches `OfflineQueuedException` → clears cart → `orderQueued = true`
- [x] **`CreateOrderScreen`** — animated amber offline banner when `isOffline`
- [x] **`CreateOrderRoute`** — `onOrderQueued` callback; navigates back to TableList on queue confirmation
- [x] **`MainViewModel`** — observes connectivity; re-schedules sync on every offline → online transition
- [x] **`SmartPosApp`** — implements `Configuration.Provider` with `HiltWorkerFactory` for on-demand WorkManager init
- [x] **Manifest** — `ACCESS_NETWORK_STATE` permission + WorkManager default initialiser removed
- [x] **Conflict resolution** — 409 on sync → `FAILED` with reason "Table occupied"; `version` field on cached orders prevents stale-cache overwrites

### ✅ 9.3 Admin Dashboard (COMPLETED — April 15, 2026)
- [x] **Live Stats Dashboard** — today's revenue, active orders, free tables, offline queue count, unavailable food count
- [x] **Menu Management** — full CRUD for food items (create / edit / delete / search / category filter); role-gated to admin via server 403
- [x] **Restaurant Settings** — tax rate, tips, auto-print bill, tax-inclusive toggle; `PATCH /restaurants/{id}`
- [x] **Staff Management** — active session profile (username, email, role, device info); role permission matrix; role guide for all 4 roles; backend-managed-accounts note
- [x] **Inventory Tracking** — food availability toggle (optimistic update + rollback); grouped by available / unavailable; category filter chips; search; `PUT /foods/{id}` per toggle
- [x] **Navigation** — `Screen.StaffManagement` + `Screen.InventoryManagement` wired in `NavHost`; Admin drawer item highlights all 5 sub-routes
- [x] Pre-existing `FoodRepository` duplicate-overload bug fixed
- [x] Pre-existing `MenuManagementScreen` curly-quote syntax errors fixed
- [x] Pre-existing `Result.Loading` exhaustive-`when` errors fixed across admin ViewModels

**Files created:**
```
feature/admin/staff/StaffManagementUiState.kt
feature/admin/staff/StaffManagementViewModel.kt
feature/admin/staff/StaffManagementScreen.kt
feature/admin/staff/StaffManagementRoute.kt
feature/admin/inventory/InventoryUiState.kt
feature/admin/inventory/InventoryViewModel.kt
feature/admin/inventory/InventoryScreen.kt
feature/admin/inventory/InventoryRoute.kt
```
**Updated:**
- `AdminDashboardScreen.kt` — 2nd row of action cards: Staff Management + Inventory Tracking
- `AdminDashboardRoute.kt` — `onNavigateToStaffManagement` + `onNavigateToInventory` callbacks
- `Navigation.kt` — `Screen.StaffManagement` + `Screen.InventoryManagement`
- `NavHost.kt` — two new `composable()` entries
- `AppDrawerContent.kt` — admin `activeRoutes` extended with new routes
- `FoodRepository.kt` — removed duplicate `searchFoodsPaginated` declaration

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
| W2 | 4 | Table Management | ✅ DONE |
| W3 | 5 | Order Flow + KDS | ✅ DONE (5.1 ✅ 5.2 ✅ 5.3 ✅ 5.4 ✅) |
| W4 | 6 | Billing & Payment | ✅ DONE |
| W5 | 7 | Restaurant & Category Management | ✅ DONE |
| W6 | 8 | Reports & Analytics | ✅ DONE |
| W7+ | 9 | Advanced (Real-time, Offline, Admin) | ✅ DONE |

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

**🎉 Full MVP COMPLETE — All 9 phases shipped. Build: SUCCESSFUL ✅**

---

## ✅ Pre-Completion Audit Fixes (April 15, 2026)

Resolved all outstanding TODOs before declaring the project complete:

| # | Issue | Fix |
|---|-------|-----|
| 1 | `MockFoodRepository` still wired — entire app ran on mock food data | `RepositoryModule` now binds `FoodRepositoryImpl` |
| 2 | `onManageMenuClick` no-op in `HomeRoute` — Manage Menu button did nothing | Wired to `Screen.MenuManagement` via new `onNavigateToMenuManagement` callback |
| 3 | `onApplyDiscountClick` no-op in `CartSummaryFooter` — wrong place in flow | Discount belongs at bill-generation time (already in `BillingScreen`); button hidden (`canApplyDiscount = false`) at cart stage |
| 4 | `Screen.Search` dead route — empty stub, never navigated to | Removed from `Navigation.kt` and `NavHost.kt`; search lives in `SearchFilterPanel` on the food list |
| 5 | Category filter chips hardcoded — tapping a chip never filtered foods | `FoodViewModel` now injects `GetCategoriesUseCase`, exposes `categories: StateFlow<List<Category>>`; `HomeRoute` maps real backend categories to `CategoryUI`; `FoodRepositoryImpl.getFoodsPaginated` passes `category?.toLongOrNull()` as `categoryId` to `GET /foods/restaurant/{id}` |

