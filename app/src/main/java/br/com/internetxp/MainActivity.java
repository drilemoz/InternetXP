package br.com.internetxp;

import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.app.role.RoleManager;
import android.os.Build;
import android.provider.Settings;
import android.content.res.ColorStateList;
import org.mozilla.geckoview.GeckoWebExecutor;
import org.mozilla.geckoview.WebRequest;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.HorizontalScrollView;
import android.widget.FrameLayout;
import android.view.WindowManager;
import java.net.URI;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mozilla.geckoview.AllowOrDeny;
import androidx.annotation.OptIn;
import org.mozilla.geckoview.ExperimentalGeckoViewApi;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.StorageController;
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
import java.util.LinkedHashSet;

/** Navegador Android com GeckoView e layout XP compacto para tela de telefone. */
public final class MainActivity extends Activity {
    private static final int XP_BLUE = 0xff0d68c8;
    private static final int LIGHT_BG = 0xfff1f2f6;
    private static final int PANEL_BG = 0xffedf0f5;
    private static final int BORDER = 0xffbcc8d6;
    private static final int TEXT = 0xff222222;
    private static final int SUBTLE = 0xff6c7885;
    private static final int SUCCESS = 0xff2d8a57;
    private static final int PRIVATE_PURPLE = 0xff663399;
    private static final String HOME = "about:blank";
    private static final String PREF_BOOKMARKS = "bookmarks";
    private static final String PREF_HISTORY = "history";
    private static final String PREF_DOWNLOADS = "downloads";
    private static final String PREF_HISTORY_DETAILS = "history_details";
    private static final String PREF_OPEN_TABS = "open_tabs_v1";
    private static final String PREF_TRACKING_PROTECTION = "tracking_protection_v1";
    private static final String PREF_HOME_PAGE = "home_page_v1";
    private static final int REQUEST_UPLOAD_FILES = 701;
    private static final int REQUEST_SITE_PERMISSIONS = 702;

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
    private LinearLayout browserRoot;
    private boolean fullScreen;
    private boolean trackingProtectionEnabled;
    private LinearLayout addressPanel;
    private GeckoResult<GeckoSession.PromptDelegate.PromptResponse> filePickerResult;
    private GeckoSession.PromptDelegate.FilePrompt filePickerPrompt;
    private Runnable permissionGranted;
    private Runnable permissionDenied;
    private TextView status;
    private TextView backIcon;
    private TextView forwardIcon;
    private TextView tabsBadge;
    private ImageView securityIcon;
    private ImageView faviconIcon;
    private HomePageView homeScreen;
    private FaviconRepository faviconRepository;
    private String homePageUrl = HOME;
    private ImageView dropdownIcon;
    private View menuAnchor;
    private LinearLayout downloadsPanel;
    private LinearLayout downloadsList;
    private TextView downloadsSummary;
    private LinearLayout downloadsFilters;
    private int downloadFilter = 0; // 0 Todos, 1 Ativos, 2 Finalizados
    private boolean suppressSuggestionReload;
    private static final int DOWNLOAD_CANCELLED = AppDownloadEngine.STATUS_CANCELLED;
    private AppDownloadEngine nativeDownloads;
    private DownloadItem exportTarget;
    private static final int REQUEST_EXPORT_DOWNLOAD = 703;
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
        boolean fullScreen;
        final boolean privateMode;
        GeckoSession.ProgressDelegate.SecurityInformation securityInfo;
        Tab(GeckoSession s, boolean privateMode) { session = s; this.privateMode = privateMode; }
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
        boolean privateDownload;
        long lastSampleBytes = -1;
        long lastSampleMs;
        long bytesPerSecond = -1;
    }

    private static final class MenuAction {
        final String icon;
        final String title;
        final String description;
        final Runnable action;
        MenuAction(String icon, String title, String description, Runnable action) {
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.action = action;
        }
    }

    private int dp(int n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    private int screenWidthDp() {
        return getResources().getConfiguration().screenWidthDp;
    }

    private int screenHeightDp() {
        return getResources().getConfiguration().screenHeightDp;
    }

    private boolean isCompactPhone() {
        int width = screenWidthDp();
        return width > 0 && width <= 360;
    }

    private boolean isTinyPhone() {
        int width = screenWidthDp();
        return width > 0 && width <= 320;
    }

    private boolean isShortPhone() {
        int height = screenHeightDp();
        return height > 0 && height <= 720;
    }

    private int toolbarHeightDp() {
        return isCompactPhone() ? 56 : 62;
    }

    private int downloadsPanelHeightDp() {
        return isShortPhone() ? 180 : (isCompactPhone() ? 200 : 220);
    }

    private String sanitizeHomePage(String address) {
        return HomePageSettings.normalize(address);
    }

    private String configuredHome() {
        return homePageUrl == null ? HOME : homePageUrl;
    }

    private boolean isHomeVisible() {
        Tab tab = current();
        return tab != null && HOME.equals(tab.url);
    }

    private void updateHomeScreen() {
        if (homeScreen == null) return;
        boolean show = isHomeVisible() && !fullScreen;
        homeScreen.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) homeScreen.showFavorites(bookmarks);
        if (faviconIcon != null) {
            Tab t = current();
            faviconIcon.setImageDrawable(null);
            faviconIcon.setVisibility(t != null && !HOME.equals(t.url) && FaviconRepository.origin(t.url) != null
                    ? View.VISIBLE : View.GONE);
            if (faviconIcon.getVisibility() == View.VISIBLE) faviconRepository.load(t.url, faviconIcon);
        }
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
        nativeDownloads = AppDownloadEngine.get(getApplicationContext());
        trackingProtectionEnabled = storage.getBoolean(PREF_TRACKING_PROTECTION, true);
        homePageUrl = sanitizeHomePage(storage.getString(PREF_HOME_PAGE, HOME));
        faviconRepository = new FaviconRepository();
        createLayout();
        refreshSuggestionAdapter("");
        try {
            if (runtime == null) runtime = GeckoRuntime.create(getApplicationContext());
            extensionPrompts();
            if (!restoreTabs()) addTab(configuredHome());
            handleIncomingLink(getIntent());
            // Novidades são exibidas apenas uma vez por versionCode e reaparecem ao atualizar.
            if (state == null && WhatsNewActivity.shouldShow(storage)) {
                startActivity(new Intent(this, WhatsNewActivity.class));
            }
        } catch (RuntimeException e) {
            showError(e);
        }
    }

    private void createLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(LIGHT_BG);
        browserRoot = root;
        setContentView(root);

        final boolean compactPhone = isCompactPhone();
        final int addressPanelHeight = compactPhone ? dp(34) : dp(38);
        final int addressInnerHeight = compactPhone ? dp(32) : dp(36);
        final int navIconHeight = compactPhone ? dp(22) : dp(24);
        final float navLabelSize = compactPhone ? 6.4f : 7f;

        // Painel de endereço: adaptativo para telas estreitas.
        addressPanel = new LinearLayout(this);
        addressPanel.setBackgroundColor(PANEL_BG);
        addressPanel.setGravity(Gravity.CENTER_VERTICAL);
        addressPanel.setPadding(dp(compactPhone ? 4 : 6), dp(compactPhone ? 4 : 6), dp(compactPhone ? 4 : 8), dp(compactPhone ? 4 : 6));

        LinearLayout addressBox = new LinearLayout(this);
        addressBox.setOrientation(LinearLayout.HORIZONTAL);
        addressBox.setGravity(Gravity.CENTER_VERTICAL);
        addressBox.setBackground(rounded(Color.WHITE, BORDER, 0));
        addressBox.setPadding(dp(compactPhone ? 6 : 8), 0, dp(compactPhone ? 2 : 4), 0);
        addressBox.setClickable(true);

        securityIcon = new ImageView(this);
        securityIcon.setImageResource(R.drawable.ic_globe_xp);
        securityIcon.setColorFilter(SUBTLE);
        securityIcon.setContentDescription("Informações de segurança da página");
        securityIcon.setOnClickListener(v -> showSiteInfo());
        securityIcon.setPadding(dp(2), dp(8), dp(2), dp(8));
        addressBox.addView(securityIcon, new LinearLayout.LayoutParams(dp(compactPhone ? 18 : 22), addressInnerHeight));
        faviconIcon = new ImageView(this);
        faviconIcon.setContentDescription("Ícone do site atual");
        faviconIcon.setVisibility(View.GONE);
        LinearLayout.LayoutParams faviconParams = new LinearLayout.LayoutParams(dp(16), dp(16));
        faviconParams.leftMargin = dp(3);
        addressBox.addView(faviconIcon, faviconParams);

        urlBox = new AutoCompleteTextView(this);
        urlBox.setSingleLine();
        urlBox.setFocusable(true);
        urlBox.setFocusableInTouchMode(true);
        // IE-like behavior: tapping the URL selects the entire address.
        // The very first typed character replaces the selection.
        urlBox.setSelectAllOnFocus(true);
        urlBox.setOnClickListener(v -> focusAddressBar(true));
        urlBox.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) focusAddressBar(true);
            return false;
        });
        addressBox.setOnClickListener(v -> focusAddressBar(true));
        urlBox.setTextSize(compactPhone ? 12 : 13);
        urlBox.setTextColor(TEXT);
        urlBox.setHint("https://www.exemplo.com");
        urlBox.setThreshold(0);
        urlBox.setDropDownHeight(dp(compactPhone ? 180 : 220));
        urlBox.setBackgroundColor(Color.TRANSPARENT);
        urlBox.setPadding(dp(compactPhone ? 4 : 6), 0, dp(2), 0);
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
                refreshSuggestionAdapter(urlBox.getText().toString());
                urlBox.post(() -> {
                    urlBox.selectAll();
                    if (current() == null || !current().privateMode) urlBox.showDropDown();
                });
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
        addressBox.addView(urlBox, new LinearLayout.LayoutParams(0, addressInnerHeight, 1));

        dropdownIcon = new ImageView(this);
        dropdownIcon.setImageResource(R.drawable.ic_dropdown_xp);
        dropdownIcon.setColorFilter(XP_BLUE);
        dropdownIcon.setPadding(dp(6), dp(9), dp(6), dp(9));
        dropdownIcon.setContentDescription("Abrir endereços do histórico");
        dropdownIcon.setOnClickListener(v -> {
            if (current() != null && current().privateMode) {
                Toast.makeText(this, "Histórico desativado na aba privada", Toast.LENGTH_SHORT).show();
                return;
            }
            refreshSuggestionAdapter(urlBox.getText().toString());
            if (urlBox.isPopupShowing()) urlBox.dismissDropDown();
            else {
                urlBox.requestFocus();
                urlBox.showDropDown();
            }
        });
        addressBox.addView(dropdownIcon, new LinearLayout.LayoutParams(dp(compactPhone ? 22 : 26), addressInnerHeight));

        TextView open = text("↗", 15, TEXT);
        open.setGravity(Gravity.CENTER);
        open.setOnClickListener(v -> submitAddress());
        addressBox.addView(open, new LinearLayout.LayoutParams(dp(compactPhone ? 18 : 24), addressInnerHeight));

        addressPanel.addView(addressBox, new LinearLayout.LayoutParams(0, addressPanelHeight, 1));

        ImageView refresh = new ImageView(this);
        refresh.setImageResource(R.drawable.ic_refresh_ie_xp);
        refresh.setContentDescription("Atualizar página");
        refresh.setPadding(dp(9), dp(7), dp(9), dp(7));
        refresh.setOnClickListener(v -> {
            Tab t = current();
            if (t != null) t.session.reload();
        });
        LinearLayout.LayoutParams refreshParams = new LinearLayout.LayoutParams(dp(compactPhone ? 30 : 40), addressPanelHeight);
        refreshParams.leftMargin = dp(compactPhone ? 4 : 8);
        addressPanel.addView(refresh, refreshParams);

        root.addView(addressPanel);
        divider(root, 1);

        FrameLayout webArea = new FrameLayout(this);
        view = new GeckoView(this);
        view.setBackgroundColor(Color.WHITE);
        webArea.addView(view, new FrameLayout.LayoutParams(-1, -1));
        homeScreen = new HomePageView(this, faviconRepository, new HomePageView.Actions() {
            @Override public void open(String address) {
                navigate(address);
                hideKeyboard();
            }
            @Override public void addFavorite() { showAddQuickAccess(); }
        });
        homeScreen.setVisibility(View.GONE);
        webArea.addView(homeScreen, new FrameLayout.LayoutParams(-1, -1));
        root.addView(webArea, new LinearLayout.LayoutParams(-1, 0, 1));

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
        downloadsPanel.addView(downloadsScroll, new LinearLayout.LayoutParams(-1, dp(downloadsPanelHeightDp())));
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
        bottomBar.setPadding(dp(compactPhone ? 2 : 4), dp(compactPhone ? 2 : 4), dp(compactPhone ? 2 : 4), dp(compactPhone ? 2 : 4));

        backIcon = navText(bottomBar, "←", "Voltar", () -> {
            Tab t = current();
            if (t != null && t.back) t.session.goBack();
        });
        forwardIcon = navText(bottomBar, "→", "Avançar", () -> {
            Tab t = current();
            if (t != null && t.forward) t.session.goForward();
        });
        navText(bottomBar, "⌂", "Início", () -> navigate(configuredHome()));
        navImage(bottomBar, R.drawable.ic_download_xp, "Baixar", this::showDownloads);
        tabsBadge = navText(bottomBar, "▤", "Abas", this::showTabs);
        navText(bottomBar, "≡", "Menu", this::showMainMenu);
        menuAnchor = bottomBar.getChildAt(bottomBar.getChildCount() - 1);

        root.addView(bottomBar, new LinearLayout.LayoutParams(-1, dp(toolbarHeightDp())));
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
        boolean compactPhone = isCompactPhone();
        TextView symbol = text(icon, compactPhone ? 18 : 20, XP_BLUE);
        symbol.setTypeface(Typeface.DEFAULT_BOLD);
        symbol.setGravity(Gravity.CENTER);
        cell.addView(symbol, new LinearLayout.LayoutParams(-1, isCompactPhone() ? dp(20) : dp(24)));
        TextView name = text(caption, isCompactPhone() ? 6.4f : 7f, TEXT);
        name.setGravity(Gravity.CENTER);
        cell.addView(name, new LinearLayout.LayoutParams(-1, dp(18)));
        return symbol;
    }

    private void navImage(LinearLayout parent, int drawable, String caption, Runnable action) {
        LinearLayout cell = navCell(parent, caption, action);
        ImageView icon = new ImageView(this);
        icon.setImageResource(drawable);
        icon.setPadding(dp(isCompactPhone() ? 6 : 8), dp(2), dp(isCompactPhone() ? 6 : 8), dp(2));
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        cell.addView(icon, new LinearLayout.LayoutParams(-1, isCompactPhone() ? dp(20) : dp(24)));
        TextView name = text(caption, isCompactPhone() ? 6.4f : 7f, TEXT);
        name.setGravity(Gravity.CENTER);
        cell.addView(name, new LinearLayout.LayoutParams(-1, dp(18)));
    }

    private void showMainMenu() {
        ArrayList<MenuAction> primary = new ArrayList<>();
        ArrayList<MenuAction> secondary = new ArrayList<>();

        primary.add(new MenuAction("＋", "Nova aba", "Abrir uma nova aba.", () -> addTab(configuredHome())));
        primary.add(new MenuAction("◌", "Privada", "Abrir uma aba privada.", () -> addPrivateTab(configuredHome())));
        primary.add(new MenuAction("★", "Favoritos", "Seus sites salvos.", this::showBookmarks));
        primary.add(new MenuAction("🕘", "Histórico", "Páginas visitadas.", this::showHistory));
        primary.add(new MenuAction("↓", "Downloads", "Transferências do app.", this::showDownloads));
        primary.add(new MenuAction("⚙", "Configurações", "Página inicial e atalhos.", this::showBrowserSettings));

        secondary.add(new MenuAction("↻", "Atualizar página", "Recarregue o site atual caso algo não tenha carregado direito.", () -> { Tab t = current(); if (t != null) t.session.reload(); }));
        secondary.add(new MenuAction("🔒", "Privacidade e dados", "Limpe cookies, cache, permissões e ajuste o modo de proteção.", this::showPrivacyMenu));
        secondary.add(new MenuAction("🧩", "Extensões", "Gerencie complementos do Firefox compatíveis com o Internet XP.", this::showExtensions));
        secondary.add(new MenuAction("⇪", "Compartilhar página", "Copie ou compartilhe o endereço aberto em outros aplicativos.", this::share));
        secondary.add(new MenuAction("ⓘ", "Novidades desta versão", "Consulte a tela com o resumo estilizado das últimas alterações.", () -> startActivity(new Intent(this, WhatsNewActivity.class))));
        secondary.add(new MenuAction("ℹ", "Sobre", "Informações da versão, motor e principais recursos do navegador.", this::showAbout));
        secondary.add(new MenuAction("🌐", "Navegador padrão", "Peça ao Android para oferecer o Internet XP ao abrir links HTTP e HTTPS.", this::requestDefaultBrowserRole));

        showMainMenuHubDialog("Internet XP", "Menu rápido com aparência mais viva, atalhos principais em destaque e ferramentas extras abaixo.", primary, secondary);
    }

    private void showMainMenuHubDialog(String title, String subtitle, List<MenuAction> primary, List<MenuAction> secondary) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setBackground(rounded(LIGHT_BG, BORDER, 18));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(16), dp(14), dp(16), dp(12));
        GradientDrawable headerBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xff3a8df3, XP_BLUE, 0xff0b58aa});
        headerBg.setCornerRadii(new float[]{dp(18), dp(18), dp(18), dp(18), 0, 0, 0, 0});
        header.setBackground(headerBg);
        TextView titleView = text("◉  " + title, 19, Color.WHITE);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        header.addView(titleView);
        TextView subtitleView = text(subtitle, 11, 0xffe6effa);
        subtitleView.setPadding(0, dp(4), 0, 0);
        header.addView(subtitleView);
        wrapper.addView(header);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(isCompactPhone() ? 10 : 12), dp(12), dp(isCompactPhone() ? 10 : 12), dp(10));
        scroll.addView(content);

        TextView quick = text("Acesso rápido", 12, XP_BLUE);
        quick.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(quick);
        TextView quickSub = text("Os atalhos mais usados ficam logo no topo.", 10.5f, SUBTLE);
        quickSub.setPadding(0, dp(2), 0, dp(8));
        content.addView(quickSub);

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        content.addView(grid);
        int cols = isTinyPhone() ? 2 : 3;
        for (int i = 0; i < primary.size(); i += cols) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
            rowParams.bottomMargin = dp(8);
            grid.addView(row, rowParams);
            for (int c = 0; c < cols; c++) {
                int index = i + c;
                if (index >= primary.size()) {
                    View spacer = new View(this);
                    row.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1));
                    continue;
                }
                MenuAction action = primary.get(index);
                row.addView(createHubTile(action, holder), new LinearLayout.LayoutParams(0, -2, 1));
                if (c < cols - 1) {
                    View gap = new View(this);
                    row.addView(gap, new LinearLayout.LayoutParams(dp(isCompactPhone() ? 6 : 8), 1));
                }
            }
        }

        TextView tools = text("Mais ferramentas", 12, XP_BLUE);
        tools.setTypeface(Typeface.DEFAULT_BOLD);
        tools.setPadding(0, dp(6), 0, 0);
        content.addView(tools);
        TextView toolsSub = text("Ações extras do navegador em uma lista mais compacta.", 10.5f, SUBTLE);
        toolsSub.setPadding(0, dp(2), 0, dp(8));
        content.addView(toolsSub);

        final AlertDialog[] holder = new AlertDialog[1];
        for (MenuAction action : secondary) {
            LinearLayout row = createCompactMenuRow(action, holder);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(7);
            content.addView(row, params);
        }

        int contentHeight = Math.min(isCompactPhone() ? 560 : 600, Math.max(isCompactPhone() ? 390 : 420, (int) (screenHeightDp() * (isCompactPhone() ? .62f : .58f))));
        wrapper.addView(scroll, new LinearLayout.LayoutParams(-1, dp(contentHeight)));

        TextView close = text("Fechar", 13, XP_BLUE);
        close.setTypeface(Typeface.DEFAULT_BOLD);
        close.setGravity(Gravity.CENTER);
        close.setPadding(dp(16), dp(12), dp(16), dp(14));
        close.setBackground(rounded(Color.WHITE, BORDER, 0));
        wrapper.addView(close, new LinearLayout.LayoutParams(-1, -2));

        AlertDialog dialog = new AlertDialog.Builder(this).setView(wrapper).create();
        holder[0] = dialog;
        close.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        styleBottomMenuDialog(dialog);
        animateDialogOpen(wrapper, true);
    }

    private LinearLayout createHubTile(MenuAction action, AlertDialog[] holder) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER_HORIZONTAL);
        tile.setPadding(dp(isCompactPhone() ? 6 : 8), dp(isCompactPhone() ? 8 : 10), dp(isCompactPhone() ? 6 : 8), dp(isCompactPhone() ? 8 : 10));
        tile.setBackground(rounded(Color.WHITE, BORDER, 14));
        tile.setClickable(true);
        tile.setFocusable(true);

        TextView icon = text(action.icon, isCompactPhone() ? 18 : 20, Color.WHITE);
        icon.setTypeface(Typeface.DEFAULT_BOLD);
        icon.setGravity(Gravity.CENTER);
        GradientDrawable iconBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0xff3993ff, XP_BLUE});
        iconBg.setCornerRadius(dp(12));
        icon.setBackground(iconBg);
        tile.addView(icon, new LinearLayout.LayoutParams(dp(isCompactPhone() ? 34 : 38), dp(isCompactPhone() ? 34 : 38)));

        TextView title = text(action.title, isCompactPhone() ? 10.5f : 11.5f, TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setPadding(0, dp(8), 0, 0);
        tile.addView(title);

        TextView desc = text(action.description, isCompactPhone() ? 8.8f : 9.5f, SUBTLE);
        desc.setGravity(Gravity.CENTER_HORIZONTAL);
        desc.setPadding(dp(2), dp(2), dp(2), 0);
        desc.setMaxLines(isCompactPhone() ? 1 : 2);
        tile.addView(desc);

        tile.setOnClickListener(v -> {
            if (holder != null && holder[0] != null) holder[0].dismiss();
            action.action.run();
        });
        return tile;
    }

    private LinearLayout createCompactMenuRow(MenuAction action, AlertDialog[] holder) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(isCompactPhone() ? 9 : 10), dp(isCompactPhone() ? 8 : 9), dp(isCompactPhone() ? 9 : 10), dp(isCompactPhone() ? 8 : 9));
        row.setBackground(rounded(Color.WHITE, BORDER, 12));
        row.setClickable(true);
        row.setFocusable(true);

        TextView icon = text(action.icon, 15, Color.WHITE);
        icon.setTypeface(Typeface.DEFAULT_BOLD);
        icon.setGravity(Gravity.CENTER);
        GradientDrawable iconBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0xff5ea7ff, XP_BLUE});
        iconBg.setCornerRadius(dp(10));
        icon.setBackground(iconBg);
        row.addView(icon, new LinearLayout.LayoutParams(dp(30), dp(30)));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(10), 0, dp(8), 0);
        TextView rowTitle = text(action.title, isCompactPhone() ? 11.5f : 12.5f, TEXT);
        rowTitle.setTypeface(Typeface.DEFAULT_BOLD);
        labels.addView(rowTitle);
        TextView rowDesc = text(action.description, isCompactPhone() ? 9.8f : 10.5f, SUBTLE);
        rowDesc.setPadding(0, dp(1), 0, 0);
        rowDesc.setMaxLines(2);
        labels.addView(rowDesc);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));

        TextView chevron = text("›", 18, XP_BLUE);
        chevron.setGravity(Gravity.CENTER);
        row.addView(chevron, new LinearLayout.LayoutParams(dp(18), dp(18)));

        row.setOnClickListener(v -> {
            if (holder != null && holder[0] != null) holder[0].dismiss();
            action.action.run();
        });
        return row;
    }

    private void styleBottomMenuDialog(AlertDialog dialog) {
        if (dialog.getWindow() == null) return;
        int width = dp(Math.min(Math.max(isCompactPhone() ? 334 : 320, screenWidthDp() - (isCompactPhone() ? 10 : 18)), 450));
        dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setDimAmount(0.35f);
        dialog.getWindow().setWindowAnimations(android.R.style.Animation_Dialog);
    }

    private void animateDialogOpen(View view, boolean fromBottom) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setTranslationY(dp(fromBottom ? 18 : 10));
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(180)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void showModuleMenuDialog(String title, String subtitle, List<MenuAction> actions) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setBackground(rounded(LIGHT_BG, BORDER, 14));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(14), dp(13), dp(14), dp(11));
        GradientDrawable moduleHeader = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0xff4b98f6, XP_BLUE});
        moduleHeader.setCornerRadii(new float[]{dp(14), dp(14), dp(14), dp(14), 0, 0, 0, 0});
        header.setBackground(moduleHeader);
        TextView titleView = text(title, 18, Color.WHITE);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        header.addView(titleView);
        TextView subtitleView = text(subtitle, 11.5f, 0xffeef5ff);
        subtitleView.setPadding(0, dp(4), 0, 0);
        header.addView(subtitleView);
        wrapper.addView(header);

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(isCompactPhone() ? 8 : 10), dp(10), dp(isCompactPhone() ? 8 : 10), dp(6));
        scroll.addView(list);
        int contentHeight = Math.min(isCompactPhone() ? 460 : (isShortPhone() ? 360 : 420), Math.max(isCompactPhone() ? 280 : 260, (int) (screenHeightDp() * (isCompactPhone() ? .50f : .48f))));
        wrapper.addView(scroll, new LinearLayout.LayoutParams(-1, dp(contentHeight)));

        final AlertDialog[] holder = new AlertDialog[1];
        for (MenuAction action : actions) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), dp(10), dp(10), dp(10));
            row.setBackground(rounded(Color.WHITE, BORDER, 10));
            row.setClickable(true);
            row.setFocusable(true);

            TextView icon = text(action.icon, 18, Color.WHITE);
            icon.setTypeface(Typeface.DEFAULT_BOLD);
            icon.setGravity(Gravity.CENTER);
            icon.setBackground(rounded(XP_BLUE, 0, 10));
            row.addView(icon, new LinearLayout.LayoutParams(dp(38), dp(38)));

            LinearLayout labels = new LinearLayout(this);
            labels.setOrientation(LinearLayout.VERTICAL);
            labels.setPadding(dp(10), 0, dp(10), 0);
            TextView rowTitle = text(action.title, 13, TEXT);
            rowTitle.setTypeface(Typeface.DEFAULT_BOLD);
            labels.addView(rowTitle);
            TextView rowDesc = text(action.description, 11, SUBTLE);
            rowDesc.setPadding(0, dp(2), 0, 0);
            rowDesc.setMaxLines(3);
            labels.addView(rowDesc);
            row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));

            TextView chevron = text("›", 18, XP_BLUE);
            chevron.setGravity(Gravity.CENTER);
            row.addView(chevron, new LinearLayout.LayoutParams(dp(22), dp(22)));

            row.setOnClickListener(v -> {
                if (holder[0] != null) holder[0].dismiss();
                action.action.run();
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(8);
            list.addView(row, params);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(wrapper)
                .setNegativeButton("Fechar", null)
                .create();
        holder[0] = dialog;
        dialog.show();
        compactDialog(dialog);
        animateDialogOpen(column, false);
        animateDialogOpen(wrapper, false);
    }

    private void compactDialog(AlertDialog dialog) {
        if (dialog.getWindow() == null) return;
        int width = dp(Math.min(Math.max(isCompactPhone() ? 324 : 304, screenWidthDp() - (isCompactPhone() ? 16 : 26)), 430));
        dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setDimAmount(0.28f);
        dialog.getWindow().setWindowAnimations(android.R.style.Animation_Dialog);
    }

    private void showBrowserSettings() {
        ArrayList<MenuAction> actions = new ArrayList<>();
        actions.add(new MenuAction("⌂", "Página inicial: Internet XP", HOME.equals(configuredHome())
                ? "Ativa. Página em branco com pesquisa central e atalhos." : "Definir a página inicial padrão do Internet XP.",
                () -> setHomePage(HOME)));
        actions.add(new MenuAction("↗", "Escolher um site inicial", "Informe o endereço de uma página para abrir ao usar Início ou Nova aba.", this::askCustomHomePage));
        actions.add(new MenuAction("★", "Adicionar acesso rápido", "Salve um site favorito para aparecer abaixo da pesquisa na página inicial.", this::showAddQuickAccess));
        actions.add(new MenuAction("⚙", "Ver página inicial", "Abra a página inicial definida nas configurações.", () -> navigate(configuredHome())));
        showModuleMenuDialog("Configurações", "Personalize o início da navegação e os atalhos do Internet XP.", actions);
    }

    private void setHomePage(String chosen) {
        homePageUrl = sanitizeHomePage(chosen);
        storage.edit().putString(PREF_HOME_PAGE, homePageUrl).apply();
        Toast.makeText(this, HOME.equals(homePageUrl) ? "Página inicial Internet XP definida" : "Site inicial definido", Toast.LENGTH_SHORT).show();
    }

    private void askCustomHomePage() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(14);
        input.setHint("https://exemplo.com");
        if (!HOME.equals(configuredHome())) input.setText(configuredHome());
        LinearLayout content = styledDialogContent("Página inicial personalizada", "Escolha o site aberto pelo botão Início.");
        content.addView(input, new LinearLayout.LayoutParams(-1, dp(52)));
        AlertDialog d = new AlertDialog.Builder(this).setView(content)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String entered = input.getText().toString().trim();
                    String resolved = BrowserAddress.resolve(entered);
                    if (entered.isEmpty() || HOME.equals(HomePageSettings.normalize(entered))) {
                        Toast.makeText(this, "Informe o endereço válido de um site", Toast.LENGTH_LONG).show();
                    } else setHomePage(resolved);
                }).setNegativeButton("Cancelar", null).create();
        d.show(); compactDialog(d);
    }

    private void showAddQuickAccess() {
        if (current() != null && current().privateMode) {
            Toast.makeText(this, "Adicione favoritos numa aba normal", Toast.LENGTH_SHORT).show();
            return;
        }
        LinearLayout content = styledDialogContent("Adicionar favorito", "Salve um endereço para aparecer na página inicial.");
        EditText input = new EditText(this);
        input.setHint("https://exemplo.com");
        input.setSingleLine(true);
        input.setTextSize(14);
        Tab t = current();
        if (t != null && t.url.startsWith("http")) input.setText(t.url);
        content.addView(input, new LinearLayout.LayoutParams(-1, dp(52)));
        AlertDialog d = new AlertDialog.Builder(this).setView(content).setPositiveButton("Adicionar", (dialog, which) -> {
            String address = BrowserAddress.resolve(input.getText().toString());
            if (HOME.equals(HomePageSettings.normalize(input.getText().toString()))) {
                Toast.makeText(this, "Endereço inválido", Toast.LENGTH_LONG).show();
                return;
            }
            bookmarks.remove(address);
            bookmarks.add(0, address);
            saveStringList(PREF_BOOKMARKS, bookmarks);
            refreshSuggestionAdapter("");
            updateHomeScreen();
        }).setNegativeButton("Cancelar", null).create();
        d.show(); compactDialog(d);
    }

    private LinearLayout styledDialogContent(String title, String subtitle) {
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(12), dp(12), dp(12), dp(10));
        body.setBackground(rounded(LIGHT_BG, BORDER, 14));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(14), dp(12), dp(14), dp(11));
        GradientDrawable headerBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xff4e9cf7, XP_BLUE, 0xff0d5fae});
        headerBg.setCornerRadii(new float[]{dp(12), dp(12), dp(12), dp(12), dp(10), dp(10), dp(10), dp(10)});
        header.setBackground(headerBg);
        TextView heading = text(title, 17, Color.WHITE);
        heading.setTypeface(Typeface.DEFAULT_BOLD);
        header.addView(heading);
        TextView explanation = text(subtitle, 11, 0xffedf4ff);
        explanation.setPadding(0, dp(4), 0, 0);
        header.addView(explanation);
        body.addView(header, new LinearLayout.LayoutParams(-1, -2));

        TextView spacer = text("", 1, Color.TRANSPARENT);
        spacer.setPadding(0, dp(10), 0, 0);
        body.addView(spacer);
        return body;
    }

    private void showAbout() {
        LinearLayout content = styledDialogContent("Internet XP " + ReleaseNotes.VERSION_NAME, "Navegador com visual clássico, menu XP moderno e motor Mozilla GeckoView.");
        for (String line : new String[]{"Motor de navegação: GeckoView 158", "Android 8.0 ou superior", "Página inicial com pesquisa e atalhos", "Extensões, downloads, abas e privacidade", "Projeto independente, sem vínculo com Mozilla ou Microsoft"}) {
            TextView detail = text("•  " + line, 12, TEXT);
            detail.setPadding(dp(6), dp(7), dp(4), dp(7));
            detail.setBackground(rounded(Color.WHITE, BORDER, 10));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(8);
            content.addView(detail, params);
        }
        AlertDialog dialog = new AlertDialog.Builder(this).setView(content).setPositiveButton("Fechar", null).create();
        dialog.show(); compactDialog(dialog); animateDialogOpen(content, false);
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

    private void focusAddressBar(boolean showSuggestions) {
        if (urlBox == null) return;
        urlBox.setFocusable(true);
        urlBox.setFocusableInTouchMode(true);
        urlBox.requestFocus();
        urlBox.post(() -> {
            urlBox.selectAll();
            urlBox.setSelection(0, urlBox.getText().length());
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(urlBox, InputMethodManager.SHOW_IMPLICIT);
            refreshSuggestionAdapter(urlBox.getText().toString());
            if (showSuggestions && (current() == null || !current().privateMode)) urlBox.showDropDown();
        });
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
        return createTab(restoredUrl, restoredTitle, false);
    }

    private Tab createTab(String restoredUrl, String restoredTitle, boolean privateMode) {
        GeckoSessionSettings settings = new GeckoSessionSettings.Builder()
                .usePrivateMode(privateMode)
                .useTrackingProtection(trackingProtectionEnabled)
                .suspendMediaWhenInactive(true)
                .build();
        GeckoSession session = new GeckoSession(settings);
        Tab tab = new Tab(session, privateMode);
        tab.url = TabSessionStore.restorable(restoredUrl) ? restoredUrl : HOME;
        tab.title = TabSessionStore.safeTitle(restoredTitle);
        session.setContentDelegate(new GeckoSession.ContentDelegate() {
            @Override public void onTitleChange(GeckoSession s, String title) {
                if (title != null && !title.trim().isEmpty()) {
                    tab.title = TabSessionStore.safeTitle(title);
                    if (!tab.privateMode) {
                        HistoryRecords.updateTitle(historyDetails, tab.url, tab.title);
                        saveHistoryDetails();
                    }
                    saveTabs();
                }
                if (tab == current()) updateChrome();
            }
            @Override public void onFullScreen(GeckoSession s, boolean entered) {
                tab.fullScreen = entered;
                if (tab == current()) applyFullScreen(entered);
            }
            @Override public void onCrash(GeckoSession s) {
                if (tab == current()) status.setText("Falha da página. Reabra a aba.");
            }
            @Override public void onExternalResponse(GeckoSession s, WebResponse response) {
                offerDownload(response, tab.privateMode);
            }
        });
        session.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
            @Override public GeckoResult<AllowOrDeny> onLoadRequest(
                    GeckoSession s, GeckoSession.NavigationDelegate.LoadRequest request) {
                if (request.target == GeckoSession.NavigationDelegate.TARGET_WINDOW_NEW
                        && !request.hasUserGesture) {
                    return GeckoResult.fromValue(AllowOrDeny.DENY);
                }
                if (SiteCompatibility.isExternalLink(request.uri)) {
                    if (request.hasUserGesture) confirmExternalLink(request.uri);
                    return GeckoResult.fromValue(AllowOrDeny.DENY);
                }
                return null;
            }
            @Override public GeckoResult<GeckoSession> onNewSession(GeckoSession s, String uri) {
                if (tabs.size() >= TabSessionStore.MAX_TABS) {
                    Toast.makeText(MainActivity.this, "Limite de abas atingido", Toast.LENGTH_SHORT).show();
                    return null;
                }
                // A nova janela é carregada pelo próprio GeckoView. Não chamar loadUri aqui.
                Tab popup = createTab(HOME, "Nova aba", tab.privateMode);
                popup.session.open(runtime);
                popup.opened = true;
                popup.loaded = true;
                tabs.add(popup);
                selectTab(tabs.size() - 1);
                return GeckoResult.fromValue(popup.session);
            }
            @Override public void onLocationChange(GeckoSession s, String uri,
                    List<GeckoSession.PermissionDelegate.ContentPermission> perms, Boolean gesture) {
                if (uri == null) return;
                if (!uri.equals(tab.url)) tab.title = "Nova aba";
                tab.url = uri;
                tab.securityInfo = null;
                if (tab == current()) {
                    if (!urlBox.hasFocus()) setUrlBoxText(uri.equals(HOME) ? "" : uri);
                    updateSecurity(uri);
                    updateHomeScreen();
                }
                if (PrivacyRules.shouldRecordHistory(tab.privateMode, uri)) rememberHistory(uri, tab.title);
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
        session.setPromptDelegate(new GeckoSession.PromptDelegate() {
            @Override public GeckoResult<GeckoSession.PromptDelegate.PromptResponse> onFilePrompt(
                    GeckoSession s, GeckoSession.PromptDelegate.FilePrompt prompt) {
                return openFilePicker(prompt);
            }
        });
        session.setPermissionDelegate(new GeckoSession.PermissionDelegate() {
            @Override public void onAndroidPermissionsRequest(GeckoSession s, String[] permissions,
                    GeckoSession.PermissionDelegate.Callback callback) {
                requestSitePermissions(permissions, callback::grant, callback::reject);
            }
            @Override public GeckoResult<Integer> onContentPermissionRequest(
                    GeckoSession s, GeckoSession.PermissionDelegate.ContentPermission perm) {
                return askContentPermission(perm);
            }
            @Override public void onMediaPermissionRequest(GeckoSession s, String uri,
                    GeckoSession.PermissionDelegate.MediaSource[] video,
                    GeckoSession.PermissionDelegate.MediaSource[] audio,
                    GeckoSession.PermissionDelegate.MediaCallback callback) {
                askMediaPermission(uri, video, audio, callback);
            }
        });
        session.setProgressDelegate(new GeckoSession.ProgressDelegate() {
            @Override public void onPageStart(GeckoSession s, String uri) {
                tab.securityInfo = null;
                if (tab == current()) {
                    status.setText(tab.privateMode ? "Privado • Carregando…" : "Carregando…");
                    updateSecurity(tab.url);
                }
            }
            @Override public void onSecurityChange(GeckoSession s,
                    GeckoSession.ProgressDelegate.SecurityInformation info) {
                tab.securityInfo = info;
                if (tab == current()) updateSecurity(tab.url);
            }
            @Override public void onPageStop(GeckoSession s, boolean success) {
                if (tab == current()) status.setText(tab.privateMode ? (success ? "Aba privada • Concluído" : "Aba privada • Erro") : (success ? "Concluído" : "Erro ao carregar"));
            }
        });
        return tab;
    }

    private void addTab(String url) {
        addTab(url, false);
    }

    private void addPrivateTab(String url) {
        addTab(url, true);
    }

    private void addTab(String url, boolean privateMode) {
        if (tabs.size() >= TabSessionStore.MAX_TABS) {
            Toast.makeText(this, "Limite de 8 abas para poupar memória", Toast.LENGTH_SHORT).show();
            return;
        }
        tabs.add(createTab(url, privateMode ? "Aba privada" : "Nova aba", privateMode));
        selectTab(tabs.size() - 1);
    }

    private void selectTab(int index) {
        if (index < 0 || index >= tabs.size()) return;
        if (active == index && view.getSession() == tabs.get(index).session) return;
        GeckoSession old = view.releaseSession();
        if (old != null) old.setActive(false);
        if (fullScreen) applyFullScreen(false);
        active = index;
        Tab tab = current();
        if (!tab.opened) {
            tab.session.open(runtime);
            tab.opened = true;
        }
        tab.session.setActive(true);
        view.setSession(tab.session);
        setUrlBoxText(tab.url.equals(HOME) ? "" : tab.url);
        urlBox.dismissDropDown();
        updateSecurity(tab.url);
        updateChrome();
        updateHomeScreen();
        addressPanel.setBackgroundColor(tab.privateMode ? 0xffe9e1f4 : PANEL_BG);
        if (tab.privateMode) status.setText("Aba privada • Não salva histórico");
        refreshSuggestionAdapter("");
        if (tab.fullScreen) applyFullScreen(true);
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
            int selectedNormal = -1;
            for (Tab tab : tabs) {
                if (tab.privateMode) continue;
                if (tab == current()) selectedNormal = array.length();
                JSONObject item = new JSONObject();
                item.put("url", TabSessionStore.restorable(tab.url) ? tab.url : HOME);
                item.put("title", TabSessionStore.safeTitle(tab.title));
                array.put(item);
            }
            if (array.length() == 0) {
                JSONObject blank = new JSONObject();
                blank.put("url", HOME);
                blank.put("title", "Nova aba");
                array.put(blank);
            }
            root.put("tabs", array);
            root.put("active", selectedNormal < 0 ? 0 : selectedNormal);
            storage.edit().putString(PREF_OPEN_TABS, root.toString()).apply();
        } catch (Exception ignored) { }
    }

    private void updateSecurity(String url) {
        if (securityIcon == null) return;
        Tab tab = current();
        GeckoSession.ProgressDelegate.SecurityInformation info = tab == null ? null : tab.securityInfo;
        boolean secure = PrivacyRules.isVerifiedSecure(url, info != null && info.isSecure,
                info != null && info.isException,
                info != null && info.mixedModeActive == GeckoSession.ProgressDelegate.SecurityInformation.CONTENT_LOADED);
        securityIcon.setImageResource(secure ? R.drawable.ic_lock_xp : R.drawable.ic_globe_xp);
        securityIcon.setColorFilter(secure ? SUCCESS : tab != null && tab.privateMode ? PRIVATE_PURPLE : SUBTLE);
        securityIcon.setContentDescription(secure ? "Conexão HTTPS verificada pelo GeckoView" :
                "Segurança da conexão não confirmada, toque para detalhes");
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
            addTab(configuredHome());
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
        Tab selected = current();
        tabsBadge.setText((selected != null && selected.privateMode ? "♟" : "▤") + tabs.size());
        tabsBadge.setContentDescription((selected != null && selected.privateMode ? "Privada, " : "") +
                (tabs.size() == 1 ? "1 aba" : tabs.size() + " abas"));
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
                .setPositiveButton("+ Nova aba", (d, n) -> addTab(configuredHome()))
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
            card.setBackground(rounded(tab.privateMode ? 0xfff2ebfa : index == active ? 0xffe5f0ff : Color.WHITE, BORDER, 2));

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            TextView title = text((tab.privateMode ? "♟ Privada • " : index == active ? "● " : "▣ ") + trim(tab.title, 36), 14, TEXT);
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
                item.unmeteredOnly = false;
                item.privateDownload = object.optBoolean("privateDownload", false);
                // Registros antigos usavam o DownloadManager do Android.
                // Não reusar suas URIs nem fingir que agora são arquivos gerenciados pelo app.
                if (object.optInt("engineVersion", 0) != 2 || isDownloadRunning(item.status)) {
                    item.status = AppDownloadEngine.STATUS_FAILED;
                    item.reason = AppDownloadEngine.REASON_INTERRUPTED;
                    item.localUri = null;
                    item.bytesPerSecond = -1;
                }
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
                object.put("unmeteredOnly", false);
                object.put("privateDownload", item.privateDownload);
                object.put("engineVersion", 2);
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
        if (current() != null && current().privateMode) return new ArrayList<>();
        return HistorySuggestions.filter(history, bookmarks, query, 24);
    }

    private void toggleBookmark() {
        Tab t = current();
        if (t == null || !t.url.startsWith("http")) return;
        if (t.privateMode) {
            Toast.makeText(this, "Não é possível salvar favoritos na aba privada", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bookmarks.remove(t.url)) bookmarks.add(0, t.url);
        saveStringList(PREF_BOOKMARKS, bookmarks);
        refreshSuggestionAdapter(urlBox.getText().toString());
        updateHomeScreen();
    }

    private void showBookmarks() {
        ArrayList<MenuAction> actions = new ArrayList<>();
        actions.add(new MenuAction("＋", "Adicionar favorito", "Crie um novo atalho para sua página inicial.", this::showAddQuickAccess));
        Tab tab = current();
        if (tab != null && tab.url.startsWith("http") && !tab.privateMode) {
            actions.add(new MenuAction("★", "Favoritar página atual", "Adicionar ou remover o site aberto da sua lista.", this::toggleBookmark));
        }
        for (String address : bookmarks) {
            String host = PrivacyRules.displayHost(address);
            actions.add(new MenuAction("🌐", host, address, () -> navigate(address)));
        }
        showModuleMenuDialog("Favoritos", bookmarks.isEmpty() ? "Nenhum favorito salvo. Use Adicionar para criar seu primeiro atalho." : "Seus sites favoritos, também disponíveis na página inicial.", actions);
    }

    private void showHistory() {
        if (current() != null && current().privateMode) {
            Toast.makeText(this, "Histórico indisponível na aba privada", Toast.LENGTH_SHORT).show();
            return;
        }
        LinearLayout column = styledDialogContent("Histórico de navegação", "Pesquise seus sites visitados, abra ou remova registros.");
        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(14);
        search.setHint("Pesquisar título ou endereço");
        search.setPadding(dp(10), 0, dp(10), 0);
        search.setBackground(rounded(Color.WHITE, BORDER, 8));
        column.addView(search, new LinearLayout.LayoutParams(-1, dp(46)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(results);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(-1, dp(Math.min(300, Math.max(180, (int) (screenHeightDp() * .37f)))));
        scrollParams.topMargin = dp(8);
        column.addView(scroll, scrollParams);
        AlertDialog dialog = new AlertDialog.Builder(this)
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
        compactDialog(dialog);
        animateDialogOpen(wrapper, false);
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
            if (item.status == AppDownloadEngine.STATUS_SUCCESSFUL) {
                addDownloadAction(actions, "Abrir", () -> openDownload(item));
                addDownloadAction(actions, "Compartilhar", () -> shareDownload(item));
                addDownloadAction(actions, "Salvar cópia", () -> exportDownload(item));
            } else if (isDownloadRunning(item.status)) {
                addDownloadAction(actions, "Cancelar", () -> confirmCancelDownload(item));
            } else if (item.status == AppDownloadEngine.STATUS_FAILED || item.status == DOWNLOAD_CANCELLED) {
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
        if (item == null || item.status != AppDownloadEngine.STATUS_SUCCESSFUL) return null;
        if (item.localUri == null || !item.localUri.startsWith("content://")) return null;
        return Uri.parse(item.localUri);
    }

    private void shareDownload(DownloadItem item) {
        try {
            Uri uri = uriForDownload(item);
            if (uri == null) { Toast.makeText(this, "Arquivo indisponível", Toast.LENGTH_SHORT).show(); return; }
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(item.mime == null || item.mime.isEmpty() ? "application/octet-stream" : item.mime);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setClipData(ClipData.newUri(getContentResolver(), "Arquivo Internet XP", uri));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Compartilhar download"));
        } catch (RuntimeException error) { showError(error); }
    }

    private void exportDownload(DownloadItem item) {
        if (uriForDownload(item) == null) { Toast.makeText(this, "Arquivo indisponível", Toast.LENGTH_SHORT).show(); return; }
        try {
            exportTarget = item;
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(item.mime == null || item.mime.isEmpty() ? "application/octet-stream" : item.mime);
            intent.putExtra(Intent.EXTRA_TITLE, item.fileName);
            startActivityForResult(intent, REQUEST_EXPORT_DOWNLOAD);
        } catch (ActivityNotFoundException e) { showError(e); }
    }

    private void showDownloadDetails(DownloadItem item) {
        String message = "Nome: " + item.fileName
                + "\nStatus: " + downloadStatusText(item.status)
                + "\nTamanho: " + formatBytes(item.total)
                + "\nBaixado: " + formatBytes(item.bytes)
                + "\nArmazenamento: gerenciado pelo Internet XP"
                + "\nOrigem: " + item.url;
        if (item.status == AppDownloadEngine.STATUS_FAILED) message += "\nMotivo: " + downloadReason(item);
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
        return status == AppDownloadEngine.STATUS_PENDING || status == AppDownloadEngine.STATUS_RUNNING;
    }

    private void confirmCancelDownload(DownloadItem item) {
        new AlertDialog.Builder(this).setTitle("Cancelar download?")
                .setMessage("O Internet XP interromperá a transferência e apagará o arquivo parcial.")
                .setPositiveButton("Cancelar download", (d, w) -> {
                    nativeDownloads.cancel(item.id);
                    item.status = AppDownloadEngine.STATUS_CANCELLED;
                    item.bytesPerSecond = -1;
                    item.localUri = null;
                    saveDownloads();
                    renderDownloadsPanel();
                }).setNegativeButton("Manter", null).show();
    }

    private void retryDownload(DownloadItem item) {
        if (!AppDownloadEngine.isWebUrl(item.url)) {
            Toast.makeText(this, "Link inválido para download", Toast.LENGTH_SHORT).show(); return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Baixar novamente?")
                .setMessage("O Internet XP fará uma nova requisição pelo GeckoView. É um novo download, não retomada por partes.\nArquivos com login podem exigir nova autenticação.")
                .setPositiveButton("Reiniciar", (dialog, which) -> fetchForDownload(item))
                .setNegativeButton("Voltar", null).show();
    }

    private void startNativeDownload(DownloadItem item, WebResponse response) {
        if (response.statusCode >= 400 || response.body == null) {
            if (response.body != null) try { response.body.close(); } catch (Exception ignored) { }
            item.status = AppDownloadEngine.STATUS_FAILED;
            item.reason = response.statusCode >= 400 ? response.statusCode : AppDownloadEngine.REASON_MISSING_BODY;
            saveDownloads();
            if (!isFinishing() && !isDestroyed() && downloadsPanel.getVisibility() == View.VISIBLE) renderDownloadsPanel();
            return;
        }
        item.status = AppDownloadEngine.STATUS_PENDING;
        item.bytes = 0;
        item.total = AppDownloadEngine.contentLength(response.headers);
        item.reason = 0;
        item.bytesPerSecond = -1;
        item.localUri = null;
        saveDownloads();
        nativeDownloads.start(item.id, item.fileName, response.body, item.total, new AppDownloadEngine.Listener() {
            @Override public void onProgress(long received, long total, long speed) {
                if (item.status == AppDownloadEngine.STATUS_CANCELLED) return;
                item.status = AppDownloadEngine.STATUS_RUNNING;
                item.bytes = received;
                item.total = total;
                item.bytesPerSecond = speed;
                if (!isFinishing() && !isDestroyed() && downloadsPanel != null && downloadsPanel.getVisibility() == View.VISIBLE) renderDownloadsPanel();
            }
            @Override public void onDone(String savedUri, long received) {
                if (item.status == AppDownloadEngine.STATUS_CANCELLED) return;
                item.status = AppDownloadEngine.STATUS_SUCCESSFUL;
                item.localUri = savedUri;
                item.bytes = received;
                item.total = received;
                item.bytesPerSecond = -1;
                saveDownloads();
                if (status != null) status.setText("Download concluído: " + item.fileName);
                if (!isFinishing() && !isDestroyed() && downloadsPanel != null && downloadsPanel.getVisibility() == View.VISIBLE) renderDownloadsPanel();
            }
            @Override public void onError(int reason) {
                if (item.status == AppDownloadEngine.STATUS_CANCELLED) return;
                item.status = AppDownloadEngine.STATUS_FAILED;
                item.reason = reason;
                item.bytesPerSecond = -1;
                saveDownloads();
                if (!isFinishing() && !isDestroyed() && downloadsPanel != null && downloadsPanel.getVisibility() == View.VISIBLE) renderDownloadsPanel();
            }
        });
    }

    private void fetchForDownload(DownloadItem item) {
        if (runtime == null) return;
        item.id = System.currentTimeMillis();
        item.status = AppDownloadEngine.STATUS_PENDING;
        item.bytes = 0; item.total = -1; item.localUri = null; item.reason = 0;
        saveDownloads();
        GeckoWebExecutor executor = new GeckoWebExecutor(runtime);
        int flags = item.privateDownload ? GeckoWebExecutor.FETCH_FLAGS_PRIVATE : GeckoWebExecutor.FETCH_FLAGS_NONE;
        try {
            executor.fetch(new WebRequest.Builder(item.url).build(), flags)
                    .accept(response -> runOnUiThread(() -> startNativeDownload(item, response)),
                            error -> runOnUiThread(() -> {
                                item.status = AppDownloadEngine.STATUS_FAILED;
                                item.reason = AppDownloadEngine.REASON_NETWORK;
                                saveDownloads();
                                if (!isFinishing() && !isDestroyed() && downloadsPanel.getVisibility() == View.VISIBLE) renderDownloadsPanel();
                            }));
        } catch (Exception e) { item.status = AppDownloadEngine.STATUS_FAILED; item.reason = AppDownloadEngine.REASON_NETWORK; saveDownloads(); }
    }

    private String buildDownloadSubtitle(DownloadItem item) {
        String label = downloadStatusText(item.status);
        if (item.status == AppDownloadEngine.STATUS_RUNNING) {
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
        if (item.status == AppDownloadEngine.STATUS_SUCCESSFUL)
            return label + (item.total > 0 ? " • " + formatBytes(item.total) : "");
        if (item.status == AppDownloadEngine.STATUS_FAILED)
            return label + " • " + downloadReason(item);
        return label;
    }

    private String downloadReason(DownloadItem item) {
        if (item.reason >= 400 && item.reason <= 599) return "HTTP " + item.reason;
        if (item.reason == AppDownloadEngine.REASON_MISSING_BODY) return "servidor não entregou dados";
        if (item.reason == AppDownloadEngine.REASON_INTERRUPTED) return "transferência antiga ou interrompida; reinicie";
        if (item.reason == AppDownloadEngine.REASON_STORAGE) return "erro ao gravar no armazenamento";
        if (item.reason == AppDownloadEngine.REASON_NETWORK) return "falha de transferência; confira conexão";
        return "falha ao baixar";
    }

    private String downloadStatusText(int status) {
        switch (status) {
            case AppDownloadEngine.STATUS_PENDING: return "Na fila";
            case AppDownloadEngine.STATUS_RUNNING: return "Baixando";
            case AppDownloadEngine.STATUS_SUCCESSFUL: return "Concluído";
            case AppDownloadEngine.STATUS_FAILED: return "Falhou";
            case AppDownloadEngine.STATUS_CANCELLED: return "Cancelado";
            default: return "Desconhecido";
        }
    }

    private String formatBytes(long bytes) { return DownloadDisplay.bytes(bytes); }

    private void refreshDownloadsState() {
        // Os bytes chegam pelo fluxo nativo do GeckoView; nenhum DownloadManager é consultado.
    }

    private void openDownload(DownloadItem item) {
        try {
            Uri uri = uriForDownload(item);
            if (uri == null) { Toast.makeText(this, "Arquivo indisponível", Toast.LENGTH_SHORT).show(); return; }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, item.mime == null || item.mime.isEmpty() ? "*/*" : item.mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Abrir arquivo"));
        } catch (Exception e) { showError(e); }
    }

    private void offerDownload(WebResponse response, boolean fromPrivateTab) {
        String uri = response.uri;
        if (!AppDownloadEngine.isWebUrl(uri)) {
            if (response.body != null) try { response.body.close(); } catch (Exception ignored) { }
            status.setText("Download não suportado: somente HTTP(S)");
            return;
        }
        String mime = AppDownloadEngine.header(response.headers, "Content-Type");
        String disposition = AppDownloadEngine.header(response.headers, "Content-Disposition");
        String filename = AppDownloadEngine.filename(uri, disposition, mime);
        // O fluxo recebido pelo GeckoView não é descartado e não exige nova autenticação.
        new AlertDialog.Builder(this)
                .setTitle(fromPrivateTab ? "Baixar arquivo da aba privada?" : "Baixar com Internet XP?")
                .setMessage("Arquivo: " + filename + "\nOrigem: " + uri + "\n\n"
                        + "O Internet XP gerenciará o download. Para salvá-lo em outra pasta, use Salvar cópia após concluir."
                        + (fromPrivateTab ? "\n\nAviso: o arquivo baixado continuará no armazenamento do aplicativo." : ""))
                .setPositiveButton("Baixar", (d, n) -> {
                    DownloadItem item = new DownloadItem();
                    item.id = System.currentTimeMillis();
                    item.fileName = filename;
                    item.url = uri;
                    item.mime = mime;
                    item.status = AppDownloadEngine.STATUS_PENDING;
                    item.bytes = 0; item.total = -1;
                    item.createdAt = System.currentTimeMillis();
                    item.privateDownload = fromPrivateTab;
                    downloads.add(0, item);
                    while (downloads.size() > 40) downloads.remove(downloads.size() - 1);
                    saveDownloads();
                    showDownloads();
                    if (response.body != null) startNativeDownload(item, response);
                    else fetchForDownload(item);
                })
                .setNegativeButton("Cancelar", (d, n) -> {
                    if (response.body != null) try { response.body.close(); } catch (Exception ignored) { }
                })
                .setOnCancelListener(d -> {
                    if (response.body != null) try { response.body.close(); } catch (Exception ignored) { }
                })
                .show();
    }

    /** A seleção usa o seletor de documentos do Android, sem permissão geral de armazenamento. */
    private GeckoResult<GeckoSession.PromptDelegate.PromptResponse> openFilePicker(
            GeckoSession.PromptDelegate.FilePrompt prompt) {
        if (filePickerResult != null) return GeckoResult.fromValue(prompt.dismiss());
        if (prompt.type == GeckoSession.PromptDelegate.FilePrompt.Type.FOLDER) {
            Toast.makeText(this, "Envio de pastas ainda não disponível", Toast.LENGTH_SHORT).show();
            return GeckoResult.fromValue(prompt.dismiss());
        }
        GeckoResult<GeckoSession.PromptDelegate.PromptResponse> result = new GeckoResult<>();
        filePickerPrompt = prompt;
        filePickerResult = result;
        Intent choose = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        choose.addCategory(Intent.CATEGORY_OPENABLE);
        choose.setType(SiteCompatibility.primaryMimeType(prompt.mimeTypes));
        String[] types = SiteCompatibility.mimeTypes(prompt.mimeTypes);
        if (types.length > 1) choose.putExtra(Intent.EXTRA_MIME_TYPES, types);
        choose.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,
                prompt.type == GeckoSession.PromptDelegate.FilePrompt.Type.MULTIPLE);
        choose.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivityForResult(choose, REQUEST_UPLOAD_FILES);
        } catch (ActivityNotFoundException | SecurityException e) {
            filePickerPrompt = null;
            filePickerResult = null;
            result.complete(prompt.dismiss());
            Toast.makeText(this, "Seletor de arquivos indisponível", Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EXPORT_DOWNLOAD) {
            DownloadItem file = exportTarget;
            exportTarget = null;
            if (resultCode == RESULT_OK && data != null && data.getData() != null && file != null) {
                Uri source = uriForDownload(file);
                if (source != null) nativeDownloads.exportCopy(source, data.getData(),
                        () -> Toast.makeText(this, "Cópia salva", Toast.LENGTH_SHORT).show(), this::showError);
            }
            return;
        }
        if (requestCode != REQUEST_UPLOAD_FILES || filePickerResult == null) return;
        GeckoResult<GeckoSession.PromptDelegate.PromptResponse> result = filePickerResult;
        GeckoSession.PromptDelegate.FilePrompt prompt = filePickerPrompt;
        filePickerResult = null;
        filePickerPrompt = null;
        if (prompt == null) return;
        if (resultCode != RESULT_OK || data == null) {
            result.complete(prompt.dismiss());
            return;
        }
        LinkedHashSet<Uri> selected = new LinkedHashSet<>();
        if (data.getClipData() != null) {
            for (int i = 0; i < data.getClipData().getItemCount() && selected.size() < 30; i++) {
                Uri uri = data.getClipData().getItemAt(i).getUri();
                if (uri != null && "content".equals(uri.getScheme())) selected.add(uri);
            }
        }
        if (data.getData() != null && "content".equals(data.getData().getScheme())) {
            selected.add(data.getData());
        }
        if (selected.isEmpty()) result.complete(prompt.dismiss());
        else if (prompt.type == GeckoSession.PromptDelegate.FilePrompt.Type.MULTIPLE) {
            result.complete(prompt.confirm(getApplicationContext(), selected.toArray(new Uri[0])));
        } else {
            result.complete(prompt.confirm(getApplicationContext(), selected.iterator().next()));
        }
    }

    private void requestSitePermissions(String[] permissions, Runnable granted, Runnable denied) {
        if (permissions == null || permissions.length == 0) { granted.run(); return; }
        if (permissionGranted != null) { denied.run(); return; }
        ArrayList<String> missing = new ArrayList<>();
        for (String permission : permissions) {
            // Não solicitar permissões arbitrárias declaradas por páginas/extensões.
            if (!SiteCompatibility.isKnownAndroidPermission(permission)) { denied.run(); return; }
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) missing.add(permission);
        }
        if (missing.isEmpty()) { granted.run(); return; }
        permissionGranted = granted;
        permissionDenied = denied;
        try {
            requestPermissions(missing.toArray(new String[0]), REQUEST_SITE_PERMISSIONS);
        } catch (Exception e) {
            permissionGranted = null;
            permissionDenied = null;
            denied.run();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_SITE_PERMISSIONS) return;
        Runnable granted = permissionGranted;
        Runnable denied = permissionDenied;
        permissionGranted = null;
        permissionDenied = null;
        if (granted == null) return;
        boolean allowed = permissions.length > 0 && grantResults.length == permissions.length;
        for (int grant : grantResults) allowed &= grant == PackageManager.PERMISSION_GRANTED;
        if (allowed) granted.run();
        else if (denied != null) denied.run();
    }

    // notifyShown() é experimental no GeckoView 158; opt-in explícito e localizado.
    @OptIn(markerClass = ExperimentalGeckoViewApi.class)
    private GeckoResult<Integer> askContentPermission(
            GeckoSession.PermissionDelegate.ContentPermission permission) {
        final int type = permission.permission;
        if (type != GeckoSession.PermissionDelegate.PERMISSION_GEOLOCATION) {
            // Outras permissões continuam com a política-padrão do GeckoView.
            return null;
        }
        if (!SiteCompatibility.isSecureOrigin(permission.uri)) {
            return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY);
        }
        if (permission.value == GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW
                || permission.value == GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY) {
            return GeckoResult.fromValue(permission.value);
        }
        GeckoResult<Integer> result = new GeckoResult<>();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Permissão de localização")
                .setMessage("Permitir que " + SiteCompatibility.originLabel(permission.uri)
                        + " acesse sua localização?")
                .setPositiveButton("Permitir", (d, n) ->
                        result.complete(GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW))
                .setNegativeButton("Negar", (d, n) ->
                        result.complete(GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY))
                .setOnCancelListener(d -> result.complete(
                        GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY))
                .create();
        dialog.show();
        permission.notifyShown();
        return result;
    }

    private void askMediaPermission(String uri,
            GeckoSession.PermissionDelegate.MediaSource[] video,
            GeckoSession.PermissionDelegate.MediaSource[] audio,
            GeckoSession.PermissionDelegate.MediaCallback callback) {
        if (!SiteCompatibility.isSecureOrigin(uri)) { callback.reject(); return; }
        GeckoSession.PermissionDelegate.MediaSource camera = null;
        GeckoSession.PermissionDelegate.MediaSource microphone = null;
        if (video != null && video.length > 0) {
            for (GeckoSession.PermissionDelegate.MediaSource source : video) {
                if (source.source == GeckoSession.PermissionDelegate.MediaSource.SOURCE_CAMERA) {
                    camera = source;
                    break;
                }
            }
            if (camera == null) { callback.reject(); return; }
        }
        if (audio != null && audio.length > 0) {
            for (GeckoSession.PermissionDelegate.MediaSource source : audio) {
                if (source.source == GeckoSession.PermissionDelegate.MediaSource.SOURCE_MICROPHONE) {
                    microphone = source;
                    break;
                }
            }
            if (microphone == null) { callback.reject(); return; }
        }
        if (camera == null && microphone == null) { callback.reject(); return; }
        final GeckoSession.PermissionDelegate.MediaSource selectedCamera = camera;
        final GeckoSession.PermissionDelegate.MediaSource selectedMicrophone = microphone;
        ArrayList<String> needed = new ArrayList<>();
        if (camera != null) needed.add(Manifest.permission.CAMERA);
        if (microphone != null) needed.add(Manifest.permission.RECORD_AUDIO);
        requestSitePermissions(needed.toArray(new String[0]), () -> {
            String devices = selectedCamera != null && selectedMicrophone != null ? "câmera e microfone"
                    : selectedCamera != null ? "câmera" : "microfone";
            new AlertDialog.Builder(this)
                    .setTitle("Permissão de " + devices)
                    .setMessage("Permitir que " + SiteCompatibility.originLabel(uri)
                            + " utilize " + devices + " nesta página?")
                    .setPositiveButton("Permitir", (d, n) -> callback.grant(selectedCamera, selectedMicrophone))
                    .setNegativeButton("Negar", (d, n) -> callback.reject())
                    .setOnCancelListener(d -> callback.reject())
                    .show();
        }, callback::reject);
    }

    private void confirmExternalLink(String uri) {
        String label = SiteCompatibility.originLabel(uri);
        new AlertDialog.Builder(this)
                .setTitle("Abrir aplicativo externo?")
                .setMessage("O site deseja abrir este link fora do Internet XP:\n" + label)
                .setPositiveButton("Abrir", (d, n) -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        startActivity(intent);
                    } catch (ActivityNotFoundException | SecurityException e) {
                        Toast.makeText(this, "Não há aplicativo compatível", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void applyFullScreen(boolean enabled) {
        fullScreen = enabled;
        if (browserRoot == null) return;
        for (int i = 0; i < browserRoot.getChildCount(); i++) {
            View child = browserRoot.getChildAt(i);
            if (child != view) child.setVisibility(enabled ? View.GONE : View.VISIBLE);
        }
        if (!enabled && downloadsPanel != null) downloadsPanel.setVisibility(View.GONE);
        if (enabled) uiHandler.removeCallbacks(downloadRefresh);
        int flags = enabled ? View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY : 0;
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    private void closePrivateTabs() {
        for (int index = tabs.size() - 1; index >= 0; index--) {
            if (tabs.get(index).privateMode) closeTab(index);
        }
        refreshSuggestionAdapter("");
    }

    private void toggleTrackingProtection() {
        trackingProtectionEnabled = !trackingProtectionEnabled;
        storage.edit().putBoolean(PREF_TRACKING_PROTECTION, trackingProtectionEnabled).apply();
        for (Tab tab : tabs) tab.session.getSettings().setUseTrackingProtection(trackingProtectionEnabled);
        Toast.makeText(this, trackingProtectionEnabled ? "Proteção antirrastreamento ativada" :
                "Proteção antirrastreamento desativada", Toast.LENGTH_LONG).show();
    }

    private void showSiteInfo() {
        Tab tab = current();
        if (tab == null) return;
        GeckoSession.ProgressDelegate.SecurityInformation info = tab.securityInfo;
        String message;
        if (HOME.equals(tab.url)) {
            message = "Nenhum site carregado.";
        } else if (!tab.url.startsWith("https://")) {
            message = "A página não usa HTTPS. A conexão pode não estar criptografada.";
        } else if (info == null) {
            message = "HTTPS detectado no endereço, mas a verificação pelo motor ainda não terminou.";
        } else if (info.isException) {
            message = "Há uma exceção de certificado. Esta conexão não é considerada verificada.";
        } else if (!info.isSecure) {
            message = "O GeckoView não confirmou uma conexão segura para esta página.";
        } else if (info.mixedModeActive == GeckoSession.ProgressDelegate.SecurityInformation.CONTENT_LOADED) {
            message = "HTTPS ativo, mas conteúdo misto inseguro foi carregado.";
        } else {
            message = "Conexão HTTPS confirmada pelo GeckoView.";
            if (info.mixedModePassive == GeckoSession.ProgressDelegate.SecurityInformation.CONTENT_LOADED)
                message += "\nA página contém conteúdo misto passivo.";
        }
        new AlertDialog.Builder(this)
                .setTitle("Segurança da página")
                .setMessage(PrivacyRules.displayHost(tab.url) + "\n\n" + message +
                        "\n\n" + (tab.privateMode ? "Aba privada: o histórico local não é salvo." : "Aba normal."))
                .setPositiveButton("OK", null)
                .show();
    }

    private void showPrivacyMenu() {
        ArrayList<MenuAction> actions = new ArrayList<>();
        actions.add(new MenuAction("⌘", "Proteção antirrastreamento", trackingProtectionEnabled ? "Atualmente ligada. Toque para desligar ou religar a proteção do GeckoView." : "Atualmente desligada. Toque para ativar a proteção do GeckoView.", this::toggleTrackingProtection));
        actions.add(new MenuAction("🍪", "Limpar cookies e sessões", "Remove cookies e sessões de login salvas pelo navegador.", () -> confirmClearData(StorageController.ClearFlags.COOKIES | StorageController.ClearFlags.AUTH_SESSIONS, false, "cookies e sessões de login")));
        actions.add(new MenuAction("🗂", "Limpar cache", "Apaga o cache de páginas, imagens e outros arquivos temporários.", () -> confirmClearData(StorageController.ClearFlags.ALL_CACHES, false, "cache de páginas e imagens")));
        actions.add(new MenuAction("✔", "Limpar permissões", "Esquece permissões concedidas anteriormente a sites, como câmera e localização.", () -> confirmClearData(StorageController.ClearFlags.PERMISSIONS, false, "permissões de sites")));
        actions.add(new MenuAction("✖", "Limpeza completa", "Fecha as abas e limpa todos os dados de navegação, incluindo o histórico local.", () -> confirmClearData(StorageController.ClearFlags.ALL, true, "todos os dados de navegação")));
        actions.add(new MenuAction("🔎", "Permissões do site atual", "Consulte ou redefina permissões armazenadas para o site aberto agora.", this::showCurrentSitePermissions));
        actions.add(new MenuAction("◌", "Fechar abas privadas", "Fecha todas as abas privadas ainda abertas nesta sessão.", this::closePrivateTabs));
        showModuleMenuDialog("Privacidade e dados", "Ferramentas para controlar rastreamento, permissões e dados locais do Internet XP.", actions);
    }

    private void confirmClearData(long flags, boolean clearLocalHistory, String what) {
        new AlertDialog.Builder(this)
                .setTitle("Apagar " + what + "?")
                .setMessage("Para limpar de maneira consistente, todas as abas serão fechadas e uma página vazia será aberta. " +
                        "Os favoritos e os arquivos já baixados serão preservados. Dados de sessão de sites e extensões podem ser afetados. Esta ação não pode ser desfeita.")
                .setPositiveButton("Limpar", (d, n) -> clearBrowserData(flags, clearLocalHistory))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void clearBrowserData(long flags, boolean clearLocalHistory) {
        if (runtime == null) return;
        uiHandler.removeCallbacks(downloadRefresh);
        if (view != null) view.releaseSession();
        for (Tab tab : tabs) if (tab.opened) tab.session.close();
        tabs.clear();
        active = -1;
        // Remove URLs de abas antigas imediatamente, mesmo se o processo for morto
        // antes da conclusão assíncrona de StorageController.clearData().
        storage.edit().putString(PREF_OPEN_TABS,
                "{\"tabs\":[{\"url\":\"about:blank\",\"title\":\"Nova aba\"}],\"active\":0}")
                .apply();
        if (clearLocalHistory) {
            history.clear();
            historyDetails.clear();
            saveStringList(PREF_HISTORY, history);
            saveHistoryDetails();
        }
        setUrlBoxText("");
        refreshSuggestionAdapter("");
        status.setText("Limpando dados do navegador…");
        runtime.getStorageController().clearData(flags).accept(
                result -> runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        addTab(HOME);
                        status.setText("Dados do navegador limpos");
                    }
                }),
                error -> runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        addTab(HOME);
                        showError(error);
                    }
                }));
    }

    private void showCurrentSitePermissions() {
        Tab tab = current();
        if (runtime == null || tab == null || !tab.url.startsWith("http")) {
            Toast.makeText(this, "Abra um site para consultar as permissões", Toast.LENGTH_SHORT).show();
            return;
        }
        final String url = tab.url;
        runtime.getStorageController().getPermissions(url, tab.privateMode).accept(
                permissions -> runOnUiThread(() -> displaySitePermissions(url, permissions)),
                error -> runOnUiThread(() -> showError(error)));
    }

    private String permissionLabel(int type) {
        switch (type) {
            case GeckoSession.PermissionDelegate.PERMISSION_GEOLOCATION: return "Localização";
            case GeckoSession.PermissionDelegate.PERMISSION_DESKTOP_NOTIFICATION: return "Notificações";
            case GeckoSession.PermissionDelegate.PERMISSION_PERSISTENT_STORAGE: return "Armazenamento persistente";
            case GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_AUDIBLE: return "Reprodução automática com áudio";
            case GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_INAUDIBLE: return "Reprodução automática sem áudio";
            case GeckoSession.PermissionDelegate.PERMISSION_TRACKING: return "Exceção de rastreamento";
            case GeckoSession.PermissionDelegate.PERMISSION_STORAGE_ACCESS: return "Cookies de terceiros";
            default: return "Permissão tipo " + type;
        }
    }

    private void displaySitePermissions(String url,
            List<GeckoSession.PermissionDelegate.ContentPermission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Permissões do site")
                    .setMessage("Nenhuma permissão armazenada para " + PrivacyRules.displayHost(url))
                    .setPositiveButton("OK", null).show();
            return;
        }
        String[] titles = new String[permissions.size()];
        for (int i = 0; i < permissions.size(); i++) {
            GeckoSession.PermissionDelegate.ContentPermission permission = permissions.get(i);
            String state = permission.value == GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW ? "Permitido" :
                    permission.value == GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY ? "Bloqueado" : "Perguntar";
            titles[i] = permissionLabel(permission.permission) + " • " + state;
        }
        new AlertDialog.Builder(this).setTitle("Permissões • " + PrivacyRules.displayHost(url))
                .setItems(titles, (d, index) -> {
                    GeckoSession.PermissionDelegate.ContentPermission permission = permissions.get(index);
                    if (permission.permission == GeckoSession.PermissionDelegate.PERMISSION_PERSISTENT_STORAGE &&
                            permission.value == GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW) {
                        new AlertDialog.Builder(this).setTitle("Armazenamento persistente")
                                .setMessage("O GeckoView não permite revogar individualmente esta autorização. Use Privacidade e dados > Limpar todos os dados de navegação.")
                                .setPositiveButton("OK", null).show();
                        return;
                    }
                    new AlertDialog.Builder(this).setTitle(titles[index])
                            .setItems(new String[]{"Perguntar novamente", "Bloquear"}, (d2, action) -> {
                                int value = action == 0 ?
                                        GeckoSession.PermissionDelegate.ContentPermission.VALUE_PROMPT :
                                        GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY;
                                runtime.getStorageController().setPermission(permission, value);
                                Toast.makeText(this, "Permissão atualizada", Toast.LENGTH_SHORT).show();
                            }).setNegativeButton("Cancelar", null).show();
                }).setNegativeButton("Fechar", null).show();
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
            if (isFinishing() || isDestroyed()) return;
            ArrayList<MenuAction> actions = new ArrayList<>();
            actions.add(new MenuAction("＋", "Instalar extensão XPI", "Instale um pacote Mozilla assinado a partir de uma URL HTTPS.", this::installExtension));
            actions.add(new MenuAction("🧩", "Catálogo Mozilla Add-ons", "Visite a página de complementos compatíveis com Firefox Android.", () -> navigate("https://addons.mozilla.org/android/")));
            for (WebExtension extension : list) {
                String name = extension.metaData.name == null ? extension.id : extension.metaData.name;
                String description = extension.metaData.enabled ? "Ativada. Toque para gerenciar ou desinstalar." : "Desativada. Toque para ativar ou desinstalar.";
                actions.add(new MenuAction("▣", name, description, () -> extensionActions(extension)));
            }
            showModuleMenuDialog("Extensões Firefox", list.isEmpty() ? "Nenhuma extensão instalada. Explore o catálogo ou instale um XPI." : "Gerencie suas extensões e permissões.", actions);
        }), error -> runOnUiThread(() -> showError(error)));
    }

    private void extensionActions(WebExtension x) {
        String name = x.metaData.name == null ? x.id : x.metaData.name;
        ArrayList<MenuAction> actions = new ArrayList<>();
        actions.add(new MenuAction(x.metaData.enabled ? "⏻" : "✓", x.metaData.enabled ? "Desativar" : "Ativar",
                x.metaData.enabled ? "Desligue a extensão temporariamente sem removê-la." : "Ative a extensão novamente para uso imediato.", () -> {
                    GeckoResult<WebExtension> r = x.metaData.enabled
                            ? runtime.getWebExtensionController().disable(x, WebExtensionController.EnableSource.USER)
                            : runtime.getWebExtensionController().enable(x, WebExtensionController.EnableSource.USER);
                    r.accept(v -> runOnUiThread(this::showExtensions), e -> runOnUiThread(() -> showError(e)));
                }));
        actions.add(new MenuAction("✕", "Desinstalar", "Remova a extensão instalada do navegador.", () -> confirmUninstallExtension(x)));
        showModuleMenuDialog(name, x.metaData.enabled ? "Extensão ativa. Escolha uma ação abaixo." : "Extensão desativada. Você pode ativá-la ou remover.", actions);
    }

    private void confirmUninstallExtension(WebExtension x) {
        LinearLayout content = styledDialogContent("Desinstalar extensão", "Remover este complemento do Internet XP?");
        String name = x.metaData.name == null ? x.id : x.metaData.name;
        TextView detail = text(name, 13, TEXT);
        detail.setTypeface(Typeface.DEFAULT_BOLD);
        detail.setPadding(dp(6), dp(8), dp(6), dp(8));
        detail.setBackground(rounded(Color.WHITE, BORDER, 10));
        content.addView(detail, new LinearLayout.LayoutParams(-1, -2));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(content)
                .setPositiveButton("Remover", (d2, n2) -> runtime.getWebExtensionController().uninstall(x)
                        .accept(v -> runOnUiThread(this::showExtensions), e -> runOnUiThread(() -> showError(e))))
                .setNegativeButton("Cancelar", null)
                .create();
        dialog.show(); compactDialog(dialog); animateDialogOpen(content, false);
    }

    private void installExtension() {
        AutoCompleteTextView link = new AutoCompleteTextView(this);
        link.setSingleLine();
        link.setHint("https://.../complemento.xpi");
        link.setTextSize(14);
        link.setBackground(rounded(Color.WHITE, BORDER, 10));
        link.setPadding(dp(10), dp(12), dp(10), dp(12));
        LinearLayout content = styledDialogContent("Instalar extensão Mozilla assinada", "Cole o link direto HTTPS para o pacote XPI assinado.");
        content.addView(link, new LinearLayout.LayoutParams(-1, -2));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(content)
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
                .create();
        dialog.show(); compactDialog(dialog); animateDialogOpen(content, false);
    }

    private void showError(Throwable e) {
        LinearLayout content = styledDialogContent("Falha", "Ocorreu um erro durante a operação solicitada.");
        TextView detail = text(String.valueOf(e.getMessage()), 12, TEXT);
        detail.setPadding(dp(8), dp(10), dp(8), dp(10));
        detail.setBackground(rounded(Color.WHITE, BORDER, 10));
        content.addView(detail, new LinearLayout.LayoutParams(-1, -2));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(content)
                .setPositiveButton("OK", null)
                .create();
        dialog.show(); compactDialog(dialog); animateDialogOpen(content, false);
    }

    private void handleIncomingLink(Intent intent) {
        if (intent == null || !Intent.ACTION_VIEW.equals(intent.getAction())) return;
        Uri link = intent.getData();
        if (link == null || !AppDownloadEngine.isWebUrl(link.toString())) return;
        Tab activeTab = current();
        if (activeTab != null && activeTab.privateMode) addTab(link.toString());
        else if (activeTab != null) navigate(link.toString());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingLink(intent);
    }

    private void requestDefaultBrowserRole() {
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                RoleManager roles = (RoleManager) getSystemService(ROLE_SERVICE);
                if (roles != null && roles.isRoleAvailable(RoleManager.ROLE_BROWSER)) {
                    if (roles.isRoleHeld(RoleManager.ROLE_BROWSER)) {
                        Toast.makeText(this, "Internet XP já é o navegador padrão", Toast.LENGTH_LONG).show(); return;
                    }
                    startActivity(roles.createRequestRoleIntent(RoleManager.ROLE_BROWSER)); return;
                }
            }
            startActivity(new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS));
        } catch (Exception error) {
            Toast.makeText(this, "Defina o Internet XP nas configurações de apps padrão do Android", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (fullScreen) {
            Tab t = current();
            if (t != null) t.session.exitFullScreen();
            if (t != null) t.fullScreen = false;
            applyFullScreen(false);
            return;
        }
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
        if (filePickerResult != null && filePickerPrompt != null) {
            filePickerResult.complete(filePickerPrompt.dismiss());
            filePickerResult = null;
            filePickerPrompt = null;
        }
        if (permissionDenied != null) permissionDenied.run();
        permissionGranted = null;
        permissionDenied = null;
        uiHandler.removeCallbacks(downloadRefresh);
        if (view != null) view.releaseSession();
        for (Tab t : tabs) if (t.opened) t.session.close();
        tabs.clear();
        if (faviconRepository != null) faviconRepository.shutdown();
        super.onDestroy();
    }
}
