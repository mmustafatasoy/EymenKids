package com.eymen.kids;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import java.io.InputStream;
import java.io.File;
import java.util.Locale;
import java.util.regex.Pattern;

/** Offline-only player. Never loads a remote HTTP(S) media URL. */
public final class OfflinePlayerActivity extends Activity {
    public static final String EXTRA_ASSET_FILE = "eymen.assetFile";
    public static final String EXTRA_TITLE = "eymen.title";
    public static final String EXTRA_DOWNLOADED_ID = "eymen.downloadedId";
    private static final Pattern SAFE_FILE = Pattern.compile("[a-z0-9][a-z0-9-]*\\.(mp4|m4v|webm)");
    private ExoPlayer player;
    private PlayerView playerView;

    private int dp(float size) {
        return Math.round(getResources().getDisplayMetrics().density * size);
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(1, 7, 24));
        playerView = new PlayerView(this);
        playerView.setBackgroundColor(Color.BLACK);
        playerView.setUseController(true);
        playerView.setControllerAutoShow(true);
        playerView.setControllerShowTimeoutMs(4500);
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        playerView.setKeepScreenOn(true);
        root.addView(playerView, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(16), dp(8), dp(18), dp(8));
        top.setBackground(new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{0xEE030A27, 0xCC07183A}
        ));

        Button back = new Button(this);
        back.setText("← Geri");
        back.setTextColor(Color.WHITE);
        back.setTextSize(14);
        back.setAllCaps(false);
        back.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2258A9));
        back.setOnClickListener(v -> finish());
        top.addView(back, new LinearLayout.LayoutParams(dp(96), dp(46)));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(14), 0, 0, 0);
        TextView title = new TextView(this);
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        title.setText(getIntent().getStringExtra(EXTRA_TITLE) == null
            ? "Eymen Kids" : getIntent().getStringExtra(EXTRA_TITLE));
        info.addView(title);
        TextView badge = new TextView(this);
        badge.setText("📦 İNTERNETSİZ • EYMEN KIDS");
        badge.setTextSize(11);
        badge.setTextColor(0xFF93EBCD);
        info.addView(badge);
        top.addView(info, new LinearLayout.LayoutParams(0, -2, 1f));

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -2, Gravity.TOP);
        root.addView(top, lp);
        setContentView(root);

        Uri uri = null;
        String file = getIntent().getStringExtra(EXTRA_ASSET_FILE);
        String downloadedId = getIntent().getStringExtra(EXTRA_DOWNLOADED_ID);
        if (downloadedId != null) {
            if (!OfflineVideoStore.validId(downloadedId)) {
                showError("Geçersiz video kimliği."); return;
            }
            File saved = OfflineVideoStore.offlineFile(this, downloadedId);
            if (!saved.isFile() || saved.length() <= 0) {
                showError("Bu video henüz çevrimdışı indirilmemiş."); return;
            }
            uri = Uri.fromFile(saved);
        } else if (file != null) {
            file = file.toLowerCase(Locale.ROOT);
            if (!SAFE_FILE.matcher(file).matches()) {
                showError("Geçersiz video dosya adı.");
                return;
            }
            // Do not attempt to stream the video's YouTube URL.
            try (InputStream in = getAssets().open("videos/" + file)) {
                // Existence check. APK assets remain local and accessible offline.
                if (in.read() < 0) throw new IllegalStateException("Boş video dosyası");
            } catch (Exception exception) {
                showError("Bu video henüz APK içine eklenmedi: " + file);
                return;
            }
            uri = Uri.parse("asset:///videos/" + file);
        } else {
            Uri selected = getIntent().getData();
            if (selected != null && "content".equals(selected.getScheme())) {
                uri = selected;
            }
        }
        if (uri == null) {
            showError("Oynatılacak yerel MP4 bulunamadı.");
            return;
        }
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.addListener(new Player.Listener() {
            @Override public void onPlayerError(PlaybackException error) {
                Toast.makeText(OfflinePlayerActivity.this,
                    "Video açılamadı. H.264/AAC MP4 biçimini deneyin.", Toast.LENGTH_LONG).show();
            }
        });
        player.setMediaItem(MediaItem.fromUri(uri));
        player.prepare();
        player.play();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override protected void onPause() {
        super.onPause();
        if (player != null) player.pause();
    }

    @Override protected void onDestroy() {
        if (playerView != null) playerView.setPlayer(null);
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }
}
