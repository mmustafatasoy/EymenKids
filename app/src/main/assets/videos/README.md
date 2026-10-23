# Çevrimdışı MP4 Video Kütüphanesi (v1.2.0)

Bu klasördeki `catalog.json` yalnızca **gerçekten APK'ya dahil edilen** videoları listeler. Boş başlangıç kataloğu normaldir; uygulama olmayan videoları çevrimdışı olarak göstermez.

## Videoyu uygulamanın içine eklemek

1. Dağıtım hakkınız bulunan bir H.264/AAC MP4 dosyasını `app/src/main/assets/videos/` klasörüne kopyalayın. Küçük harf ve tire içeren dosya adlarını kullanın; örnek: `cu-cu-cu.mp4`.
2. Aynı klasördeki `catalog.json` dosyasının `videos` dizisine dosyanın kart kimliğini ve **yalnızca dosya adını** yazın:

```json
{
  "version": 1,
  "videos": [
    {"id": "cu-cu-cu", "file": "cu-cu-cu.mp4"},
    {"id": "ari-viz-viz", "file": "ari-viz-viz.mp4"}
  ]
}
```

3. Yeni APK'yı derleyin. Uygulama açılınca **İnternetsiz Arşiv** bölümünde eşleşen kartlar otomatik listelenir. Normal ana menü kartları da önce APK'daki MP4 dosyasını oynatır; YouTube veya internet kullanılmaz.

Desteklenen kart kimlikleri: `cu-cu-cu`, `bakkal-amca`, `niloya`, `kral-sakir`, `rafadan-tayfa`, `ari-viz-viz`, `elifin-dusleri`, `tavsan-momo`, `mini-mini`, `parmak-ailesi`, `yaz-sarkilari`.

## Önemli sınırlar

- APK'ya 1 GB video eklerseniz indirme boyutu yaklaşık 1 GB artar; yeni APK sürümlerinde bu medya yeniden indirilir. **İzleme esnasında internet gerekmez**, ama kurulum ve APK güncellemesi internet harcayabilir.
- GitHub normal Git deposunda tek dosya için 100 MiB sınır uygular. Büyük MP4'ler için Git LFS veya derleme esnasında kontrollü varlık indirme sistemi gerekir; lisanssız içerikleri açık depoya koymayın.
- Mevcut Android WebView oynatıcısı paketli MP4'leri yerel `WebViewAssetLoader` üzerinden açar. Çok büyük medya, ileri/geri sarma veya farklı Android TV cihazları için ayrıca cihaz testi ve gerektiğinde Media3/ExoPlayer geçişi önerilir.
- Başkasına ait şarkı, çizgi film ve video dosyalarını dağıtmadan önce gerekli izinleri alın. Ekrandaki resimler de medya lisansı sağlamaz.
- Uygulama kaldırılmadığı sürece, aynı imzalı APK sürümlerine güncelleme normalde kullanıcı ayarlarını korur; APK içine gömülü videoların sürümleri yeni paketin içeriğiyle değiştirilir.

## FT-P_DUFtIA denemesi ve native oynatıcı (v1.3.0)

Gönderilen YouTube referansı: https://youtu.be/FT-P_DUFtIA?feature=shared

Bu bağlantı doğrudan MP4 olmadığı için, uygulama bağlantıyı YouTube'dan indirmez veya çevrimiçi oynatmaz. MP4 dosyasını yalnızca videoyu bu şekilde çoğaltma/dağıtma izniniz varsa ekleyin.

1. H.264 (AVC) + AAC ile kodlanmış, tercihen 1080p bir MP4 dosyasını `app/src/main/assets/videos/ft-p-duftia.mp4` yoluna ekleyin.
2. `catalog.json` içeriğini aşağıdaki gibi ayarlayın:

```json
{
  "version": 1,
  "videos": [
    {"id": "ft-p-duftia", "file": "ft-p-duftia.mp4"}
  ]
}
```

3. GitHub Actions içinde APK'yı derleyin. Deneme Videosu kartına dokununca uygulamanın Android Media3 yerel oynatıcısı açılır ve `asset:///videos/ft-p-duftia.mp4` kaynağını internet kullanmadan oynatır.
4. Native oynatıcıyı videoyu APK'ya eklemeden test etmek için Deneme Videosu → **Telefonda Video Seç** yoluyla cihazdaki herhangi bir MP4 dosyasını açın.

Not: GitHub'ın normal Git depolarında 100 MiB tek dosya yükleme sınırı vardır. Büyük bir MP4 için lisanslı medya deposundan CI sırasında indirme veya farklı bir dağıtım yöntemi gerekir. Telif hakkı sahibi izni, görsel kalite ve kaynak MP4'ün gerçek dosya boyutu ayrıca doğrulanmalıdır.
