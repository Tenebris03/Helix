# Calorie Tracker

Personal Android app for logging food, tracking weight, and getting unsolicited Gemini coaching about your lunch choices.

## What it does

- **Barcode scanning** — point camera at a product, get nutritional info from Open Food Facts
- **Manual food log** — add/skip/delete entries, see daily macro totals on gauges
- **Weight tracking** — log weight, view progress over time
- **Coach** — after you log something questionable, Gemini judges you via notification

## Module architecture

```mermaid
graph LR
    subgraph App Shell
        A[:app]
    end

    subgraph Features Layer
        B[:feature:dashboard]
        C[:feature:tracking]
        D[:feature:onboarding]
        E[:feature:settings]
    end

    subgraph Core Bedrock
        F[:core:data]
        G[:core:ui]
        H[:core:model]
    end

    A --> B
    A --> C
    A --> D
    A --> E

    B --> F & G
    C --> F & G
    D --> F & G
    E --> F & G

    F --> H
    G --> H
```

- **`:core:model`** — pure Kotlin JVM, zero Android dependencies. Data classes + serialization.
- **`:core:data`** — Room DB, repositories, Retrofit APIs, WorkManager workers, Gemini integration.
- **`:core:ui`** — Shared Compose components and theming.
- **`:feature:*`** — Screens, ViewModels, per-feature Koin modules.

## Data flow

```mermaid
flowchart LR
    cam[Camera] --> mlkit[ML Kit Barcode]
    mlkit --> repo[FoodRepository]
    repo --> api[Open Food Facts API]
    repo --> cache[(Room Cache)]
    repo --> vm[DashboardViewModel]
    vm --> ui[Dashboard UI]

    foodLog[Manual Entry] --> dao[(Room FoodDao)]
    dao --> repo
```

```mermaid
flowchart LR
    log[User logs food] --> vm[DashboardViewModel]
    vm --> detector[FoodProblemDetector]
    detector --> coach[InvisibleCoachWorker]
    coach --> gemini[Gemini API]
    gemini --> notification[Notification]
```

## What's rough

- **OCR food recognition** via Google Vision is unreliable — don't trust it
- **No meal grouping** — entries are a flat list per day
- **Coach occasionally tells you to eat a salad while you're eating pizza** — it's AI, not a nutritionist
- **Single user, single device** — no accounts, no sync
- **No tests for the ViewModels** — architecture tests exist but business logic is untested

## Build

Requires Android Studio Ladybug+, JDK 21, Gradle 9.4.

```bash
# Coach feature needs this (optional — everything else works without it)
echo "GEMINI_API_KEY=your_key_here" > secrets.properties
```

## Tech

Kotlin 2.1, Jetpack Compose, Room, Retrofit + Kotlinx Serialization, Koin, WorkManager, CameraX, ML Kit.
