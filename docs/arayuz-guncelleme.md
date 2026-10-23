# Eymen Kids – Yeni renkli ana ekran / canlı güncellemeler (v1.5.0)

Tek bir açılır-kapanır menü ve renkli çocuk dostu bir ana ekran vardır. Menü: **Ana Sayfa**, **Videolarım / İndir**, **Güncellemeleri Denetle**, **Hakkında**.

Uygulamanın arayüzündeki dört ana renk, ana afiş başlığı ve alt açıklama `ui-config.json` dosyasından okunur. Bu JSON dosyasında `revision` sayısını artırıp alanları değiştirirseniz, kullanıcı uygulama içindeki **Güncellemeleri Denetle** seçeneğine bastığında renkler ve metinler **yeni APK kurmadan** güncellenir.

Desteklenen renkler 6 haneli hexadecimal biçimindedir: `#RRGGBB`. Aşağıdaki alanlar kullanılabilir: `purple`, `pink`, `blue`, `mint`, `heroTitle`, `heroSubtitle`. Uygulama yeni renk dosyalarını yalnızca GitHub'daki resmi `mmustafatasoy/EymenKids` kaynağından alır; dosya internet yoksa son kaydedilen görünümü kullanır.

**Sınır:** Renk ve metinler ile GitHub video kataloğu canlı güncellenebilir. Android'in Java/Kotlin kodunu, Media3 oynatıcısını ve APK içindeki dosyaları değiştiren güncellemeler **yeni bir APK kurulumu** gerektirir. Android'de yeni APK kurulumu kullanıcı/sistem onayı olmadan sessizce yapılamaz.

Videoların GitHub üzerinden bir kez indirilip telefon hafızasına saklanması devam eder. Ana sayfada henüz yüklenmemiş video kartları **Yakında** olarak işaretlidir. Video ekleme: [video-catalog/README.md](video-catalog/README.md).
