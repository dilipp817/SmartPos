# SmartPos - Restaurant Billing System

**A modern, tablet-first Point of Sale (POS) system for restaurants**

![Platform](https://img.shields.io/badge/Platform-Android-green)
![Language](https://img.shields.io/badge/Language-Kotlin-blue)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20Multi--Module-orange)
![Status](https://img.shields.io/badge/Status-In%20Development-yellow)

---

## 🎯 Overview

SmartPos is a production-ready restaurant billing and POS system built with modern Android development practices:

- **Platform:** Android (Kotlin + Jetpack Compose)
- **Architecture:** Multi-module Clean Architecture
- **Backend:** 28 RESTful API endpoints
- **Database:** Room (local) + Remote API
- **DI:** Hilt/Dagger
- **UI:** 100% Jetpack Compose

### Key Features

- 🍽️ **Menu Management** - Categories, items, variants, bulk operations
- 🪑 **Table Management** - Floor plans, table status, assignments
- 📋 **Order Management** - Create, modify, track orders in real-time
- 🧾 **Billing** - Generate bills with discounts, taxes, tips
- 💳 **Payments** - Cash, Card, UPI, split payments, refunds
- 👥 **Customer Management** - Profiles, loyalty points, order history
- 📊 **Analytics** - Sales reports, menu performance, insights
- 🔄 **Real-time Sync** - WebSocket for live updates

---

## 📚 Documentation

**Complete documentation is in the [`docs/`](docs/) folder.**

### 🚀 Quick Links

- **[📖 Documentation Index](docs/README.md)** - Start here!
- **[🎯 Getting Started Guide](docs/setup/GETTING_STARTED.md)** - Setup & onboarding
- **[📋 Decision Summary](docs/decisions/DECISION_SUMMARY.md)** - Key technical decisions
- **[🔧 API Specification v1.0](docs/api/API_SPECIFICATION_v1.0.md)** - Complete API docs

### 📁 Documentation Structure

```
docs/
├── README.md                          # Documentation index & navigation
├── decisions/                         # Technical decision records
│   ├── DECISION_SUMMARY.md           # Overview of all decisions
│   ├── 01_API_DESIGN_DECISION.md     # Why 28 APIs, not 50+
│   ├── 02_ARCHITECTURE_DECISION.md   # Multi-module setup
│   └── 03_PERFORMANCE_ANALYSIS.md    # Load & cost analysis
├── api/                              # API documentation
│   ├── API_SPECIFICATION_v1.0.md     # ⭐ LOCKED API spec
│   ├── API_QUICK_REFERENCE.md        # Quick examples
│   └── API_COMPARISON.md             # Before/after analysis
├── setup/                            # Setup & deployment
│   └── GETTING_STARTED.md            # Quick start guide
└── archive/                          # Archived documents
```

---

## 🏗️ Architecture

### Multi-Module Clean Architecture

```
SmartPos/
├── app/                    # Main application module
├── core/                   # Core utilities & common code
├── data/                   # Data layer (Repository, DTOs, Entities)
│   ├── local/             # Room database
│   ├── remote/            # API communication
│   ├── repository/        # Repository implementations
│   └── mapper/            # Data mappers
├── domain/                 # Domain layer (Business logic, Models)
│   ├── model/             # Business models
│   ├── repository/        # Repository interfaces
│   └── usecase/           # Use cases
├── feature/                # Feature modules
│   └── food/              # Food menu feature
└── ui-components/         # Shared UI components
```

**Why Multi-Module?**
- ✅ Clear separation of concerns
- ✅ Parallel builds (faster compilation)
- ✅ Easier testing
- ✅ Reusable components
- ✅ Team scalability

[Read more about architecture decisions](docs/decisions/02_ARCHITECTURE_DECISION.md)

---

## 🔧 API Overview

### 28 RESTful Endpoints

```
🔐 Authentication:    3 endpoints
🏢 Restaurant:        2 endpoints
🍽️ Menu:             5 endpoints
🪑 Tables:           3 endpoints
📋 Orders:           5 endpoints
🧾 Bills:            4 endpoints
💳 Payments:         3 endpoints
👥 Customers:        2 endpoints
📊 Analytics:        1 endpoint
```

**Key Design Decisions:**
- ✅ Optimized from 50+ to 28 endpoints (44% reduction)
- ✅ Batch operations for 78% performance improvement
- ✅ 27% cost savings on infrastructure
- ✅ Industry best practices (Stripe, Shopify patterns)

**Base URL:** `https://api.smartpos.com/v1`

[📖 Full API Specification](docs/api/API_SPECIFICATION_v1.0.md) | [📝 Quick Reference](docs/api/API_QUICK_REFERENCE.md)

---

## 🚀 Quick Start

### Prerequisites
- JDK 11 or higher
- Android Studio (latest stable)
- Gradle 9.1.0+

### Setup

```bash
# Clone the repository
git clone <repository-url>
cd SmartPos

# Open in Android Studio
# File > Open > Select SmartPos directory

# Build the project
./gradlew build

# Run on device/emulator
./gradlew installDebug
```

**Detailed setup guide:** [Getting Started](docs/setup/GETTING_STARTED.md)

---

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run lint
./gradlew lint

# Generate coverage report
./gradlew jacocoTestReport
```

---

## 📊 Project Status

### Current Phase: Foundation ✅

- [x] Multi-module architecture setup
- [x] Domain models created
- [x] Data models (DTOs, Entities)
- [x] UI components library
- [x] API specification locked
- [ ] Backend API implementation (In Progress)
- [ ] Feature screens implementation
- [ ] Integration testing
- [ ] Production deployment

### Timeline

| Phase | Duration | Status |
|-------|----------|--------|
| Phase 1: Core Setup | Week 1-2 | ✅ Complete |
| Phase 2: API Development | Week 3-4 | 🔄 In Progress |
| Phase 3: Features | Week 5-6 | ⏳ Pending |
| Phase 4: Testing & Deploy | Week 7-8 | ⏳ Pending |

**Target Launch:** April 2026

---

## 🎯 Key Technical Decisions

All major technical decisions are documented with rationale:

1. **[API Design](docs/decisions/01_API_DESIGN_DECISION.md)** - Why 28 APIs
   - 44% fewer endpoints (50+ → 28)
   - 78% performance improvement
   - 27% cost savings

2. **[Architecture](docs/decisions/02_ARCHITECTURE_DECISION.md)** - Multi-module setup
   - Clean separation of concerns
   - Better testability
   - Team scalability

3. **[Performance](docs/decisions/03_PERFORMANCE_ANALYSIS.md)** - Load optimization
   - Batch operations reduce load by 40-90%
   - Atomic transactions
   - Fewer database connections

[📋 View All Decisions](docs/decisions/DECISION_SUMMARY.md)

---

## 🛠️ Technology Stack

### Frontend (Android)
- **Language:** Kotlin
- **UI:** Jetpack Compose
- **DI:** Hilt
- **Async:** Coroutines + Flow
- **Local DB:** Room
- **Navigation:** Compose Navigation

### Backend (API)
- **Protocol:** REST
- **Format:** JSON
- **Auth:** JWT
- **Real-time:** WebSocket

### Architecture
- **Pattern:** Clean Architecture
- **Structure:** Multi-module
- **Design:** MVVM with Use Cases

---

## 📝 Contributing

### Commit Convention

```
feat: Add new feature
fix: Fix bug
docs: Update documentation
refactor: Code refactoring
test: Add tests
chore: Update dependencies
```

### Workflow

1. Create feature branch from `develop`
2. Make changes and write tests
3. Ensure build passes: `./gradlew build`
4. Create pull request

---

## 📖 Additional Resources

### Internal
- [Architecture Details](docs/architecture/)
- [API Examples](docs/api/API_QUICK_REFERENCE.md)
- [Setup Guide](docs/setup/GETTING_STARTED.md)

### External
- [Kotlin Docs](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Android Architecture](https://developer.android.com/topic/architecture)

---

## 📞 Support

- **Documentation:** Check [docs/](docs/) folder
- **Issues:** Create GitHub issue
- **Questions:** Team chat

---

## 📄 License

[Add your license here]

---

**Built with ❤️ using Kotlin & Jetpack Compose**

- `localhost` from Android emulator is `10.0.2.2`.
- If your HTTPS cert is self-signed, configure trusted certs for debug builds.
