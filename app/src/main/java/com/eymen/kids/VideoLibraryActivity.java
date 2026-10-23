package com.eymen.kids;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** TV/phone-friendly offline video library, synchronized with a public GitHub catalog. */
public final class VideoLibraryActivity extends Activity {
    private static final int NAVY = 0xFF433364;
    private static final int CARD = 0xFFFFFFFF;
    private static final int WHITE = 0xFF293452;
    private static final int SOFT = 0xFF7785A0;
    private static final int GOLD = 0xFFFFDA4E;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private JSONObject catalog;
    private LinearLayout list;
    private TextView summary;
    private boolean downloadInProgress = false;
    private String selectedSeriesId;

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }

    private TextView text(String content, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(content);
        view.setTextColor(color);
        view.setTextSize(sp);
        view.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private GradientDrawable background(int color, int stroke, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        if (stroke != 0) g.setStroke(dp(1), stroke);
        return g;
    }

    private Button button(String label, boolean primary) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(primary ? NAVY : Color.WHITE);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackgroundTintList(ColorStateList.valueOf(primary ? GOLD : 0xFF8149ED));
        b.setMinHeight(dp(46));
        return b;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        catalog = OfflineVideoStore.cachedCatalog(this);
        selectedSeriesId = getIntent().getStringExtra("eymen.seriesId");
        if (!OfflineVideoStore.validId(selectedSeriesId)) selectedSeriesId = null;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(12), dp(12), 0);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{0xFFECFAFF, 0xFFF5E9FF, 0xFFFFFAF4}));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(10), dp(10), dp(10), dp(10));
        GradientDrawable headerBackground = new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{0xFF8149ED, 0xFFFA579B});
        headerBackground.setCornerRadius(dp(22));
        header.setBackground(headerBackground);
        Button close = button("← Geri", false);
        close.setOnClickListener(v -> finish());
        header.addView(close, new LinearLayout.LayoutParams(dp(102), dp(52)));
        String headingText = selectedSeriesId == null ? "🌈 Eymen Kids • Video Arşivi"
            : "🎬 " + selectedSeriesId.replace("-", " ").toUpperCase(new Locale("tr","TR"));
        TextView heading = text(headingText, 20, Color.WHITE, true);
        heading.setPadding(dp(8), 0, 0, 0);
        header.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(header);

        TextView subheading = text("GitHub'dan bir kez indir • Sonra internetsiz izle", 15, SOFT, false);
        subheading.setPadding(dp(4), dp(12), dp(4), dp(8));
        root.addView(subheading);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        summary = text("Kaydedilmiş videolar yükleniyor...", 13, 0xFF1A9278, false);
        top.addView(summary, new LinearLayout.LayoutParams(0, -2, 1));
        Button refresh = button("↻ Yenile", true);
        refresh.setOnClickListener(v -> refreshCatalog());
        top.addView(refresh, new LinearLayout.LayoutParams(dp(110), dp(50)));
        root.addView(top);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(2), dp(12), dp(2), dp(24));
        scroll.addView(list, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        render();
        refreshCatalog();  // Updates small JSON metadata; it does not download any MP4.
    }

    private JSONArray videoArray() {
        return catalog == null ? new JSONArray() : catalog.optJSONArray("videos") == null
            ? new JSONArray() : catalog.optJSONArray("videos");
    }

    private static String sizeText(long count) {
        if (count <= 0) return "Boyut bilinmiyor";
        if (count >= 1024 * 1024) return String.format(Locale.ROOT, "%.1f MB", count / 1048576.0);
        return String.format(Locale.ROOT, "%d KB", count / 1024);
    }

    private void render() {
        list.removeAllViews();
        JSONArray videos = videoArray();
        int downloaded = 0;
        java.util.ArrayList<JSONObject> visible = new java.util.ArrayList<>();
        for (int i = 0; i < videos.length(); i++) {
            JSONObject entry = videos.optJSONObject(i);
            if (entry == null) continue;
            String group = entry.optString("series_id", entry.optString("id"));
            if (selectedSeriesId == null || selectedSeriesId.equals(group)) visible.add(entry);
        }
        java.util.Collections.sort(visible, (a, b) -> {
            int season = Integer.compare(a.optInt("season", 1), b.optInt("season", 1));
            if (season != 0) return season;
            int episode = Integer.compare(a.optInt("episode", 1), b.optInt("episode", 1));
            return episode != 0 ? episode :
                a.optString("id", "").compareTo(b.optString("id", ""));
        });
        int lastSeason = -1;
        for (JSONObject entry : visible) {
            int season = entry.optInt("season", 1);
            if (selectedSeriesId != null && season != lastSeason) {
                TextView seasonHeading = text("🎞️ " + season + ". Sezon", 21, 0xFF8149ED, true);
                LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(-1, dp(48));
                headingParams.topMargin = dp(13);
                list.addView(seasonHeading, headingParams);
                lastSeason = season;
            }
            if (OfflineVideoStore.isDownloaded(this, entry)) downloaded++;
            addVideoCard(entry);
        }
        summary.setText("📦 " + downloaded + " indirilen / " + visible.size() + " video");
        if (visible.isEmpty()) {
            LinearLayout info = new LinearLayout(this);
            info.setPadding(dp(22), dp(22), dp(22), dp(22));
            info.setOrientation(LinearLayout.VERTICAL);
            info.setBackground(background(CARD, 0xFFB99CF4, 20));
            TextView heading = text("📺 Henüz video yayımlanmadı", 21, WHITE, true);
            info.addView(heading);
            TextView description = text(
                "Kendi MP4 videolarını GitHub → Releases bölümüne yükle. "
                + "video-catalog/catalog.json dosyasına adını, boyutunu, SHA-256 değerini "
                + "ve indirme bağlantısını eklediğinde videolar burada otomatik görünecek. "
                + "İndirilen videolar cihazda kalacak; izlerken internet kullanılmayacak.",
                15, SOFT, false);
            description.setPadding(0, dp(16), 0, dp(12));
            info.addView(description);
            list.addView(info);
        }
    }

    private void addVideoCard(JSONObject entry) {
        String id = entry.optString("id", "");
        String title = entry.optString("episode_title", entry.optString("title", "Video"));
        String quality = entry.optString("quality", "1080p");
        long size = entry.optLong("size_bytes", 0);
        boolean saved = OfflineVideoStore.isDownloaded(this, entry);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(13), dp(16), dp(15));
        card.setBackground(background(CARD, saved ? 0xFF51D7B1 : 0xFFD5C4FA, 19));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.bottomMargin = dp(12);
        list.addView(card, cp);

        TextView name = text("🎬 " + title, 19, WHITE, true);
        card.addView(name);
        TextView details = text("Sezon " + entry.optInt("season",1) + " • Bölüm " +
            entry.optInt("episode",1) + "   •   " + quality + "   •   " + sizeText(size)
            + (saved ? "   •   ✅ İndirildi" : "   •   ☁️ GitHub'da"), 13, SOFT, false);
        details.setPadding(0, dp(7), 0, dp(7));
        card.addView(details);
        String description = entry.optString("description", "");
        if (!description.isEmpty()) {
            TextView note = text(description, 13, SOFT, false);
            note.setPadding(0, 0, 0, dp(8));
            card.addView(note);
        }

        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(0);
        bar.setProgressTintList(ColorStateList.valueOf(GOLD));
        bar.setVisibility(View.GONE);
        card.addView(bar, new LinearLayout.LayoutParams(-1, dp(8)));
        TextView progress = text("", 12, 0xFF7651CB, true);
        progress.setVisibility(View.GONE);
        card.addView(progress);

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        card.addView(actions);

        if (saved) {
            Button play = button("▶ İnternetsiz İzle", true);
            play.setOnClickListener(v -> playDownloaded(entry));
            actions.addView(play, new LinearLayout.LayoutParams(0, dp(50), 1));
            Button delete = button("🗑 Sil", false);
            LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(dp(110), dp(50));
            deleteParams.leftMargin = dp(8);
            actions.addView(delete, deleteParams);
            delete.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Videoyu sil?")
                .setMessage(title + " cihazdan silinecek. Tekrar izlemek için yeniden indirmek gerekir.")
                .setPositiveButton("Sil", (dialog, which) -> {
                    try { OfflineVideoStore.delete(this, entry); render(); }
                    catch (Exception ex) { showMessage(ex.getMessage()); }
                })
                .setNegativeButton("Vazgeç", null).show());
        } else {
            Button online = button("▶ İzle", false);
            actions.addView(online, new LinearLayout.LayoutParams(0, dp(50), 1));
            online.setOnClickListener(v -> playOnline(entry));
            Button download = button("⬇ İndir", true);
            LinearLayout.LayoutParams downloadParams = new LinearLayout.LayoutParams(0, dp(50), 1);
            downloadParams.leftMargin = dp(9);
            actions.addView(download, downloadParams);
            download.setOnClickListener(v -> {
                if (downloadInProgress) {
                    showMessage("Başka bir video indiriliyor.");
                    return;
                }
                if (isUnmeteredConnection()) {
                    download(entry, download, bar, progress);
                } else {
                    new AlertDialog.Builder(this)
                        .setTitle("Mobil veri kullanılabilir")
                        .setMessage(title + " yaklaşık " + sizeText(size)
                            + ". Wi-Fi bağlı değil. Mobil veri kullanarak indirmek istiyor musun?")
                        .setNegativeButton("Wi-Fi bekle", null)
                        .setPositiveButton("Yine de indir", (dialog, which) ->
                            download(entry, download, bar, progress))
                        .show();
                }
            });
        }
    }

    private boolean isUnmeteredConnection() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) return false;
        Network network = manager.getActiveNetwork();
        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
        return capabilities != null
            && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    private void refreshCatalog() {
        if (downloadInProgress) {
            showMessage("İndirme sırasında katalog yenilenemez.");
            return;
        }
        summary.setText("🌐 GitHub video listesi kontrol ediliyor...");
        worker.execute(() -> {
            try {
                JSONObject latest = OfflineVideoStore.refreshCatalog(getApplicationContext());
                runOnUiThread(() -> {
                    catalog = latest;
                    render();
                    Toast.makeText(this, "GitHub video listesi yenilendi", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception exception) {
                runOnUiThread(() -> {
                    render();
                    Toast.makeText(this,
                        "İnternet yok veya GitHub erişilemiyor. Kaydedilen videolar kullanılabilir.",
                        Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void download(JSONObject entry, Button trigger, ProgressBar bar, TextView label) {
        if (downloadInProgress) return;
        downloadInProgress = true;
        trigger.setEnabled(false);
        trigger.setText("İndiriliyor...");
        bar.setVisibility(View.VISIBLE);
        label.setVisibility(View.VISIBLE);
        String title = entry.optString("title", "Video");
        worker.execute(() -> {
            try {
                OfflineVideoStore.download(getApplicationContext(), entry, (done, total) -> {
                    int percent = total <= 0 ? 0 : (int) Math.min(100, Math.round(done * 100.0 / total));
                    runOnUiThread(() -> {
                        bar.setProgress(percent);
                        label.setText(percent + "%  •  " + sizeText(done) + " / " + sizeText(total));
                    });
                });
                runOnUiThread(() -> {
                    downloadInProgress = false;
                    render();
                    showMessage(title + " indirildi. İnternetsiz izleyebilirsin.");
                });
            } catch (Exception exception) {
                runOnUiThread(() -> {
                    downloadInProgress = false;
                    bar.setVisibility(View.GONE);
                    label.setVisibility(View.GONE);
                    trigger.setEnabled(true);
                    trigger.setText("⬇ Yeniden Dene");
                    showMessage("İndirme başarısız: " + exception.getMessage());
                });
            }
        });
    }

    private void playOnline(JSONObject entry) {
        if (OfflineVideoStore.isDownloaded(this, entry)) {
            playDownloaded(entry);
            return;
        }
        try {
            String url = entry.getString("url");
            OfflineVideoStore.validateReleaseUrl(url);
            Intent player = new Intent(this, OfflinePlayerActivity.class);
            player.putExtra(OfflinePlayerActivity.EXTRA_STREAM_URL, url);
            player.putExtra(OfflinePlayerActivity.EXTRA_TITLE,
                entry.optString("episode_title", entry.optString("title", "Video")));
            startActivity(player);
        } catch (Exception exception) {
            showMessage("Video bağlantısı geçersiz");
        }
    }

    private void playDownloaded(JSONObject entry) {
        if (!OfflineVideoStore.isDownloaded(this, entry)) {
            render();
            showMessage("Video cihazda bulunamadı.");
            return;
        }
        Intent player = new Intent(this, OfflinePlayerActivity.class);
        player.putExtra(OfflinePlayerActivity.EXTRA_DOWNLOADED_ID, entry.optString("id"));
        player.putExtra(OfflinePlayerActivity.EXTRA_TITLE, entry.optString("title", "Video"));
        startActivity(player);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override protected void onResume() {
        super.onResume();
        if (list != null) render();
    }

    @Override protected void onDestroy() {
        worker.shutdownNow(); // Interrupted partial files are removed, not treated as completed.
        super.onDestroy();
    }
}
