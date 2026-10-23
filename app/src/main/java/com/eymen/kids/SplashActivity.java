package com.eymen.kids;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Five-second colorful welcome animation before the web-based homepage. */
public final class SplashActivity extends Activity {
    private static final long DURATION_MS = 5000L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<ObjectAnimator> animations = new ArrayList<>();
    private boolean navigated = false;
    private final int[] letterColors = {0xFFFFD643, 0xFFFC6099, 0xFF6DDAFF, 0xFF8B5CF6,
            0xFF60DAB5, 0xFFFF974D, 0xFFFFDE64, 0xFF5ABDF9, 0xFFEF7BDC};

    private int dp(float n) { return Math.round(getResources().getDisplayMetrics().density * n); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFF34228B);
        getWindow().setNavigationBarColor(0xFF34228B);
        FrameLayout root = new FrameLayout(this);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{0xFF8D5AFE, 0xFF5D8CFA, 0xFF20C6D2, 0xFFFF82B6}));
        root.addView(new ConfettiView(), new FrameLayout.LayoutParams(-1, -1));

        LinearLayout panel = new LinearLayout(this);
        panel.setGravity(Gravity.CENTER);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(12), dp(12), dp(12), dp(12));
        FrameLayout.LayoutParams panelLp = new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER);
        root.addView(panel, panelLp);

        TextView star = new TextView(this);
        star.setText("⭐ 🌈 ⭐");
        star.setGravity(Gravity.CENTER);
        star.setTextSize(31);
        star.setPadding(0, 0, 0, dp(12));
        panel.addView(star);

        LinearLayout wordOne = createWord("Eymen", 49);
        LinearLayout wordTwo = createWord("Kids", 58);
        panel.addView(wordOne);
        panel.addView(wordTwo);

        TextView subtitle = new TextView(this);
        subtitle.setText("Dombilinin uşağı gıppık");
        subtitle.setTextColor(0xFFFFFFFF);
        subtitle.setTextSize(21);
        subtitle.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        subtitle.setShadowLayer(dp(4), 0, dp(3), 0xFF672596);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(dp(12), dp(12), dp(12), dp(12));
        GradientDrawable subBg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{0xFFFA4998, 0xFF7A3CD5, 0xFFFF8A5C});
        subBg.setCornerRadius(dp(28));
        subtitle.setBackground(subBg);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-2, -2);
        subLp.topMargin = dp(24);
        panel.addView(subtitle, subLp);
        subtitle.setAlpha(0f);
        subtitle.setScaleX(.7f);
        subtitle.setScaleY(.7f);
        subtitle.animate().alpha(1f).scaleX(1f).scaleY(1f)
            .setStartDelay(2750).setDuration(650).start();
        ObjectAnimator sway = ObjectAnimator.ofFloat(subtitle, "translationX", -dp(9), dp(9));
        sway.setDuration(650);
        sway.setRepeatCount(ValueAnimator.INFINITE);
        sway.setRepeatMode(ValueAnimator.REVERSE);
        sway.setStartDelay(3350);
        sway.setInterpolator(new AccelerateDecelerateInterpolator());
        sway.start();
        animations.add(sway);

        TextView footer = new TextView(this);
        footer.setText("✨ EYMEN KIDS ✨");
        footer.setGravity(Gravity.CENTER);
        footer.setTextColor(0xFFFFFFFF);
        footer.setTextSize(13);
        footer.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        FrameLayout.LayoutParams footerLp =
            new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM);
        footerLp.bottomMargin = dp(36);
        root.addView(footer, footerLp);
        setContentView(root);

        handler.postDelayed(() -> {
            if (isFinishing() || isDestroyed() || navigated) return;
            navigated = true;
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, DURATION_MS);
    }

    private LinearLayout createWord(String word, int sp) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        row.setOrientation(LinearLayout.HORIZONTAL);
        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
        final float widthFactor = Math.min(1f,
            (screenWidth - dp(28)) / (dp(sp * (word.length() == 5 ? 3.28f : 2.7f))));
        int adjustedSize = Math.max(27, Math.round(sp * widthFactor));
        for (int i = 0; i < word.length(); i++) {
            TextView letter = new TextView(this);
            letter.setText(String.valueOf(word.charAt(i)));
            letter.setTextSize(adjustedSize);
            letter.setIncludeFontPadding(false);
            letter.setTextColor(letterColors[(i + ("Kids".equals(word) ? 5 : 0)) % letterColors.length]);
            letter.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
            letter.setShadowLayer(dp(2.8f), dp(2), dp(3), 0xFF3A258B);
            row.addView(letter);
            letter.setTranslationY(-dp(520));
            letter.setRotation(i % 2 == 0 ? -18 : 18);
            ObjectAnimator falling = ObjectAnimator.ofFloat(letter, "translationY", -dp(520), dp(8), 0);
            falling.setDuration(720);
            falling.setStartDelay(350 + (("Kids".equals(word) ? 5 : 0) + i) * 255L);
            falling.setInterpolator(new android.view.animation.BounceInterpolator());
            falling.start();
            animations.add(falling);
            letter.animate().rotation(0).setStartDelay(350 +
                (("Kids".equals(word) ? 5 : 0) + i) * 255L).setDuration(800).start();
        }
        return row;
    }

    private final class ConfettiView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Random rng = new Random(2201);
        ConfettiView() { super(SplashActivity.this); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            rng.setSeed(2201);
            int[] colors = {0xCCFFDB51, 0xBBFFFFFF, 0xAAFF8ED8, 0xAA7CFAD7};
            for (int i = 0; i < 75; i++) {
                paint.setColor(colors[i % colors.length]);
                float x = rng.nextFloat() * getWidth();
                float y = rng.nextFloat() * getHeight();
                canvas.drawCircle(x, y, dp(2.2f + rng.nextFloat() * 4), paint);
            }
        }
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        for (ObjectAnimator animation : animations) animation.cancel();
        super.onDestroy();
    }
}
