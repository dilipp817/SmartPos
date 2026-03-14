# SmartPos

Android app with MVVM + Clean Architecture, repository pattern, Retrofit networking, and Room caching.

## Architecture

The app is organized by layers:

- `app/src/main/java/com/autobill/smartpos/domain`: business models, repository contracts, use cases
- `app/src/main/java/com/autobill/smartpos/data`: DTOs, Room entities/DAO, repository implementation, mappers
- `app/src/main/java/com/autobill/smartpos/presentation`: Compose UI state + ViewModel + screen
- `app/src/main/java/com/autobill/smartpos/di`: manual production-style dependency container (`AppContainer`)
- `app/src/main/java/com/autobill/smartpos/core`: shared result wrapper (`Resource`)

## Food API Integration

Configured endpoint:

- `GET https://10.0.2.2:8443/restaurants/{restaurantId}/foods`

Example response:

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

Data flow:

1. `FoodApiService` fetches `List<FoodDto>`.
2. DTOs are mapped to `FoodEntity` and cached in Room.
3. Room emits entities as `Flow`.
4. Entities are mapped to domain `List<Food>`.
5. `GetFoodsUseCase` returns `Resource<List<Food>>` to `FoodViewModel`.
6. Compose `FoodScreen` renders loading/success/error states.

## Build and Test

```zsh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## Notes

- `localhost` from Android emulator is `10.0.2.2`.
- If your HTTPS cert is self-signed, configure trusted certs for debug builds.
