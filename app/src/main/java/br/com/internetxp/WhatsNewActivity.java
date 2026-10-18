package br.com.internetxp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Tela exclusiva de atualização, com cabeçalho e cartões no estilo mobile Windows XP. */
public final class WhatsNewActivity extends Activity {
    private static final String PREFS = "local";
    private static final String KEY_LAST_SEEN = "whats_new_last_seen_version_code";
    private static final int BLUE = 0xff0c63bd;
    private static final int DEEP_BLUE = 0xff07377f;
    private static final int DARK = 0xff152b46;
    private static final int MUTED = 0xff61748b;
    private static final int BORDER = 0xffcad9ea;
    private static final int LIGHT = 0xfff2f7fd;

    public static boolean shouldShow(SharedPreferences storage) {
        return ReleaseNotes.shouldShow(storage.getInt(KEY_LAST_SEEN, 0));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable fill(int color, int strokeColor, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        if (strokeColor != 0) d.setStroke(dp(1), strokeColor);
        return d;
    }

    private GradientDrawable gradient(int top, int bottom, int radius) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{top, bottom});
        d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView label(String text, float size, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(DEEP_BLUE);
        getWindow().setNavigationBarColor(0xff10243b);
        buildScreen();
    }

    private void buildScreen() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(LIGHT);
        setContentView(page);

        // Cabeçalho clássico sem desperdiçar espaço em telas pequenas.
        LinearLayout topBar = new LinearLayout(this);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(18), dp(9), dp(18), dp(9));
        topBar.setBackground(gradient(BLUE, DEEP_BLUE, 0));
        TextView brand = label("Internet XP", 17, Color.WHITE, true);
        topBar.addView(brand, new LinearLayout.LayoutParams(0, dp(34), 1));
        TextView edition = label("EDIÇÃO " + ReleaseNotes.VERSION_NAME, 10, 0xffd3e9ff, true);
        edition.setGravity(Gravity.CENTER);
        topBar.addView(edition);
        page.addView(topBar);

        View highlight = new View(this);
        highlight.setBackgroundColor(0xff74baf8);
        page.addView(highlight, new LinearLayout.LayoutParams(-1, dp(2)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(18), dp(18), dp(18), dp(16));
        scroll.addView(body);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(18), dp(16), dp(18), dp(17));
        hero.setBackground(gradient(0xffecf6ff, 0xffffffff, 18));
        hero.setElevation(dp(3));

        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.mipmap.ic_launcher);
        logo.setContentDescription("Globo azul do Internet XP");
        heading.addView(logo, new LinearLayout.LayoutParams(dp(64), dp(64)));
        LinearLayout headingText = new LinearLayout(this);
        headingText.setOrientation(LinearLayout.VERTICAL);
        headingText.setPadding(dp(9), 0, 0, 0);
        TextView eyebrow = label("NOVIDADES DA VERSÃO", 10, BLUE, true);
        headingText.addView(eyebrow);
        TextView heroTitle = label("O que há de novo?", 24, DARK, true);
        headingText.addView(heroTitle);
        heading.addView(headingText, new LinearLayout.LayoutParams(0, -2, 1));
        hero.addView(heading);

        TextView headline = label(ReleaseNotes.HEADLINE, 18, DEEP_BLUE, true);
        LinearLayout.LayoutParams headlineParams = new LinearLayout.LayoutParams(-1, -2);
        headlineParams.topMargin = dp(11);
        hero.addView(headline, headlineParams);
        TextView lead = label(ReleaseNotes.SUBTITLE, 13, MUTED, false);
        lead.setLineSpacing(dp(3), 1.0f);
        LinearLayout.LayoutParams leadParams = new LinearLayout.LayoutParams(-1, -2);
        leadParams.topMargin = dp(6);
        hero.addView(lead, leadParams);
        body.addView(hero);

        TextView section = label("DESTAQUES DA VERSÃO", 11, DEEP_BLUE, true);
        section.setLetterSpacing(0.07f);
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(-1, -2);
        sectionParams.topMargin = dp(22);
        sectionParams.bottomMargin = dp(10);
        body.addView(section, sectionParams);

        for (ReleaseNotes.Feature item : ReleaseNotes.FEATURES) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.TOP);
            card.setPadding(dp(13), dp(12), dp(13), dp(12));
            card.setBackground(fill(Color.WHITE, BORDER, 12));

            TextView number = label(item.number, 13, Color.WHITE, true);
            number.setGravity(Gravity.CENTER);
            number.setBackground(gradient(0xff237ee2, DEEP_BLUE, 9));
            card.addView(number, new LinearLayout.LayoutParams(dp(36), dp(36)));

            LinearLayout description = new LinearLayout(this);
            description.setOrientation(LinearLayout.VERTICAL);
            description.setPadding(dp(11), 0, 0, 0);
            TextView title = label(item.title, 14, DARK, true);
            description.addView(title);
            TextView details = label(item.description, 12, MUTED, false);
            details.setLineSpacing(dp(2), 1.0f);
            LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(-1, -2);
            detailParams.topMargin = dp(3);
            description.addView(details, detailParams);
            card.addView(description, new LinearLayout.LayoutParams(0, -2, 1));

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
            cardParams.bottomMargin = dp(9);
            body.addView(card, cardParams);
        }

        TextView note = label("Novidades disponíveis também em Menu > Novidades desta versão.",
                11, MUTED, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(dp(4), dp(10), dp(4), dp(6));
        body.addView(note);

        View line = new View(this);
        line.setBackgroundColor(BORDER);
        page.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));
        LinearLayout bottom = new LinearLayout(this);
        bottom.setPadding(dp(18), dp(10), dp(18), dp(13));
        bottom.setBackgroundColor(Color.WHITE);
        TextView button = label("COMEÇAR A NAVEGAR  ›", 15, Color.WHITE, true);
        button.setGravity(Gravity.CENTER);
        button.setContentDescription("Fechar novidades e continuar a navegar");
        button.setBackground(gradient(0xff2d95f0, BLUE, 12));
        button.setElevation(dp(2));
        button.setOnClickListener(v -> finish());
        bottom.addView(button, new LinearLayout.LayoutParams(-1, dp(50)));
        page.addView(bottom);
    }

    @Override public void finish() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putInt(KEY_LAST_SEEN, ReleaseNotes.VERSION_CODE).apply();
        super.finish();
    }

    @Override public void onBackPressed() {
        finish();
    }
}
