# SmartPos - Clean Architecture Setup Summary

## ✅ Problem Fixed

**Error**: `Unable to create converter for java.util.List<com.autobill.smartpos.data.remote.dto.FoodDto>`

**Root Cause**: Moshi requires KSP code generation for Kotlin data classes to create JSON adapters.

**Solution**:
1. Added `moshi-kotlin-codegen` KSP processor to `app/build.gradle.kts`
2. Added `@JsonClass(generateAdapter = true)` annotation to `FoodDto`

## 🏗️ Architecture Structure Created

### Package Organization

```
com.autobill.smartpos/
├── core/common/
│   └── Resource.kt                    # Sealed result wrapper (Loading/Success/Error)
│
├── domain/                            # Business Logic Layer
│   ├── model/
│   │   └── Food.kt                   # Pure domain model
│   ├── repository/
│   │   └── FoodRepository.kt         # Repository contract (interface)
│   └── usecase/
│       └── GetFoodsUseCase.kt        # Business use case
│
├── data/                              # Data Layer
│   ├── remote/
│   │   ├── dto/
│   │   │   └── FoodDto.kt            # Network response DTO (@JsonClass)
│   │   └── FoodApiService.kt         # Retrofit API interface
│   ├── local/
│   │   ├── entity/
│   │   │   └── FoodEntity.kt         # Room database entity
│   │   ├── dao/
│   │   │   └── FoodDao.kt            # Room DAO with Flow support
│   │   └── AppDatabase.kt            # Room database config
│   ├── mapper/
│   │   └── FoodMappers.kt            # DTO↔Entity↔Domain converters
│   └── repository/
│       └── FoodRepositoryImpl.kt     # Repository implementation
│
├── presentation/                      # UI Layer
│   └── food/
│       ├── FoodUiState.kt            # Compose UI state
│       ├── FoodViewModel.kt          # ViewModel with StateFlow
│       └── FoodScreen.kt             # Compose UI (route + screen)
│
├── di/                                # Dependency Injection
│   ├── AppContainer.kt               # Manual DI container
│   ├── NetworkModule.kt              # Retrofit + OkHttp providers
│   ├── DatabaseModule.kt             # Room singleton provider
│   ├── RepositoryModule.kt           # Repository factory
│   └── DispatchersModule.kt          # Coroutine dispatchers
│
├── ui/theme/                          # Compose theming
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
│
├── SmartPosApp.kt                     # Application class with AppContainer
└── MainActivity.kt                    # Entry activity with ViewModel setup
```

## 📊 Data Flow

```
[API] https://10.0.2.2:8443/restaurants/1/foods
   ↓
[FoodApiService] GET request
   ↓
[List<FoodDto>] JSON response with Moshi converter
   ↓
[FoodMappers.toEntity()] Convert DTO → Entity
   ↓
[FoodDao.upsertAll()] Cache in Room database
   ↓
[FoodDao.observeFoodsByRestaurant()] Emit Flow<List<FoodEntity>>
   ↓
[FoodMappers.toDomain()] Convert Entity → Domain
   ↓
[Flow<Resource<List<Food>>>] Wrap with Loading/Success/Error states
   ↓
[GetFoodsUseCase] Business logic layer
   ↓
[FoodViewModel] Collect flow → update StateFlow<FoodUiState>
   ↓
[FoodScreen] Compose UI renders foods list
```

## 🔑 Key Production Features

### 1. **Clean Architecture Layers**
- **Domain**: Framework-agnostic business logic
- **Data**: Implements domain contracts with Room + Retrofit
- **Presentation**: Android-specific UI with Jetpack Compose

### 2. **Repository Pattern**
- Single source of truth (Room database)
- Network-first with offline fallback
- Flow-based reactive updates

### 3. **Dependency Injection**
- Manual `AppContainer` for explicit dependency graph
- Singleton database & API service
- ViewModelProvider.Factory for ViewModel construction

### 4. **Error Handling**
- `Resource<T>` sealed class wrapper
- Offline fallback from Room cache
- UI states: Loading → Success/Error

### 5. **Database**
- Room with KSP annotation processing
- Flow-based reactive queries
- Upsert strategy for cache refresh

### 6. **Networking**
- Retrofit with Moshi JSON converter
- OkHttp with logging interceptor
- BuildConfig-based BASE_URL (10.0.2.2 for emulator)
- Timeouts: 15s connect/read/write

### 7. **Testing**
- Unit tests for mapper layer
- Test-friendly architecture (DI via constructor injection)

## 🛠️ Build Commands

```bash
# Clean build
./gradlew clean

# Compile Kotlin
./gradlew :app:compileDebugKotlin

# Run unit tests
./gradlew :app:testDebugUnitTest

# Build debug APK
./gradlew :app:assembleDebug

# Install on device/emulator
./gradlew :app:installDebug
```

## 📱 API Configuration

**Endpoint**: `GET https://10.0.2.2:8443/restaurants/{restaurantId}/foods`

**Response Example**:
```json
[
  {
    "id": 1,
    "name": "Margherita Pizza",
    "price": 299.99,
    "restroId": 1
  }
]
```

**Note**: `10.0.2.2` is the Android emulator's alias for `localhost` on the host machine.

## 🔐 HTTPS Localhost Setup

For self-signed certificates in development:

1. Add network security config for debug builds
2. Trust your local certificate in Android
3. Or use HTTP for local development (change BASE_URL)

## ✨ What's Working Now

- ✅ MVVM + Clean Architecture structure
- ✅ Repository pattern with Room caching
- ✅ Retrofit API service with Moshi JSON parsing
- ✅ Flow-based reactive data streams
- ✅ ViewModel with StateFlow
- ✅ Compose UI with loading/error states
- ✅ Manual DI container (production-ready pattern)
- ✅ Unit tests compiling and passing
- ✅ Offline-first capability with database fallback

## 🎯 Next Steps (Optional)

1. **Add network security config** for HTTPS localhost debugging
2. **Implement pull-to-refresh** in FoodScreen
3. **Add pagination** if API supports it
4. **Write repository tests** with fake API/DAO
5. **Add Timber** for production logging
6. **Configure ProGuard rules** for Retrofit/Moshi/Room
7. **Add error analytics** (Crashlytics, Sentry)
8. **Implement retry logic** with exponential backoff

