package com.eymen.kids;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * GitHub is only a delivery server. Downloaded MP4 files are stored in
 * /data/data/com.eymen.kids/files/offline_videos and are never streamed.
 */
public final class OfflineVideoStore {
    private OfflineVideoStore() {}

    public static final String CATALOG_URL =
        "https://raw.githubusercontent.com/mmustafatasoy/EymenKids/main/video-catalog/catalog.json";
    private static final String CACHE_NAME = "github_video_catalog.json";
    private static final long MAX_VIDEO_BYTES = 2L * 1024 * 1024 * 1024 - 1;
    private static final int MAX_CATALOG_BYTES = 512 * 1024;
    private static final String PREFERENCES = "eymen_video_downloads";

    public interface Progress {
        void update(long downloaded, long total);
    }

    public static boolean validId(String id) {
        return id != null && id.matches("[a-z0-9][a-z0-9-]{0,63}");
    }

    public static File offlineFile(Context context, String id) {
        if (!validId(id)) throw new IllegalArgumentException("Geçersiz video kimliği");
        File dir = new File(context.getFilesDir(), "offline_videos");
        return new File(dir, id + ".mp4");
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public static boolean isDownloaded(Context context, JSONObject entry) {
        String id = entry.optString("id", "");
        if (!validId(id)) return false;
        File file = offlineFile(context, id);
        String expected = entry.optString("sha256", "").toLowerCase(Locale.ROOT);
        long bytes = entry.optLong("size_bytes", -1);
        return file.isFile() && file.length() > 0
            && (bytes <= 0 || file.length() == bytes)
            && expected.equals(preferences(context).getString("sha_" + id, ""));
    }

    public static void delete(Context context, JSONObject entry) throws IOException {
        String id = entry.optString("id", "");
        if (!validId(id)) throw new IOException("Geçersiz video kimliği");
        File file = offlineFile(context, id);
        if (file.exists() && !file.delete()) throw new IOException("Video silinemedi");
        preferences(context).edit().remove("sha_" + id).apply();
    }

    private static String readText(InputStream in, int maxBytes) throws IOException {
        try (InputStream source = in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = source.read(buffer)) != -1) {
                if (out.size() + count > maxBytes) {
                    throw new IOException("Katalog çok büyük");
                }
                out.write(buffer, 0, count);
            }
            return out.toString("UTF-8");
        }
    }

    private static JSONObject parseCatalog(String text) throws Exception {
        JSONObject catalog = new JSONObject(text);
        if (catalog.optInt("schema_version", 0) != 1) throw new IOException("Katalog sürümü desteklenmiyor");
        JSONArray videos = catalog.getJSONArray("videos");
        if (videos.length() > 500) throw new IOException("Katalogda çok fazla video var");
        Set<String> knownIds = new HashSet<>();
        for (int i = 0; i < videos.length(); i++) {
            JSONObject video = videos.getJSONObject(i);
            String id = video.optString("id", "");
            if (!validId(id) || !knownIds.add(id)) throw new IOException("Geçersiz veya yinelenen video kimliği");
            if (video.optString("title", "").trim().length() == 0
                || video.optString("title", "").length() > 120)
                throw new IOException("Geçersiz video başlığı");
            validateReleaseUrl(video.optString("url", ""));
            String hash = video.optString("sha256", "").toLowerCase(Locale.ROOT);
            if (!hash.matches("[0-9a-f]{64}")) throw new IOException("SHA-256 eksik: " + id);
            long size = video.optLong("size_bytes", 0);
            if (size < 1 || size > MAX_VIDEO_BYTES) throw new IOException("Geçersiz dosya boyutu: " + id);
        }
        return catalog;
    }

    public static JSONObject cachedCatalog(Context context) {
        try {
            File file = new File(context.getFilesDir(), CACHE_NAME);
            if (file.isFile()) return parseCatalog(readText(new FileInputStream(file), MAX_CATALOG_BYTES));
        } catch (Exception ignored) {}
        try {
            return parseCatalog(readText(context.getAssets().open("video-catalog/catalog.json"), MAX_CATALOG_BYTES));
        } catch (Exception ignored) {
            try { return new JSONObject("{\"schema_version\":1,\"videos\":[]}"); }
            catch (Exception impossible) { throw new IllegalStateException(impossible); }
        }
    }

    public static JSONObject refreshCatalog(Context context) throws Exception {
        URL url = new URL(CATALOG_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(12000);
        connection.setReadTimeout(15000);
        connection.setRequestProperty("User-Agent", "EymenKidsAndroid/1.4");
        try {
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                throw new IOException("GitHub katalogu açılamadı (HTTP " + connection.getResponseCode() + ")");
            JSONObject catalog = parseCatalog(readText(connection.getInputStream(), MAX_CATALOG_BYTES));
            File dir = context.getFilesDir();
            File temp = new File(dir, CACHE_NAME + ".tmp");
            File file = new File(dir, CACHE_NAME);
            byte[] content = catalog.toString().getBytes(StandardCharsets.UTF_8);
            try (FileOutputStream out = new FileOutputStream(temp)) {
                out.write(content);
                out.getFD().sync();
            }
            if (file.exists() && !file.delete()) throw new IOException("Eski katalog değiştirilemedi");
            if (!temp.renameTo(file)) throw new IOException("Yeni katalog kaydedilemedi");
            return catalog;
        } finally {
            connection.disconnect();
        }
    }

    /** Accept only this project's GitHub Release MP4 assets, never YouTube URLs. */
    private static URL validateReleaseUrl(String source) throws IOException {
        try {
            URL result = new URL(source);
            if (!"https".equalsIgnoreCase(result.getProtocol())
                || !"github.com".equalsIgnoreCase(result.getHost())
                || result.getUserInfo() != null
                || result.getPort() != -1
                || !result.getPath().startsWith("/mmustafatasoy/EymenKids/releases/download/")
                || !result.getPath().toLowerCase(Locale.ROOT).endsWith(".mp4")) {
                throw new IOException("Yalnızca bu deponun GitHub Release MP4 bağlantıları kabul edilir");
            }
            return result;
        } catch (IllegalArgumentException exception) {
            throw new IOException("Video adresi geçersiz", exception);
        }
    }

    private static boolean trustedRedirect(URL url) {
        String host = url.getHost().toLowerCase(Locale.ROOT);
        return "https".equalsIgnoreCase(url.getProtocol())
            && url.getUserInfo() == null
            && (host.equals("github.com")
                || host.equals("release-assets.githubusercontent.com")
                || host.equals("objects.githubusercontent.com")
                || host.endsWith(".githubusercontent.com"));
    }

    private static HttpURLConnection openVideoConnection(URL original) throws IOException {
        URL location = original;
        for (int i = 0; i < 9; i++) {
            if (!trustedRedirect(location)) throw new IOException("Güvenilmeyen indirme yönlendirmesi");
            HttpURLConnection connection = (HttpURLConnection) location.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(16000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("Accept", "application/octet-stream");
            connection.setRequestProperty("Accept-Encoding", "identity");
            connection.setRequestProperty("User-Agent", "EymenKidsAndroid/1.4");
            int code = connection.getResponseCode();
            if (code == 301 || code == 302 || code == 303 || code == 307 || code == 308) {
                String target = connection.getHeaderField("Location");
                connection.disconnect();
                if (target == null || target.isEmpty()) throw new IOException("GitHub yönlendirme adresi yok");
                location = new URL(location, target);
                continue;
            }
            if (code != HttpURLConnection.HTTP_OK) {
                connection.disconnect();
                throw new IOException("GitHub video dosyası alınamadı (HTTP " + code + ")");
            }
            return connection;
        }
        throw new IOException("GitHub çok fazla yönlendirme gönderdi");
    }

    public static void download(Context context, JSONObject entry, Progress progress) throws Exception {
        String id = entry.getString("id");
        if (!validId(id)) throw new IOException("Geçersiz video kimliği");
        URL url = validateReleaseUrl(entry.getString("url"));
        String expectedHash = entry.getString("sha256").toLowerCase(Locale.ROOT);
        long expectedSize = entry.getLong("size_bytes");
        if (!expectedHash.matches("[a-f0-9]{64}") || expectedSize < 1 || expectedSize > MAX_VIDEO_BYTES)
            throw new IOException("Dosya doğrulama bilgileri eksik");

        File finalFile = offlineFile(context, id);
        File folder = finalFile.getParentFile();
        if (!folder.isDirectory() && !folder.mkdirs()) throw new IOException("Video klasörü oluşturulamadı");
        if (folder.getUsableSpace() < expectedSize + 10L * 1024 * 1024)
            throw new IOException("Cihazda yeterli boş alan yok");
        File temp = new File(folder, id + ".part");
        if (temp.exists() && !temp.delete()) throw new IOException("Önceki indirme temizlenemedi");

        HttpURLConnection connection = null;
        try {
            connection = openVideoConnection(url);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long downloaded = 0L;
            long lastProgress = 0L;
            try (InputStream in = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(temp)) {
                byte[] buffer = new byte[64 * 1024];
                int count;
                while ((count = in.read(buffer)) != -1) {
                    if (Thread.currentThread().isInterrupted()) throw new IOException("İndirme durduruldu");
                    downloaded += count;
                    if (downloaded > expectedSize) throw new IOException("İndirilen video beklenenden büyük");
                    digest.update(buffer, 0, count);
                    out.write(buffer, 0, count);
                    long time = android.os.SystemClock.elapsedRealtime();
                    if (time - lastProgress >= 250) {
                        progress.update(downloaded, expectedSize);
                        lastProgress = time;
                    }
                }
                out.getFD().sync();
            }
            if (downloaded != expectedSize) throw new IOException("Video eksik indirildi");
            StringBuilder actual = new StringBuilder(64);
            for (byte b : digest.digest()) actual.append(String.format(Locale.ROOT, "%02x", b & 0xff));
            if (!expectedHash.equals(actual.toString())) throw new IOException("Video doğrulaması başarısız: SHA-256 uyuşmuyor");
            if (finalFile.exists() && !finalFile.delete()) throw new IOException("Eski video silinemedi");
            if (!temp.renameTo(finalFile)) throw new IOException("Video saklanamadı");
            preferences(context).edit().putString("sha_" + id, expectedHash).apply();
            progress.update(expectedSize, expectedSize);
        } finally {
            if (connection != null) connection.disconnect();
            if (temp.isFile()) temp.delete();
        }
    }
}
