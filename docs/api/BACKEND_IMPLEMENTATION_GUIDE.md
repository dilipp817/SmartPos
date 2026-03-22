# 🔧 Backend Implementation Guide - SmartPos API v1.1

**For:** Backend Development Team  
**Date:** March 22, 2026  
**Version:** 1.1  
**Priority:** HIGH - Required for ODRfast UI

---

## 🎯 Quick Summary

This document highlights the **NEW and ENHANCED** endpoints required for the SmartPos ODRfast UI. All changes are **backward compatible**.

---

## 🆕 Key Changes Summary

### **1. New Simplified Foods Endpoint**

**Endpoint:** `GET /api/v1/foods`

**Purpose:** Lightweight endpoint optimized for POS UI food listing

**Key Features:**
- ✅ Offset-based pagination (not page-based)
- ✅ Category filtering
- ✅ Sorting support
- ✅ Search capability
- ✅ Enhanced response with images and availability

---

## 📋 Detailed Implementation Requirements

### **Endpoint 1: GET /api/v1/foods**

#### **Request:**
```http
GET /api/v1/foods?offset=0&limit=20&category=Main%20Course&sort=price:asc&search=chicken
```

#### **Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `offset` | integer | No | 0 | Starting position (0-based) |
| `limit` | integer | No | 20 | Items per page (max: 100) |
| `category` | string | No | null | Filter by category name (exact match) |
| `sort` | string | No | null | Sort order (see values below) |
| `search` | string | No | null | Search in item name |

#### **Sort Values:**
```
price:asc   -> Sort by price, low to high
price:desc  -> Sort by price, high to low
name:asc    -> Sort by name, A to Z
name:desc   -> Sort by name, Z to A
```

#### **Response Format:**
```json
{
  "data": [
    {
      "id": 101,
      "name": "Butter Chicken",
      "price": 350.00,
      "restroId": 1,
      "image_url": "https://cdn.smartpos.com/items/butter-chicken.png",
      "category": "Main Course",
      "description": "Creamy tomato-based chicken curry",
      "is_available": true
    }
  ],
  "current_page": 0,
  "limit": 20,
  "total": 156,
  "has_more": true
}
```

#### **Response Fields:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `data` | array | ✅ Yes | Array of food items |
| `current_page` | integer | ✅ Yes | Current page number (0-indexed) |
| `limit` | integer | ✅ Yes | Items per page |
| `total` | integer | ✅ Yes | Total items available |
| `has_more` | boolean | ✅ Yes | Whether more pages exist |

#### **Food Item Fields:**

| Field | Type | Required | Nullable | Description |
|-------|------|----------|----------|-------------|
| `id` | integer | ✅ Yes | No | Unique item ID |
| `name` | string | ✅ Yes | No | Item name |
| `price` | decimal | ✅ Yes | No | Item price |
| `restroId` | integer | ✅ Yes | No | Restaurant ID |
| `image_url` | string | ⚠️ Optional | Yes | URL to item image (NEW) |
| `category` | string | ⚠️ Optional | Yes | Category name (NEW) |
| `description` | string | ⚠️ Optional | Yes | Item description (NEW) |
| `is_available` | boolean | ⚠️ Optional | No | Availability status (NEW), default: true |

---

## 🔍 Implementation Details

### **1. Category Filtering**

**Logic:**
```sql
-- Example SQL
WHERE category = :categoryParam
-- Or for case-insensitive:
WHERE LOWER(category) = LOWER(:categoryParam)
```

**Examples:**
- `?category=Main Course` → Returns only Main Course items
- `?category=Appetizers` → Returns only Appetizers
- No category param → Returns all items

**Important:**
- Use **exact match** or case-insensitive match
- Empty/null category → Return all items

---

### **2. Sorting**

**Logic:**
```sql
-- price:asc
ORDER BY price ASC

-- price:desc
ORDER BY price DESC

-- name:asc
ORDER BY name ASC

-- name:desc
ORDER BY name DESC

-- No sort param
ORDER BY id ASC (default)
```

**Important:**
- Invalid sort values → Ignore and use default
- Combine with filtering seamlessly

---

### **3. Pagination**

**Logic:**
```sql
-- Offset-based pagination
LIMIT :limit OFFSET :offset

-- Calculate has_more
has_more = (total - offset - returned_count) > 0
```

**Examples:**
- Page 1: `offset=0, limit=20`
- Page 2: `offset=20, limit=20`
- Page 3: `offset=40, limit=20`

**Important:**
- `current_page` = `offset / limit`
- `has_more` = true if more items exist beyond current page

---

### **4. Search**

**Logic:**
```sql
WHERE name LIKE CONCAT('%', :search, '%')
-- Or case-insensitive:
WHERE LOWER(name) LIKE LOWER(CONCAT('%', :search, '%'))
```

**Examples:**
- `?search=chicken` → Returns items with "chicken" in name
- `?search=paneer` → Returns items with "paneer" in name

---

### **5. Combined Queries**

**Example:** Filter + Sort + Search + Pagination
```http
GET /api/v1/foods?category=Main%20Course&sort=price:asc&search=chicken&offset=0&limit=20
```

**SQL Logic:**
```sql
SELECT *
FROM food_items
WHERE category = 'Main Course'
  AND name LIKE '%chicken%'
  AND is_available = true
ORDER BY price ASC
LIMIT 20 OFFSET 0
```

---

## 💾 Database Schema Requirements

### **Recommended Table Structure:**

```sql
CREATE TABLE food_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    restro_id INT NOT NULL,
    image_url VARCHAR(500) NULL,          -- NEW: Can be NULL
    category VARCHAR(100) NULL,            -- NEW: Can be NULL
    description TEXT NULL,                 -- NEW: Can be NULL
    is_available BOOLEAN DEFAULT true,     -- NEW: Default true
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_restro_id (restro_id),
    INDEX idx_category (category),         -- NEW: For filtering
    INDEX idx_is_available (is_available), -- NEW: For filtering
    INDEX idx_price (price),               -- NEW: For sorting
    INDEX idx_name (name)                  -- NEW: For search
);
```

---

## 🧪 Test Cases

### **Test Case 1: Basic Pagination**
```http
GET /api/v1/foods?offset=0&limit=5

Expected:
- Returns 5 items
- current_page = 0
- has_more = true (if total > 5)
```

### **Test Case 2: Category Filter**
```http
GET /api/v1/foods?category=Main%20Course

Expected:
- All items have category = "Main Course"
- Pagination still works
```

### **Test Case 3: Sort by Price**
```http
GET /api/v1/foods?sort=price:asc&limit=10

Expected:
- Items sorted by price ascending
- First item has lowest price
- Last item in response has highest price
```

### **Test Case 4: Combined Query**
```http
GET /api/v1/foods?category=Beverages&sort=name:asc&search=coffee&offset=0&limit=10

Expected:
- Only beverages
- Only items with "coffee" in name
- Sorted alphabetically
- Paginated (10 items)
```

### **Test Case 5: Empty Results**
```http
GET /api/v1/foods?category=NonExistent

Expected:
- data = []
- total = 0
- has_more = false
```

### **Test Case 6: New Fields Present**
```http
GET /api/v1/foods?limit=1

Expected Response:
{
  "data": [{
    "id": 1,
    "name": "Item Name",
    "price": 100.00,
    "restroId": 1,
    "image_url": "https://...",  // Can be null
    "category": "Main Course",    // Can be null
    "description": "...",         // Can be null
    "is_available": true
  }],
  ...
}
```

---

## ⚠️ Important Notes for Backend Team

### **1. Backward Compatibility**
- ✅ All new fields (`image_url`, `category`, `description`, `is_available`) are **optional**
- ✅ If fields are null/missing, Android app will handle gracefully
- ✅ Existing clients won't break

### **2. Performance**
- ✅ Add database indices on `category`, `is_available`, `price`, `name`
- ✅ Use query optimization for combined filters
- ✅ Consider caching for frequently accessed categories

### **3. Validation**
- ✅ Validate `limit` (max 100 to prevent abuse)
- ✅ Validate `offset` (non-negative)
- ✅ Validate `sort` values (reject invalid ones)
- ✅ Sanitize `search` input (prevent SQL injection)

### **4. Error Handling**
```json
// Invalid sort parameter
{
  "error": "Invalid sort parameter. Use: price:asc, price:desc, name:asc, name:desc",
  "code": "INVALID_SORT"
}

// Invalid limit
{
  "error": "Limit must be between 1 and 100",
  "code": "INVALID_LIMIT"
}
```

---

## 📊 Response Examples

### **Example 1: Successful Response with Items**
```json
{
  "data": [
    {
      "id": 101,
      "name": "Butter Chicken",
      "price": 350.00,
      "restroId": 1,
      "image_url": "https://cdn.smartpos.com/items/butter-chicken.png",
      "category": "Main Course",
      "description": "Creamy tomato-based chicken curry",
      "is_available": true
    },
    {
      "id": 102,
      "name": "Paneer Tikka",
      "price": 250.00,
      "restroId": 1,
      "image_url": "https://cdn.smartpos.com/items/paneer-tikka.png",
      "category": "Appetizers",
      "description": "Grilled cottage cheese with spices",
      "is_available": true
    }
  ],
  "current_page": 0,
  "limit": 20,
  "total": 156,
  "has_more": true
}
```

### **Example 2: Empty Results**
```json
{
  "data": [],
  "current_page": 0,
  "limit": 20,
  "total": 0,
  "has_more": false
}
```

### **Example 3: Items with Null Optional Fields**
```json
{
  "data": [
    {
      "id": 103,
      "name": "Special Item",
      "price": 150.00,
      "restroId": 1,
      "image_url": null,        // Null is OK
      "category": null,          // Null is OK
      "description": null,       // Null is OK
      "is_available": true
    }
  ],
  "current_page": 0,
  "limit": 20,
  "total": 1,
  "has_more": false
}
```

---

## ✅ Checklist for Backend Team

### **Implementation:**
- [ ] Create `GET /api/v1/foods` endpoint
- [ ] Add `offset` parameter support (integer, default: 0)
- [ ] Add `limit` parameter support (integer, default: 20, max: 100)
- [ ] Add `category` parameter support (string, optional)
- [ ] Add `sort` parameter support (string, optional)
- [ ] Add `search` parameter support (string, optional)
- [ ] Return response in specified format
- [ ] Add 4 new fields to food items table/model
- [ ] Add database indices for performance
- [ ] Implement pagination logic (offset-based)
- [ ] Calculate `has_more` correctly
- [ ] Implement category filtering
- [ ] Implement sort logic (4 variations)
- [ ] Implement search logic
- [ ] Handle combined queries (filter + sort + search)

### **Testing:**
- [ ] Test basic pagination
- [ ] Test category filtering
- [ ] Test sorting (all 4 variations)
- [ ] Test search
- [ ] Test combined queries
- [ ] Test empty results
- [ ] Test with null optional fields
- [ ] Test edge cases (offset > total, invalid params)
- [ ] Test performance with large datasets
- [ ] Validate response format matches spec

### **Documentation:**
- [ ] Update API documentation
- [ ] Add example requests/responses
- [ ] Document error codes
- [ ] Share Postman collection with team

---

## 📞 Contact

**Questions?** Contact Android team for clarification on:
- Expected behavior
- Edge cases
- UI requirements

**Reference Documents:**
- `API_SPECIFICATION_v1.0.md` - Full API spec
- `API_ENHANCEMENTS_COMPLETED.md` - What changed on Android side
- This document - Backend implementation guide

---

## 🎯 Priority

**PRIORITY: HIGH**

This endpoint is required for the new ODRfast UI to function properly. Please implement as soon as possible.

**Estimated Backend Effort:**
- Endpoint creation: 2-3 hours
- Testing: 1-2 hours
- Database changes: 1 hour
- **Total: 4-6 hours**

---

## ✅ Definition of Done

The implementation is complete when:

1. ✅ Endpoint responds at `GET /api/v1/foods`
2. ✅ All query parameters work correctly
3. ✅ Response format matches specification
4. ✅ All 4 new fields are present in response
5. ✅ Pagination works (offset-based)
6. ✅ Category filtering works
7. ✅ Sorting works (all 4 variations)
8. ✅ Search works
9. ✅ Combined queries work
10. ✅ All test cases pass
11. ✅ Performance is acceptable (< 500ms response time)
12. ✅ Android team confirms it works with UI

---

**Status:** 🚀 **Ready for Backend Implementation**  
**Updated:** March 22, 2026  
**Version:** 1.1

