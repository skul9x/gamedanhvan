# Changelog

## [2026-02-07]
### Fixed
- **MainViewModel.kt**: Fixed 3 HIGH severity bugs:
    - **Race Condition**: Replaced Java `synchronized` with Kotlin `Mutex` (StarMutex) and wrapped all star transactions (`addStar`, `claimDailyReward`, `spinWheel`, `purchaseItem`).
    - **Improper Error Handling**: Added detailed `Log.e` logging, data reset logic for corrupted states, and ensured `CancellationException` is rethrown for structured concurrency.
    - **Json Parsing Errors**: Implemented version migration system (`SHOP_DATA_VERSION`, `STICKER_DATA_VERSION`) to reset data gracefully when class structure changes.
- **MainViewModel.kt**: Fixed 1 MEDIUM severity bug:
    - **Leaking Mutable State**: Refactored `appendLog()` to use immutable list operations (`_debugLog.value + message`) instead of mutating a `toMutableList()`.
- **GameModes.kt**: 
    - Fixed 7 bugs (2 Critical, 3 Medium, 2 Minor) using State Machine Refactor:
        - **Critical**: Unified `AnimationPhase` State Machine to fix stuck merge animations.
        - **Critical**: Implemented `isResultLocked` guard to fix partial match overwriting correct results.
        - **Medium**: Added `safeReward()` with local locks to prevent double-rewards and SpellingMode farming.
        - **Medium**: Keyed all game states by `word.text` to ensure proper reset on navigation.
        - **Minor**: Added `minFontSize` and `maxIterations` to `ResizingStrokeText` to stop infinite shrinking.
        - **Minor**: Added data guards to `QuizMode`, `FillInMode`, and `.distinct()` syllable distractors.
    - Fixed NPE in word image URI handling.
    - Fixed Resource Leak by migrating MediaPlayer to SoundManager.
- **ParentalScreen.kt**:
    - **Blocking Main Thread**: Moved share logic to `viewModel.shareBackupFile()`.
    - **Memory Leak**: Removed `Context` parameter from `importJson()` call.
    - **Performance**: Added `key` to `LazyColumn` items.
- **GameScreen.kt**: 
    - Fixed Resource Leak by using shared `context.imageLoader`.
    - Fixed Structured Concurrency by replacing `launch(IO)` with `withContext(IO)`.
- **SpeechRecognizerHelper.kt**:
    - Fixed 2 HIGH severity bugs:
        - **Resource Leak**: Added listener unsetting and StateFlow resetting in `destroy()`.
        - **Race Condition**: Added `@Synchronized` and null guards to public methods.
    - Fixed 2 MEDIUM severity bugs:
        - **TOCTOU**: Used local variable capture for `speechRecognizer`.
        - **Platform Type Pitfall**: Added explicit null checks for `ArrayList<String>!` elements using `firstOrNull()`.
- **MainViewModel.kt**:
    - **Memory Leak**: Updated `importJson()` to use `getApplication()` instead of passed-in Activity context.
    - **Blocking Main Thread**: Added `shareBackupFile()` with proper error handling and UI thread dispatch.
    - **Improper Error Handling**: Added user notifications via StateFlow when sharing backup fails.
- **StickerBookScreen.kt**:
    - **Null Pointer Exception**: Replaced unsafe `!!` operators with safe calls (`?.let`) for `flyingStickerItem` and `selectedStickerId`.
    - **Memory Churn**: Optimized `Paint` object creation by using `remember` to cache it, preventing allocation during draw operations.
- **ShopScreen.kt**:
    - **Stale State Bug**: Fixed celebration dialog showing even if purchase failed by using an `onSuccess` callback from the ViewModel.
    - **Dead Code**: Fixed press animation in `ShopItemCard` by implementing `InteractionSource` and `collectIsPressedAsState`.
    - **UX Bug**: Fixed nested clickable issue where clicking a sticker image swallowed the event; made clickable modifier conditional based on item type.
    - **Bug 3 (UX)**: Fixed celebration dialog dismiss on content tap by consuming click propagation in the inner container.
    - **Bug 4 (Stability)**: Replaced all unsafe `!!` operators with local snapshots to prevent race-condition `NullPointerException` during recomposition.
    - **Bug 7 (Security/UX)**: Randomized Math Gate problems (range 10-30) using `remember` to prevent children from memorizing fixed answers.
    - **Feature**: Added Purchase Confirmation Dialog to prevent accidental star spending.
    - **Build Fix**: Resolved "Conflicting import" errors by removing duplicate `Brush`, `Color`, and `LocalContext` imports.
- **MainViewModel.kt**:
    - **UI Synchronization**: Updated `purchaseItem` signature to support an `onSuccess` callback for safe UI state transitions.
    - **Bug 1 (Logic)**: Implemented `equippedSticker` StateFlow and `equipSticker()` toggle function to fix hardcoded `isEquipped = false` bug.
    - **Persistence**: Added SharedPreferences persistence for the currently equipped sticker.

### Changed
- Refactored all star management logic in `MainViewModel.kt` to be thread-safe and coroutine-compatible.
- Added explicit versioning to JSON data stored in `SharedPreferences`.
