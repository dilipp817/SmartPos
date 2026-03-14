# Why 10.0.2.2 Instead of localhost?

## TL;DR

**`10.0.2.2` = Your Mac's `localhost` when accessed from Android emulator**

Your backend runs on your Mac at `localhost:8443`, but the Android emulator needs `10.0.2.2:8443` to reach it.

---

## The Complete Explanation

### What Happens with `localhost`

```
❌ If your app uses: https://localhost:8443/restaurants/1/foods

Android Emulator thinks:
"localhost = ME (the emulator itself)"
→ Looks for a server inside the emulator
→ No server found there
→ Connection refused ❌
```

### What Happens with `10.0.2.2`

```
✅ If your app uses: https://10.0.2.2:8443/restaurants/1/foods

Android Emulator thinks:
"10.0.2.2 = my host machine (the Mac)"
→ Routes request to your Mac
→ Finds your backend at localhost:8443
→ Connection successful ✅
```

---

## Visual Diagram

```
┌─────────────────────────────────────────┐
│          Your Mac (Host)                │
│                                         │
│  Backend Server: localhost:8443 ✅      │
│  (Spring Boot / Node / Django / etc)    │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │   Android Emulator (Guest VM)    │ │
│  │                                   │ │
│  │   SmartPos App                    │ │
│  │   │                               │ │
│  │   ├─ localhost:8443 ❌            │ │
│  │   │  (points to emulator)         │ │
│  │   │                               │ │
│  │   └─ 10.0.2.2:8443 ✅             │ │
│  │      (points to Mac)  ───────────┼─┼──→ Mac's localhost:8443
│  │                                   │ │
│  └───────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

---

## Android Emulator Special Addresses

The Android emulator provides these special network addresses:

| Address | What It Points To |
|---------|-------------------|
| `10.0.2.1` | Emulator's gateway/router |
| `10.0.2.2` | **Your Mac (host machine)** ⭐ |
| `10.0.2.3` | First DNS server |
| `10.0.2.15` | Emulator itself |
| `127.0.0.1` / `localhost` | Emulator itself (NOT your Mac) |

**You need `10.0.2.2` to reach your Mac's localhost!**

---

## Different Devices Need Different Addresses

### 1. Android Emulator (Current Setup ✅)

```kotlin
buildConfigField("String", "BASE_URL", "\"https://10.0.2.2:8443/\"")
```

Your backend: `localhost:8443` on your Mac ✅

---

### 2. Real Android Device (Same WiFi Network)

```kotlin
buildConfigField("String", "BASE_URL", "\"https://192.168.1.100:8443/\"")
```

Find your Mac's IP:
```bash
ipconfig getifaddr en0  # WiFi
# Example output: 192.168.1.100
```

Your backend: Must be accessible on your Mac's LAN IP, not just `localhost`

---

### 3. Real Android Device (USB + ADB Reverse)

```bash
adb reverse tcp:8443 tcp:8443
```

```kotlin
buildConfigField("String", "BASE_URL", "\"https://localhost:8443/\"")
```

This forwards device's `localhost:8443` → Mac's `localhost:8443`

---

### 4. Production Server

```kotlin
buildConfigField("String", "BASE_URL", "\"https://api.yourcompany.com/\"")
```

---

## Current Project Configuration

### Where It's Configured

**File**: `app/build.gradle.kts`

```kotlin
buildTypes {
    debug {
        buildConfigField("String", "BASE_URL", "\"https://10.0.2.2:8443/\"")
    }
    release {
        buildConfigField("String", "BASE_URL", "\"https://10.0.2.2:8443/\"")
    }
}
```

### How It's Used

**File**: `di/NetworkModule.kt`

```kotlin
fun provideRetrofit(): Retrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.BASE_URL)  // ← Uses 10.0.2.2:8443
    .client(provideOkHttpClient())
    .addConverterFactory(MoshiConverterFactory.create(provideMoshi()))
    .build()
```

---

## Make It Configurable (Recommended Enhancement)

### Option A: Build Flavors (Best for Teams)

```kotlin
// app/build.gradle.kts
android {
    flavorDimensions += "device"
    
    productFlavors {
        create("emulator") {
            dimension = "device"
            buildConfigField("String", "BASE_URL", "\"https://10.0.2.2:8443/\"")
        }
        
        create("device") {
            dimension = "device"
            // Use your Mac's actual LAN IP
            buildConfigField("String", "BASE_URL", "\"https://192.168.1.100:8443/\"")
        }
        
        create("production") {
            dimension = "device"
            buildConfigField("String", "BASE_URL", "\"https://api.smartpos.com/\"")
        }
    }
}
```

Build variants:
- `emulatorDebug` → For emulator testing
- `deviceDebug` → For real device testing
- `productionRelease` → For production

### Option B: Local Properties (Best for Solo Development)

Create/update `local.properties`:
```properties
base.url=https://10.0.2.2:8443/
# Or comment/uncomment based on your device:
# base.url=https://192.168.1.100:8443/  # Real device
# base.url=https://localhost:8443/      # With adb reverse
```

Update `app/build.gradle.kts`:
```kotlin
val localProps = Properties().apply {
    file(rootProject.file("local.properties")).inputStream().use { load(it) }
}

android {
    buildTypes {
        debug {
            buildConfigField(
                "String",
                "BASE_URL",
                "\"${localProps.getProperty("base.url", "https://10.0.2.2:8443/")}\""
            )
        }
    }
}
```

### Option C: Environment Variables

```bash
export BASE_URL="https://192.168.1.100:8443/"
./gradlew installDebug
```

```kotlin
android {
    buildTypes {
        debug {
            buildConfigField(
                "String",
                "BASE_URL",
                "\"${System.getenv("BASE_URL") ?: "https://10.0.2.2:8443/"}\""
            )
        }
    }
}
```

---

## Testing Different Scenarios

### Scenario 1: You're Testing in Emulator
**Current config works!** ✅
```
BASE_URL = "https://10.0.2.2:8443/"
```

### Scenario 2: You Want to Test on Real Phone
**Update BASE_URL to your Mac's IP:**

1. Find your Mac's IP:
   ```bash
   ipconfig getifaddr en0
   # Output: 192.168.1.100 (example)
   ```

2. Update `build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"https://192.168.1.100:8443/\"")
   ```

3. Make sure your backend binds to all interfaces:
   ```bash
   # NOT: localhost:8443 (only accessible locally)
   # YES: 0.0.0.0:8443 (accessible from network)
   ```

### Scenario 3: Your Backend is on a Remote Server
**Use the actual server URL:**
```kotlin
buildConfigField("String", "BASE_URL", "\"https://dev-server.yourcompany.com/\"")
```

---

## Troubleshooting

### "Connection Refused" Error

**Possible causes:**

1. ✅ Using emulator but `BASE_URL = localhost` → Change to `10.0.2.2`
2. ✅ Using real device but `BASE_URL = 10.0.2.2` → Change to your Mac's LAN IP
3. ✅ Backend not running → Start your backend server
4. ✅ Backend only listening on `127.0.0.1` → Change to `0.0.0.0`
5. ✅ Firewall blocking → Allow port 8443

### "SSL Handshake Failed" Error

For self-signed certificates in development, create:

**`app/src/main/res/xml/network_security_config.xml`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <debug-overrides>
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>
```

**Update `AndroidManifest.xml`:**
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

---

## Summary

| Question | Answer |
|----------|--------|
| **Why not `localhost`?** | In emulator, `localhost` = emulator itself, not your Mac |
| **What is `10.0.2.2`?** | Special IP that Android emulator uses to reach host machine |
| **Real device?** | Use your Mac's actual LAN IP (e.g., `192.168.1.100`) |
| **Production?** | Use your actual API domain |

**Current setup is correct for emulator! ✅**

If you want to support multiple environments, I can implement Option A (build flavors) for you.

