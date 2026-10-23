package com.eymen.kids;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Immersive Eymen Kids player.
 *
 * All published catalog videos form one ordered playlist: season, episode,
 * followed by subsequent series. User skips immediately; natural completion
 * shows a four-second "Her şey Gıppık için" screen before the next video.
 * Controls start hidden and appear only after a tap.
 */
public final class OfflinePlayerActivity extends Activity {
    public static final String EXTRA_ASSET_FILE = "eymen.assetFile";
    public static final String EXTRA_TITLE = "eymen.title";
    public static final String EXTRA_DOWNLOADED_ID = "eymen.downloadedId";
    public static final String EXTRA_STREAM_URL = "eymen.streamUrl";
    public static final String EXTRA_VIDEO_ID = "eymen.videoId";
    private static final Pattern SAFE_FILE = Pattern.compile("[a-z0-9][a-z0-9-]*\\.(mp4|m4v|webm)");
    private static final long INTERSTITIAL_MS = 4200L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ArrayList<JSONObject> playlist = new ArrayList<>();
    private ExoPlayer player;
    private PlayerView playerView;
    private LinearLayout topControls;
    private LinearLayout bottomControls;
    private FrameLayout interstitial;
    private TextView titleLabel;
    private TextView subtitleLabel;
    private Button playPauseButton;
    private Button nextButton;
    private Button backButton;
    private Button downloadButton;
    private SeekBar seekBar;
    private ObjectAnimator sparkle;
    private Runnable transitionJob;
    private String currentId;
    private String currentTitle = "Eymen Kids";
    private boolean inTransition = false;
    private boolean foreground = false;
    private Runnable hideJob;
    private final Runnable progressJob = new Runnable() {
        @Override public void run() {
            if (player != null && seekBar != null && !inTransition) {
                long duration = player.getDuration();
                if (duration > 0) {
                    seekBar.setProgress((int) Math.min(1000,
                        (player.getCurrentPosition() * 1000L) / duration));
                }
                if (playPauseButton != null) {
                    playPauseButton.setText(player.isPlaying() ? "⏸ Duraklat" : "▶ Oynat");
                }
            }
            handler.postDelayed(this, 550);
        }
    };

    private int dp(float value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }

    private GradientDrawable gradient(int[] colors, int radius) {
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR, colors);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private Button control(String text, int color, int width) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(13);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(ColorStateList.valueOf(color));
        b.setMinimumHeight(dp(48));
        b.setMinWidth(dp(width));
        return b;
    }

    private void immersiveMode() {
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        immersiveMode();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        buildUi();
        buildPlaylist(OfflineVideoStore.cachedCatalog(this));
        refreshPlaylistInBackground();

        Uri initial = initialUri();
        if (initial == null) return;

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.addListener(new Player.Listener() {
            @Override public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED && !inTransition) {
                    advance(true);
                }
            }
            @Override public void onPlayerError(PlaybackException error) {
                Toast.makeText(OfflinePlayerActivity.this,
                    "Video açılamadı. İnterneti ya da MP4 dosyasını kontrol et.",
                    Toast.LENGTH_LONG).show();
                showControls();
            }
        });
        player.setMediaItem(MediaItem.fromUri(initial));
        player.prepare();
        player.play();
        refreshLabels();
        handler.post(progressJob);
    }

    private void buildUi() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        playerView = new PlayerView(this);
        playerView.setBackgroundColor(Color.BLACK);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING);
        playerView.setKeepScreenOn(true);
        playerView.setUseController(false);
        playerView.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (topControls.getVisibility() == View.VISIBLE) hideControls();
                else showControls();
                view.performClick();
            }
            return true;
        });
        root.addView(playerView, new FrameLayout.LayoutParams(-1, -1));

        topControls = new LinearLayout(this);
        topControls.setOrientation(LinearLayout.HORIZONTAL);
        topControls.setGravity(Gravity.CENTER_VERTICAL);
        topControls.setPadding(dp(14), dp(8), dp(14), dp(9));
        topControls.setBackground(gradient(new int[]{0xF2020B23, 0xD52B1B60}, 0));

        Button back = control("← Geri", 0xFF7146CA, 88);
        backButton = back;
        back.setOnClickListener(v -> finish());
        topControls.addView(back, new LinearLayout.LayoutParams(dp(96), dp(48)));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(12), 0, dp(10), 0);
        titleLabel = new TextView(this);
        titleLabel.setTextSize(17);
        titleLabel.setTextColor(Color.WHITE);
        titleLabel.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titleLabel.setSingleLine(true);
        titleLabel.setEllipsize(android.text.TextUtils.TruncateAt.END);
        labels.addView(titleLabel);
        subtitleLabel = new TextView(this);
        subtitleLabel.setTextSize(11);
        subtitleLabel.setTextColor(0xFFB9F9E9);
        labels.addView(subtitleLabel);
        topControls.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));

        downloadButton = control("⬇ İndir", 0xFFCF4C92, 96);
        downloadButton.setOnClickListener(v -> {
            if (!OfflineVideoStore.validId(currentId)) return;
            Intent intent = new Intent(this, VideoLibraryActivity.class);
            intent.putExtra("eymen.downloadId", currentId);
            startActivity(intent);
        });
        topControls.addView(downloadButton, new LinearLayout.LayoutParams(dp(108), dp(48)));

        FrameLayout.LayoutParams topLp =
            new FrameLayout.LayoutParams(-1, -2, Gravity.TOP);
        root.addView(topControls, topLp);
        topControls.setVisibility(View.GONE);

        bottomControls = new LinearLayout(this);
        bottomControls.setOrientation(LinearLayout.HORIZONTAL);
        bottomControls.setGravity(Gravity.CENTER_VERTICAL);
        bottomControls.setPadding(dp(16), dp(6), dp(16), dp(8));
        bottomControls.setBackground(gradient(
            new int[]{0xDD34236D, 0xEE020A21}, 0));

        playPauseButton = control("⏸ Duraklat", 0xFF7448D4, 104);
        playPauseButton.setOnClickListener(v -> {
            if (player == null || inTransition) return;
            if (player.isPlaying()) player.pause();
            else player.play();
            playPauseButton.setText(player.isPlaying() ? "⏸ Duraklat" : "▶ Oynat");
            postponeHide();
        });
        bottomControls.addView(playPauseButton,
            new LinearLayout.LayoutParams(dp(122), dp(48)));

        seekBar = new SeekBar(this);
        seekBar.setMax(1000);
        seekBar.setProgressTintList(ColorStateList.valueOf(0xFF44D9F2));
        seekBar.setThumbTintList(ColorStateList.valueOf(0xFFFFD64B));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress,
                                                    boolean fromUser) {
                if (fromUser && player != null && player.getDuration() > 0)
                    player.seekTo((player.getDuration() * progress) / 1000);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(hideJob == null ? progressJob : hideJob);
            }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                postponeHide();
            }
        });
        LinearLayout.LayoutParams seekLp = new LinearLayout.LayoutParams(0, dp(46), 1f);
        seekLp.leftMargin = dp(8);
        seekLp.rightMargin = dp(8);
        bottomControls.addView(seekBar, seekLp);

        nextButton = control("⏭ Sonraki", 0xFFEC5C9D, 106);
        nextButton.setOnClickListener(v -> advance(false));
        bottomControls.addView(nextButton,
            new LinearLayout.LayoutParams(dp(116), dp(48)));
        FrameLayout.LayoutParams bottomLp =
            new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM);
        root.addView(bottomControls, bottomLp);
        bottomControls.setVisibility(View.GONE);

        // Covers 100% of the video for a moment before autoplay begins.
        interstitial = new FrameLayout(this);
        interstitial.setBackground(gradient(new int[]{
            0xFF5926B0, 0xFF8D42E5, 0xFFF45D9F, 0xFF3C8FF5
        }, 0));

        LinearLayout message = new LinearLayout(this);
        message.setOrientation(LinearLayout.VERTICAL);
        message.setGravity(Gravity.CENTER);
        message.setPadding(dp(14), 0, dp(14), 0);

        TextView stars = new TextView(this);
        stars.setText("🌟 ✨ ⭐ ✨ 🌟");
        stars.setTextColor(0xFFFFE46A);
        stars.setTextSize(38);
        stars.setGravity(Gravity.CENTER);
        message.addView(stars);

        TextView headline = new TextView(this);
        headline.setText("HER ŞEY\nGIPPIK İÇİN!");
        headline.setTextColor(0xFFFFE55D);
        headline.setTextSize(36);
        headline.setGravity(Gravity.CENTER);
        headline.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        headline.setShadowLayer(dp(5), 0, dp(4), 0xFF54215B);
        headline.setPadding(0, dp(12), 0, dp(8));
        message.addView(headline);

        TextView footer = new TextView(this);
        footer.setText("💖 Sıradaki eğlence geliyor! 💖");
        footer.setTextColor(Color.WHITE);
        footer.setTextSize(17);
        footer.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        footer.setGravity(Gravity.CENTER);
        message.addView(footer);

        FrameLayout.LayoutParams center = new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER);
        interstitial.addView(message, center);
        root.addView(interstitial, new FrameLayout.LayoutParams(-1, -1));
        interstitial.setVisibility(View.GONE);

        sparkle = ObjectAnimator.ofFloat(headline, "scaleX", 0.94f, 1.09f, 0.94f);
        sparkle.setRepeatCount(ValueAnimator.INFINITE);
        sparkle.setDuration(1100);
        setContentView(root);
    }

    /** For bundled or phone-picked MP4s, keep simple local playback. */
    private Uri initialUri() {
        Intent intent = getIntent();
        String id = intent.getStringExtra(EXTRA_DOWNLOADED_ID);
        currentId = id == null ? intent.getStringExtra(EXTRA_VIDEO_ID) : id;
        currentTitle = intent.getStringExtra(EXTRA_TITLE) == null ?
            "Eymen Kids" : intent.getStringExtra(EXTRA_TITLE);

        if (id != null) {
            if (!OfflineVideoStore.validId(id)) {
                showError("Video kimliği geçersiz."); return null;
            }
            File file = OfflineVideoStore.offlineFile(this, id);
            if (!file.isFile() || file.length() <= 0) {
                showError("Video henüz indirilmemiş."); return null;
            }
            return Uri.fromFile(file);
        }

        String asset = intent.getStringExtra(EXTRA_ASSET_FILE);
        if (asset != null) {
            asset = asset.toLowerCase(Locale.ROOT);
            currentId = null;
            if (!SAFE_FILE.matcher(asset).matches()) {
                showError("Geçersiz yerel video."); return null;
            }
            try (InputStream in = getAssets().open("videos/" + asset)) {
                if (in.read() < 0) throw new IllegalStateException("Boş MP4");
                return Uri.parse("asset:///videos/" + asset);
            } catch (Exception e) {
                showError("Yerel video bulunamadı: " + asset); return null;
            }
        }

        String stream = intent.getStringExtra(EXTRA_STREAM_URL);
        if (stream != null) {
            try {
                OfflineVideoStore.validateReleaseUrl(stream);
                return Uri.parse(stream);
            } catch (Exception e) {
                showError("Güvenilmeyen video bağlantısı."); return null;
            }
        }
        Uri picked = intent.getData();
        if (picked != null && "content".equals(picked.getScheme())) {
            currentId = null;
            return picked;
        }
        showError("Oynatılacak video bulunamadı.");
        return null;
    }

    private void buildPlaylist(JSONObject catalog) {
        ArrayList<JSONObject> newEntries = new ArrayList<>();
        JSONArray array = catalog.optJSONArray("videos");
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null || !OfflineVideoStore.validId(item.optString("id"))) continue;
                try { OfflineVideoStore.validateReleaseUrl(item.getString("url")); }
                catch (Exception ignored) { continue; }
                newEntries.add(item);
            }
        }
        Collections.sort(newEntries, (a, b) -> {
            int s = a.optString("series_id", a.optString("id"))
                .compareTo(b.optString("series_id", b.optString("id")));
            if (s != 0) return s;
            int season = Integer.compare(a.optInt("season", 1), b.optInt("season", 1));
            if (season != 0) return season;
            int ep = Integer.compare(a.optInt("episode", 1), b.optInt("episode", 1));
            return ep != 0 ? ep :
                a.optString("id").compareTo(b.optString("id"));
        });
        playlist.clear();
        playlist.addAll(newEntries);
        if (nextButton != null) refreshLabels();
    }

    private void refreshPlaylistInBackground() {
        new Thread(() -> {
            try {
                JSONObject updated = OfflineVideoStore.refreshCatalog(getApplicationContext());
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) buildPlaylist(updated);
                });
            } catch (Exception ignored) {
                // Offline use relies on verified app-private cached catalog and MP4 files.
            }
        }, "EymenPlaylistUpdate").start();
    }

    private boolean hasInternet() {
        ConnectivityManager manager =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) return false;
        Network active = manager.getActiveNetwork();
        NetworkCapabilities c = manager.getNetworkCapabilities(active);
        return c != null && c.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private JSONObject nextPlayable() {
        if (currentId == null || playlist.isEmpty()) return null;
        int index = -1;
        for (int i = 0; i < playlist.size(); i++) {
            if (currentId.equals(playlist.get(i).optString("id"))) {
                index = i;
                break;
            }
        }
        if (index < 0) return null;
        boolean online = hasInternet();
        for (int step = 1; step <= playlist.size(); step++) {
            JSONObject next = playlist.get((index + step) % playlist.size());
            if (OfflineVideoStore.isDownloaded(this, next) || online) return next;
        }
        return null;
    }

    private void advance(boolean fromCompletion) {
        JSONObject next = nextPlayable();
        if (next == null) {
            if (!fromCompletion) Toast.makeText(this,
                "Sıradaki video bulunamadı. Çevrimdışı kullanıyorsan önce indir.",
                Toast.LENGTH_LONG).show();
            showControls();
            return;
        }
        if (fromCompletion) showGippikTransition(next);
        else {
            stopTransition();
            startEntry(next);
            hideControls();
        }
    }

    private void showGippikTransition(JSONObject next) {
        if (inTransition) return;
        inTransition = true;
        hideControls();
        interstitial.setVisibility(View.VISIBLE);
        interstitial.setAlpha(0f);
        interstitial.animate().alpha(1f).setDuration(300).start();
        if (sparkle != null) sparkle.start();
        transitionJob = () -> {
            if (!isFinishing() && !isDestroyed()) {
                if (foreground) startEntry(next);
                else {
                    // Don't autoplay into an invisible/background activity.
                    transitionJob = () -> {
                        if (!isFinishing() && !isDestroyed()) startEntry(next);
                    };
                    return;
                }
            }
            stopTransition();
        };
        handler.postDelayed(transitionJob, INTERSTITIAL_MS);
    }

    private void stopTransition() {
        if (transitionJob != null) handler.removeCallbacks(transitionJob);
        transitionJob = null;
        inTransition = false;
        if (sparkle != null) sparkle.cancel();
        if (interstitial != null) {
            interstitial.animate().cancel();
            interstitial.setVisibility(View.GONE);
        }
    }

    private void startEntry(JSONObject entry) {
        if (player == null) return;
        String id = entry.optString("id");
        Uri uri;
        if (OfflineVideoStore.isDownloaded(this, entry)) {
            uri = Uri.fromFile(OfflineVideoStore.offlineFile(this, id));
        } else {
            try {
                String url = entry.getString("url");
                OfflineVideoStore.validateReleaseUrl(url);
                uri = Uri.parse(url);
            } catch (Exception error) {
                showError("Sıradaki video adresi geçersiz."); return;
            }
        }
        currentId = id;
        currentTitle = entry.optString("episode_title", entry.optString("title", "Eymen Kids"));
        seekBar.setProgress(0);
        player.setMediaItem(MediaItem.fromUri(uri));
        player.prepare();
        if (foreground) player.play();
        refreshLabels();
    }

    private void refreshLabels() {
        if (titleLabel == null) return;
        titleLabel.setText("🎬 " + currentTitle);
        boolean downloaded = false;
        for (JSONObject item : playlist) {
            if (currentId != null && currentId.equals(item.optString("id"))) {
                downloaded = OfflineVideoStore.isDownloaded(this, item);
                break;
            }
        }
        subtitleLabel.setText(downloaded ? "📦 Çevrimdışı • Eymen Kids" :
            "🌐 Çevrimiçi • Eymen Kids");
        if (downloadButton != null) {
            downloadButton.setVisibility(
                OfflineVideoStore.validId(currentId) && !downloaded ?
                View.VISIBLE : View.GONE);
        }
        if (nextButton != null) nextButton.setEnabled(currentId != null);
    }

    private void showControls() {
        if (inTransition) return;
        topControls.setVisibility(View.VISIBLE);
        bottomControls.setVisibility(View.VISIBLE);
        postponeHide();
    }

    private void hideControls() {
        if (hideJob != null) handler.removeCallbacks(hideJob);
        topControls.setVisibility(View.GONE);
        bottomControls.setVisibility(View.GONE);
    }

    private void postponeHide() {
        if (hideJob != null) handler.removeCallbacks(hideJob);
        hideJob = this::hideControls;
        handler.postDelayed(hideJob, 3600);
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int key = event.getKeyCode();
            if (key == KeyEvent.KEYCODE_MEDIA_NEXT || key == KeyEvent.KEYCODE_CHANNEL_UP ||
                key == KeyEvent.KEYCODE_BUTTON_R1) {
                advance(false); return true;
            }
            if (key == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE && player != null) {
                if (player.isPlaying()) player.pause(); else player.play();
                showControls(); return true;
            }
            if (!inTransition && topControls != null &&
                topControls.getVisibility() == View.GONE &&
                (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER ||
                 key == KeyEvent.KEYCODE_DPAD_UP || key == KeyEvent.KEYCODE_DPAD_DOWN ||
                 key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                showControls();
                if (backButton != null) backButton.requestFocus();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) immersiveMode();
    }

    @Override protected void onResume() {
        super.onResume();
        foreground = true;
        if (inTransition && transitionJob != null &&
            interstitial.getVisibility() == View.VISIBLE) {
            // Preserve a transition paused in the background.
            Runnable job = transitionJob;
            handler.removeCallbacks(job);
            handler.postDelayed(job, 700);
        }
    }

    @Override protected void onPause() {
        foreground = false;
        if (player != null) player.pause();
        super.onPause();
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (sparkle != null) sparkle.cancel();
        if (playerView != null) playerView.setPlayer(null);
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }
}
