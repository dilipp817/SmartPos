# SmartPos - Restaurant Billing System

**A modern, tablet-first Point of Sale (POS) system for restaurants**

![Platform](https://img.shields.io/badge/Platform-Android-green)
![Language](https://img.shields.io/badge/Language-Kotlin-blue)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20Multi--Module-orange)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen)

---

## 📚 Documentation

| File | Purpose |
|------|---------|
| [`README.md`](README.md) | Project overview (this file) |
| [`DEVELOPMENT_ROADMAP.md`](DEVELOPMENT_ROADMAP.md) | Development phases, progress tracking, next steps |
| [`docs/API_REFERENCE.md`](docs/API_REFERENCE.md) | Complete API documentation — all 28 endpoints |

---

## 🎯 Overview

SmartPos is a production-ready restaurant billing and POS system built with modern Android development practices:

- **Platform:** Android (Kotlin + Jetpack Compose)
- **Architecture:** Multi-module Clean Architecture
- **API:** 28 RESTful endpoints (approved & locked)
- **Database:** Room (local) + Remote API
- **DI:** Hilt
- **UI:** 100% Jetpack Compose, tablet-first, landscape

### Key Features

- 🍽️ **Menu Management** — Categories, items with pagination, search and filter
- 🪑 **Table Management** — Table status, assignments, real-time availability
- 📋 **Order Management** — Create, modify, track orders
- 🧾 **Billing** — Auto bill generation with 18% GST (9% CGST + 9% SGST)
- 💳 **Payments** — Cash, Card, UPI, Wallet with idempotency
- 🔄 **Optimistic Locking** — Safe concurrent order/table updates

---

## 🏗️ Architecture

### Multi-Module Clean Architecture

```
SmartPos/
├── app/              # Application entry point, Hilt setup, navigation
├── core/             # Shared utilities, constants, extensions
├── data/             # Data layer — DTOs, Room entities, Retrofit, Repository impl, Mappers
├── domain/           # Domain layer — Business models, Repository interfaces, Use cases
├── feature/
│   └── food/         # Home screen, food listing, cart summary, search/filter
└── ui-components/    # Shared Compose components, Material 3 theme
```

---

## 🚀 Quick Start

### Prerequisites
- Android Studio (latest stable)
- JDK 17+
- Android emulator or physical tablet (landscape preferred)

### Setup

```bash
git clone <repository-url>
cd SmartPos
./gradlew build
./gradlew installDebug
```

> **Note:** `localhost` from Android emulator is `10.0.2.2`.

---

## 🔧 API Overview

**28 RESTful endpoints** — see [`docs/API_REFERENCE.md`](docs/API_REFERENCE.md) for full details.

```
🔐 Authentication:  3 endpoints  (login, me, validate)
🏢 Restaurant:      2 endpoints  (get, update)
🍽️ Foods:           5 endpoints  (list, search, get, list by restaurant, create)
📂 Categories:      6 endpoints  (list, get, create, update, delete, foods by category)
🪑 Tables:          9 endpoints  (CRUD + status filters + count)
📋 Orders:         10 endpoints  (CRUD + status + search + generate-bill)
🧾 Bills:           8 endpoints  (CRUD + paid/cancel + items)
💳 Payments:        5 endpoints  (process, get, list, status, refund)
```

**Status:** ✅ API contract approved (100/100 review score) — April 4, 2026

---

## 🧪 Testing

```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests
./gradlew lint                    # Lint check
```

---

## 📊 Project Status

| Phase | Description | Status |
|-------|-------------|--------|
| Phase 0 | Multi-module setup, Hilt DI, Clean Architecture | ✅ Complete |
| Phase 1 | Material 3 theme, UI component library | ✅ Complete |
| Phase 2 | Feature screens (Food list done, Cart/Order pending) | 🔄 In Progress |
| Phase 3 | Table management, Order history | ⏳ Pending |
| Phase 4 | Auth, Settings, Backend integration | ⏳ Pending |
| Phase 5 | Testing, performance, production release | ⏳ Pending |

See [`DEVELOPMENT_ROADMAP.md`](DEVELOPMENT_ROADMAP.md) for detailed task breakdown.

---

## 🛠️ Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose |
| DI | Hilt |
| Async | Coroutines + Flow |
| Local DB | Room |
| Network | Retrofit + OkHttp + Moshi |
| Architecture | MVVM + Use Cases + Repository pattern |
| Navigation | Compose Navigation |

---

## 📝 Commit Convention

```
feat:     New feature
fix:      Bug fix
docs:     Documentation update
refactor: Code refactoring
test:     Add or update tests
chore:    Dependencies, build config
```

---

**Built with ❤️ using Kotlin & Jetpack Compose**

```
