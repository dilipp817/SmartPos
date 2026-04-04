# SmartPos — API Reference

**Project:** SmartPos Restaurant Billing System  
**Base URL:** `http://localhost:8080`  
**API Version:** v1  
**Prefix:** All endpoints start with `/api/v1/`  
**Status:** ✅ **Approved & Locked** — April 4, 2026  
**Total Endpoints:** 28

---

## 📌 Key Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| **API Count** | 28 endpoints | Proper separation without over-fetching; better performance at scale |
| **API Design** | RESTful with envelope response | Industry standard, consistent error handling |
| **Tax** | 18% GST (9% CGST + 9% SGST) | Indian restaurant compliance |
| **Pagination** | Offset-based | Simpler for POS use case |
| **Idempotency** | `referenceNumber` field | Prevents duplicate payments on retry |
| **Concurrency** | Optimistic locking via `version` | Safe concurrent order updates |
| **Backend staging** | Not required to start | Proceed with mock data, integrate when ready |

**Review result:** 100/100 — all 13 issues raised by mobile team were resolved by backend team.

---

## 📌 Standard Response Format

Every API response is wrapped in this envelope:

```json
{
  "success": true,
  "message": "Human readable message",
  "data": { ... },
  "error": null
}
```

### Error Response
```json
{
  "success": false,
  "message": null,
  "data": null,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Order not found with ID: 5",
    "details": {}
  }
}
```

### Error Codes
| Code | HTTP Status | Meaning |
|------|-------------|---------|
| `RESOURCE_NOT_FOUND` | 404 | Record doesn't exist |
| `VALIDATION_ERROR` | 400 | Input validation failed |
| `CONFLICT` | 409 | Duplicate or state conflict |
| `UNAUTHORIZED` | 401 | Missing or invalid token |
| `FORBIDDEN` | 403 | Not allowed |
| `INTERNAL_ERROR` | 500 | Server error |

---

## 📋 Endpoint Summary

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 1 | POST | `/auth/login` | Login and get JWT token |
| 2 | GET | `/auth/me` | Get current user info |
| 3 | POST | `/auth/validate` | Validate JWT token |
| 4 | GET | `/restaurants/{id}` | Get restaurant details |
| 5 | PATCH | `/restaurants/{id}` | Update restaurant settings |
| 6 | GET | `/foods` | List foods (paginated, filtered) |
| 7 | GET | `/foods/search` | Search foods |
| 8 | GET | `/foods/{id}` | Get food by ID |
| 9 | GET | `/foods/restaurant/{restaurantId}` | Get all foods for a restaurant |
| 10 | POST | `/foods/restaurant/{restaurantId}` | Create food item |
| 11 | GET | `/categories` | List categories |
| 12 | GET | `/categories/{id}` | Get category by ID |
| 13 | POST | `/categories` | Create category |
| 14 | PUT | `/categories/{id}` | Update category |
| 15 | DELETE | `/categories/{id}` | Delete category |
| 16 | GET | `/categories/{id}/foods` | Get foods for a category |
| 17 | POST | `/restaurants/{rId}/tables` | Create table |
| 18 | GET | `/restaurants/{rId}/tables` | List tables |
| 19 | GET | `/restaurants/{rId}/tables/available` | Get available tables |
| 20 | PATCH | `/restaurants/{rId}/tables/{id}/status` | Update table status |
| 21 | POST | `/restaurants/{rId}/orders` | Create order |
| 22 | GET | `/restaurants/{rId}/orders` | List orders |
| 23 | GET | `/restaurants/{rId}/orders/{id}` | Get order by ID |
| 24 | PATCH | `/restaurants/{rId}/orders/{id}/status` | Update order status |
| 25 | POST | `/restaurants/{rId}/orders/{id}/generate-bill` | Auto-generate bill with tax |
| 26 | GET | `/bills/{id}` | Get bill by ID |
| 27 | POST | `/payments` | Process payment |
| 28 | PATCH | `/payments/{id}/process` | Mark payment as SUCCESS |

---

## 🔐 Authentication

### POST `/api/v1/auth/login`
Login and get JWT token.

**Request Body:**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Validation:**
- `username` — required, 3–50 characters
- `password` — required, 6–100 characters

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@restaurant.com",
    "role": "ADMIN",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400
  }
}
```

> `expiresIn` is in seconds (86400 = 24 hours)

---

### GET `/api/v1/auth/me`
Get current logged-in user info.

**Header:** `Authorization: Bearer <token>`

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "User info retrieved",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@restaurant.com",
    "role": "ADMIN",
    "isActive": true
  }
}
```

---

### POST `/api/v1/auth/validate`
Validate a JWT token.

**Query Param:** `?token=<jwt_token>`

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Token is valid",
  "data": {
    "valid": true,
    "username": "admin",
    "userId": 1,
    "role": "staff"
  }
}
```

---

## 🏢 Restaurants

### GET `/api/v1/restaurants/{id}`
Get restaurant details.

**Success Response `200 OK`:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "The Food Corner",
    "address": "123 Main Street, City",
    "phone": "+1234567890",
    "email": "contact@foodcorner.com",
    "logoUrl": "https://cdn.example.com/logo.png",
    "timezone": "Asia/Kolkata",
    "currency": "INR",
    "taxRate": 18.0,
    "isActive": true,
    "settings": {
      "enableTips": true,
      "defaultTipPercentage": 10.0,
      "autoPrintBill": true,
      "taxInclusive": false
    },
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-03-16T10:00:00"
  }
}
```

---

### PATCH `/api/v1/restaurants/{id}`
Update restaurant settings.

**Request Body:**
```json
{
  "taxRate": 18.0,
  "settings": {
    "enableTips": true,
    "defaultTipPercentage": 12.0
  }
}
```

**Success Response `200 OK`:** Updated restaurant object.

---

## 🍽️ Foods

### GET `/api/v1/foods`
Get all foods with pagination, filtering, and sorting.

**Query Parameters:**
| Param | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `restaurantId` | Long | No | — | Filter by restaurant |
| `categoryId` | Long | No | — | Filter by category |
| `search` | String | No | — | Search in food name |
| `isVegetarian` | Boolean | No | — | Filter vegetarian items |
| `isSpicy` | Boolean | No | — | Filter spicy items |
| `sort` | String | No | `id:asc` | `price:asc`, `price:desc`, `name:asc`, `name:desc` |
| `offset` | Int | No | `0` | Pagination start position |
| `limit` | Int | No | `20` | Items per page (max 100) |

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Foods retrieved successfully",
  "data": {
    "data": [
      {
        "id": 1,
        "name": "Margherita Pizza",
        "price": 299.0,
        "imageUrl": "https://cdn.example.com/pizza.jpg",
        "categoryName": "Pizza",
        "isAvailable": true,
        "isVegetarian": true,
        "isSpicy": false
      }
    ],
    "pagination": {
      "currentPage": 0,
      "limit": 20,
      "total": 45,
      "totalPages": 3,
      "hasNext": true,
      "hasPrevious": false
    }
  }
}
```

---

### GET `/api/v1/foods/search`
Search foods by keyword with filters (database-level, efficient).

**Query Parameters:**
| Param | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `q` | String | No | — | Keyword — searches food name (case-insensitive) |
| `restaurantId` | Long | No | — | Filter by restaurant |
| `categoryId` | Long | No | — | Filter by category |
| `isVegetarian` | Boolean | No | — | Filter vegetarian |
| `isSpicy` | Boolean | No | — | Filter spicy |
| `isAvailable` | Boolean | No | — | Filter by availability |
| `offset` | Int | No | `0` | Pagination offset |
| `limit` | Int | No | `20` | Page size (max 100) |

**Example:** `GET /api/v1/foods/search?q=pizza&restaurantId=1&isVegetarian=true`

**Response:** Same paginated format as `GET /api/v1/foods`

---

### GET `/api/v1/foods/{id}`
Get a single food item by ID.

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Food retrieved successfully",
  "data": {
    "id": 1,
    "name": "Margherita Pizza",
    "price": 299.0,
    "description": "Classic tomato and mozzarella pizza",
    "imageUrl": "https://cdn.example.com/pizza.jpg",
    "categoryId": 2,
    "categoryName": "Pizza",
    "restaurantId": 1,
    "restaurantName": "Pizza Palace",
    "isAvailable": true,
    "preparationTime": 15,
    "allergens": "Gluten, Dairy",
    "calories": 720,
    "isVegetarian": true,
    "isSpicy": false,
    "createdAt": "2026-03-28T10:00:00",
    "updatedAt": "2026-03-28T10:00:00"
  }
}
```

---

### GET `/api/v1/foods/restaurant/{restaurantId}`
Get all foods for a specific restaurant (paginated).

**Query Params:** `page` (default 0), `limit` (default 20)  
**Response:** Same paginated format as `GET /api/v1/foods`

---

### POST `/api/v1/foods/restaurant/{restaurantId}`
Create a new food item.

**Request Body:**
```json
{
  "name": "Margherita Pizza",
  "price": 299.0,
  "description": "Classic tomato and mozzarella pizza",
  "imageUrl": "https://cdn.example.com/pizza.jpg",
  "categoryId": 2,
  "isVegetarian": true,
  "isSpicy": false
}
```

**Validation:** `name` — required; `price` — required, > 0, max 999999.99

**Success Response `201 Created`:** Full `FoodResponse`

---

## 📂 Categories

### GET `/api/v1/categories`
Get all categories for a restaurant.

**Query Param:** `?restaurantId=1` (required)

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Pizza",
      "description": "All types of pizza",
      "imageUrl": "https://cdn.example.com/pizza-cat.jpg",
      "displayOrder": 1,
      "isActive": true,
      "foodCount": 8
    }
  ]
}
```

---

### GET `/api/v1/categories/{id}`
Get a single category by ID.

**Success Response `200 OK`:** Single `CategoryResponse`

---

### POST `/api/v1/categories?restaurantId={id}`
Create a new category.

**Request Body:**
```json
{
  "name": "Beverages",
  "description": "Cold and hot drinks",
  "imageUrl": "https://cdn.example.com/drinks.jpg",
  "displayOrder": 3
}
```

**Validation:** `name` — required, 2–100 characters  
**Success Response `201 Created`:** Full `CategoryResponse`

---

### PUT `/api/v1/categories/{id}`
Update a category. Same body as POST.  
**Success Response `200 OK`:** Updated `CategoryResponse`

---

### DELETE `/api/v1/categories/{id}`
Delete a category.  
**Success Response `204 No Content`**

---

### GET `/api/v1/categories/{id}/foods`
Get all foods for a specific category (paginated).

**Query Params:** `offset` (default 0), `limit` (default 20)  
**Response:** Same paginated format as `GET /api/v1/foods`

---

## 🪑 Tables

> Base URL: `/api/v1/restaurants/{restaurantId}/tables`

**Table Status Values:** `AVAILABLE`, `OCCUPIED`, `RESERVED`, `CLEANING`, `MAINTENANCE`

### POST `/api/v1/restaurants/{restaurantId}/tables`
Create a new table.

**Request Body:**
```json
{
  "tableNumber": "T-01",
  "capacity": 4,
  "status": "AVAILABLE"
}
```

**Validation:** `tableNumber` — required, unique per restaurant; `capacity` — required, > 0

**Success Response `201 Created`:**
```json
{
  "success": true,
  "message": "Table created successfully",
  "data": {
    "id": 1,
    "restaurantId": 1,
    "tableNumber": "T-01",
    "capacity": 4,
    "status": "AVAILABLE",
    "currentOrderId": null,
    "createdAt": "2026-03-28T10:00:00",
    "updatedAt": "2026-03-28T10:00:00",
    "version": 0
  }
}
```

---

### GET `/api/v1/restaurants/{restaurantId}/tables`
Get all tables for a restaurant.

**Success Response `200 OK`:**
```json
{
  "success": true,
  "data": {
    "tables": [ /* list of TableResponse */ ],
    "total": 10
  }
}
```

---

### GET `/api/v1/restaurants/{restaurantId}/tables/{id}`
Get a single table by ID. Returns single `TableResponse`.

---

### GET `/api/v1/restaurants/{restaurantId}/tables/available`
Get all available tables.

**Query Param:** `?capacity=4` (optional — returns tables with capacity ≥ 4)

---

### GET `/api/v1/restaurants/{restaurantId}/tables/occupied`
Get all currently occupied tables.

---

### GET `/api/v1/restaurants/{restaurantId}/tables/status/{status}`
Get tables filtered by status.

---

### GET `/api/v1/restaurants/{restaurantId}/tables/count/available`
Get count of available tables.

**Success Response `200 OK`:**
```json
{
  "success": true,
  "data": 5
}
```

---

### PUT `/api/v1/restaurants/{restaurantId}/tables/{id}`
Update table details (number, capacity). Same body as POST.

---

### PATCH `/api/v1/restaurants/{restaurantId}/tables/{id}/status`
Update table status only.

**Query Param:** `?newStatus=CLEANING`  
**Success Response `200 OK`:** Updated `TableResponse`

---

### DELETE `/api/v1/restaurants/{restaurantId}/tables/{id}`
Delete a table.

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Table removed",
  "data": "Table deleted successfully"
}
```

---

## 📋 Orders

> Base URL: `/api/v1/restaurants/{restaurantId}/orders`

**Order Status Values:** `PENDING`, `IN_PROGRESS`, `COMPLETED`, `DELIVERED`, `CANCELLED`, `HOLD`

**Valid Status Transitions:**
- `PENDING` → `IN_PROGRESS`, `HOLD`, `CANCELLED`
- `IN_PROGRESS` → `COMPLETED`, `CANCELLED`
- `COMPLETED` → `DELIVERED`
- `HOLD` → `PENDING`, `CANCELLED`

### POST `/api/v1/restaurants/{restaurantId}/orders`
Create a new order for a table.

**Request Body:**
```json
{
  "tableId": 1,
  "orderType": "OFFLINE",
  "items": [
    {
      "foodId": 3,
      "quantity": 2,
      "specialRequests": "Extra cheese please"
    }
  ],
  "notes": "Birthday celebration"
}
```

**Validation:**
- `tableId` — required, table must exist and be `AVAILABLE`
- `items` — required, at least 1 item; `foodId` and `quantity` (> 0) required per item
- `orderType` — `OFFLINE` (dine-in) or `ONLINE` (delivery/takeaway), default `OFFLINE`

**What happens internally:**
1. Validates table is `AVAILABLE`
2. Captures food price at time of order (price changes won't affect this order)
3. Calculates `totalAmount` from `(unitPrice × quantity)`
4. Sets table status to `OCCUPIED`
5. Generates unique `orderNumber` e.g. `ORD-20260328-0001`

**Success Response `201 Created`:**
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": 1,
    "restaurantId": 1,
    "tableId": 1,
    "tableNumber": "T-01",
    "orderNumber": "ORD-20260328-0001",
    "status": "PENDING",
    "orderType": "OFFLINE",
    "items": [
      {
        "id": 1,
        "foodId": 3,
        "foodName": "Margherita Pizza",
        "quantity": 2,
        "unitPrice": 299.00,
        "subtotal": 598.00,
        "itemStatus": "PENDING",
        "specialRequests": "Extra cheese please",
        "createdAt": "2026-03-28T20:00:00"
      }
    ],
    "totalAmount": 598.00,
    "notes": "Birthday celebration",
    "createdAt": "2026-03-28T20:00:00",
    "updatedAt": "2026-03-28T20:00:00",
    "version": 0
  }
}
```

---

### POST `/api/v1/restaurants/{restaurantId}/orders/{orderId}/items`
Add an item to an existing order.

> ⚠️ Only allowed when order status is `PENDING` or `HOLD`

**Request Body:**
```json
{
  "foodId": 5,
  "quantity": 1,
  "specialRequests": "No onions"
}
```

**Success Response `200 OK`:** Updated full `OrderResponse`

---

### GET `/api/v1/restaurants/{restaurantId}/orders`
Get all orders for a restaurant.

**Success Response `200 OK`:**
```json
{
  "success": true,
  "data": {
    "orders": [ /* list of OrderResponse */ ],
    "total": 25,
    "status": "success"
  }
}
```

---

### GET `/api/v1/restaurants/{restaurantId}/orders/{orderId}`
Get a single order by ID. Returns `OrderResponse`. Error `404` if not found.

---

### GET `/api/v1/restaurants/{restaurantId}/orders/status/{status}`
Get orders filtered by status. Returns `OrderListResponse`.

---

### GET `/api/v1/restaurants/{restaurantId}/orders/active`
Get all active orders (not `DELIVERED` or `CANCELLED`).

---

### GET `/api/v1/restaurants/{restaurantId}/orders/range`
Get orders within a date/time range.

**Query Params:**
- `startDate` — ISO datetime e.g. `2026-03-28T00:00:00`
- `endDate` — ISO datetime e.g. `2026-03-28T23:59:59`

---

### GET `/api/v1/restaurants/{restaurantId}/orders/count/pending`
Get count of pending orders.

**Success Response `200 OK`:**
```json
{
  "success": true,
  "data": { "pending_count": 7 }
}
```

---

### GET `/api/v1/restaurants/{restaurantId}/orders/search`
Search orders by order number, table number, or status.

**Query Param:** `?q=ORD-20260404` (required)  
**Example:** `GET /api/v1/restaurants/1/orders/search?q=T-01`

---

### PATCH `/api/v1/restaurants/{restaurantId}/orders/{orderId}/status`
Update order status.

**Request Body:**
```json
{
  "status": "IN_PROGRESS"
}
```

**Success Response `200 OK`:** Updated `OrderResponse`

---

### PUT `/api/v1/restaurants/{restaurantId}/orders/{orderId}/items/{itemId}`
Update an order item's quantity/special requests.

**Request Body:**
```json
{
  "quantity": 3,
  "specialRequests": "Extra spicy"
}
```

**Success Response `200 OK`:** Updated `OrderResponse`

---

### PATCH `/api/v1/restaurants/{restaurantId}/orders/{orderId}/items/{itemId}/status`
Update individual item status.

**Query Param:** `?newStatus=PREPARING`  
**Item Status Values:** `PENDING`, `PREPARING`, `READY`, `SERVED`, `CANCELLED`

---

### DELETE `/api/v1/restaurants/{restaurantId}/orders/{orderId}/items/{itemId}`
Remove an item from an order. Returns updated `OrderResponse`.

---

### DELETE `/api/v1/restaurants/{restaurantId}/orders/{orderId}`
Cancel an order. Returns `OrderResponse` with status `CANCELLED`.

---

### POST `/api/v1/restaurants/{restaurantId}/orders/{orderId}/generate-bill`
**Auto-generate a bill. No manual tax calculation needed.**

Backend auto-computes:
- `subtotal` — from order items total
- `cgstAmount` — 9% of subtotal
- `sgstAmount` — 9% of subtotal
- `taxAmount` — 18% of subtotal
- `totalAmount` — subtotal + tax - discount
- `billNumber` — auto-generated as `BILL-{restaurantId}-{yyyyMMdd}-{seq}`

**Query Param:** `?discount=50.00` (optional, default `0`)

> ⚠️ Returns `409 CONFLICT` if a bill already exists for this order.

**Success Response `201 Created`:**
```json
{
  "success": true,
  "message": "Bill generated successfully",
  "data": {
    "id": 1,
    "billNumber": "BILL-1-20260404-0001",
    "orderId": 1,
    "restaurantId": 1,
    "subtotal": 598.00,
    "cgstAmount": 53.82,
    "sgstAmount": 53.82,
    "taxAmount": 107.64,
    "discountAmount": 0.00,
    "totalAmount": 705.64,
    "status": "ISSUED",
    "createdAt": "2026-04-04T20:30:00",
    "updatedAt": "2026-04-04T20:30:00"
  }
}
```

---

## 🧾 Bills

**Bill Status Values:** `ISSUED`, `PAID`, `CANCELLED`

> **Recommended:** Use `POST /orders/{orderId}/generate-bill` for auto tax calculation.  
> Manual `POST /api/v1/bills` is available but requires you to calculate tax yourself.

### POST `/api/v1/bills`
Create a bill manually.

**Request Body:**
```json
{
  "orderId": 1,
  "restaurantId": 1,
  "subtotal": 598.00,
  "taxAmount": 107.64,
  "cgstAmount": 53.82,
  "sgstAmount": 53.82,
  "discountAmount": 0.00,
  "totalAmount": 705.64,
  "status": "ISSUED"
}
```

> `billNumber` is optional — server auto-generates if not provided.

**Success Response `201 Created`:** Full `BillResponse`

---

### GET `/api/v1/bills`
Get all bills (paginated), optionally filtered by status.

**Query Params:** `status` (optional), `page`, `size`, `sort`  
**Response:** Paginated `BillListResponse`

---

### GET `/api/v1/bills/{id}`
Get a bill by ID. Returns full `BillResponse`.

---

### GET `/api/v1/bills/number/{billNumber}`
Get a bill by its bill number.

---

### PUT `/api/v1/bills/{id}`
Update a bill (only when status is `ISSUED`).

---

### PATCH `/api/v1/bills/{id}/paid`
Mark a bill as paid.  
> ⚠️ Bill must be in `ISSUED` status.

---

### PATCH `/api/v1/bills/{id}/cancel`
Cancel a bill.  
> ⚠️ Bill must be in `ISSUED` status.

---

### POST `/api/v1/bills/{id}/items`
Add items to a bill.

**Request Body:**
```json
[
  {
    "foodId": 3,
    "quantity": 2,
    "unitPrice": 299.00
  }
]
```

---

### DELETE `/api/v1/bills/{id}/items/{itemId}`
Remove an item from a bill.

---

### DELETE `/api/v1/bills/{id}`
Delete a bill permanently.  
**Success Response `204 No Content`**

---

## 💳 Payments

**Payment Method Values:** `CASH`, `CARD`, `UPI`, `WALLET`  
**Payment Status Values:** `PENDING`, `SUCCESS`, `FAILED`, `REFUNDED`

### POST `/api/v1/payments`
Process a payment for a bill/order.

> **Idempotency:** Use a unique `referenceNumber` per payment attempt. If a payment with the same `referenceNumber` already has status `SUCCESS`, the existing payment is returned — no duplicate charge.

**Request Body:**
```json
{
  "billId": 1,
  "orderId": 1,
  "paymentMethod": "CARD",
  "amount": 705.64,
  "referenceNumber": "REF-20260328-001",
  "transactionId": "TXN-HDFC-9823741",
  "notes": "Paid via Visa card"
}
```

**Validation:**
- `orderId` — required
- `paymentMethod` — required
- `amount` — required, > 0
- `referenceNumber` — required, unique per payment attempt

**What happens internally:**
1. Checks `referenceNumber` for duplicate (idempotent if already `SUCCESS`)
2. Creates payment with `PENDING` status
3. When status updated to `SUCCESS` → linked bill automatically marked as `PAID`

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Payment processed successfully",
  "data": {
    "id": 1,
    "billId": 1,
    "orderId": 1,
    "paymentMethod": "CARD",
    "amount": 705.64,
    "status": "PENDING",
    "transactionId": "TXN-HDFC-9823741",
    "referenceNumber": "REF-20260328-001",
    "notes": "Paid via Visa card",
    "createdAt": "2026-03-28T20:35:00",
    "updatedAt": "2026-03-28T20:35:00"
  }
}
```

---

### GET `/api/v1/payments/{id}`
Get a payment by ID. Returns `PaymentResponse`. Error `404` if not found.

---

### GET `/api/v1/payments/bill/{billId}`
Get all payments for a specific bill (paginated).

**Query Params:** `offset` (default 0), `limit` (default 20)

---

### GET `/api/v1/payments/order/{orderId}`
Get all payments for a specific order (paginated).

---

### PATCH `/api/v1/payments/{id}/status`
Update payment status through its lifecycle.

**Valid transitions:**
- `PENDING` → `SUCCESS` (payment confirmed → bill auto-marked PAID)
- `PENDING` → `FAILED`
- `SUCCESS` → `REFUNDED`

**Request Body:**
```json
{
  "status": "SUCCESS",
  "transactionId": "TXN-HDFC-9823741",
  "notes": "Confirmed via bank"
}
```

---

### PATCH `/api/v1/payments/{id}/process`
Shortcut to mark a payment as `SUCCESS` in one call.

> Equivalent to `PATCH /status` with `{ "status": "SUCCESS" }`.  
> Automatically marks linked bill as `PAID`.  
> Only works when payment is in `PENDING` status.

---

### PATCH `/api/v1/payments/{id}/refund`
Mark a payment as `REFUNDED`.

> Only works when payment is in `SUCCESS` status.

---

## 🔄 Typical App Flow

```
1. Login
   POST /api/v1/auth/login → get JWT token

2. Load app data
   GET /api/v1/categories?restaurantId=1
   GET /api/v1/foods?restaurantId=1

3. Check tables
   GET /api/v1/restaurants/1/tables/available

4. Customer sits → create order
   POST /api/v1/restaurants/1/orders
     { tableId, orderType: "OFFLINE", items: [{foodId, quantity}] }
     → table auto-set to OCCUPIED

5. Kitchen updates
   PATCH /api/v1/restaurants/1/orders/{orderId}/status { status: "IN_PROGRESS" }

6. Add more items if needed
   POST /api/v1/restaurants/1/orders/{orderId}/items

7. Generate bill (backend auto-calculates all tax)
   POST /api/v1/restaurants/1/orders/{orderId}/generate-bill?discount=0

8. Collect payment
   POST /api/v1/payments { billId, orderId, paymentMethod, amount, referenceNumber }

9. Mark payment as success → bill auto-marked PAID
   PATCH /api/v1/payments/{id}/process

10. Free the table
    PATCH /api/v1/restaurants/1/tables/{tableId}/status?newStatus=AVAILABLE
```

---

## 🗄️ Data Model & Rules

### Entity Relationships
```
restaurant (1)
  ├── food (N)           — restaurantId FK
  ├── categories (N)     — restaurantId FK
  ├── tables (N)         — restaurantId FK
  │     └── orders (N)   — tableId FK
  │           ├── order_items (N)     — orderId FK → foodId FK
  │           ├── bills (1)           — orderId FK
  │           │     └── bill_items (N) — billId FK → foodId FK
  │           └── payments (N)        — orderId FK → billId FK (optional)
  └── users (N)          — restaurantId FK
```

### Key Business Rules
| Rule | Detail |
|------|--------|
| **Price capture** | Food price copied to `order_items.unit_price` at order time — price changes don't affect existing orders |
| **One bill per order** | Duplicate bill prevented — `409 CONFLICT` if already exists |
| **Idempotent payments** | Duplicate `referenceNumber` with `SUCCESS` returns existing payment, no double charge |
| **Table auto-occupy** | Creating an order sets table to `OCCUPIED` automatically |
| **Bill auto-pay** | Payment updated to `SUCCESS` → linked bill auto-set to `PAID` |
| **Optimistic locking** | All entities have `version` field — concurrent updates safely rejected |
| **GST split** | 18% GST = 9% CGST + 9% SGST, both stored separately |
| **Date format** | All timestamps: ISO 8601 `2026-03-28T20:00:00` (IST) |

---

## 🔒 Optimistic Locking

All `Order` and `Table` entities have a `version` field to handle concurrent updates safely.

**How it works:**
- Every response includes a `version` field (integer, starts at 0)
- If two clients update the same resource simultaneously, the second update is rejected with `409 CONFLICT`
- Client should re-fetch the resource and retry with the latest `version`

**Conflict Error Response `409 CONFLICT`:**
```json
{
  "success": false,
  "error": {
    "code": "CONFLICT",
    "message": "The resource was modified by another request. Please re-fetch and retry."
  }
}
```

> **Mobile guidance:** Always store the `version` from responses. On `409 CONFLICT`, re-fetch the resource and retry. Do **not** manually send the `version` field — it is managed by the server.

---

## ⚠️ Mobile Integration Notes

1. **All endpoints** return the standard `{ success, data, message, error }` envelope.
2. **Payment lifecycle** — Payment is created as `PENDING`. Use `PATCH /payments/{id}/process` to confirm or `PATCH /payments/{id}/status` with `{ "status": "SUCCESS" }` for full control.
3. **`GET /api/v1/foods`** requires `restaurantId` to return results. Use `GET /foods/search` for global search without restaurantId.
4. **Table status** is managed manually — after payment, call `PATCH /tables/{id}/status?newStatus=AVAILABLE` to free the table.
5. **Bill generation** — Use `POST /orders/{orderId}/generate-bill` — backend handles 18% GST (9% CGST + 9% SGST) automatically.
6. **`X-Idempotency-Key` header is NOT used** — idempotency is via `referenceNumber` in request body only.
7. **Order type** — `orderType: "OFFLINE"` for dine-in, `orderType: "ONLINE"` for delivery. Defaults to `OFFLINE`.

