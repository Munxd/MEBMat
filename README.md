# MEBMat

MEBMat, MEB'e ait dijital eğitim materyallerinin okunabilirlik ve erişilebilirlik açısından ön değerlendirmesini yapmak amacıyla geliştirilen bir Android uygulamasıdır.

## Proje Amacı

Uygulama; PDF veya görsel formatındaki eğitim materyallerini alarak OCR ve görüntü işleme tabanlı analizler için bir altyapı sunar. Projenin temel hedefleri arasında metinlerin algılanması, materyalin okunabilirliğinin incelenmesi ve ilerleyen aşamalarda kalite sınıflandırması için makine öğrenmesi bileşenlerinin kullanılması bulunmaktadır.

## Kullanılan Teknolojiler

- Kotlin
- Android
- Jetpack Compose
- Material 3
- Navigation Compose
- Google ML Kit Text Recognition
- Gradle
- Python / scikit-learn (ML geliştirme çalışmaları için)

## Proje Akışı

Genel sistem akışı:

```
PDF / Görsel
     ↓
Ön İşleme
     ↓
OCR
     ↓
Özellik Çıkarma
     ↓
CV + NLP Analizi
     ↓
ML Modeli
     ↓
Kalite Sınıflandırması
     ↓
Rapor
```

## Android Uygulamasını Çalıştırma

### Gereksinimler

- Android Studio
- JDK 11 veya Android Studio'nun uyumlu JDK sürümü
- İnternet bağlantısı (ilk Gradle senkronizasyonunda bağımlılıkların indirilmesi için)
- Android SDK

### Kurulum

1. Bu repository'yi klonlayın:
   ```bash
   git clone https://github.com/Munxd/MEBMat.git
   ```

2. Projeyi Android Studio ile açın.

3. Gradle senkronizasyonunun tamamlanmasını bekleyin.

4. Bir Android cihaz bağlayın veya Android Emulator oluşturun.

5. `app` yapılandırmasını çalıştırın.

Minimum Android sürümü: **Android 8.0 (API 26)**.

## Python / ML Çalışmaları

Python tarafındaki çalışmalar model eğitimi ve veri analizi amacıyla kullanılmaktadır.

Gerekli paketleri yüklemek için:

```bash
pip install -r requirements.txt
```

Model eğitimi veya Python scriptleri kullanılırken ilgili scriptin bulunduğu klasörde Python ortamının aktif olduğundan emin olun.

> Not: Eğitilmiş model dosyaları ve geçici Python çıktıları Git deposuna dahil edilmeyebilir. `.gitignore` dosyasında belirtilen dosyalar yerel ortamda yeniden oluşturulabilir.

## Proje Yapısı

Temel Android proje yapısı:

```
MEBMat/
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           └── res/
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
├── build.gradle.kts
├── requirements.txt
└── README.md
```

## Geliştirme Notu

Bu proje staj kapsamında geliştirilmektedir. Uygulamadaki analiz özellikleri aşamalı olarak geliştirilmektedir; mevcut sürümde OCR ve materyal işleme altyapısı ön plandadır.

## Lisans

Bu repository staj/proje geliştirme amacıyla oluşturulmuştur.
