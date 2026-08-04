# Word Scanner Reader – Tomoe English

Ứng dụng Android dành cho học sinh Pre-Starters:

1. Mở ứng dụng, camera bật ngay.
2. Đưa thẻ chữ tiếng Anh vào khung sáng.
3. Nhấn **QUÉT & ĐỌC**.
4. Ứng dụng nhận diện chữ, hiển thị và đọc bằng giọng Anh-Mỹ.

## Tính năng

- CameraX xem trực tiếp camera sau.
- Chỉ quét vùng nằm trong khung sáng.
- ML Kit OCR Latin dạng bundled, có thể nhận diện không cần mạng.
- Android TextToSpeech để đọc từ.
- Giao diện một nút lớn, phù hợp với trẻ nhỏ.

## Cách lấy APK bằng GitHub

1. Tạo repository mới trên GitHub.
2. Giải nén toàn bộ project rồi tải các file lên repository.
3. Mở tab **Actions**.
4. Chọn workflow **Build Android APK**.
5. Nhấn **Run workflow** (hoặc chỉ cần push lên nhánh main).
6. Khi workflow hoàn tất, mở lần chạy đó và tải artifact **WordScanner-debug-apk**.
7. Giải nén artifact để lấy `app-debug.apk`, rồi cài lên điện thoại Android.

Điện thoại có thể cảnh báo vì APK chưa phát hành qua Google Play. Bạn cần cho phép cài ứng dụng từ nguồn đó.

## Build bằng Android Studio

- Mở thư mục project trong Android Studio.
- Đợi Gradle sync.
- Chọn **Build > Build APK(s)**.
- APK nằm tại `app/build/outputs/apk/debug/app-debug.apk`.

## Chỉnh tốc độ/giọng đọc

Trong `MainActivity.kt`:

```kotlin
textToSpeech?.setLanguage(Locale.US)
textToSpeech?.setSpeechRate(0.82f)
```

Đổi `Locale.US` thành `Locale.UK` nếu muốn ưu tiên giọng Anh-Anh. Giọng thực tế phụ thuộc bộ máy Text-to-Speech có trên điện thoại.
