# Eymen Kids — GitHub'da otomatik video yükleme

**Çu Çu Çu videosu bu sohbette `cu-cu-cu.mp4` olarak hazırlanmıştır (720p, 2:04, 12.706.584 bayt).**

Videonun kullanım ve herkese açık dağıtım hakkına sahip olduğunuzdan emin olun. Yükleme işlemi:

1. Hazırlanan `cu-cu-cu.mp4` dosyasını indirin.
2. `https://github.com/mmustafatasoy/EymenKids/releases/new` sayfasına gidin.
3. **Tag** alanına `kids-videos-1` yazın; **Release title** olarak `Eymen Kids - Cu Cu Cu` girin.
4. Release eklerini ekleme alanında `cu-cu-cu.mp4` dosyasını seçin; **Publish release** düğmesine basın.
5. `.github/workflows/release-index.yml` otomatik çalışır. Dosyanın gerçek boyutunu, SHA-256 özetini ve çözünürlüğünü hesaplayıp `video-catalog/catalog.json` içine kaydeder; artık katalog JSON dosyasını elle değiştirmek gerekmez.
6. Eymen Kids v1.4.1 uygulamasını açıp `Video İndir -> Yenile -> Çu Çu Çu -> Çevrimdışı İndir` adımlarını takip edin.

**Not:** Dosya henüz GitHub Release'e yüklenmediyse uygulamanın listesinde görünmez. Buradaki otomasyon, yalnızca `kids-videos-` etiketiyle yayımlanan release'leri indeksler. Sonradan release'e MP4 eklerseniz Actions > Add uploaded videos to Eymen Kids > Run workflow üzerinden aynı etiketi vererek listeyi elle yenileyebilirsiniz.

---
# Eymen Kids — GitHub video kütüphanesi

Eymen Kids Android uygulaması, **yalnızca `mmustafatasoy/EymenKids` deposunun GitHub Releases MP4 dosyalarını** indirir. YouTube bağlantıları kullanılmaz. Videolar telefonun uygulama-özel hafızasına **bir kez** kaydedilir; internetsiz izlenir ve normal APK güncellemesinde korunur.

## İlk videoyu yayımlama

1. Dağıtım hakkınız olan bir MP4 dosyasını hazırlayın. Yüksek uyumluluk için **1080p H.264 (AVC), AAC ses, MP4** tavsiye edilir. Örnek dosya adı: `cu-cu-cu.mp4`.
2. [GitHub Releases sayfasında](https://github.com/mmustafatasoy/EymenKids/releases) **Draft a new release** seçin. Etiket olarak örneğin `kids-videos-1` yazın, MP4 dosyasını **Attach binaries** alanına bırakıp release'i yayımlayın.
3. Dosyanın gerçek boyutunu (byte olarak) ve SHA-256 özetini hesaplayın. Linux/macOS: `sha256sum cu-cu-cu.mp4`; Windows PowerShell: `(Get-FileHash .\cu-cu-cu.mp4 -Algorithm SHA256).Hash`. Gerekiyorsa bu değerleri dosyanızdan hesaplamada yardımcı olabilirim.
4. Bu klasördeki `catalog.json` dosyasında `videos` dizisine videoyu ekleyin. **Örnek değerler yer tutucudur**; dosya gerçekten yayımlanmadan ve özet hesaplanmadan kullanmayın.

```json
{
  "schema_version": 1,
  "videos": [
    {
      "id": "cu-cu-cu",
      "title": "Çu Çu Çu",
      "description": "Çocuk şarkısı",
      "quality": "1080p",
      "size_bytes": 12345678,
      "sha256": "BURAYA_DOSYANIN_64_HANELI_GERCEK_SHA256_DEGERI",
      "url": "https://github.com/mmustafatasoy/EymenKids/releases/download/kids-videos-1/cu-cu-cu.mp4"
    }
  ]
}
```

Bir önceki denemede gönderdiğin `FT-P_DUFtIA` video bağlantısı için ancak **indirilip yeniden dağıtılmasına açıkça izin verilmiş** MP4 dosyasını kullanabilirsin. Başka bir kişinin YouTube videosu, yalnızca video URL'si olduğu için otomatik olarak indirilebilir veya dağıtılabilir hale gelmez. Lisanslı kopyanın adını `ft-p-duftia.mp4` ve katalog kimliğini `ft-p-duftia` olarak seçebilirsin.

## Uygulamada kullanım

- APK’yı aç → **⬇ İndir / Video İndir** → **GitHub Video Kütüphanesi**.
- Uygulama `https://raw.githubusercontent.com/mmustafatasoy/EymenKids/main/video-catalog/catalog.json` adresindeki küçük listeyi yeniler.
- Video kartındaki **Çevrimdışı İndir** düğmesine bas. Wi-Fi önerilir; mobil veri bağlantısında onay sorulur.
- İndirme boyunca ilerleme yüzdesi görünür. Dosyanın **boyutu ve SHA-256 değeri doğrulandıktan sonra** cihazın uygulama-özel `files/offline_videos/` klasörüne kaydedilir.
- Sonraki açılışta **İnternetsiz İzle** düğmesiyle native Media3 video oynatıcısı açılır. İnternet kapalıyken de kayıtlı liste kullanılabilir. Videoyu buradan silebilirsin.

## Sürüm ve dağıtım notları

- **Video eklemek için yeni APK oluşturman gerekmez.** Yalnızca yeni release dosyası ve katalog kaydı yeterlidir.
- Bir video güncellenecekse yeni release URL'si ve yeni `sha256` / `size_bytes` bilgisi kullan. Eski indirme bozulmadan kalır; güncelleme indirilince değiştirilir.
- Her GitHub Release dosyası **2 GiB'tan küçük** olmalıdır. Açık GitHub hesabında herkesin erişebileceği videolar yalnızca dağıtım hakkınız olan içerikler olmalıdır.
- Bu ilk sürümde indirme işlemi **kütüphane ekranı açıkken** yapılır; arka plan indirme, indirmeyi kaldığı yerden sürdürme, canlı yayın, DRM, çocuk PIN koruması henüz uygulanmadı.
- APK kaldırılır veya uygulama verileri silinirse, kaydedilen videolar da kaybolur. Yedekleme sonraki geliştirme konusudur.
- Android TV, webOS ve Tizen için ayrı istemciler gerekir. Katalog biçimi bu istemcilerde yeniden kullanılabilir.

### Katalog alanları
| Alan | Açıklama |
|---|---|
| `id` | Benzersiz kısa kimlik; küçük harf/sayı/tire, örn. `cu-cu-cu` |
| `title` | Uygulamada görünen video başlığı |
| `description` | İsteğe bağlı açıklama |
| `quality` | İsteğe bağlı kalite etiketi, örn. `1080p` |
| `url` | Bu deponun HTTPS GitHub Release MP4 adresi |
| `size_bytes` | Gerçek MP4 dosyasının tam byte boyutu |
| `sha256` | MP4 dosyasının gerçek 64 karakterli SHA-256 özeti |
