<div align="center">

# 🌈 Eymen Kids
### 🎬 Minik Kalpler, Büyük Mutluluklar!

**Dombilinin uşağı Gıppık! 💖**

Çocuk şarkılarını ve çizgi film bölümlerini tek uygulamada, renkli ve kolay kullanımlı bir arayüzde izleyin.

[![Android CI](https://github.com/mmustafatasoy/EymenKids/actions/workflows/build.yml/badge.svg)](https://github.com/mmustafatasoy/EymenKids/actions/workflows/build.yml)
![Sürüm](https://img.shields.io/badge/Eymen_Kids-v1.8.0-8954ED)
![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)
![TV](https://img.shields.io/badge/Android_TV-Kumanda_deste%C4%9Fi-2776D2)
[![MIT Lisansı](https://img.shields.io/badge/Kaynak_kod-MIT-FFCC54)](LICENSE)

**[📱 APK derlemeleri](https://github.com/mmustafatasoy/EymenKids/actions/workflows/build.yml)** ·
**[🎞️ Video arşivi](https://github.com/mmustafatasoy/EymenKids/releases)** ·
**[📚 Video ekleme kılavuzu](docs/seri-bolum-sistemi.md)**

</div>

---

## ✨ Eymen Kids v1.8.0 özellikleri

| Özellik | Açıklama |
|---|---|
| 🌈 **5 saniyelik açılış** | “Eymen Kids” harflerinin tek tek düştüğü animasyon, yükleniyor simgesi ve hareketli **“Dombilinin uşağı gıppık”** yazısı. |
| 🎨 **Yeni uygulama logosu** | Renkli **EK** simgesi; Android TV uygulama menüsü için ayrı yatay banner. |
| 🎬 **Tam ekran video** | Video açılınca oynatma kontrolleri gizlenir; ekrana dokununca geri, oynat/duraklat, ilerleme ve sonraki video seçenekleri görünür. |
| ⏭️ **Kesintisiz oynatma** | Video bitince yaklaşık **4,2 saniyelik “HER ŞEY GIPPIK İÇİN!”** geçiş ekranı görünür; sonraki bölüm otomatik başlar. Son seriden sonra liste başa döner. |
| 📚 **Seri / sezon / bölüm** | Aynı seriye ait bölümler sıralı gösterilir. İleri düğmesi seriler arasında da dolaşır. |
| 🌐 **İnternetten izle** | İndirilmemiş MP4 videolar, bağlantı varsa GitHub Releases üzerinden oynatılabilir. |
| ⬇️ **Çevrimdışı indir** | İndirilen MP4 dosyası boyut ve **SHA-256** ile doğrulanarak cihazda saklanır. |
| 📺 **TV kumandası** | Android TV / Leanback başlatıcı tanımı, yön / OK tuşlarıyla menüde gezinme ve kumandayla oynatıcı kontrollerini açma. |
| 🔄 **Canlı video kataloğu** | GitHub’a yeni bölüm eklemek için APK’yı her seferinde yeniden kurmak gerekmez. |

> **Önemli:** İnternetten izleme sırasında bağlantı kesilirse video devam etmeyebilir. Gerçek çevrimdışı izleme için indirme işleminin **%100 tamamlanması** gerekir. İnternet yokken otomatik oynatma yalnızca daha önce indirilmiş videolar arasında ilerler.

## 📺 Telefon, Android TV ve Senna Whale OS

Uygulama kimliği: `com.eymen.kids` · Minimum Android sürümü: **7.0 (API 24)**.

- **Android telefon / tablet:** APK kurulumu ve dokunmatik kullanım hedeflenir.
- **Android TV / Google TV:** TV başlatıcısı ve kumanda odak desteği eklenmiştir. Gerçek cihazda uyumluluk kontrolü gereklidir.
- **Senna 32WF7500H / Whale OS:** Bu model için TV arayüzü ve kumanda desteği üzerinde çalışılmaktadır. **Whale OS’nin normal Android APK dosyalarını dışarıdan yüklemeye izin verdiği veya bu cihazda APK’nın çalışacağı henüz doğrulanmamıştır.** Whale OS Android tabanlı olsa bile APK kurulumunun TV üreticisi tarafından sınırlandırılması mümkündür.
- **LG webOS / Samsung Tizen:** Bu Android APK doğrudan kullanılamaz; ayrı platform uygulaması gerekir.

### 📥 APK'yı indirme

**[GitHub Actions → Build Eymen Kids APK](https://github.com/mmustafatasoy/EymenKids/actions/workflows/build.yml)**

1. Bağlantıdaki en son **başarılı (yeşil)** derlemeyi açın.
2. Sayfanın **Artifacts** bölümünden `EymenKids-debug-apk` dosyasını indirin (GitHub oturumu gerekebilir).
3. ZIP içindeki `app-debug.apk` dosyasını çıkarın ve uyumlu Android cihazda kurun.

**[Video Releases](https://github.com/mmustafatasoy/EymenKids/releases)** sayfasındaki `kids-videos-* ` etiketleri **APK sürümü değil, MP4 video paketidir**. Bu dosyalar uygulama tarafından katalog üzerinden kullanılır.

> ⚠️ Şimdiki iş akışı **test amaçlı debug APK** üretir. Kalıcı dağıtım için sürüm APK/AAB dosyasını sabit bir imzalama anahtarıyla ayrıca yayımlamak gerekir. Farklı anahtarla imzalanan APK, kurulu uygulamanın üzerine yüklenmeyebilir. Uygulamayı kaldırmak, indirilmiş çevrimdışı videoları silebilir.

### Senna TV üzerinde kurulum denemesi

1. APK’yı bir USB belleğe kopyalayın.
2. TV menüsündeki USB / dosya yöneticisi bölümünden APK’yı açmayı deneyin.
3. **Yükle** seçeneği varsa kurulumu başlatın; izin verilmezse TV’nin mevcut yazılımını değiştirmeyin.
4. Kurulursa **Eymen Kids** simgesini uygulamalar arasında arayın ve kumandayla test edin.

Bu adımlar **cihazdaki kurulumun kesin çalıştığı anlamına gelmez**. Android TV emülatörü veya Android telefonda yapılan test, Whale OS’nin APK kurulumu için yeterli doğrulama değildir. Telefon ekranını TV’ye yansıtmak, bu projenin doğrudan TV kurulumu hedefinin yerine geçmez.

## 🎵 Güncel video arşivi

Katalogdaki **10 MP4 video / 8 seri** (yeni yüklemelerle değişebilir):

| Seri | 1. Sezon bölümleri |
|---|---|
| 🚂 Çu Çu Çu | 1. Bölüm — Çu Çu Çu |
| 🏪 Bakkal Amca | 1. Bölüm — Bakkal Amca · 2. Bölüm — İkinci Bölüm |
| 🐢 Niloya | 1. Bölüm — Tospik · 2. Bölüm — Tospik 2 |
| 🐵 Kukuli | 1. Bölüm — Turşu Şarkısı |
| 🍉 Karpuz Adam | 1. Bölüm — Şipşakşok |
| 🐝 Arı Vız Vız | 1. Bölüm — Arı Vız Vız |
| 🐦 Mini Mini Bir Kuş | 1. Bölüm — Mini Mini Bir Kuş |
| 🖐️ Parmak Ailesi | 1. Bölüm — Parmak Ailesi |

**[📄 Canlı video listesi: `video-catalog/catalog.json`](video-catalog/catalog.json)**

Dizideki videoların listelenmesi, dağıtım haklarının doğrulandığı anlamına gelmez. İçerik sahipliği ve kullanım izni ayrıca kontrol edilmelidir.

## 🧩 Yeni video / bölüm ekleme

Dağıtım hakkınız bulunan bir MP4 dosyasını **[yeni GitHub Release](https://github.com/mmustafatasoy/EymenKids/releases/new)** üzerinden yayımlayın.

**Etiket:** `kids-videos-11` gibi daha önce kullanılmamış bir `kids-videos-*` etiketi.

**Dosya adı biçimi:**

```text
seri-adi-sXXeYY-bolum-adi.mp4
```

**Örnekler:**

```text
niloya-s01e03-yeni-bolum.mp4
bakkal-amca-s01e03-ucuncu-bolum.mp4
ari-viz-viz-s01e02-ikinci-sarki.mp4
```

- `s01`: 1. sezon · `e03`: 3. bölüm.
- Dosya adında Türkçe karakter / boşluk yerine **küçük Latin harfleri, rakam ve kısa çizgi** kullanın.
- İsteğe bağlı bölüm kapağı: MP4 ile aynı dosya gövdesine sahip `.webp`, `.jpg` veya `.jpeg` dosyası.
- MP4 ve isteğe bağlı kapak görselini aynı Release’e ekleyip **Publish release** seçin.

`.github/workflows/release-index.yml` ve `scripts/sync_release_catalog.py`, GitHub Releases kayıtlarını kontrol ederek uygun videoları `video-catalog/catalog.json` dosyasına ekler. Seri, sezon ve bölüm sırası dosya adından belirlenir. Yayın olayının kaçırılması ihtimaline karşı **3 saatte bir zamanlanmış kontrol** de yapılandırılmıştır; GitHub'ın çalıştırma gecikmeleri olabilir. Gerektiğinde **[Actions → Add uploaded videos to Eymen Kids](https://github.com/mmustafatasoy/EymenKids/actions/workflows/release-index.yml)** üzerinden elle başlatabilirsiniz.

Uygulama katalog yenilendiğinde yeni videoları gösterir. **Yeni Android kodu / arayüzü** eklenirse yine yeni APK kurulumu gerekir.

**[📖 Ayrıntılı sezon/bölüm kılavuzu](docs/seri-bolum-sistemi.md)**

## 🛠️ Geliştirme ve test

**Teknolojiler:** Java, Android WebView, Media3 / ExoPlayer, HTML/CSS/JavaScript, GitHub Actions.

```bash
git clone https://github.com/mmustafatasoy/EymenKids.git
cd EymenKids

# Java 17, Android SDK ve Gradle kurulumu gerektirir.
gradle :app:assembleDebug
```

Çıktı: `app/build/outputs/apk/debug/app-debug.apk`.

**Otomatik testler:**

```bash
python3 scripts/test_episode_catalog.py
python3 scripts/test_release_sync.py
node scripts/smoke-ui.cjs
```

Bu testler veri sıralaması, katalog güncellemesi ve temel web arayüzünü kontrol eder; **gerçek Senna TV üzerinde kurulumu ve video oynatımını doğrulamaz**.

## 🔒 Güvenlik ve sınırlamalar

- Yalnızca projenin kendi GitHub Releases HTTPS MP4 adresleri video kaynağı olarak kabul edilir.
- Çevrimdışı indirilen MP4 dosyaları tamamlanınca **boyut + SHA-256** ile doğrulanır.
- İndirmeler uygulamaya özel depolama alanında tutulur; **uygulamayı kaldırmak veya verilerini silmek** bu videoları kaybettirebilir.
- Videolar internette izlenebilir ama **otomatik olarak çevrimdışı kaydedilmez**; kullanıcı indirmeyi ayrıca başlatmalıdır.
- Ebeveyn PIN’i, DRM, indirme kaldığı yerden sürdürme ve arka planda kesintisiz indirme henüz uygulanmış özellikler değildir.
- Telif hakları saklı üçüncü taraf videolar, şarkılar, afişler ve karakterler **MIT kaynak kodu lisansına dahil değildir**. Bu içerikler yalnızca izin/lisans kapsamında dağıtılmalıdır.

## 👨‍💻 Geliştirici

**Muhammet Atasoy**

- GitHub: [@mmustafatasoy](https://github.com/mmustafatasoy)
- Instagram: [@mmustafatasoy](https://www.instagram.com/mmustafatasoy/)
- E-posta: teknobloggs@hotmail.com

## ⚖️ Lisans

Eymen Kids **kaynak kodu** [MIT Lisansı](LICENSE) altında yayımlanmaktadır. Bu lisans üçüncü taraf görsel, müzik ve video içerikleri için kullanım veya yeniden dağıtım izni vermez.

---

<div align="center">

**🌈 Eymen Kids — Her şey Gıppık için! 💖**

</div>
