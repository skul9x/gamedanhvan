# 🎓 Game Đánh Vần (Vietnamese Spelling)
### Ứng dụng học đánh vần tiếng Việt tương tác cho trẻ em

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.21-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2023.10.01-green.svg?style=flat&logo=android)](https://developer.android.com/jetpack/compose)
[![Material3](https://img.shields.io/badge/Material3-1.1.2-purple.svg?style=flat&logo=materialdesign)](https://m3.material.io)
[![Platform](https://img.shields.io/badge/Platform-Android%207.0+-brightgreen.svg?style=flat&logo=android)](https://android.com)

<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" alt="Đánh Vần Logo" width="160"/>
</p>

---

## 📱 Giới thiệu

**Game Đánh Vần** là giải pháp giáo dục toàn diện giúp trẻ em làm quen với con chữ và âm tiết tiếng Việt. Ứng dụng kết hợp giữa hình ảnh sinh động, âm thanh chân thực và công nghệ nhận dạng giọng nói AI để tạo ra một môi trường học tập "chơi mà học" đầy hứng khởi.

## ✨ Tính năng nổi bật

### 🎮 Các chế độ chơi đa dạng
- **🔍 Khám Phá (Explore)**: Tự động tìm kiếm hình ảnh Google theo từ vựng. Trẻ nói và AI sẽ kiểm tra phát âm ngay lập tức.
- **🧩 Ghép Từ (Spelling)**: Thách thức tư duy với việc sắp xếp các âm tiết thành từ đúng.
- **🤔 Trắc Nghiệm (Quiz)**: Luyện tập khả năng nhận diện mặt chữ qua hình ảnh minh họa.
- **📝 Điền Khuyết (Fill-in)**: Tìm âm tiết còn thiếu để hoàn thiện từ.
- **🧠 Lật Hình (Memory)**: Rèn luyện trí nhớ qua việc ghép cặp hình ảnh và từ vựng.

### 🌟 Hệ thống Gamification
- **Stars & Rewards**: Tặng sao sau mỗi câu trả lời đúng.
- **Shop & Stickers**: Sử dụng sao để mua sticker độc đáo và trang trí vào bộ sưu tập.
- **Lucky Spin**: Vòng quay may mắn nhận quà hàng ngày.

### 👨‍👩‍👧 Parent Control (Chế độ phụ huynh)
- Quản lý danh mục từ vựng (Động vật, Nghề nghiệp, Trái cây...).
- **Custom Data**: Nhập (Import) từ vựng từ tệp JSON tùy chỉnh.
- **Safe Backup**: Sao lưu toàn bộ tiến trình và dữ liệu vào tệp ZIP an toàn.

---

## 🛠️ Tech Stack & Architecture

Dự án được xây dựng với các tiêu chuẩn phát triển Android hiện đại nhất:

- **UI Framework**: Jetpack Compose 100%.
- **Architecture**: MVVM (Model-View-ViewModel).
- **Database**: Room Persistence Library.
- **Concurrency**: Kotlin Coroutines & Flow.
- **Image Loading**: Coil (với cơ chế Singleton tối ưu tài nguyên).
- **Speech AI**: Google Speech-to-Text Integration.
- **Audio Engine**: SoundManager singleton (quản lý MediaPlayer tránh rò rỉ bộ nhớ).

### 🧠 Robust Implementation Patterns
Ứng dụng đã được tối ưu hóa sâu với các kỹ thuật:
- **Unified State Machine**: Quản lý animation và luồng game bằng `AnimationPhase` enum, loại bỏ hoàn toàn lỗi kẹt animation.
- **Race Condition Protection**: Sử dụng `StarMutex` (Kotlin Mutex) để đảm bảo an toàn cho các giao dịch sao (stars).
- **Result Locking**: Cơ chế khóa kết quả AI giúp loại bỏ phản hồi sai lệch do độ trễ mạng.
- **Structured Concurrency**: Xử lý `CancellationException` đúng cách, đảm bảo app ổn định ngay cả khi chuyển màn hình liên tục.

---

## 🚀 Cài đặt & Phát triển

### Yêu cầu
- **Android Studio Iguana** trở lên.
- **JDK 17**.
- Một thiết bị Android API 24+.

### Build Project
```bash
# Clone source code
git clone https://github.com/skul9x/gamedanhvan.git

# Build APK Debug
./gradlew assembleDebug
```

---

## 📦 Định dạng dữ liệu Custom
Bạn có thể tự tạo bộ từ vựng riêng bằng file JSON:
```json
[
  {
    "text": "Con Voi",
    "category": "Động vật"
  },
  {
    "text": "Bác sĩ",
    "category": "Nghề nghiệp"
  }
]
```

---

## 🛡️ License & Credits
- **Tác giả:** Skul9x
- **Mục đích:** Dự án mã nguồn mở phục vụ giáo dục cộng đồng.
- **Đóng góp:** Mọi Pull Request nhằm cải thiện UX hoặc sửa lỗi đều được hoan nghênh.

<p align="center">
  <i>Được thực hiện với ❤️ dành cho sự nghiệp giáo dục trẻ em Việt Nam.</i>
</p>
