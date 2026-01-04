# 📁 Cấu trúc dự án Đánh Vần

```
gamedanhvan-main/
├── 📄 build.gradle.kts              # Build config cấp project
├── 📄 settings.gradle.kts           # Cấu hình Gradle settings
├── 📄 gradle.properties             # Gradle properties
├── 📄 gradlew                       # Gradle wrapper (Linux/Mac)
├── 📄 gradlew.bat                   # Gradle wrapper (Windows)
├── 📄 Spells Data.json              # Dữ liệu từ vựng mở rộng (~730 từ)
├── 📄 sample_words.json             # Dữ liệu từ vựng mẫu (~215 từ)
│
├── 📁 gradle/                       # Gradle wrapper files
│   └── 📁 wrapper/
│
├── 📁 app/                          # Module ứng dụng chính
│   ├── 📄 build.gradle.kts          # Build config module app
│   ├── 📄 proguard-rules.pro        # ProGuard rules
│   ├── 📄 skul9x.jks               # Release signing key
│   │
│   └── 📁 src/
│       ├── 📁 androidTest/          # Android instrumented tests
│       ├── 📁 test/                 # Unit tests
│       │
│       └── 📁 main/
│           ├── 📄 AndroidManifest.xml
│           ├── 📄 ic_launcher-playstore.png
│           │
│           ├── 📁 java/com/skul9x/danhvan/
│           │   │
│           │   ├── 📄 DanhVanApplication.kt    # Application class với crash handler
│           │   ├── 📄 MainActivity.kt          # Activity chính, Navigation host
│           │   │
│           │   ├── 📁 data/                    # Data layer
│           │   │   ├── 📄 AppDatabase.kt       # Room Database setup
│           │   │   ├── 📄 Converters.kt        # Type converters cho Room
│           │   │   ├── 📄 DailyStats.kt        # Entity thống kê hàng ngày
│           │   │   ├── 📄 DailyStatsDao.kt     # DAO cho DailyStats
│           │   │   ├── 📄 ShopItem.kt          # Data class & items cửa hàng
│           │   │   ├── 📄 Topic.kt             # Data class chủ đề
│           │   │   ├── 📄 TopicImageDao.kt     # DAO cho ảnh chủ đề
│           │   │   ├── 📄 TopicImageEntity.kt  # Entity ảnh chủ đề
│           │   │   ├── 📄 WordDao.kt           # DAO cho từ vựng
│           │   │   └── 📄 WordEntity.kt        # Entity từ vựng
│           │   │
│           │   ├── 📁 ui/                      # UI layer
│           │   │   ├── 📄 MainViewModel.kt     # ViewModel chính (762 lines)
│           │   │   ├── 📄 StickerPlacement.kt  # Data class vị trí sticker
│           │   │   │
│           │   │   ├── 📁 common/              # Shared UI components
│           │   │   │   └── [Common composables]
│           │   │   │
│           │   │   ├── 📁 crash/               # Crash handling
│           │   │   │   └── 📄 CrashActivity.kt # Màn hình hiển thị lỗi
│           │   │   │
│           │   │   ├── 📁 debug/               # Debug tools
│           │   │   │   └── 📄 DebugScreen.kt   # Màn hình debug
│           │   │   │
│           │   │   ├── 📁 game/                # Game screens
│           │   │   │   ├── 📄 GameScreen.kt    # Màn hình game chính
│           │   │   │   └── 📄 GameModes.kt     # 5 chế độ chơi (957 lines)
│           │   │   │                           # - ExploreMode
│           │   │   │                           # - SpellingMode  
│           │   │   │                           # - QuizMode
│           │   │   │                           # - FillInMode
│           │   │   │                           # - MemoryMode
│           │   │   │
│           │   │   ├── 📁 parent/              # Parent mode
│           │   │   │   └── 📄 ParentalScreen.kt # Màn hình quản lý phụ huynh
│           │   │   │
│           │   │   ├── 📁 shop/                # Shop & rewards
│           │   │   │   ├── 📄 ShopScreen.kt    # Cửa hàng (664 lines)
│           │   │   │   └── 📄 StickerBookScreen.kt # Sách sticker (22KB)
│           │   │   │
│           │   │   ├── 📁 stats/               # Statistics
│           │   │   │   └── [Stats screen]
│           │   │   │
│           │   │   ├── 📁 theme/               # App theming
│           │   │   │   └── [Theme files]
│           │   │   │
│           │   │   └── 📁 topic/               # Topic selection
│           │   │       └── 📄 TopicScreen.kt   # Chọn chủ đề
│           │   │
│           │   ├── 📁 util/                    # Utilities
│           │   │   ├── 📄 GoogleImageHelper.kt      # Tìm ảnh Google
│           │   │   ├── 📄 SpeechRecognizerHelper.kt # Nhận dạng giọng nói
│           │   │   ├── 📄 TranslationHelper.kt      # Dịch thuật
│           │   │   └── 📄 VietnameseSpeller.kt      # Logic đánh vần VN
│           │   │
│           │   └── 📁 utils/                   # Additional utilities
│           │       ├── 📄 AssetManager.kt      # Quản lý assets
│           │       ├── 📄 BackupManager.kt     # Backup/Restore (17KB)
│           │       ├── 📄 JsonImportManager.kt # Import JSON
│           │       ├── 📄 SyllableTokenizer.kt # Tách âm tiết
│           │       └── 📄 TTSManager.kt        # Text-to-Speech
│           │
│           └── 📁 res/                         # Resources
│               ├── 📁 drawable/                # Drawables
│               ├── 📁 layout/                  # XML layouts (legacy)
│               ├── 📁 mipmap-*/                # App icons (hdpi-xxxhdpi)
│               ├── 📁 raw/                     # Audio files
│               ├── 📁 values/                  # Strings, colors, themes
│               ├── 📁 values-night/            # Dark theme
│               └── 📁 xml/                     # Backup rules, file paths
│
└── 📁 build/                        # Build outputs (generated)
```

---

## 🏗️ Kiến trúc ứng dụng

```
┌──────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ MainActivity│──│  Screens    │──│ Compose Components  │  │
│  └─────────────┘  │ (Game,Shop, │  │ (Common, Theme)     │  │
│                   │  Topic,etc) │  └─────────────────────┘  │
│                   └──────┬──────┘                            │
│                          │                                   │
│  ┌───────────────────────▼───────────────────────────────┐  │
│  │                  MainViewModel                         │  │
│  │  - State management (StateFlow)                        │  │
│  │  - Business logic                                      │  │
│  │  - Coordinates data & UI                               │  │
│  └───────────────────────┬───────────────────────────────┘  │
└──────────────────────────┼───────────────────────────────────┘
                           │
┌──────────────────────────┼───────────────────────────────────┐
│                     Data Layer                               │
│  ┌───────────────────────▼───────────────────────────────┐  │
│  │                Room Database                           │  │
│  │  ┌──────────┐  ┌──────────────┐  ┌──────────────────┐ │  │
│  │  │ WordDao  │  │ DailyStatsDao│  │ TopicImageDao    │ │  │
│  │  └────┬─────┘  └──────┬───────┘  └────────┬─────────┘ │  │
│  │       │               │                   │           │  │
│  │  ┌────▼─────┐  ┌──────▼───────┐  ┌────────▼─────────┐ │  │
│  │  │WordEntity│  │ DailyStats   │  │TopicImageEntity  │ │  │
│  │  └──────────┘  └──────────────┘  └──────────────────┘ │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │                    Utilities                            │ │
│  │  ┌─────────────┐  ┌───────────────┐  ┌───────────────┐ │ │
│  │  │ TTSManager  │  │ BackupManager │  │ ImageHelper   │ │ │
│  │  └─────────────┘  └───────────────┘  └───────────────┘ │ │
│  └────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
```

---

## 📊 Chi tiết các module

### 1. Data Layer (`data/`)

| File | Mô tả | Kích thước |
|------|-------|------------|
| `AppDatabase.kt` | Room Database với 3 bảng | 1.1 KB |
| `WordEntity.kt` | Entity cho từ vựng | 0.4 KB |
| `WordDao.kt` | CRUD operations cho words | 1.1 KB |
| `DailyStats.kt` | Entity thống kê theo ngày | 0.3 KB |
| `DailyStatsDao.kt` | DAO cho thống kê | 0.7 KB |
| `ShopItem.kt` | Định nghĩa items shop + default data | 7.7 KB |
| `Topic.kt` | Định nghĩa các chủ đề | 1.3 KB |

### 2. UI Layer (`ui/`)

| Thư mục | Files | Chức năng |
|---------|-------|-----------|
| `game/` | `GameScreen.kt`, `GameModes.kt` | 5 chế độ chơi học tập |
| `shop/` | `ShopScreen.kt`, `StickerBookScreen.kt` | Cửa hàng & sách sticker |
| `topic/` | `TopicScreen.kt` | Chọn chủ đề học |
| `parent/` | `ParentalScreen.kt` | Quản lý phụ huynh |
| `common/` | Shared composables | Components dùng chung |
| `theme/` | Theme files | Material 3 theming |

### 3. Utilities (`util/` & `utils/`)

| File | Chức năng |
|------|-----------|
| `VietnameseSpeller.kt` | Logic đánh vần tiếng Việt (9KB) |
| `GoogleImageHelper.kt` | Tìm kiếm ảnh từ Google (7.5KB) |
| `SpeechRecognizerHelper.kt` | Nhận dạng giọng nói (4.5KB) |
| `BackupManager.kt` | Sao lưu/khôi phục dữ liệu (17KB) |
| `TTSManager.kt` | Text-to-Speech wrapper |
| `JsonImportManager.kt` | Import từ vựng từ JSON |

---

## 🎮 Luồng hoạt động

```
┌─────────────────────────────────────────────────────────────────┐
│                        App Launch                               │
│                            │                                    │
│                            ▼                                    │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                    MainActivity                            │ │
│  │  - Initialize ViewModel                                    │ │
│  │  - Setup Navigation (NavHost)                              │ │
│  └────────────────────────┬──────────────────────────────────┘ │
│                           │                                     │
│           ┌───────────────┼───────────────┐                    │
│           ▼               ▼               ▼                    │
│    ┌──────────┐    ┌──────────┐    ┌──────────┐               │
│    │ TopicScr │    │ GameScr  │    │ ShopScr  │               │
│    │ (Select  │───▶│ (5 Modes │    │ (Buy     │               │
│    │  topic)  │    │  to play)│    │ rewards) │               │
│    └──────────┘    └────┬─────┘    └────┬─────┘               │
│                         │               │                      │
│                         ▼               ▼                      │
│                   ┌──────────┐    ┌──────────┐                │
│                   │ ⭐ Stars │    │ Sticker  │                │
│                   │ Earned   │───▶│ Book     │                │
│                   └──────────┘    └──────────┘                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📝 Database Schema

```sql
-- Words table
CREATE TABLE words (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    text TEXT NOT NULL,
    imagePath TEXT,
    audioPath TEXT,
    category TEXT
);

-- Daily Stats table  
CREATE TABLE daily_stats (
    date INTEGER PRIMARY KEY,
    wordsLearned INTEGER DEFAULT 0,
    starsEarned INTEGER DEFAULT 0
);

-- Topic Images table
CREATE TABLE topic_images (
    topicId TEXT PRIMARY KEY,
    imagePath TEXT NOT NULL
);
```

---

## 🔧 Build Configuration

### Dependencies chính

```kotlin
// Compose UI
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.ui)
implementation(libs.androidx.material3)
implementation(libs.androidx.navigation.compose)

// Room Database
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
ksp(libs.androidx.room.compiler)

// Image Loading
implementation(libs.coil.compose)

// JSON Parsing
implementation(libs.gson)
```

### Signing Config

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("skul9x.jks")
        keyAlias = "key0"
        // passwords configured
    }
}
```
