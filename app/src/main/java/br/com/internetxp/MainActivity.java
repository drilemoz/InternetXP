package br.com.internetxp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.os.SystemClock;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.HorizontalScrollView;
import android.widget.CheckBox;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebExtension;
import org.mozilla.geckoview.WebExtensionController;
import org.mozilla.geckoview.WebResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

/** Navegador Android com GeckoView e layout XP compacto para tela de telefone. */
public final class MainActivity extends Activity {
    private static final int XP_BLUE = 0xff0d68c8;
    private static final int LIGHT_BG = 0xfff1f2f6;
    private static final int PANEL_BG = 0xffedf0f5;
    private static final int BORDER = 0xffbcc8d6;
    private static final int TEXT = 0xff222222;
    private static final int SUBTLE = 0xff6c7885;
    private static final int SUCCESS = 0xff2d8a57;
    private static final String HOME = "about:blank";
    private static final String PREF_BOOKMARKS = "bookmarks";
    private static final String PREF_HISTORY = "history";
    private static final String PREF_DOWNLOADS = "downloads";
    private static final String PREF_HISTORY_DETAILS = "history_details";
    private static final String PREF_OPEN_TABS = "open_tabs_v1";

    private static GeckoRuntime runtime;

    private final ArrayList<Tab> tabs = new ArrayList<>();
    private final ArrayList<String> bookmarks = new ArrayList<>();
    private final ArrayList<String> history = new ArrayList<>();
    private final ArrayList<HistoryRecords.Entry> historyDetails = new ArrayList<>();
    private final ArrayList<DownloadItem> downloads = new ArrayList<>();

    private SharedPreferences storage;
    private int active = -1;
    private AutoCompleteTextView urlBox;
    private ArrayAdapter<String> historyAdapter;
    private GeckoView view;
    private TextView status;
    private TextView backIcon;
    private TextView forwardIcon;
    private TextView tabsBadge;
    private ImageView securityIcon;
    private ImageView dropdownIcon;
    private View menuAnchor;
    private LinearLayout downloadsPanel;
    private LinearLayout downloadsList;
    private TextView downloadsSummary;
    private LinearLayout downloadsFilters;
    private int downloadFilter = 0; // 0 Todos, 1 Ativos, 2 Finalizados
    private boolean suppressSuggestionReload;
    private static final int DOWNLOAD_CANCELLED = -2;
    private static final long DOWNLOAD_REFRESH_MS = 2000L;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable downloadRefresh = new Runnable() {
        @Override public void run() {
            if (isFinishing() || isDestroyed() || downloadsPanel == null || downloadsPanel.getVisibility() != View.VISIBLE) return;
            renderDownloadsPanel();
            scheduleDownloadRefresh();
        }
    };

    private static final class Tab {
        final GeckoSession session;
        String url = HOME;
        String title = "Nova aba";
        boolean back;
        boolean forward;
        boolean opened;
        boolean loaded;
        Tab(GeckoSession s) { session = s; }
    }

    private static final class DownloadItem {
        long id;
        String fileName;
        String url;
        String mime;
        String localUri;
        int status;
        long bytes;
        long total;
        long createdAt;
        int reason;
        boolean unmeteredOnly;
        long lastSampleBytes = -1;
        long lastSampleMs;
        long bytesPerSecond = -1;
    }

    private int dp(int n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String s, float sp, int color) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(sp);
        v.setTextColor(color);
        v.setGravity(Gravity.CENTER_VERTICAL);
        return v;
    }

    private GradientDrawable rounded(int color, int outline, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        if (outline != 0) d.setStroke(dp(1), outline);
        return d;
    }

    private void divider(LinearLayout root, int heightDp) {
        View v = new View(this);
        v.setBackgroundColor(BORDER);
        root.addView(v, new LinearLayout.LayoutParams(-1, dp(heightDp)));
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        storage = getSharedPreferences("local", MODE_PRIVATE);
        loadStringList(PREF_BOOKMARKS, bookmarks);
        loadStringList(PREF_HISTORY, history);
        loadHistoryDetails();
        loadDownloads();
        createLayout();
        refreshSuggestionAdapter("");
        try {
            if (runtime == null) runtime = GeckoRuntime.create(getApplicationContext());
            extensionPrompts();
            if (!restoreTabs()) addTab(HOME);
        } catch (RuntimeException e) {
            showError(e);
        }
    }

    private void createLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(LIGHT_BG);
        setContentView(root);

        // Painel de endereço: mais baixo, mais largo e com seta de histórico ao estilo IE.
        LinearLayout addressPanel = new LinearLayout(this);
        addressPanel.setBackgroundColor(PANEL_BG);
        addressPanel.setGravity(Gravity.CENTER_VERTICAL);
        addressPanel.setPadding(dp(6), dp(6), dp(8), dp(6));

        LinearLayout addressBox = new LinearLayout(this);
        addressBox.setOrientation(LinearLayout.HORIZONTAL);
        addressBox.setGravity(Gravity.CENTER_VERTICAL);
        addressBox.setBackground(rounded(Color.WHITE, BORDER, 0));
        addressBox.setPadding(dp(8), 0, dp(4), 0);

        securityIcon = new ImageView(this);
        securityIcon.setImageResource(R.drawable.ic_globe_xp);
        securityIcon.setColorFilter(SUBTLE);
        securityIcon.setContentDescription("Tipo de conexão");
        securityIcon.setPadding(dp(2), dp(8), dp(2), dp(8));
        addressBox.addView(securityIcon, new LinearLayout.LayoutParams(dp(22), dp(36)));

        urlBox = new AutoCompleteTextView(this);
        urlBox.setSingleLine();
        // IE-like behavior: tapping the URL selects the entire address.
        // The very first typed character replaces the selection.
        urlBox.setSelectAllOnFocus(true);
        urlBox.setOnClickListener(v -> urlBox.selectAll());
        urlBox.setTextSize(13);
        urlBox.setTextColor(TEXT);
        urlBox.setHint("https://www.exemplo.com");
        urlBox.setThreshold(0);
        urlBox.setDropDownHeight(dp(220));
        urlBox.setBackgroundColor(Color.TRANSPARENT);
        urlBox.setPadding(dp(6), 0, dp(2), 0);
        urlBox.setImeOptions(EditorInfo.IME_ACTION_GO);
        urlBox.setOnEditorActionListener((tv, action, event) -> {
            if (action == EditorInfo.IME_ACTION_GO ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                submitAddress();
                return true;
            }
            return false;
        });
        urlBox.setOnFocusChangeListener((v, focused) -> {
            if (focused) {
                urlBox.selectAll();
                refreshSuggestionAdapter(urlBox.getText().toString());
                urlBox.post(urlBox::showDropDown);
            }
        });
        urlBox.setOnItemClickListener((parent, v, position, id) -> {
            String selected = historyAdapter.getItem(position);
            if (selected != null) {
                setUrlBoxText(selected);
                navigate(BrowserAddress.resolve(selected));
                hideKeyboard();
            }
        });
        urlBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                if (suppressSuggestionReload) return;
                refreshSuggestionAdapter(s.toString());
                if (urlBox.hasFocus()) urlBox.showDropDown();
            }
        });
        historyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        urlBox.setAdapter(historyAdapter);
        addressBox.addView(urlBox, new LinearLayout.LayoutParams(0, dp(36), 1));

        dropdownIcon = new ImageView(this);
        dropdownIcon.setImageResource(R.drawable.ic_dropdown_xp);
        dropdownIcon.setColorFilter(XP_BLUE);
        dropdownIcon.setPadding(dp(6), dp(9), dp(6), dp(9));
        dropdownIcon.setContentDescription("Abrir endereços do histórico");
        dropdownIcon.setOnClickListener(v -> {
            refreshSuggestionAdapter(urlBox.getText().toString());
            if (urlBox.isPopupShowing()) urlBox.dismissDropDown();
            else {
                urlBox.requestFocus();
                urlBox.showDropDown();
            }
        });
        addressBox.addView(dropdownIcon, new LinearLayout.LayoutParams(dp(26), dp(36)));

        TextView open = text("↗", 15, TEXT);
        open.setGravity(Gravity.CENTER);
        open.setOnClickListener(v -> submitAddress());
        addressBox.addView(open, new LinearLayout.LayoutParams(dp(24), dp(36)));

        addressPanel.addView(addressBox, new LinearLayout.LayoutParams(0, dp(38), 1));

        ImageView refresh = new ImageView(this);
        refresh.setImageResource(R.drawable.ic_refresh_ie_xp);
        refresh.setContentDescription("Atualizar página");
        refresh.setPadding(dp(9), dp(7), dp(9), dp(7));
        refresh.setOnClickListener(v -> {
            Tab t = current();
            if (t != null) t.session.reload();
        });
        LinearLayout.LayoutParams refreshParams = new LinearLayout.LayoutParams(dp(40), dp(38));
        refreshParams.leftMargin = dp(8);
        addressPanel.addView(refresh, refreshParams);

        root.addView(addressPanel);
        divider(root, 1);

        view = new GeckoView(this);
        view.setBackgroundColor(Color.WHITE);
        root.addView(view, new LinearLayout.LayoutParams(-1, 0, 1));

        downloadsPanel = new LinearLayout(this);
        downloadsPanel.setOrientation(LinearLayout.VERTICAL);
        downloadsPanel.setBackgroundColor(Color.WHITE);
        downloadsPanel.setVisibility(View.GONE);

        LinearLayout downloadsHeader = new LinearLayout(this);
        downloadsHeader.setGravity(Gravity.CENTER_VERTICAL);
        downloadsHeader.setPadding(dp(10), dp(6), dp(10), dp(6));
        downloadsHeader.setBackgroundColor(PANEL_BG);
        TextView downloadsTitle = text("Downloads", 13, TEXT);
        downloadsTitle.setTypeface(Typeface.DEFAULT_BOLD);
        downloadsHeader.addView(downloadsTitle, new LinearLayout.LayoutParams(0, -2, 1));
        TextView clearDone = text("Limpar finalizados", 11, XP_BLUE);
        clearDone.setPadding(dp(7),dp(8),dp(8),dp(8));
        clearDone.setOnClickListener(v -> confirmClearFinished());
        downloadsHeader.addView(clearDone);
        TextView closeDownloads = text("Ocultar", 12, XP_BLUE);
        closeDownloads.setOnClickListener(v -> hideDownloadsPanel());
        downloadsHeader.addView(closeDownloads);
        downloadsPanel.addView(downloadsHeader);
        downloadsSummary = text("", 11, SUBTLE);
        downloadsSummary.setPadding(dp(10), dp(4), dp(8), dp(4));
        downloadsPanel.addView(downloadsSummary);
        downloadsFilters = new LinearLayout(this);
        downloadsFilters.setGravity(Gravity.CENTER_VERTICAL);
        downloadsFilters.setBackgroundColor(PANEL_BG);
        downloadsPanel.addView(downloadsFilters, new LinearLayout.LayoutParams(-1, dp(36)));
        divider(downloadsPanel, 1);

        ScrollView downloadsScroll = new ScrollView(this);
        downloadsList = new LinearLayout(this);
        downloadsList.setOrientation(LinearLayout.VERTICAL);
        downloadsScroll.addView(downloadsList, new ScrollView.LayoutParams(-1, -2));
        downloadsPanel.addView(downloadsScroll, new LinearLayout.LayoutParams(-1, dp(220)));
        root.addView(downloadsPanel);

        divider(root, 1);

        LinearLayout footer = new LinearLayout(this);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.setPadding(dp(12), 0, dp(12), 0);
        footer.setBackgroundColor(LIGHT_BG);
        status = text("Concluído", 11, SUBTLE);
        footer.addView(status, new LinearLayout.LayoutParams(0, dp(26), 1));
        TextView addons = text("▧ Extensões", 11, XP_BLUE);
        addons.setOnClickListener(v -> showExtensions());
        footer.addView(addons, new LinearLayout.LayoutParams(-2, dp(26)));
        root.addView(footer);

        divider(root, 1);

        LinearLayout bottomBar = new LinearLayout(this);
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomBar.setGravity(Gravity.CENTER_VERTICAL);
        bottomBar.setBackgroundColor(PANEL_BG);
        bottomBar.setPadding(dp(4), dp(4), dp(4), dp(4));

        backIcon = navText(bottomBar, "←", "Voltar", () -> {
            Tab t = current();
            if (t != null && t.back) t.session.goBack();
        });
        forwardIcon = navText(bottomBar, "→", "Avançar", () -> {
            Tab t = current();
            if (t != null && t.forward) t.session.goForward();
        });
        navText(bottomBar, "⌂", "Início", () -> navigate(HOME));
        navImage(bottomBar, R.drawable.ic_download_xp, "Baixar", this::showDownloads);
        tabsBadge = navText(bottomBar, "▤", "Abas", this::showTabs);
        navText(bottomBar, "≡", "Menu", this::showMainMenu);
        menuAnchor = bottomBar.getChildAt(bottomBar.getChildCount() - 1);

        root.addView(bottomBar, new LinearLayout.LayoutParams(-1, dp(62)));
    }

    private LinearLayout navCell(LinearLayout parent, String caption, Runnable action) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setFocusable(true);
        cell.setClickable(true);
        cell.setContentDescription(caption);
        cell.setOnClickListener(v -> action.run());
        parent.addView(cell, new LinearLayout.LayoutParams(0, -1, 1));
        return cell;
    }

    private TextView navText(LinearLayout parent, String icon, String caption, Runnable action) {
        LinearLayout cell = navCell(parent, caption, action);
        TextView symbol = text(icon, 20, XP_BLUE);
        symbol.setTypeface(Typeface.DEFAULT_BOLD);
        symbol.setGravity(Gravity.CENTER);
        cell.addView(symbol, new LinearLayout.LayoutParams(-1, dp(24)));
        TextView name = text(caption, 7, TEXT);
        name.setGravity(Gravity.CENTER);
        cell.addView(name, new LinearLayout.LayoutParams(-1, dp(18)));
        return symbol;
    }

    private void navImage(LinearLayout parent, int drawable, String caption, Runnable action) {
        LinearLayout cell = navCell(parent, caption, action);
        ImageView icon = new ImageView(this);
        icon.setImageResource(drawable);
        icon.setPadding(dp(8), dp(2), dp(8), dp(2));
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        cell.addView(icon, new LinearLayout.LayoutParams(-1, dp(24)));
        TextView name = text(caption, 7, TEXT);
        name.setGravity(Gravity.CENTER);
        cell.addView(name, new LinearLayout.LayoutParams(-1, dp(18)));
    }

    private void showMainMenu() {
        PopupMenu p = new PopupMenu(this, menuAnchor, Gravity.END);
        item(p, "Nova aba", () -> addTab(HOME));
        item(p, "Fechar aba atual", () -> closeTab(active));
        item(p, "Atualizar", () -> { Tab t = current(); if (t != null) t.session.reload(); });
        item(p, "Copiar endereço", this::copyUrl);
        item(p, "Compartilhar página", this::share);
        item(p, "Favoritos", this::showBookmarks);
        item(p, "Adicionar/remover favorito", this::toggleBookmark);
        item(p, "Histórico pesquisável", this::showHistory);
        item(p, "Downloads", this::showDownloads);
        item(p, "Extensões", this::showExtensions);
        item(p, "Limpar histórico local", this::clearHistory);
        item(p, "Sobre", () -> new AlertDialog.Builder(this)
                .setTitle("Internet XP 1.0.15")
                .setMessage("Interface mobile XP compacta, com sugestões de histórico, lista interna de downloads e GeckoView. Projeto independente, sem vínculo com Microsoft ou Mozilla.")
                .setPositiveButton("OK", null).show());
        p.show();
    }

    private void item(PopupMenu p, String caption, Runnable run) {
        p.getMenu().add(caption).setOnMenuItemClickListener(m -> {
            run.run();
            return true;
        });
    }

    private Tab current() {
        return active >= 0 && active < tabs.size() ? tabs.get(active) : null;
    }

    private void setUrlBoxText(String text) {
        suppressSuggestionReload = true;
        urlBox.setText(text);
        urlBox.setSelection(urlBox.getText().length());
        suppressSuggestionReload = false;
    }

    private void hideKeyboard() {
        urlBox.clearFocus();
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(urlBox.getWindowToken(), 0);
    }

    private void submitAddress() {
        String value = urlBox.getText().toString();
        navigate(BrowserAddress.resolve(value));
        hideKeyboard();
    }

    private void navigate(String url) {
        Tab t = current();
        if (t != null) t.session.loadUri(url);
    }

    private Tab createTab(String restoredUrl, String restoredTitle) {
        GeckoSession session = new GeckoSession();
        Tab tab = new Tab(session);
        tab.url = TabSessionStore.restorable(restoredUrl) ? restoredUrl : HOME;
        tab.title = TabSessionStore.safeTitle(restoredTitle);
        session.setContentDelegate(new GeckoSession.ContentDelegate() {
            @Override public void onTitleChange(GeckoSession s, String title) {
                if (title != null && !title.trim().isEmpty()) {
                    tab.title = TabSessionStore.safeTitle(title);
                    HistoryRecords.updateTitle(historyDetails, tab.url, tab.title);
                    saveHistoryDetails();
                    saveTabs();
                }
                if (tab == current()) updateChrome();
            }
            @Override public void onCrash(GeckoSession s) {
                if (tab == current()) status.setText("Falha da página. Reabra a aba.");
            }
            @Override public void onExternalResponse(GeckoSession s, WebResponse response) {
                offerDownload(response);
            }
        });
        session.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
            @Override public void onLocationChange(GeckoSession s, String uri,
                    List<GeckoSession.PermissionDelegate.ContentPermission> perms, Boolean gesture) {
                if (uri == null) return;
                if (!uri.equals(tab.url)) tab.title = "Nova aba";
                tab.url = uri;
                if (tab == current()) {
                    if (!urlBox.hasFocus()) setUrlBoxText(uri.equals(HOME) ? "" : uri);
                    updateSecurity(uri);
                }
                if (uri.startsWith("http://") || uri.startsWith("https://")) rememberHistory(uri, tab.title);
                saveTabs();
            }
            @Override public void onCanGoBack(GeckoSession s, boolean b) {
                tab.back = b;
                if (tab == current()) updateArrows();
            }
            @Override public void onCanGoForward(GeckoSession s, boolean b) {
                tab.forward = b;
                if (tab == current()) updateArrows();
            }
        });
        session.setProgressDelegate(new GeckoSession.ProgressDelegate() {
            @Override public void onPageStart(GeckoSession s, String uri) {
                if (tab == current()) status.setText("Carregando…");
            }
            @Override public void onPageStop(GeckoSession s, boolean success) {
                if (tab == current()) status.setText(success ? "Concluído" : "Erro ao carregar");
            }
        });
        return tab;
    }

    private void addTab(String url) {
        if (tabs.size() >= TabSessionStore.MAX_TABS) {
            Toast.makeText(this, "Limite de 8 abas para poupar memória", Toast.LENGTH_SHORT).show();
            return;
        }
        tabs.add(createTab(url, "Nova aba"));
        selectTab(tabs.size() - 1);
    }

    private void selectTab(int index) {
        if (index < 0 || index >= tabs.size()) return;
        if (active == index && view.getSession() == tabs.get(index).session) return;
        GeckoSession old = view.releaseSession();
        if (old != null) old.setActive(false);
        active = index;
        Tab tab = current();
        if (!tab.opened) {
            tab.session.open(runtime);
            tab.opened = true;
        }
        tab.session.setActive(true);
        view.setSession(tab.session);
        if (!urlBox.hasFocus()) setUrlBoxText(tab.url.equals(HOME) ? "" : tab.url);
        updateSecurity(tab.url);
        updateChrome();
        // Abas restauradas inativas não abrem motor nem página até serem selecionadas.
        if (!tab.loaded) {
            tab.loaded = true;
            tab.session.loadUri(tab.url);
        }
        saveTabs();
    }

    private boolean restoreTabs() {
        try {
            JSONObject saved = new JSONObject(storage.getString(PREF_OPEN_TABS, "{}"));
            JSONArray storedTabs = saved.optJSONArray("tabs");
            if (storedTabs == null) return false;
            for (int i = 0; i < storedTabs.length() && tabs.size() < TabSessionStore.MAX_TABS; i++) {
                JSONObject item = storedTabs.optJSONObject(i);
                if (item == null) continue;
                String url = item.optString("url", HOME);
                if (!TabSessionStore.restorable(url)) continue;
                tabs.add(createTab(url, item.optString("title", "Nova aba")));
            }
            if (tabs.isEmpty()) return false;
            active = -1;
            selectTab(TabSessionStore.activeIndex(saved.optInt("active", 0), tabs.size()));
            return true;
        } catch (Exception ignored) {
            for (Tab tab : tabs) if (tab.opened) tab.session.close();
            tabs.clear();
            active = -1;
            return false;
        }
    }

    private void saveTabs() {
        if (storage == null || tabs.isEmpty()) return;
        JSONObject root = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            for (Tab tab : tabs) {
                JSONObject item = new JSONObject();
                item.put("url", TabSessionStore.restorable(tab.url) ? tab.url : HOME);
                item.put("title", TabSessionStore.safeTitle(tab.title));
                array.put(item);
            }
            root.put("tabs", array);
            root.put("active", TabSessionStore.activeIndex(active, tabs.size()));
            storage.edit().putString(PREF_OPEN_TABS, root.toString()).apply();
        } catch (Exception ignored) { }
    }

    private void updateSecurity(String url) {
        if (securityIcon == null) return;
        boolean secure = url != null && url.startsWith("https://");
        securityIcon.setImageResource(secure ? R.drawable.ic_lock_xp : R.drawable.ic_globe_xp);
        securityIcon.setColorFilter(secure ? SUCCESS : SUBTLE);
        securityIcon.setContentDescription(secure ? "Endereço HTTPS" : "Conexão não indicada como HTTPS");
    }

    private void updateChrome() {
        updateArrows();
        drawTabs();
    }

    private void closeTab(int index) {
        if (index < 0 || index >= tabs.size()) return;
        Tab closing = tabs.get(index);
        boolean isActive = index == active;
        if (isActive) {
            GeckoSession released = view.releaseSession();
            if (released != null) released.setActive(false);
        }
        tabs.remove(index);
        if (closing.opened) closing.session.close();
        if (tabs.isEmpty()) {
            active = -1;
            addTab(HOME);
        } else if (isActive) {
            active = -1;
            selectTab(Math.min(index, tabs.size() - 1));
        } else {
            if (index < active) active--;
            updateChrome();
            saveTabs();
        }
    }

    private void updateArrows() {
        Tab t = current();
        backIcon.setTextColor(t != null && t.back ? XP_BLUE : BORDER);
        forwardIcon.setTextColor(t != null && t.forward ? XP_BLUE : BORDER);
    }

    private void drawTabs() {
        if (tabsBadge == null) return;
        tabsBadge.setText("▤" + tabs.size());
        tabsBadge.setContentDescription(tabs.size() == 1 ? "1 aba" : tabs.size() + " abas");
    }

    private void showTabs() {
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(8), dp(8), dp(8), dp(8));
        ScrollView scroll = new ScrollView(this);
        scroll.addView(list);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Abas abertas • " + tabs.size())
                .setView(scroll)
                .setPositiveButton("+ Nova aba", (d, n) -> addTab(HOME))
                .setNegativeButton("Fechar", null)
                .create();
        populateTabCards(list, dialog);
        dialog.show();
    }

    private void populateTabCards(LinearLayout container, AlertDialog dialog) {
        container.removeAllViews();
        for (int i = 0; i < tabs.size(); i++) {
            final int index = i;
            Tab tab = tabs.get(i);
            LinearLayout card = new LinearLayout(this);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(10), dp(7), dp(3), dp(7));
            card.setBackground(rounded(index == active ? 0xffe5f0ff : Color.WHITE, BORDER, 2));

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            TextView title = text((index == active ? "● " : "▣ ") + trim(tab.title, 36), 14, TEXT);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            info.addView(title);
            TextView address = text(trim(tab.url.equals(HOME) ? "Página inicial" : tab.url, 56), 11, SUBTLE);
            address.setSingleLine(true);
            info.addView(address);
            info.setContentDescription("Abrir aba: " + tab.title);
            info.setOnClickListener(v -> {
                selectTab(index);
                dialog.dismiss();
            });
            card.addView(info, new LinearLayout.LayoutParams(0, dp(54), 1));
            TextView close = text("×", 24, XP_BLUE);
            close.setGravity(Gravity.CENTER);
            close.setContentDescription("Fechar aba: " + tab.title);
            close.setOnClickListener(v -> {
                closeTab(index);
                populateTabCards(container, dialog);
                dialog.setTitle("Abas abertas • " + tabs.size());
            });
            card.addView(close, new LinearLayout.LayoutParams(dp(48), dp(48)));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(7);
            container.addView(card, params);
        }
    }

    private String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }

    private void loadStringList(String key, ArrayList<String> list) {
        try {
            JSONArray array = new JSONArray(storage.getString(key, "[]"));
            for (int i = 0; i < array.length(); i++) list.add(array.getString(i));
        } catch (Exception ignored) { }
    }

    private void loadHistoryDetails() {
        historyDetails.clear();
        try {
            JSONArray data = new JSONArray(storage.getString(PREF_HISTORY_DETAILS, "[]"));
            for (int i = 0; i < data.length() && historyDetails.size() < HistoryRecords.LIMIT; i++) {
                JSONObject item = data.optJSONObject(i);
                if (item == null) continue;
                String url = item.optString("url", "");
                if (TabSessionStore.restorable(url) && !HOME.equals(url)) {
                    historyDetails.add(new HistoryRecords.Entry(url,
                            item.optString("title", ""), item.optLong("visitedAt", 0)));
                }
            }
        } catch (Exception ignored) { }
        // Migração conservadora: histórico antigo não recebe datas inventadas.
        for (String url : history) {
            boolean found = false;
            for (HistoryRecords.Entry entry : historyDetails) {
                if (entry.url.equals(url)) { found = true; break; }
            }
            if (!found && TabSessionStore.restorable(url) && !HOME.equals(url)) {
                historyDetails.add(new HistoryRecords.Entry(url, "", 0));
            }
        }
        while (historyDetails.size() > HistoryRecords.LIMIT) {
            historyDetails.remove(historyDetails.size() - 1);
        }
        saveHistoryDetails();
    }

    private void saveHistoryDetails() {
        if (storage == null) return;
        JSONArray result = new JSONArray();
        try {
            for (HistoryRecords.Entry entry : historyDetails) {
                JSONObject row = new JSONObject();
                row.put("url", entry.url);
                row.put("title", entry.title);
                row.put("visitedAt", entry.visitedAt);
                result.put(row);
            }
            storage.edit().putString(PREF_HISTORY_DETAILS, result.toString()).apply();
        } catch (Exception ignored) { }
    }

    private void saveStringList(String key, ArrayList<String> list) {
        storage.edit().putString(key, new JSONArray(list).toString()).apply();
    }

    private void loadDownloads() {
        downloads.clear();
        try {
            JSONArray array = new JSONArray(storage.getString(PREF_DOWNLOADS, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                DownloadItem item = new DownloadItem();
                item.id = object.optLong("id", -1);
                item.fileName = object.optString("fileName", "Arquivo");
                item.url = object.optString("url", "");
                item.mime = object.optString("mime", null);
                item.localUri = object.optString("localUri", null);
                item.status = object.optInt("status", 0);
                item.bytes = object.optLong("bytes", 0);
                item.total = object.optLong("total", -1);
                item.createdAt = object.optLong("createdAt", 0);
                item.reason = object.optInt("reason", 0);
                item.unmeteredOnly = object.optBoolean("unmeteredOnly", false);
                downloads.add(item);
            }
        } catch (Exception ignored) { }
    }

    private void saveDownloads() {
        JSONArray array = new JSONArray();
        for (DownloadItem item : downloads) {
            JSONObject object = new JSONObject();
            try {
                object.put("id", item.id);
                object.put("fileName", item.fileName);
                object.put("url", item.url);
                object.put("mime", item.mime);
                object.put("localUri", item.localUri);
                object.put("status", item.status);
                object.put("bytes", item.bytes);
                object.put("total", item.total);
                object.put("createdAt", item.createdAt);
                object.put("reason", item.reason);
                object.put("unmeteredOnly", item.unmeteredOnly);
                array.put(object);
            } catch (Exception ignored) { }
        }
        storage.edit().putString(PREF_DOWNLOADS, array.toString()).apply();
    }

    private void rememberHistory(String url, String pageTitle) {
        if (!TabSessionStore.restorable(url) || HOME.equals(url)) return;
        history.remove(url);
        history.add(0, url);
        while (history.size() > HistoryRecords.LIMIT) history.remove(history.size() - 1);
        HistoryRecords.visited(historyDetails, url, pageTitle, System.currentTimeMillis());
        saveStringList(PREF_HISTORY, history);
        saveHistoryDetails();
        refreshSuggestionAdapter(urlBox == null ? "" : urlBox.getText().toString());
    }

    private void refreshSuggestionAdapter(String query) {
        if (historyAdapter == null) return;
        ArrayList<String> items = buildSuggestions(query);
        historyAdapter.clear();
        historyAdapter.addAll(items);
        historyAdapter.notifyDataSetChanged();
    }

    private ArrayList<String> buildSuggestions(String query) {
        return HistorySuggestions.filter(history, bookmarks, query, 24);
    }

    private void toggleBookmark() {
        Tab t = current();
        if (t == null || !t.url.startsWith("http")) return;
        if (!bookmarks.remove(t.url)) bookmarks.add(0, t.url);
        saveStringList(PREF_BOOKMARKS, bookmarks);
        refreshSuggestionAdapter(urlBox.getText().toString());
    }

    private void showBookmarks() {
        if (bookmarks.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Favoritos")
                    .setMessage("Nenhum favorito salvo.")
                    .setPositiveButton("Adicionar atual", (d, n) -> toggleBookmark())
                    .setNegativeButton("Fechar", null)
                    .show();
            return;
        }
        String[] items = bookmarks.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Favoritos")
                .setItems(items, (d, n) -> navigate(items[n]))
                .setPositiveButton("Adicionar atual", (d, n) -> toggleBookmark())
                .setNegativeButton("Fechar", null)
                .show();
    }

    private void showHistory() {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(10), dp(6), dp(10), dp(4));
        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(14);
        search.setHint("Pesquisar título ou endereço");
        column.addView(search, new LinearLayout.LayoutParams(-1, dp(48)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(results);
        column.addView(scroll, new LinearLayout.LayoutParams(-1, dp(350)));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Histórico de navegação")
                .setView(column)
                .setPositiveButton("Limpar histórico", (d, n) -> clearHistory())
                .setNegativeButton("Fechar", null)
                .create();
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                populateHistoryRows(results, s.toString(), dialog);
            }
            @Override public void afterTextChanged(Editable editable) { }
        });
        populateHistoryRows(results, "", dialog);
        dialog.show();
        dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void populateHistoryRows(LinearLayout container, String query, AlertDialog dialog) {
        container.removeAllViews();
        ArrayList<HistoryRecords.Entry> entries = HistoryRecords.filter(historyDetails, query, 200);
        if (entries.isEmpty()) {
            TextView empty = text("Nenhuma página encontrada.", 13, SUBTLE);
            empty.setPadding(dp(8), dp(20), dp(8), dp(20));
            container.addView(empty);
            return;
        }
        String lastDate = "";
        for (HistoryRecords.Entry entry : entries) {
            String group = historyDay(entry.visitedAt);
            if (!group.equals(lastDate)) {
                TextView header = text(group, 12, XP_BLUE);
                header.setTypeface(Typeface.DEFAULT_BOLD);
                header.setPadding(dp(4), dp(8), dp(4), dp(4));
                container.addView(header);
                lastDate = group;
            }
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(7), dp(3), dp(2), dp(3));
            row.setBackground(rounded(Color.WHITE, BORDER, 1));
            LinearLayout labels = new LinearLayout(this);
            labels.setOrientation(LinearLayout.VERTICAL);
            TextView title = text(trim(entry.title.isEmpty() ? entry.url : entry.title, 52), 13, TEXT);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setSingleLine(true);
            labels.addView(title);
            TextView url = text(trim(entry.url, 60), 11, SUBTLE);
            url.setSingleLine(true);
            labels.addView(url);
            labels.setOnClickListener(v -> {
                navigate(entry.url);
                dialog.dismiss();
            });
            row.addView(labels, new LinearLayout.LayoutParams(0, dp(52), 1));
            TextView remove = text("×", 21, XP_BLUE);
            remove.setGravity(Gravity.CENTER);
            remove.setContentDescription("Remover do histórico: " + entry.url);
            remove.setOnClickListener(v -> {
                history.remove(entry.url);
                historyDetails.removeIf(item -> item.url.equals(entry.url));
                saveStringList(PREF_HISTORY, history);
                saveHistoryDetails();
                refreshSuggestionAdapter("");
                populateHistoryRows(container, query, dialog);
            });
            row.addView(remove, new LinearLayout.LayoutParams(dp(48), dp(48)));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(4);
            container.addView(row, params);
        }
    }

    private String historyDay(long timestamp) {
        if (timestamp <= 0) return "Data não registrada";
        Calendar today = Calendar.getInstance();
        Calendar visited = Calendar.getInstance();
        visited.setTimeInMillis(timestamp);
        if (today.get(Calendar.YEAR) == visited.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == visited.get(Calendar.DAY_OF_YEAR)) return "Hoje";
        today.add(Calendar.DAY_OF_YEAR, -1);
        if (today.get(Calendar.YEAR) == visited.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == visited.get(Calendar.DAY_OF_YEAR)) return "Ontem";
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(timestamp));
    }

    private void clearHistory() {
        new AlertDialog.Builder(this)
                .setTitle("Limpar histórico local?")
                .setMessage("Cookies e cache do GeckoView não serão apagados.")
                .setPositiveButton("Apagar", (d, n) -> {
                    history.clear();
                    historyDetails.clear();
                    saveStringList(PREF_HISTORY, history);
                    saveHistoryDetails();
                    refreshSuggestionAdapter("");
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void copyUrl() {
        Tab t = current();
        if (t != null) {
            ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE))
                    .setPrimaryClip(ClipData.newPlainText("Endereço", t.url));
        }
    }

    private void share() {
        Tab t = current();
        if (t == null || !t.url.startsWith("http")) return;
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, t.url);
        startActivity(Intent.createChooser(i, "Compartilhar"));
    }

    private void showDownloads() {
        if (downloadsPanel.getVisibility() != View.VISIBLE) downloadsPanel.setVisibility(View.VISIBLE);
        renderDownloadsPanel();
        scheduleDownloadRefresh();
    }

    private void hideDownloadsPanel() {
        downloadsPanel.setVisibility(View.GONE);
        uiHandler.removeCallbacks(downloadRefresh);
    }

    private void scheduleDownloadRefresh() {
        uiHandler.removeCallbacks(downloadRefresh);
        if (downloadsPanel != null && downloadsPanel.getVisibility() == View.VISIBLE && hasActiveDownloads()) {
            uiHandler.postDelayed(downloadRefresh, DOWNLOAD_REFRESH_MS);
        }
    }

    private boolean hasActiveDownloads() {
        for (DownloadItem item : downloads) if (isDownloadRunning(item.status)) return true;
        return false;
    }

    private void renderDownloadFilters() {
        downloadsFilters.removeAllViews();
        String[] labels = {"Todos", "Ativos", "Finalizados"};
        for (int i = 0; i < labels.length; i++) {
            final int filter = i;
            TextView button = text(labels[i], 11, i == downloadFilter ? XP_BLUE : SUBTLE);
            button.setTypeface(i == downloadFilter ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            button.setGravity(Gravity.CENTER);
            button.setBackground(rounded(i == downloadFilter ? Color.WHITE : PANEL_BG,
                    i == downloadFilter ? BORDER : 0, 0));
            button.setOnClickListener(v -> {
                downloadFilter = filter;
                renderDownloadsPanel();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(32), 1);
            lp.setMargins(dp(4), dp(2), dp(4), dp(2));
            downloadsFilters.addView(button, lp);
        }
    }

    private void renderDownloadsPanel() {
        refreshDownloadsState();
        renderDownloadFilters();
        downloadsList.removeAllViews();
        int activeCount = 0, doneCount = 0;
        for (DownloadItem item : downloads) {
            if (isDownloadRunning(item.status)) activeCount++;
            else doneCount++;
        }
        downloadsSummary.setText(downloads.size() + " registrados • " + activeCount + " ativos • " + doneCount + " finalizados");
        int visible = 0;
        for (DownloadItem item : downloads) {
            if (downloadFilter == 1 && !isDownloadRunning(item.status)) continue;
            if (downloadFilter == 2 && isDownloadRunning(item.status)) continue;
            if (visible++ >= 30) break;
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(10), dp(8), dp(10), dp(8));
            card.setBackground(rounded(Color.WHITE, BORDER, 0));

            TextView title = text(trim(item.fileName == null || item.fileName.isEmpty() ? "Arquivo" : item.fileName, 48), 12, TEXT);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            card.addView(title);

            TextView subtitle = text(buildDownloadSubtitle(item), 11, SUBTLE);
            subtitle.setPadding(0, dp(2), 0, 0);
            card.addView(subtitle);

            if (isDownloadRunning(item.status)) {
                ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
                progress.setMax(100);
                progress.setProgressTintList(ColorStateList.valueOf(XP_BLUE));
                progress.setProgressBackgroundTintList(ColorStateList.valueOf(0xffdbe2ed));
                long percent = DownloadDisplay.percent(item.bytes, item.total);
                progress.setIndeterminate(percent < 0);
                if (percent >= 0) progress.setProgress((int) percent);
                LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(-1, dp(9));
                progressParams.topMargin = dp(6);
                card.addView(progress, progressParams);
            }

            LinearLayout actions = new LinearLayout(this);
            actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            actions.setPadding(0, dp(4), 0, 0);
            if (item.status == DownloadManager.STATUS_SUCCESSFUL) {
                addDownloadAction(actions, "Abrir", () -> openDownload(item));
                addDownloadAction(actions, "Compartilhar", () -> shareDownload(item));
            } else if (isDownloadRunning(item.status)) {
                addDownloadAction(actions, "Cancelar", () -> confirmCancelDownload(item));
            } else if (item.status == DownloadManager.STATUS_FAILED || item.status == DOWNLOAD_CANCELLED) {
                addDownloadAction(actions, "Tentar novamente", () -> retryDownload(item));
            }
            addDownloadAction(actions, "Detalhes", () -> showDownloadDetails(item));
            if (!isDownloadRunning(item.status)) {
                addDownloadAction(actions, "Remover", () -> {
                    downloads.remove(item);
                    saveDownloads();
                    renderDownloadsPanel();
                });
            }
            HorizontalScrollView actionScroll = new HorizontalScrollView(this);
            actionScroll.setHorizontalScrollBarEnabled(false);
            actionScroll.addView(actions, new HorizontalScrollView.LayoutParams(-2, -2));
            card.addView(actionScroll);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.setMargins(dp(8),dp(5),dp(8),dp(2));
            downloadsList.addView(card, params);
        }
        if (visible == 0) {
            TextView empty = text(downloads.isEmpty() ? "Nenhum download registrado." : "Nenhum download neste filtro.", 12, SUBTLE);
            empty.setPadding(dp(12), dp(12), dp(12), dp(12));
            downloadsList.addView(empty);
        }
    }

    private void confirmClearFinished() {
        int finished = 0;
        for (DownloadItem item : downloads) if (!isDownloadRunning(item.status)) finished++;
        if (finished == 0) {
            Toast.makeText(this, "Nenhum registro finalizado", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this).setTitle("Limpar registros finalizados?")
                .setMessage("Remove " + finished + " registros da lista do Internet XP, sem apagar os arquivos baixados do celular.")
                .setPositiveButton("Limpar", (d,n) -> {
                    downloads.removeIf(item -> !isDownloadRunning(item.status));
                    saveDownloads();
                    renderDownloadsPanel();
                }).setNegativeButton("Cancelar", null).show();
    }

    private Uri uriForDownload(DownloadItem item) {
        DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        return manager == null ? null : manager.getUriForDownloadedFile(item.id);
    }

    private void shareDownload(DownloadItem item) {
        try {
            Uri uri = uriForDownload(item);
            if (uri == null || !"content".equalsIgnoreCase(uri.getScheme())) {
                Toast.makeText(this, "Arquivo não disponível para compartilhar", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(item.mime == null || item.mime.isEmpty() ? "application/octet-stream" : item.mime);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setClipData(ClipData.newUri(getContentResolver(), "Arquivo baixado", uri));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Compartilhar download"));
        } catch (RuntimeException error) { showError(error); }
    }

    private void showDownloadDetails(DownloadItem item) {
        String message = "Nome: " + item.fileName
                + "\nStatus: " + downloadStatusText(item.status)
                + "\nTamanho: " + formatBytes(item.total)
                + "\nBaixado: " + formatBytes(item.bytes)
                + "\nRede: " + (item.unmeteredOnly ? "Somente rede não tarifada" : "Todas as redes permitidas")
                + "\nOrigem: " + item.url;
        if (item.status == DownloadManager.STATUS_FAILED || item.status == DownloadManager.STATUS_PAUSED) {
            message += "\nMotivo: " + downloadReason(item);
        }
        new AlertDialog.Builder(this).setTitle("Detalhes do download").setMessage(message)
                .setPositiveButton("Fechar", null).show();
    }

    private void addDownloadAction(LinearLayout row, String label, Runnable callback) {
        TextView action = text(label, 11, XP_BLUE);
        action.setPadding(dp(9), dp(7), dp(5), dp(7));
        action.setOnClickListener(v -> callback.run());
        row.addView(action);
    }

    private boolean isDownloadRunning(int status) {
        return status == DownloadManager.STATUS_PENDING || status == DownloadManager.STATUS_RUNNING
                || status == DownloadManager.STATUS_PAUSED;
    }

    private void confirmCancelDownload(DownloadItem item) {
        new AlertDialog.Builder(this)
            .setTitle("Cancelar download?")
            .setMessage("O download será interrompido e o arquivo parcial poderá ser excluído pelo Android.")
            .setPositiveButton("Cancelar download", (dialog, which) -> {
                try {
                    DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    if (manager != null) manager.remove(item.id);
                    item.status = DOWNLOAD_CANCELLED;
                    item.bytes = 0;
                    item.total = -1;
                    item.localUri = null;
                    item.reason = 0;
                    item.lastSampleBytes = -1;
                    item.bytesPerSecond = -1;
                    saveDownloads();
                    renderDownloadsPanel();
                } catch (RuntimeException error) { showError(error); }
            })
            .setNegativeButton("Manter", null).show();
    }

    private DownloadManager.Request createDownloadRequest(String url, String fileName, boolean unmeteredOnly) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setAllowedOverMetered(!unmeteredOnly);
        request.setAllowedOverRoaming(false);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        return request;
    }

    private void retryDownload(DownloadItem item) {
        if (item.url == null || !(item.url.startsWith("https://") || item.url.startsWith("http://"))) {
            Toast.makeText(this, "Endereço do arquivo inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("Tentar novamente?")
            .setMessage("Será iniciado um novo download desde o começo, não uma retomada do arquivo parcial.")
            .setPositiveButton("Reiniciar", (dialog, which) -> {
                try {
                    DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    if (manager == null) throw new IllegalStateException("DownloadManager indisponível");
                    // Remove failed/cancelled request and its partial file before reusing its name.
                    if (item.id > 0) manager.remove(item.id);
                    DownloadManager.Request request = createDownloadRequest(item.url, item.fileName, item.unmeteredOnly);
                    item.id = manager.enqueue(request);
                    item.status = DownloadManager.STATUS_PENDING;
                    item.bytes = 0;
                    item.total = -1;
                    item.localUri = null;
                    item.reason = 0;
                    item.lastSampleBytes = -1;
                    item.bytesPerSecond = -1;
                    saveDownloads();
                    renderDownloadsPanel();
                } catch (RuntimeException error) { showError(error); }
            })
            .setNegativeButton("Voltar", null).show();
    }

    private String buildDownloadSubtitle(DownloadItem item) {
        String label = downloadStatusText(item.status);
        if (item.status == DownloadManager.STATUS_RUNNING) {
            long percent = DownloadDisplay.percent(item.bytes, item.total);
            String result = label + (percent >= 0 ? " • " + percent + "%" : "")
                    + " • " + formatBytes(item.bytes)
                    + (item.total > 0 ? " / " + formatBytes(item.total) : "");
            if (item.bytesPerSecond > 0) {
                result += " • " + DownloadDisplay.bytes(item.bytesPerSecond) + "/s";
                long left = DownloadDisplay.remainingSeconds(item.bytes, item.total, item.bytesPerSecond);
                if (left >= 0) result += " • ~" + DownloadDisplay.duration(left) + " restantes";
            }
            return result;
        }
        if (item.status == DownloadManager.STATUS_SUCCESSFUL)
            return label + (item.total > 0 ? " • " + formatBytes(item.total) : "");
        if (item.status == DownloadManager.STATUS_PAUSED || item.status == DownloadManager.STATUS_FAILED)
            return label + " • " + downloadReason(item);
        return label;
    }

    private String downloadReason(DownloadItem item) {
        int reason = item.reason;
        if (item.status == DownloadManager.STATUS_PAUSED) {
            switch (reason) {
                case DownloadManager.PAUSED_WAITING_TO_RETRY: return "aguardando nova tentativa";
                case DownloadManager.PAUSED_WAITING_FOR_NETWORK: return "aguardando conexão";
                case DownloadManager.PAUSED_QUEUED_FOR_WIFI: return "aguardando Wi-Fi";
                default: return "aguardando condições do sistema";
            }
        }
        if (reason >= 400 && reason <= 599) return "HTTP " + reason;
        switch (reason) {
            case DownloadManager.ERROR_INSUFFICIENT_SPACE: return "sem espaço de armazenamento";
            case DownloadManager.ERROR_FILE_ALREADY_EXISTS: return "arquivo já existe";
            case DownloadManager.ERROR_CANNOT_RESUME: return "transferência não retomável";
            case DownloadManager.ERROR_HTTP_DATA_ERROR: return "erro de transferência HTTP";
            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE: return "resposta HTTP não suportada";
            case DownloadManager.ERROR_TOO_MANY_REDIRECTS: return "redirecionamentos em excesso";
            default: return "falha reportada pelo Android";
        }
    }

    private String downloadStatusText(int status) {
        switch (status) {
            case DownloadManager.STATUS_PENDING: return "Na fila";
            case DownloadManager.STATUS_RUNNING: return "Baixando";
            case DownloadManager.STATUS_PAUSED: return "Pausado pelo sistema";
            case DownloadManager.STATUS_SUCCESSFUL: return "Concluído";
            case DownloadManager.STATUS_FAILED: return "Falhou";
            case DOWNLOAD_CANCELLED: return "Cancelado";
            default: return "Desconhecido";
        }
    }

    private String formatBytes(long bytes) { return DownloadDisplay.bytes(bytes); }

    private void refreshDownloadsState() {
        if (downloads.isEmpty()) return;
        DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (manager == null) return;
        long[] ids = new long[downloads.size()];
        for (int i = 0; i < downloads.size(); i++) ids[i] = downloads.get(i).id;
        Cursor cursor = null;
        boolean changed = false;
        try {
            cursor = manager.query(new DownloadManager.Query().setFilterById(ids));
            if (cursor == null) return;
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));
                DownloadItem item = findDownload(id);
                if (item == null) continue;
                int newStatus = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                long newBytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                long newTotal = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                int newReason = (newStatus == DownloadManager.STATUS_FAILED || newStatus == DownloadManager.STATUS_PAUSED)
                        ? cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON)) : 0;
                if (newStatus != item.status || newBytes != item.bytes || newTotal != item.total || newReason != item.reason) changed = true;
                long now = SystemClock.elapsedRealtime();
                if (newStatus == DownloadManager.STATUS_RUNNING && item.lastSampleBytes >= 0) {
                    long measured = DownloadDisplay.speed(item.lastSampleBytes, newBytes, now - item.lastSampleMs);
                    item.bytesPerSecond = measured;
                } else if (newStatus != DownloadManager.STATUS_RUNNING) {
                    item.bytesPerSecond = -1;
                }
                item.lastSampleBytes = newStatus == DownloadManager.STATUS_RUNNING ? newBytes : -1;
                item.lastSampleMs = now;
                item.reason = newReason;
                item.status = newStatus;
                item.bytes = newBytes;
                item.total = newTotal;
                int titleColumn = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE);
                if (titleColumn >= 0) {
                    String filename = cursor.getString(titleColumn);
                    if (filename != null && !filename.equals(item.fileName)) { item.fileName = filename; changed = true; }
                }
                int uriColumn = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                if (uriColumn >= 0) {
                    String local = cursor.getString(uriColumn);
                    if (local != null && !local.equals(item.localUri)) { item.localUri = local; changed = true; }
                }
                int mimeColumn = cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE);
                if (mimeColumn >= 0) {
                    String mime = cursor.getString(mimeColumn);
                    if (mime != null && !mime.equals(item.mime)) { item.mime = mime; changed = true; }
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
            if (changed) saveDownloads();
        }
    }

    private DownloadItem findDownload(long id) {
        for (DownloadItem item : downloads) if (item.id == id) return item;
        return null;
    }

    private void openDownload(DownloadItem item) {
        try {
            Uri uri = uriForDownload(item);
            if (uri == null || !"content".equalsIgnoreCase(uri.getScheme())) {
                Toast.makeText(this, "Arquivo não disponível para abrir", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, item.mime == null || item.mime.isEmpty() ? "*/*" : item.mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Abrir download"));
        } catch (Exception e) {
            showError(e);
        }
    }

    private void offerDownload(WebResponse response) {
        if (response.body != null) try { response.body.close(); } catch (Exception ignored) { }
        String uri = response.uri;
        if (uri == null || !(uri.startsWith("https://") || uri.startsWith("http://"))) {
            status.setText("Download não suportado");
            return;
        }
        String guessed = android.webkit.URLUtil.guessFileName(uri, null, response.headers == null ? null : response.headers.get("Content-Type"));
        CheckBox unmetered = new CheckBox(this);
        unmetered.setText("Somente redes não tarifadas (economizar dados)");
        unmetered.setTextSize(12);
        unmetered.setPadding(dp(10), dp(4), dp(8), dp(4));
        new AlertDialog.Builder(this)
                .setTitle("Baixar arquivo?")
                .setMessage("Origem: " + uri + "\n\nArquivos com login/cookies podem falhar nesta versão.")
                .setView(unmetered)
                .setPositiveButton("Baixar", (d, n) -> {
                    try {
                        DownloadManager.Request request = createDownloadRequest(uri, guessed, unmetered.isChecked());
                        long id = ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(request);
                        DownloadItem item = new DownloadItem();
                        item.id = id;
                        item.fileName = guessed;
                        item.url = uri;
                        item.mime = response.headers == null ? null : response.headers.get("Content-Type");
                        item.status = DownloadManager.STATUS_PENDING;
                        item.bytes = 0;
                        item.total = -1;
                        item.createdAt = System.currentTimeMillis();
                        item.unmeteredOnly = unmetered.isChecked();
                        downloads.add(0, item);
                        while (downloads.size() > 40) downloads.remove(downloads.size() - 1);
                        saveDownloads();
                        showDownloads();
                        status.setText("Download iniciado");
                    } catch (Exception e) {
                        showError(e);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void extensionPrompts() {
        runtime.getWebExtensionController().setPromptDelegate(new WebExtensionController.PromptDelegate() {
            @Override
            public GeckoResult<WebExtension.PermissionPromptResponse> onInstallPromptRequest(WebExtension ext, String[] permissions, String[] origins, String[] collected) {
                GeckoResult<WebExtension.PermissionPromptResponse> r = new GeckoResult<>();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Permitir extensão?")
                        .setMessage("Extensão: " + ext.id + "\nPermissões: " + Arrays.toString(permissions) +
                                "\nSites: " + Arrays.toString(origins) + "\nDados: " + Arrays.toString(collected))
                        .setPositiveButton("Permitir", (d, n) -> r.complete(new WebExtension.PermissionPromptResponse(true, false, false)))
                        .setNegativeButton("Recusar", (d, n) -> r.complete(new WebExtension.PermissionPromptResponse(false, false, false)))
                        .setOnCancelListener(d -> r.complete(new WebExtension.PermissionPromptResponse(false, false, false)))
                        .show();
                return r;
            }
            @Override public GeckoResult<AllowOrDeny> onOptionalPrompt(WebExtension ext, String[] permissions, String[] origins, String[] collected) { return reviewPermissions(ext, permissions, origins, collected); }
            @Override public GeckoResult<AllowOrDeny> onUpdatePrompt(WebExtension ext, String[] permissions, String[] origins, String[] collected) { return reviewPermissions(ext, permissions, origins, collected); }
        });
    }

    private GeckoResult<AllowOrDeny> reviewPermissions(WebExtension ext, String[] permissions, String[] origins, String[] collected) {
        GeckoResult<AllowOrDeny> r = new GeckoResult<>();
        new AlertDialog.Builder(this)
                .setTitle("Novas permissões: " + ext.id)
                .setMessage(Arrays.toString(permissions) + "\nSites: " + Arrays.toString(origins) + "\nDados: " + Arrays.toString(collected))
                .setPositiveButton("Permitir", (d, n) -> r.complete(AllowOrDeny.ALLOW))
                .setNegativeButton("Recusar", (d, n) -> r.complete(AllowOrDeny.DENY))
                .setOnCancelListener(d -> r.complete(AllowOrDeny.DENY))
                .show();
        return r;
    }

    private void showExtensions() {
        if (runtime == null) return;
        runtime.getWebExtensionController().list().accept(list -> runOnUiThread(() -> {
            String[] options = new String[list.size() + 2];
            options[0] = "+ Instalar XPI por URL HTTPS";
            options[1] = "Catálogo Mozilla Add-ons";
            for (int i = 0; i < list.size(); i++) {
                WebExtension x = list.get(i);
                options[i + 2] = (x.metaData.enabled ? "✓ " : "○ ") + (x.metaData.name == null ? x.id : x.metaData.name);
            }
            new AlertDialog.Builder(this)
                    .setTitle("Extensões Firefox")
                    .setItems(options, (d, n) -> {
                        if (n == 0) installExtension();
                        else if (n == 1) navigate("https://addons.mozilla.org/android/");
                        else extensionActions(list.get(n - 2));
                    })
                    .setNegativeButton("Fechar", null)
                    .show();
        }), e -> runOnUiThread(() -> showError(e)));
    }

    private void extensionActions(WebExtension x) {
        new AlertDialog.Builder(this)
                .setTitle(x.metaData.name == null ? x.id : x.metaData.name)
                .setItems(new String[]{x.metaData.enabled ? "Desativar" : "Ativar", "Desinstalar"}, (d, n) -> {
                    if (n == 1) {
                        new AlertDialog.Builder(this)
                                .setTitle("Desinstalar extensão?")
                                .setPositiveButton("Remover", (d2, n2) -> runtime.getWebExtensionController().uninstall(x)
                                        .accept(v -> runOnUiThread(this::showExtensions), e -> runOnUiThread(() -> showError(e))))
                                .setNegativeButton("Cancelar", null)
                                .show();
                    } else {
                        GeckoResult<WebExtension> r = x.metaData.enabled
                                ? runtime.getWebExtensionController().disable(x, WebExtensionController.EnableSource.USER)
                                : runtime.getWebExtensionController().enable(x, WebExtensionController.EnableSource.USER);
                        r.accept(v -> runOnUiThread(this::showExtensions), e -> runOnUiThread(() -> showError(e)));
                    }
                }).show();
    }

    private void installExtension() {
        AutoCompleteTextView link = new AutoCompleteTextView(this);
        link.setSingleLine();
        link.setHint("https://.../complemento.xpi");
        new AlertDialog.Builder(this)
                .setTitle("Instalar extensão Mozilla assinada")
                .setMessage("Cole o link direto HTTPS para o pacote XPI assinado.")
                .setView(link)
                .setPositiveButton("Instalar", (d, n) -> {
                    String uri = link.getText().toString().trim();
                    if (!uri.startsWith("https://")) {
                        Toast.makeText(this, "É obrigatório HTTPS", Toast.LENGTH_LONG).show();
                        return;
                    }
                    runtime.getWebExtensionController().install(uri)
                            .accept(x -> runOnUiThread(() -> {
                                        status.setText("Extensão instalada");
                                        showExtensions();
                                    }),
                                    e -> runOnUiThread(() -> showError(e)));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showError(Throwable e) {
        new AlertDialog.Builder(this)
                .setTitle("Falha")
                .setMessage(String.valueOf(e.getMessage()))
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (downloadsPanel != null && downloadsPanel.getVisibility() == View.VISIBLE) {
            hideDownloadsPanel();
            return;
        }
        Tab t = current();
        if (t != null && t.back) t.session.goBack();
        else new AlertDialog.Builder(this)
                .setTitle("Sair do Internet XP?")
                .setPositiveButton("Sair", (d, n) -> finish())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onPause() {
        uiHandler.removeCallbacks(downloadRefresh);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (downloadsPanel != null && downloadsPanel.getVisibility() == View.VISIBLE) {
            renderDownloadsPanel();
            scheduleDownloadRefresh();
        }
    }

    @Override
    protected void onStop() {
        saveTabs();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        uiHandler.removeCallbacks(downloadRefresh);
        if (view != null) view.releaseSession();
        for (Tab t : tabs) if (t.opened) t.session.close();
        tabs.clear();
        super.onDestroy();
    }
}
