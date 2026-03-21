# Getting Started with SmartPos

**Welcome to SmartPos!** 🎉  
This guide will help you get up and running with the SmartPos Restaurant Billing System.

**Estimated Time:** 30-60 minutes

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Overview](#project-overview)
3. [Quick Start](#quick-start)
4. [Project Structure](#project-structure)
5. [Key Decisions](#key-decisions)
6. [API Documentation](#api-documentation)
7. [Development Workflow](#development-workflow)
8. [Next Steps](#next-steps)

---

## Prerequisites

### Required
- **JDK:** 11 or higher
- **Android Studio:** Latest stable version
- **Gradle:** 9.1.0+ (comes with Android Studio)
- **Git:** For version control

### Recommended
- **Device:** Android tablet (10" or larger) for testing
- **Emulator:** Tablet emulator configured
- **IDE Plugins:** Kotlin, Compose

### Knowledge
- Kotlin programming
- Jetpack Compose basics
- Android development fundamentals
- REST API concepts

---

## Project Overview

### What is SmartPos?

SmartPos is a modern, tablet-first restaurant billing and Point of Sale (POS) system built with:

- **Platform:** Android (Kotlin)
- **UI:** Jetpack Compose
- **Architecture:** Multi-module Clean Architecture
- **Backend:** REST API (34 endpoints)
- **Database:** Room (local), Remote API (cloud)
- **DI:** Hilt/Dagger

### Key Features

- 🍽️ Menu management with categories and variants
- 🪑 Table management and floor plans
- 📋 Order taking and modification
- 🧾 Bill generation with discounts and taxes
- 💳 Multiple payment methods (Cash, Card, UPI)
- 👥 Customer management and loyalty points
- 📊 Analytics and reporting
- 🍳 Kitchen display system (future)

---

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd SmartPos
```

### 2. Open in Android Studio

```bash
# Open Android Studio
# File > Open > Select SmartPos directory
# Wait for Gradle sync to complete
```

### 3. Verify Build

```bash
# From terminal
./gradlew build

# Or use Android Studio:
# Build > Make Project
```

### 4. Run the App

```bash
# Connect tablet or start emulator
# Click Run (▶️) button in Android Studio
# Or from terminal:
./gradlew installDebug
```

---

## Project Structure

### Module Overview

```
SmartPos/
├── app/                    # Main application module
│   ├── MainActivity.kt     # Entry point
│   └── SmartPosApp.kt     # Application class (Hilt)
│
├── core/                   # Core utilities & common code
│   ├── util/              # Helper functions
│   └── network/           # Network configurations
│
├── data/                   # Data layer
│   ├── local/             # Room database
│   │   ├── entity/        # Database entities
│   │   └── dao/           # Data access objects
│   ├── remote/            # API communication
│   │   ├── dto/           # Data transfer objects
│   │   └── api/           # Retrofit services
│   ├── repository/        # Repository implementations
│   └── mapper/            # Data mappers
│
├── domain/                 # Domain layer
│   ├── model/             # Business models
│   ├── repository/        # Repository interfaces
│   └── usecase/           # Business logic (future)
│
├── feature/                # Feature modules
│   └── food/              # Food menu feature
│       ├── ui/            # Compose screens
│       └── viewmodel/     # ViewModels
│
└── ui-components/         # Shared UI components
    ├── buttons/           # Button components
    ├── cards/             # Card components
    ├── dialogs/           # Dialog components
    └── theme/             # App theme
```

### Key Files

**Configuration:**
- `build.gradle.kts` - Project dependencies
- `gradle/libs.versions.toml` - Version catalog
- `local.properties` - Local SDK path

**Application:**
- `app/src/main/AndroidManifest.xml` - App manifest
- `app/src/main/kotlin/SmartPosApp.kt` - App class

**Domain Models:**
- `domain/src/main/kotlin/model/` - All business models

---

## Key Decisions

### Architecture: Multi-Module Clean Architecture

**Why?**
- ✅ Separation of concerns
- ✅ Easier testing
- ✅ Parallel builds
- ✅ Reusable components

**Read More:** [Architecture Decision](../decisions/02_ARCHITECTURE_DECISION.md)

### APIs: 34 RESTful Endpoints

**Why?**
- ✅ 32% fewer than original design
- ✅ Better performance (78% faster batch ops)
- ✅ Lower cost (27% savings)
- ✅ Industry best practices

**Read More:** [API Design Decision](../decisions/01_API_DESIGN_DECISION.md)

### Performance: Batch Operations

**Why?**
- ✅ Reduces backend load by 40-90%
- ✅ Atomic transactions
- ✅ Fewer database connections

**Read More:** [Performance Analysis](../decisions/03_PERFORMANCE_ANALYSIS.md)

---

## API Documentation

### API Specification

**Main Document:** [API Specification v1.0](../api/API_SPECIFICATION_v1.0.md)

**Quick Reference:** [API Quick Reference](../api/API_QUICK_REFERENCE.md)

### API Overview

```
Base URL: https://api.smartpos.com/v1

Endpoints:
🔐 Authentication:    3 endpoints
🏢 Restaurant:        2 endpoints  
🍽️ Menu:             6 endpoints
🪑 Tables:           3 endpoints
📋 Orders:           5 endpoints
🧾 Bills:            4 endpoints
💳 Payments:         3 endpoints
👥 Customers:        4 endpoints
📊 Analytics:        1 endpoint
🏥 Health:           2 endpoints
```

### Example API Call

```kotlin
// Create an order
val order = CreateOrderRequest(
    tableId = 2,
    customerId = 123,
    orderType = OrderType.DINE_IN,
    items = listOf(
        OrderItemRequest(
            menuItemId = 101,
            quantity = 2,
            variantId = 1001
        )
    )
)

val response = apiService.createOrder(order)
```

---

## Development Workflow

### Daily Development

1. **Pull latest code**
   ```bash
   git pull origin develop
   ```

2. **Create feature branch**
   ```bash
   git checkout -b feature/menu-management
   ```

3. **Make changes**
   - Write code
   - Write tests
   - Update docs

4. **Test locally**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

5. **Commit changes**
   ```bash
   git add .
   git commit -m "feat: add menu item management"
   ```

6. **Push and create PR**
   ```bash
   git push origin feature/menu-management
   # Create pull request on GitHub
   ```

### Commit Convention

Use conventional commits:

```
feat: Add new feature
fix: Fix bug
docs: Update documentation
refactor: Code refactoring
test: Add tests
chore: Update dependencies
```

### Code Style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable names
- Write KDoc for public APIs
- Keep functions small and focused

---

## Development Tools

### Useful Gradle Commands

```bash
# Build project
./gradlew build

# Run tests
./gradlew test

# Run lint
./gradlew lint

# Clean build
./gradlew clean build

# Install debug APK
./gradlew installDebug

# Generate APK
./gradlew assembleDebug
```

### Android Studio Shortcuts

- **Run App:** Ctrl+R (Mac) / Shift+F10 (Windows)
- **Debug:** Ctrl+D (Mac) / Shift+F9 (Windows)
- **Find:** Cmd+F (Mac) / Ctrl+F (Windows)
- **Build:** Cmd+F9 (Mac) / Ctrl+F9 (Windows)

---

## Testing

### Unit Tests

Location: `module/src/test/kotlin/`

```kotlin
@Test
fun `test order total calculation`() {
    val order = createTestOrder()
    val total = order.calculateTotal()
    assertEquals(984.00, total, 0.01)
}
```

### UI Tests

Location: `module/src/androidTest/kotlin/`

```kotlin
@Test
fun testMenuItemDisplay() {
    composeTestRule.setContent {
        MenuItemCard(item = testMenuItem)
    }
    composeTestRule.onNodeWithText("Butter Chicken").assertIsDisplayed()
}
```

---

## Troubleshooting

### Common Issues

**Gradle Sync Failed**
```bash
# Solution 1: Invalidate caches
File > Invalidate Caches > Invalidate and Restart

# Solution 2: Clean project
./gradlew clean
```

**Hilt Compilation Error**
```bash
# Make sure you have:
# 1. @HiltAndroidApp on Application class
# 2. @AndroidEntryPoint on Activities
# 3. KSP plugin configured
```

**Import Issues**
```bash
# Rebuild project
Build > Rebuild Project

# Check if all dependencies are in libs.versions.toml
```

---

## Next Steps

### For Backend Developers

1. ✅ Review [API Specification](../api/API_SPECIFICATION_v1.0.md)
2. ✅ Implement Phase 1 endpoints (Week 1-2)
3. ✅ Set up health checks
4. ✅ Configure monitoring

### For Android Developers

1. ✅ Familiarize with project structure
2. ✅ Review UI components in `ui-components/`
3. ✅ Start with feature implementation
4. ✅ Write tests

### For Everyone

1. ✅ Read [Decision Summary](../decisions/DECISION_SUMMARY.md)
2. ✅ Join team chat
3. ✅ Set up development environment
4. ✅ Ask questions!

---

## Resources

### Internal Documentation
- [📚 Documentation Index](../README.md)
- [🎯 Decision Summary](../decisions/DECISION_SUMMARY.md)
- [🔧 API Specification](../api/API_SPECIFICATION_v1.0.md)
- [🏗️ Architecture](../architecture/PROJECT_STRUCTURE.md)

### External Resources
- [Kotlin Docs](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Android Architecture](https://developer.android.com/topic/architecture)
- [REST API Best Practices](https://restfulapi.net/)

---

## Support

### Getting Help

1. **Documentation:** Check docs/ folder first
2. **Team Chat:** Ask in development channel
3. **Issues:** Create GitHub issue for bugs
4. **Code Review:** Tag relevant team members

### Contact

- **Tech Lead:** [email]
- **Project Manager:** [email]
- **Team Chat:** [Slack/Discord/Teams]

---

## Checklist for New Developers

Day 1:
- [ ] Clone repository
- [ ] Set up development environment
- [ ] Run app successfully
- [ ] Read this guide
- [ ] Join team chat

Week 1:
- [ ] Review all decision documents
- [ ] Understand project structure
- [ ] Make first small contribution
- [ ] Write first test
- [ ] Attend team standup

Month 1:
- [ ] Complete first feature
- [ ] Review others' code
- [ ] Update documentation
- [ ] Help onboard new team member

---

**Welcome to the SmartPos team! Let's build something amazing! 🚀**

