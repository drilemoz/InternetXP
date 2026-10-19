package br.com.internetxp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.net.URI;
import java.util.function.Consumer;
import java.util.List;

/** Nova aba nativa, leve, sem carregamento de site e com favoritos tocáveis. */
final class HomePageView extends FrameLayout {
    interface Actions {
        void open(String address);
        void addFavorite();
    }
    private final LinearLayout tiles;
    private final LinearLayout inlineAdd;
    private final EditText inlineAddress;
    private final FaviconRepository icons;
    private final Actions callbacks;

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + .5f);
    }

    private GradientDrawable bg(int fill, int border, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radius));
        if (border != 0) drawable.setStroke(dp(1), border);
        return drawable;
    }

    HomePageView(Context context, FaviconRepository icons, Actions callbacks) {
        super(context);
        this.icons = icons;
        this.callbacks = callbacks;
        setBackgroundColor(Color.WHITE);
        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        addView(scroll, new LayoutParams(-1, -1));
        LinearLayout page = new LinearLayout(context);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(20), dp(20), dp(20), dp(14));
        scroll.addView(page);
        page.addView(new android.view.View(context), new LinearLayout.LayoutParams(1, 0, 1));

        TextView title = new TextView(context);
        title.setText("Internet XP");
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(0xff0d68c8);
        page.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Sua janela para a internet");
        subtitle.setTextColor(0xff697c91);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTextSize(12);
        LinearLayout.LayoutParams subtitleLp = new LinearLayout.LayoutParams(-1, -2);
        subtitleLp.bottomMargin = dp(22);
        page.addView(subtitle, subtitleLp);

        EditText search = new EditText(context);
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setTextColor(0xff243346);
        search.setHint("Pesquisar ou digitar endereço");
        search.setHintTextColor(0xff798797);
        search.setPadding(dp(16), dp(4), dp(12), dp(4));
        search.setBackground(bg(0xfff9fbfe, 0xffafc5e3, 12));
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_SEARCH || action == EditorInfo.IME_ACTION_GO || event != null) {
                String entered = search.getText().toString().trim();
                if (!entered.isEmpty()) { callbacks.open(BrowserAddress.resolve(entered)); search.setText(""); }
                return true;
            }
            return false;
        });
        LinearLayout.LayoutParams searchLp = new LinearLayout.LayoutParams(-1, dp(52));
        page.addView(search, searchLp);

        TextView favoritesTitle = new TextView(context);
        favoritesTitle.setText("ACESSO RÁPIDO");
        favoritesTitle.setTextSize(11);
        favoritesTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        favoritesTitle.setTextColor(0xff536d8b);
        LinearLayout.LayoutParams favoritesTitleLp = new LinearLayout.LayoutParams(-1, -2);
        favoritesTitleLp.topMargin = dp(28);
        favoritesTitleLp.bottomMargin = dp(12);
        page.addView(favoritesTitle, favoritesTitleLp);

        inlineAdd = new LinearLayout(context);
        inlineAdd.setOrientation(LinearLayout.HORIZONTAL);
        inlineAdd.setPadding(dp(10), dp(5), dp(10), dp(5));
        inlineAdd.setBackground(bg(0xffeef5ff, 0xffafc5e3, 12));
        inlineAdd.setVisibility(GONE);
        inlineAddress = new EditText(context);
        inlineAddress.setSingleLine(true);
        inlineAddress.setTextSize(13);
        inlineAddress.setHint("https://site.com");
        inlineAddress.setImeOptions(EditorInfo.IME_ACTION_DONE);
        inlineAdd.addView(inlineAddress, new LinearLayout.LayoutParams(0, dp(45), 1));
        TextView closeInline = new TextView(context);
        closeInline.setText("×");
        closeInline.setTextSize(26);
        closeInline.setGravity(Gravity.CENTER);
        closeInline.setOnClickListener(v -> inlineAdd.setVisibility(GONE));
        inlineAdd.addView(closeInline, new LinearLayout.LayoutParams(dp(42), dp(45)));
        LinearLayout.LayoutParams inlineLp = new LinearLayout.LayoutParams(-1, -2);
        inlineLp.bottomMargin = dp(8);
        page.addView(inlineAdd, inlineLp);
        tiles = new LinearLayout(context);
        tiles.setOrientation(LinearLayout.VERTICAL);
        page.addView(tiles);
        page.addView(new android.view.View(context), new LinearLayout.LayoutParams(1, 0, 1));
        setFocusableInTouchMode(true);
        requestFocus();
    }

    void showAddFavoriteField(Consumer<String> onAdd) {
        inlineAdd.setVisibility(VISIBLE);
        if (inlineAdd.getChildCount() > 2) inlineAdd.removeViewAt(2);
        TextView save = new TextView(getContext());
        save.setText("Salvar");
        save.setTextSize(14);
        save.setTextColor(0xff0d68c8);
        save.setGravity(Gravity.CENTER);
        save.setOnClickListener(v -> {
            String value = inlineAddress.getText().toString().trim();
            if (value.isEmpty()) { inlineAddress.setError("Digite o endereço"); return; }
            onAdd.accept(value);
            inlineAddress.setText("");
            inlineAdd.setVisibility(GONE);
        });
        inlineAdd.addView(save, new LinearLayout.LayoutParams(dp(58), dp(45)));
        inlineAddress.requestFocus();
    }

    void showFavorites(List<String> favorites) {
        tiles.removeAllViews();
        int total = Math.min(favorites.size() + 1, 13);
        LinearLayout row = null;
        for (int position = 0; position < total; position++) {
            if (position % 4 == 0) {
                row = new LinearLayout(getContext());
                row.setGravity(Gravity.TOP);
                tiles.addView(row, new LinearLayout.LayoutParams(-1, dp(104)));
            }
            final boolean add = position == total - 1;
            final String address = add ? "" : favorites.get(position);
            LinearLayout cell = new LinearLayout(getContext());
            cell.setGravity(Gravity.CENTER_HORIZONTAL);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setPadding(dp(3), dp(5), dp(3), dp(5));
            cell.setOnClickListener(v -> {
                if (add) callbacks.addFavorite();
                else callbacks.open(address);
            });
            cell.setContentDescription(add ? "Adicionar favorito" : "Abrir favorito " + address);
            LinearLayout.LayoutParams cellLp = new LinearLayout.LayoutParams(0, -1, 1);
            row.addView(cell, cellLp);

            FrameLayout iconFrame = new FrameLayout(getContext());
            iconFrame.setBackground(bg(add ? 0xffe5f0ff : 0xfff6f9fc, 0xffd2dfee, 14));
            cell.addView(iconFrame, new LinearLayout.LayoutParams(dp(52), dp(52)));
            if (add) {
                TextView plus = new TextView(getContext());
                plus.setText("+");
                plus.setTextSize(30);
                plus.setTextColor(0xff0d68c8);
                plus.setGravity(Gravity.CENTER);
                iconFrame.addView(plus, new FrameLayout.LayoutParams(-1, -1));
            } else {
                String name = host(address);
                TextView fallback = new TextView(getContext());
                fallback.setText(name.isEmpty() ? "🌐" : name.substring(0, 1).toUpperCase());
                fallback.setTextSize(20);
                fallback.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                fallback.setTextColor(0xff0d68c8);
                fallback.setGravity(Gravity.CENTER);
                iconFrame.addView(fallback, new FrameLayout.LayoutParams(-1, -1));
                if (FaviconRepository.origin(address) != null) {
                    ImageView favicon = new ImageView(getContext());
                    FrameLayout.LayoutParams faviconLp = new FrameLayout.LayoutParams(dp(32), dp(32), Gravity.CENTER);
                    iconFrame.addView(favicon, faviconLp);
                    icons.load(address, favicon);
                }
            }
            TextView caption = new TextView(getContext());
            caption.setText(add ? "Adicionar" : host(address));
            caption.setMaxLines(2);
            caption.setEllipsize(android.text.TextUtils.TruncateAt.END);
            caption.setTextSize(10);
            caption.setTextColor(0xff35455b);
            caption.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams captionLp = new LinearLayout.LayoutParams(-1, dp(36));
            captionLp.topMargin = dp(2);
            cell.addView(caption, captionLp);
        }
    }

    private String host(String address) {
        try {
            String h = new URI(address).getHost();
            if (h != null && !h.isEmpty()) return h.replaceFirst("^www\\.", "");
        } catch (Exception ignored) { }
        return address;
    }
}
