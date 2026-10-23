# Eymen Kids v1.6.0 — Seri, sezon ve bölüm sistemi

## Ekran yapısı
**Ana ekran → Niloya → 1. Sezon → Bölüm 1: Tospik**. Her bölüm için iki seçenek bulunur:
- **▶ İzle**: Video indirilmemişse GitHub Release içindeki MP4 dosyasını çevrimiçi açar. Daha önce indirilmişse doğrudan cihazdan oynatır.
- **⬇ Çevrimdışı İndir**: Android cihazında Wi-Fi ile (veya mobil veri onayıyla) MP4'ü indirir, boyut ve SHA-256 ile doğrular. İndirme bitince internet bağlantısı gerekmez.

Video henüz indirilmemişse, çevrimiçi yayın esnasında internet kesildiğinde oynatmanın sürmesi **garanti değildir**. Çevrimdışı izleme için indirmenin %100 bitmiş olması gerekir.

## GitHub'a seri yüklemenin otomatik sıralama kuralı
MP4 dosyalarının adını **\`seri-sXXeYY-bolum-adi.mp4\`** biçiminde yaz.

| Yüklenen dosyanın adı | Uygulamadaki yeri |
|---|---|
| \`niloya-s01e01-tospik.mp4\` | Niloya → 1. Sezon → 1. Bölüm: Tospik |
| \`niloya-s01e02-ucurtma.mp4\` | Niloya → 1. Sezon → 2. Bölüm: Uçurtma |
| \`niloya-s02e01-kar-yagiyor.mp4\` | Niloya → 2. Sezon → 1. Bölüm: Kar Yağıyor |
| \`cu-cu-cu-s01e02-baska-sarki.mp4\` | Çu Çu Çu → 1. Sezon → 2. Bölüm |
| \`cu-cu-cu.mp4\` | Çu Çu Çu → 1. Sezon → 1. Bölüm |

Dosya adlarında Türkçe harf/boşluk kullanma; küçük harf, rakam ve tire kullan. Sezon/bölüm numarasını sıfırla tamamla (**s01e01**). Bölüm başlıkları uygulamada Türkçeye uygun görüntülenir.

Aynı dosya köküyle isteğe bağlı bölüm kapak görseli ekleyebilirsin: \`niloya-s01e01-tospik.webp\`. Bunun için MP4 ve kapak görselini **aynı GitHub Release'e** yükle. Kapak yoksa ilgili serinin mevcut afişi kullanılır. Yüksek kaliteli kapak için yaklaşık **640×360 WebP** uygundur.

1. GitHub → https://github.com/mmustafatasoy/EymenKids/releases/new → Tag: örneğin \`kids-videos-2\`.
2. Yalnızca dağıtım hakkın olan MP4 dosyalarını release **Assets** alanına ekle.
3. **Publish release** dediğinde GitHub Actions \`scripts/index_release_videos.py\` dosyasını çalıştırır; video boyutunu, SHA-256 değerini, seri/sezon/bölüm bilgisini ve isteğe bağlı kapak bağlantısını hesaplar.
4. \`video-catalog/catalog.json\` yenilenir ve **seriye göre, sonra sezon ve bölüm numarasıyla** sıralanır. Aynı APK üzerinde yeni bölümler görünür.
5. Android uygulamasında yeniden aç veya **Güncellemeleri Denetle** kullan.

**Önemli:** Yayımlanmış eski bir Release'e sonradan yeni dosya eklersen \`Actions → Add uploaded videos to Eymen Kids → Run workflow\` adımıyla ilgili release etiketini ver. *Yeni* Release yayımlama olayı otomatik çalışır.

Bu belgede geçen Niloya/Tospik/Uçurtma gibi dosya adları **şema örneğidir**, uygulamanın arşivinde yayınlanmış video oldukları iddiası değildir. Başkasına ait çizgi film ve şarkıları halka açık GitHub depoda dağıtmak için gerekli kullanım/dağıtım iznine sahip olmalısın.
