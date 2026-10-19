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
import android.graphics.Bitmap;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import androidx.core.content.FileProvider;
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
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.view.ViewGroup;
import java.util.ArrayDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import java.util.Map;
import java.util.HashMap;

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
    private final Map<String, WebExtension> activeExtensions = new HashMap<>();
    private final Map<String, WebExtension.Action> defaultExtensionActions = new HashMap<>();
    private final Map<String, Map<GeckoSession, WebExtension.Action>> sessionExtensionActions = new HashMap<>();
    private GeckoSession extensionPopupSession;
    private GeckoView extensionPopupView;
    private AlertDialog extensionPopupDialog;

    private final ArrayList<Tab> tabs = new ArrayList<>();
    private final ArrayList<String> bookmarks = new ArrayList<>();
    private final ArrayList<String> history = new ArrayList<>();
    private final ArrayList<HistoryRecords.Entry> historyDetails = new ArrayList<>();
    private final ArrayList<DownloadItem> downloads = new ArrayList<>();

    private SharedPreferences storage;
    private int active = -1;
    private AutoCompleteTextView urlBox;
    private AddressSuggestionAdapter historyAdapter;
    private final ArrayList<String> onlineSuggestions = new ArrayList<>();
    private final Handler suggestionHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService suggestionWorker = Executors.newFixedThreadPool(2);
    private Runnable pendingSuggestionRequest;
    private int suggestionRequestId;
    private String lastOnlineQuery = "";
    private boolean onlineSuggestionsEnabled;
    private static final String PREF_ONLINE_SUGGESTIONS = "online_suggestions_v1";
    private ImageView copyAddressButton;
    private ImageView pasteAddressButton;
    private ImageView favoriteAddressButton;
    private ImageView screenshotButton;
    private ProgressBar pageProgressBar;
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private boolean suspendMediaInBackground = true;
    private boolean chatgptCompat = false;
    private boolean facebookCompat = false;
    private static final String PREF_SUSPEND_MEDIA = "suspend_inactive_media_v1";
    private static final String PREF_CHATGPT_COMPAT = "compat_chatgpt_v1";
    private static final String PREF_FACEBOOK_COMPAT = "compat_facebook_v1";
    private final ArrayDeque<View> moduleScreens = new ArrayDeque<>();
    private FrameLayout moduleHost;
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
    private ImageView backIcon;
    private ImageView forwardIcon;
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
    private static final int REQUEST_SAVE_CAPTURE = 704;
    private File pendingCaptureExport;
    private static final long DOWNLOAD_REFRESH_MS = 2000L;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable downloadRefresh = new Runnable() {
        @Override public void run() {
            if (isFinishing() || isDestroyed() || downloadsPanel == null || !downloadsPanel.isShown()) return;
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
        boolean loading;
        int progress;
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
        // Ícones e legendas legíveis, sem sacrificar área desnecessária da página.
        return isCompactPhone() ? 68 : 74;
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
        updateFavoriteAddressButton();
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
        onlineSuggestionsEnabled = storage.getBoolean(PREF_ONLINE_SUGGESTIONS, true);
        suspendMediaInBackground = storage.getBoolean(PREF_SUSPEND_MEDIA, true);
        chatgptCompat = storage.getBoolean(PREF_CHATGPT_COMPAT, false);
        facebookCompat = storage.getBoolean(PREF_FACEBOOK_COMPAT, false);
        DiagnosticLog.install(getApplicationContext());
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
            registerExtensionActions();
            handleIncomingLink(getIntent());
            // Novidades são exibidas apenas uma vez por versionCode e reaparecem ao atualizar.
            if (state == null && WhatsNewActivity.shouldShow(storage)) {
                startActivity(new Intent(this, WhatsNewActivity.class));
            }
        } catch (RuntimeException e) {
            DiagnosticLog.record(getApplicationContext(), "INICIALIZACAO");
            showError(e);
        }
    }

    private void createLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(LIGHT_BG);
        browserRoot = root;
        FrameLayout layoutHost = new FrameLayout(this);
        layoutHost.addView(root, new FrameLayout.LayoutParams(-1, -1));
        moduleHost = layoutHost;
        setContentView(layoutHost);

        final boolean compactPhone = isCompactPhone();
        final int addressPanelHeight = compactPhone ? dp(34) : dp(38);
        final int addressInnerHeight = compactPhone ? dp(32) : dp(36);

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
            if (copyAddressButton != null) copyAddressButton.setVisibility(focused ? View.VISIBLE : View.GONE);
            if (pasteAddressButton != null) pasteAddressButton.setVisibility(focused ? View.VISIBLE : View.GONE);
            if (focused) {
                refreshSuggestionAdapter(urlBox.getText().toString());
                updatePasteButtonAvailability();
                urlBox.post(() -> {
                    if (!urlBox.hasFocus() || isFinishing() || isDestroyed()) return;
                    urlBox.selectAll();
                    if (current() == null || !current().privateMode) urlBox.showDropDown();
                });
            }
        });
        urlBox.setOnItemClickListener((parent, v, position, id) -> {
            // A lista pode mudar pela resposta assíncrona durante o toque.
            // A View selecionada conserva o resultado usado em sua renderização.
            Object tagged = v == null ? null : v.getTag();
            SuggestionItem selected = tagged instanceof SuggestionItem ? (SuggestionItem) tagged : null;
            if (selected == null) return;
            invalidateOnlineSuggestions();
            urlBox.dismissDropDown();
            hideKeyboard();
            setUrlBoxText(selected.value);
            try {
                navigate(BrowserAddress.resolve(selected.value));
            } catch (RuntimeException error) {
                DiagnosticLog.record(getApplicationContext(), "NAVEGACAO_SUGESTAO");
                Toast.makeText(this, "Não foi possível abrir a sugestão", Toast.LENGTH_LONG).show();
            }
        });
        urlBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                if (suppressSuggestionReload) return;
                refreshSuggestionAdapter(s.toString());
                if (urlBox.hasFocus()) urlBox.showDropDown();
                scheduleOnlineSuggestions(s.toString());
            }
        });
        historyAdapter = new AddressSuggestionAdapter();
        urlBox.setAdapter(historyAdapter);
        addressBox.addView(urlBox, new LinearLayout.LayoutParams(0, addressInnerHeight, 1));

        copyAddressButton = addressTool(R.drawable.ic_copy_address_xp, "Copiar endereço",
                () -> { copyAddressText(); });
        pasteAddressButton = addressTool(R.drawable.ic_paste_address_xp, "Colar texto",
                () -> { pasteAddressText(); });
        addressBox.addView(copyAddressButton, new LinearLayout.LayoutParams(dp(compactPhone ? 24 : 28), addressInnerHeight));
        addressBox.addView(pasteAddressButton, new LinearLayout.LayoutParams(dp(compactPhone ? 24 : 28), addressInnerHeight));
        favoriteAddressButton = addressTool(R.drawable.ic_star_outline_xp, "Adicionar página aos favoritos", this::toggleBookmark);
        favoriteAddressButton.setVisibility(View.VISIBLE);
        addressBox.addView(favoriteAddressButton, new LinearLayout.LayoutParams(dp(compactPhone ? 23 : 27), addressInnerHeight));

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
            scheduleOnlineSuggestions(urlBox.getText().toString());
            if (urlBox.isPopupShowing()) urlBox.dismissDropDown();
            else {
                urlBox.requestFocus();
                urlBox.showDropDown();
            }
        });
        addressBox.addView(dropdownIcon, new LinearLayout.LayoutParams(dp(compactPhone ? 22 : 26), addressInnerHeight));


        addressPanel.addView(addressBox, new LinearLayout.LayoutParams(0, addressPanelHeight, 1));

        ImageView refresh = new ImageView(this);
        refresh.setImageResource(R.drawable.ic_refresh_ie_xp);
        refresh.setContentDescription("Atualizar página");
        refresh.setPadding(dp(9), dp(7), dp(9), dp(7));
        refresh.setOnClickListener(v -> {
            hideKeyboard();
            Tab t = current();
            if (t != null) t.session.reload();
        });
        LinearLayout.LayoutParams refreshParams = new LinearLayout.LayoutParams(dp(compactPhone ? 30 : 40), addressPanelHeight);
        refreshParams.leftMargin = dp(compactPhone ? 4 : 8);
        addressPanel.addView(refresh, refreshParams);
        ImageView screenshot = new ImageView(this);
        screenshot.setImageResource(R.drawable.ic_screenshot_xp);
        screenshot.setColorFilter(XP_BLUE);
        screenshot.setContentDescription("Capturar imagem da página visível");
        screenshot.setPadding(dp(6), dp(6), dp(6), dp(6));
        screenshot.setOnClickListener(v -> captureVisiblePage());
        screenshotButton = screenshot;
        screenshotButton.setVisibility(View.GONE);
        addressPanel.addView(screenshot, new LinearLayout.LayoutParams(dp(compactPhone ? 28 : 36), addressPanelHeight));

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
            @Override public void addFavorite() { homeScreen.showAddFavoriteField(address -> addQuickAccess(address)); }
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
        downloadsPanel.addView(downloadsScroll, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(downloadsPanel);

        divider(root, 1);

        LinearLayout footer = new LinearLayout(this);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.setPadding(dp(12), 0, dp(12), 0);
        footer.setBackgroundColor(LIGHT_BG);
        status = text("Concluído", 11, SUBTLE);
        LinearLayout loadColumn = new LinearLayout(this);
        loadColumn.setOrientation(LinearLayout.VERTICAL);
        loadColumn.addView(status, new LinearLayout.LayoutParams(-1, dp(25)));
        pageProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pageProgressBar.setIndeterminate(false);
        pageProgressBar.setMax(100);
        pageProgressBar.setProgress(0);
        pageProgressBar.setProgressTintList(ColorStateList.valueOf(XP_BLUE));
        pageProgressBar.setProgressBackgroundTintList(ColorStateList.valueOf(0xffd8e0ea));
        pageProgressBar.setVisibility(View.GONE);
        loadColumn.addView(pageProgressBar, new LinearLayout.LayoutParams(-1, dp(3)));
        footer.addView(loadColumn, new LinearLayout.LayoutParams(0, dp(28), 1));
        TextView addons = text("▧ Extensões", 11, XP_BLUE);
        addons.setGravity(Gravity.CENTER);
        addons.setContentDescription("Abrir Central de Extensões");
        addons.setOnClickListener(v -> showExtensions());
        // O peso à esquerda faz a barra terminar exatamente no início de Extensões,
        // sem margens rígidas que quebram com zoom ou fontes maiores.
        footer.addView(addons, new LinearLayout.LayoutParams(-2, dp(28)));
        root.addView(footer);

        divider(root, 1);

        LinearLayout bottomBar = new LinearLayout(this);
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomBar.setGravity(Gravity.CENTER_VERTICAL);
        bottomBar.setBackgroundColor(PANEL_BG);
        bottomBar.setPadding(dp(compactPhone ? 2 : 4), dp(compactPhone ? 2 : 4), dp(compactPhone ? 2 : 4), dp(compactPhone ? 2 : 4));

        backIcon = navImage(bottomBar, R.drawable.ic_nav_back_xp, "Voltar", () -> {
            Tab t = current();
            if (t != null && t.back) t.session.goBack();
        });
        forwardIcon = navImage(bottomBar, R.drawable.ic_nav_forward_xp, "Avançar", () -> {
            Tab t = current();
            if (t != null && t.forward) t.session.goForward();
        });
        createTabNavigation(bottomBar);
        navImage(bottomBar, R.drawable.ic_download_xp, "Baixar", this::showDownloads);
        navImage(bottomBar, R.drawable.ic_nav_home_xp, "Início", () -> navigate(configuredHome()));
        navImage(bottomBar, R.drawable.ic_nav_menu_xp, "Menu", this::showMainMenu);
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
        TextView symbol = text(icon, compactPhone ? 26 : 29, XP_BLUE);
        symbol.setTypeface(Typeface.DEFAULT_BOLD);
        symbol.setGravity(Gravity.CENTER);
        cell.addView(symbol, new LinearLayout.LayoutParams(-1, dp(compactPhone ? 34 : 38)));
        TextView name = text(caption, compactPhone ? 9.5f : 10.5f, TEXT);
        name.setGravity(Gravity.CENTER);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        cell.addView(name, new LinearLayout.LayoutParams(-1, dp(19)));
        return symbol;
    }

    private ImageView navImage(LinearLayout parent, int drawable, String caption, Runnable action) {
        LinearLayout cell = navCell(parent, caption, action);
        ImageView icon = new ImageView(this);
        icon.setImageResource(drawable);
        icon.setColorFilter(XP_BLUE);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // Cinco vetores e Download são agora exibidos no mesmo quadro de 24dp.
        icon.setPadding(dp(7), dp(7), dp(7), dp(7));
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        cell.addView(icon, new LinearLayout.LayoutParams(-1, dp(isCompactPhone() ? 34 : 38)));
        TextView name = text(caption, isCompactPhone() ? 9.5f : 10.5f, TEXT);
        name.setGravity(Gravity.CENTER);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        cell.addView(name, new LinearLayout.LayoutParams(-1, dp(19)));
        return icon;
    }

    private void createTabNavigation(LinearLayout parent) {
        LinearLayout cell = navCell(parent, "Abas", this::showTabs);
        FrameLayout holder = new FrameLayout(this);
        cell.addView(holder, new LinearLayout.LayoutParams(-1, dp(isCompactPhone() ? 34 : 38)));
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_nav_tabs_xp);
        icon.setColorFilter(XP_BLUE);
        icon.setPadding(dp(7), dp(7), dp(7), dp(7));
        holder.addView(icon, new FrameLayout.LayoutParams(-1, -1));
        tabsBadge = text("1", 10, Color.WHITE);
        tabsBadge.setTypeface(Typeface.DEFAULT_BOLD);
        tabsBadge.setGravity(Gravity.CENTER);
        tabsBadge.setBackground(rounded(XP_BLUE, 0, 20));
        FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(dp(18), dp(18), Gravity.TOP | Gravity.RIGHT);
        badgeParams.rightMargin = dp(5);
        holder.addView(tabsBadge, badgeParams);
        TextView label = text("Abas", isCompactPhone() ? 9.5f : 10.5f, TEXT);
        label.setGravity(Gravity.CENTER);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        cell.addView(label, new LinearLayout.LayoutParams(-1, dp(19)));
    }

    private void showMainMenu() {
        ArrayList<MenuAction> primary = new ArrayList<>();
        ArrayList<MenuAction> secondary = new ArrayList<>();

        primary.add(new MenuAction("＋", "Nova aba", "Abrir uma nova aba.", () -> { addTab(configuredHome()); closeModuleScreens(); }));
        primary.add(new MenuAction("◌", "Privada", "Abrir uma aba privada.", () -> { addPrivateTab(configuredHome()); closeModuleScreens(); }));
        primary.add(new MenuAction("★", "Favoritos", "Seus sites salvos.", this::showBookmarks));
        primary.add(new MenuAction("🕘", "Histórico", "Páginas visitadas.", this::showHistory));
        primary.add(new MenuAction("↓", "Downloads", "Transferências do app.", this::showDownloads));
        primary.add(new MenuAction("⚙", "Configurações", "Página inicial e atalhos.", this::showBrowserSettings));

        secondary.add(new MenuAction("↻", "Atualizar página", "Recarregue o site atual caso algo não tenha carregado direito.", () -> { closeModuleScreens(); Tab t = current(); if (t != null) t.session.reload(); }));
        secondary.add(new MenuAction("🔒", "Privacidade e dados", "Limpe cookies, cache, permissões e ajuste o modo de proteção.", this::showPrivacyMenu));
        secondary.add(new MenuAction("🧩", "Extensões", "Instalar, importar, excluir e configurar extensões Firefox.", this::showExtensions));
        secondary.add(new MenuAction("▣", "Ações das extensões", "Execute botões dos complementos ativos, inclusive pop-ups.", this::showExtensionActions));
        secondary.add(new MenuAction("⇪", "Compartilhar página", "Copie ou compartilhe o endereço aberto em outros aplicativos.", this::share));
        secondary.add(new MenuAction("ⓘ", "Novidades desta versão", "Consulte a tela com o resumo estilizado das últimas alterações.", () -> startActivity(new Intent(this, WhatsNewActivity.class))));
        secondary.add(new MenuAction("ℹ", "Sobre", "Informações da versão, motor e principais recursos do navegador.", this::showAbout));
        secondary.add(new MenuAction("🌐", "Navegador padrão", "Peça ao Android para oferecer o Internet XP ao abrir links HTTP e HTTPS.", this::requestDefaultBrowserRole));

        secondary.add(new MenuAction("⚠", "Captura de erros", "Falhas recentes, relatório e informações de diagnóstico.", this::showDiagnostics));
        showMainMenuHubDialog("Internet XP", "Menu rápido com aparência mais viva, atalhos principais em destaque e ferramentas extras abaixo.", primary, secondary);
    }

    /** Painel de menu real. Não cria Window/Dialog em cima do GeckoView. */
    private void showMainMenuHubDialog(String title, String subtitle, List<MenuAction> primary, List<MenuAction> secondary) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(16), dp(12), dp(18));
        scroll.addView(content);
        TextView quick = text("Acesso rápido", 16, XP_BLUE);
        quick.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(quick);
        TextView hint = text("Atalhos principais do navegador", 12, SUBTLE);
        hint.setPadding(0, 0, 0, dp(12));
        content.addView(hint);
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        content.addView(grid);
        int cols = isTinyPhone() ? 2 : 3;
        for (int i = 0; i < primary.size(); i += cols) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
            rowParams.bottomMargin = dp(10);
            grid.addView(row, rowParams);
            for (int c = 0; c < cols; c++) {
                int index = i + c;
                if (index < primary.size()) row.addView(createHubTile(primary.get(index), null),
                        new LinearLayout.LayoutParams(0, -2, 1));
                else row.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));
                if (c < cols - 1) row.addView(new View(this), new LinearLayout.LayoutParams(dp(8), 1));
            }
        }
        TextView tools = text("Mais ferramentas", 16, XP_BLUE);
        tools.setTypeface(Typeface.DEFAULT_BOLD);
        tools.setPadding(0, dp(10), 0, dp(10));
        content.addView(tools);
        for (MenuAction action : secondary) {
            LinearLayout row = createCompactMenuRow(action, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(9);
            content.addView(row, params);
        }
        showModuleScreen("Menu Internet XP", subtitle, scroll);
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
        if ("🌐".equals(action.icon) && FaviconRepository.origin(action.description) != null) {
            ImageView favicon = new ImageView(this);
            favicon.setImageResource(R.drawable.ic_globe_xp);
            row.removeView(icon);
            row.addView(favicon, 0, new LinearLayout.LayoutParams(dp(30), dp(30)));
            faviconRepository.load(action.description, favicon);
        }

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
        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(14), dp(12), dp(16));
        scroll.addView(list);
        for (MenuAction action : actions) {
            LinearLayout row = createCompactMenuRow(action, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(10);
            list.addView(row, params);
        }
        showModuleScreen(title, subtitle, scroll);
    }

    /** Pilha de telas internas, mantendo a mesma sessão GeckoView intacta por baixo. */
    private void showModuleScreen(String title, String subtitle, View body) {
        hideKeyboard();
        if (!moduleScreens.isEmpty()) moduleScreens.peek().setVisibility(View.GONE);
        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(LIGHT_BG);
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(12), dp(12), dp(14), dp(12));
        header.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xff3789ef, XP_BLUE}));
        TextView back = text("‹", 34, Color.WHITE);
        back.setGravity(Gravity.CENTER);
        back.setContentDescription("Voltar para a tela anterior");
        back.setOnClickListener(v -> popModuleScreen());
        header.addView(back, new LinearLayout.LayoutParams(dp(48), dp(54)));
        LinearLayout headings = new LinearLayout(this);
        headings.setOrientation(LinearLayout.VERTICAL);
        TextView name = text(title, 20, Color.WHITE);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        headings.addView(name);
        TextView description = text(subtitle, 11, 0xffe5f0ff);
        description.setMaxLines(2);
        headings.addView(description);
        header.addView(headings, new LinearLayout.LayoutParams(0, -2, 1));
        screen.addView(header);
        screen.addView(body, new LinearLayout.LayoutParams(-1, 0, 1));
        moduleHost.addView(screen, new FrameLayout.LayoutParams(-1, -1));
        moduleScreens.push(screen);
    }

    private void popModuleScreen() {
        if (moduleScreens.isEmpty()) return;
        View page = moduleScreens.pop();
        if (downloadsPanel != null && downloadsPanel.getParent() == page) {
            ((ViewGroup) page).removeView(downloadsPanel);
            browserRoot.addView(downloadsPanel, 3);
            downloadsPanel.setVisibility(View.GONE);
            uiHandler.removeCallbacks(downloadRefresh);
        }
        moduleHost.removeView(page);
        if (!moduleScreens.isEmpty()) moduleScreens.peek().setVisibility(View.VISIBLE);
    }

    private void closeModuleScreens() {
        while (!moduleScreens.isEmpty()) popModuleScreen();
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
        actions.add(new MenuAction("🔎", "Sugestões online: " + (onlineSuggestionsEnabled ? "ligadas" : "desligadas"),
                "Quando ligadas, o texto digitado é enviado ao DuckDuckGo após 350 ms. Não envia em abas privadas.",
                () -> { onlineSuggestionsEnabled = !onlineSuggestionsEnabled;
                    storage.edit().putBoolean(PREF_ONLINE_SUGGESTIONS, onlineSuggestionsEnabled).apply();
                    invalidateOnlineSuggestions();
                    Toast.makeText(this, onlineSuggestionsEnabled ? "Sugestões online ativadas" : "Sugestões online desativadas", Toast.LENGTH_SHORT).show();
                    popModuleScreen(); showBrowserSettings(); }));
        actions.add(new MenuAction("⚡", "Desempenho e sites", "Mídia em abas inativas, ChatGPT e Facebook; opções com efeito real.", this::showPerformanceSettings));
        actions.add(new MenuAction("⚠", "Diagnósticos", "Falhas de páginas e fechamento; exportar relatório local.", this::showDiagnostics));
        actions.add(new MenuAction("⚙", "Ver página inicial", "Abra a página inicial definida nas configurações.", () -> navigate(configuredHome())));
        showModuleMenuDialog("Configurações", "Personalize o início da navegação e os atalhos do Internet XP.", actions);
    }

    private void showPerformanceSettings() {
        ArrayList<MenuAction> actions = new ArrayList<>();
        actions.add(new MenuAction("♫", "Pausar mídia em abas inativas: " + (suspendMediaInBackground ? "sim" : "não"),
                "Reduz atividade de mídia ao trocar de aba. Pode interferir em áudio em segundo plano.", () -> {
                    suspendMediaInBackground = !suspendMediaInBackground;
                    storage.edit().putBoolean(PREF_SUSPEND_MEDIA, suspendMediaInBackground).apply();
                    for (Tab tab : tabs) tab.session.getSettings().setSuspendMediaWhenInactive(suspendMediaInBackground);
                    popModuleScreen(); showPerformanceSettings();
                }));
        actions.add(new MenuAction("🤖", "Compatibilidade ChatGPT: " + (chatgptCompat ? "ligada" : "padrão"),
                "Se ativada, desliga a proteção antirrastreamento APENAS em chatgpt.com e seus subdomínios. Reduz privacidade; não garante ganho de velocidade.", () -> {
                    chatgptCompat = !chatgptCompat;
                    storage.edit().putBoolean(PREF_CHATGPT_COMPAT, chatgptCompat).apply();
                    for (Tab tab : tabs) applySitePreferences(tab);
                    popModuleScreen(); showPerformanceSettings();
                }));
        actions.add(new MenuAction("f", "Compatibilidade Facebook: " + (facebookCompat ? "ligada" : "padrão"),
                "Se ativada, desliga a proteção antirrastreamento APENAS em facebook.com e seus subdomínios. Reduz privacidade; não garante ganho de velocidade.", () -> {
                    facebookCompat = !facebookCompat;
                    storage.edit().putBoolean(PREF_FACEBOOK_COMPAT, facebookCompat).apply();
                    for (Tab tab : tabs) applySitePreferences(tab);
                    popModuleScreen(); showPerformanceSettings();
                }));
        showModuleMenuDialog("Desempenho e compatibilidade", "Modo padrão recomendado. Ajustes de compatibilidade são opcionais e reversíveis.", actions);
    }

    private void showDiagnostics() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(14), dp(12), dp(14), dp(12));
        addSecurityDetail(page, "Versão", ReleaseNotes.VERSION_NAME + " • versionCode " + ReleaseNotes.VERSION_CODE);
        addSecurityDetail(page, "Motor", "Mozilla GeckoView 158");
        addSecurityDetail(page, "Android", String.valueOf(Build.VERSION.SDK_INT));
        addSecurityDetail(page, "Eventos locais recentes", DiagnosticLog.read(getApplicationContext()));
        TextView share = text("Compartilhar relatório de diagnóstico  ›", 15, XP_BLUE);
        share.setGravity(Gravity.CENTER);
        share.setBackground(rounded(Color.WHITE, BORDER, 12));
        share.setOnClickListener(v -> {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_TEXT, DiagnosticLog.report(getApplicationContext(), ReleaseNotes.VERSION_NAME));
            startActivity(Intent.createChooser(send, "Compartilhar relatório Internet XP"));
        });
        page.addView(share, new LinearLayout.LayoutParams(-1, dp(54)));
        TextView clear = text("Limpar diagnóstico local", 14, XP_BLUE);
        clear.setGravity(Gravity.CENTER);
        clear.setOnClickListener(v -> new XpDialogs.Builder(this).setTitle("Limpar diagnóstico?")
                .setMessage("Apaga somente eventos técnicos locais. Histórico e favoritos permanecem.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Limpar", (d, w) -> { DiagnosticLog.clear(getApplicationContext()); popModuleScreen(); showDiagnostics(); }).show());
        page.addView(clear, new LinearLayout.LayoutParams(-1, dp(52)));
        ScrollView scroll = new ScrollView(this);
        scroll.addView(page);
        showModuleScreen("Diagnósticos do navegador", "Relatórios sem cookies, senhas ou URLs completas", scroll);
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
        AlertDialog d = new XpDialogs.Builder(this).setView(content)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String entered = input.getText().toString().trim();
                    String resolved = BrowserAddress.resolve(entered);
                    if (entered.isEmpty() || HOME.equals(HomePageSettings.normalize(entered))) {
                        Toast.makeText(this, "Informe o endereço válido de um site", Toast.LENGTH_LONG).show();
                    } else setHomePage(resolved);
                }).setNegativeButton("Cancelar", null).create();
        d.show(); compactDialog(d);
    }

    private void addQuickAccess(String rawAddress) {
        if (current() != null && current().privateMode) {
            Toast.makeText(this, "Favoritos indisponíveis em aba privada", Toast.LENGTH_SHORT).show();
            return;
        }
        if (HOME.equals(HomePageSettings.normalize(rawAddress))) {
            Toast.makeText(this, "Informe um endereço válido", Toast.LENGTH_SHORT).show();
            return;
        }
        String address = BrowserAddress.resolve(rawAddress);
        if (!address.startsWith("https://") && !address.startsWith("http://")) return;
        bookmarks.remove(address);
        bookmarks.add(0, address);
        saveStringList(PREF_BOOKMARKS, bookmarks);
        refreshSuggestionAdapter("");
        updateHomeScreen();
    }

    private void updateFavoriteAddressButton() {
        if (favoriteAddressButton == null) return;
        Tab tab = current();
        boolean eligible = tab != null && !tab.privateMode &&
                (tab.url.startsWith("http://") || tab.url.startsWith("https://"));
        favoriteAddressButton.setVisibility(eligible ? View.VISIBLE : View.GONE);
        if (eligible) {
            boolean saved = bookmarks.contains(tab.url);
            favoriteAddressButton.setImageResource(saved ? R.drawable.ic_star_filled_xp : R.drawable.ic_star_outline_xp);
            favoriteAddressButton.setContentDescription(saved ? "Remover dos favoritos" : "Adicionar aos favoritos");
        }
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
        AlertDialog d = new XpDialogs.Builder(this).setView(content).setPositiveButton("Adicionar", (dialog, which) -> {
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
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14), dp(20), dp(14), dp(20));
        scroll.addView(content);
        for (String line : new String[]{"Motor de navegação: GeckoView 158", "Android 8.0 ou superior",
                "Página inicial com pesquisa e atalhos", "Extensões, downloads, abas e privacidade",
                "Projeto independente, sem vínculo com Mozilla ou Microsoft"}) {
            TextView detail = text("•  " + line, 14, TEXT);
            detail.setPadding(dp(14), dp(16), dp(12), dp(16));
            detail.setBackground(rounded(Color.WHITE, BORDER, 12));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(10);
            content.addView(detail, params);
        }
        showModuleScreen("Sobre • " + ReleaseNotes.VERSION_NAME,
                "Informações e recursos do navegador Internet XP", scroll);
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
            if (!urlBox.hasFocus() || isFinishing() || isDestroyed()) return;
            urlBox.selectAll();
            urlBox.setSelection(0, urlBox.getText().length());
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(urlBox, InputMethodManager.SHOW_IMPLICIT);
            refreshSuggestionAdapter(urlBox.getText().toString());
            if (showSuggestions && (current() == null || !current().privateMode)) urlBox.showDropDown();
        });
    }

    private void submitAddress() {
        invalidateOnlineSuggestions();
        String value = urlBox.getText().toString();
        hideKeyboard();
        navigate(BrowserAddress.resolve(value));
    }

    private void navigate(String url) {
        closeModuleScreens();
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
                .suspendMediaWhenInactive(suspendMediaInBackground)
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
                DiagnosticLog.record(getApplicationContext(), "GECKOVIEW_CRASH");
                tab.loading = false;
                if (tab == current()) status.setText("Processo da página falhou • Diagnóstico no Menu");
            }
            @Override public void onKill(GeckoSession s) {
                DiagnosticLog.record(getApplicationContext(), "GECKOVIEW_KILL");
                tab.loading = false;
                if (tab == current()) status.setText("Processo da página encerrado • Diagnóstico no Menu");
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
                attachExtensionActionListeners(popup.session);
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
                applySitePreferences(tab);
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
                tab.loading = true;
                tab.progress = 0;
                if (tab == current()) {
                    updatePageProgress();
                    updateSecurity(tab.url);
                }
            }
            @Override public void onProgressChange(GeckoSession s, int progress) {
                tab.progress = Math.max(0, Math.min(100, progress));
                if (tab == current()) updatePageProgress();
            }
            @Override public void onSecurityChange(GeckoSession s,
                    GeckoSession.ProgressDelegate.SecurityInformation info) {
                tab.securityInfo = info;
                if (tab == current()) updateSecurity(tab.url);
            }
            @Override public void onPageStop(GeckoSession s, boolean success) {
                tab.loading = false;
                tab.progress = success ? 100 : tab.progress;
                if (!success) DiagnosticLog.record(getApplicationContext(), "FALHA_CARREGAMENTO");
                if (tab == current()) updatePageProgress();
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
            attachExtensionActionListeners(tab.session);
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
        updatePageProgress();
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

    private void updatePageProgress() {
        Tab tab = current();
        if (status == null || pageProgressBar == null || tab == null) return;
        if (screenshotButton != null) screenshotButton.setVisibility(
                !tab.loading && tab.progress == 100 && !HOME.equals(tab.url) ? View.VISIBLE : View.GONE);
        progressHandler.removeCallbacksAndMessages(null);
        if (tab.loading) {
            status.setText((tab.privateMode ? "Privado • " : "") + "Carregando " + tab.progress + "%");
            pageProgressBar.setProgress(tab.progress);
            pageProgressBar.setVisibility(View.VISIBLE);
        } else if (tab.progress == 100) {
            status.setText(tab.privateMode ? "Privado • Concluído" : "Concluído");
            pageProgressBar.setProgress(100);
            pageProgressBar.setVisibility(View.VISIBLE);
            progressHandler.postDelayed(() -> {
                if (tab == current() && !tab.loading) pageProgressBar.setVisibility(View.GONE);
            }, 700);
        } else {
            status.setText(tab.privateMode ? "Privado • Erro ao carregar" : "Erro ao carregar");
            pageProgressBar.setVisibility(View.GONE);
        }
    }

    private void applySitePreferences(Tab tab) {
        if (tab == null) return;
        boolean exception = SitePerformanceRules.isCompatibilityMode(tab.url, chatgptCompat, facebookCompat);
        tab.session.getSettings().setUseTrackingProtection(trackingProtectionEnabled && !exception);
    }

    private void updateChrome() {
        updateArrows();
        drawTabs();
        updateFavoriteAddressButton();
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
        backIcon.setColorFilter(t != null && t.back ? XP_BLUE : BORDER);
        forwardIcon.setColorFilter(t != null && t.forward ? XP_BLUE : BORDER);
    }

    private void drawTabs() {
        if (tabsBadge == null) return;
        Tab selected = current();
        tabsBadge.setText(String.valueOf(tabs.size()));
        tabsBadge.setBackground(rounded(selected != null && selected.privateMode ? PRIVATE_PURPLE : XP_BLUE, 0, 20));
        tabsBadge.setContentDescription((selected != null && selected.privateMode ? "Privada, " : "") +
                (tabs.size() == 1 ? "1 aba" : tabs.size() + " abas"));
    }

    private void showTabs() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);
        TextView add = text("＋  Nova aba", 15, Color.WHITE);
        add.setTypeface(Typeface.DEFAULT_BOLD);
        add.setGravity(Gravity.CENTER);
        add.setBackground(rounded(XP_BLUE, 0, 12));
        add.setOnClickListener(v -> { addTab(configuredHome()); closeModuleScreens(); });
        actions.addView(add, new LinearLayout.LayoutParams(0, dp(50), 1));
        TextView privateButton = text("♟  Privada", 15, PRIVATE_PURPLE);
        privateButton.setTypeface(Typeface.DEFAULT_BOLD);
        privateButton.setGravity(Gravity.CENTER);
        privateButton.setBackground(rounded(0xfff1eafb, BORDER, 12));
        privateButton.setOnClickListener(v -> { addPrivateTab(configuredHome()); closeModuleScreens(); });
        LinearLayout.LayoutParams privateParams = new LinearLayout.LayoutParams(0, dp(50), 1);
        privateParams.leftMargin = dp(8);
        actions.addView(privateButton, privateParams);
        page.addView(actions);
        TextView subtitle = text("Toque em uma aba para abrir • × para fechar", 12, SUBTLE);
        subtitle.setPadding(dp(2), dp(12), dp(2), dp(12));
        page.addView(subtitle);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        populateTabCards(list, null);
        showModuleScreen("Abas abertas • " + tabs.size(), "Organize abas normais e privadas", page);
    }

    private void populateTabCards(LinearLayout container, AlertDialog dialog) {
        container.removeAllViews();
        for (int i = 0; i < tabs.size(); i++) {
            final int index = i;
            Tab tab = tabs.get(i);
            LinearLayout card = new LinearLayout(this);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(12), dp(9), dp(4), dp(9));
            int background = tab.privateMode ? 0xfff4effa : index == active ? 0xffe5f1ff : Color.WHITE;
            card.setBackground(rounded(background, index == active ? XP_BLUE : BORDER, 12));
            card.setContentDescription("Abrir " + tab.title);
            card.setOnClickListener(v -> { selectTab(index); closeModuleScreens(); if (dialog != null) dialog.dismiss(); });
            ImageView favicon = new ImageView(this);
            favicon.setImageResource(R.drawable.ic_globe_xp);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dp(30), dp(30));
            imageParams.rightMargin = dp(12);
            card.addView(favicon, imageParams);
            if (!tab.privateMode && !HOME.equals(tab.url)) faviconRepository.load(tab.url, favicon);
            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            TextView name = text(tab.title == null || tab.title.trim().isEmpty() ? "Nova aba" : tab.title, 14, TEXT);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            name.setSingleLine(true);
            name.setEllipsize(android.text.TextUtils.TruncateAt.END);
            info.addView(name);
            TextView address = text(HOME.equals(tab.url) ? "Página inicial" : PrivacyRules.displayHost(tab.url), 11, SUBTLE);
            address.setSingleLine(true);
            address.setEllipsize(android.text.TextUtils.TruncateAt.END);
            info.addView(address);
            TextView tag = text((tab.privateMode ? "● Privada" : "● Normal") + (index == active ? " • Atual" : ""), 10,
                    tab.privateMode ? PRIVATE_PURPLE : XP_BLUE);
            info.addView(tag);
            card.addView(info, new LinearLayout.LayoutParams(0, dp(62), 1));
            TextView close = text("×", 27, XP_BLUE);
            close.setGravity(Gravity.CENTER);
            close.setContentDescription("Fechar aba: " + tab.title);
            close.setOnClickListener(v -> {
                closeTab(index);
                populateTabCards(container, dialog);
                if (dialog != null) dialog.setTitle("Abas abertas • " + tabs.size());
            });
            card.addView(close, new LinearLayout.LayoutParams(dp(46), dp(50)));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(9);
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
        ArrayList<SuggestionItem> rows = new ArrayList<>();
        ArrayList<String> sites = buildSuggestions(query);
        for (String site : sites) {
            String host = PrivacyRules.displayHost(site);
            rows.add(new SuggestionItem(site, host, bookmarks.contains(site) ? "Favorito" : "Histórico", true));
        }
        if (onlineSuggestionsEnabled && query != null && query.equals(lastOnlineQuery)
                && (current() == null || !current().privateMode)) {
            for (String hint : onlineSuggestions) {
                if (rows.size() >= 24) break;
                rows.add(new SuggestionItem(hint, hint, "Sugestão DuckDuckGo", false));
            }
        }
        historyAdapter.update(rows);
    }

    private ImageView addressTool(int icon, String label, Runnable action) {
        ImageView button = new ImageView(this);
        button.setImageResource(icon);
        button.setColorFilter(XP_BLUE);
        button.setPadding(dp(5), dp(8), dp(5), dp(8));
        button.setContentDescription(label);
        button.setVisibility(View.GONE);
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private void copyAddressText() {
        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (manager == null) return;
        manager.setPrimaryClip(ClipData.newPlainText("Endereço Internet XP", urlBox.getText().toString()));
        Toast.makeText(this, "Endereço copiado", Toast.LENGTH_SHORT).show();
    }

    private void updatePasteButtonAvailability() {
        if (pasteAddressButton == null) return;
        boolean hasText = false;
        try {
            ClipboardManager clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            hasText = clip != null && clip.hasPrimaryClip();
        } catch (RuntimeException ignored) { }
        pasteAddressButton.setVisibility(urlBox != null && urlBox.hasFocus() && hasText
                ? View.VISIBLE : View.GONE);
    }

    private void pasteAddressText() {
        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (manager == null || !manager.hasPrimaryClip()) return;
        ClipData clip = manager.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) return;
        CharSequence text = clip.getItemAt(0).coerceToText(this);
        if (text == null) return;
        String safe = text.toString();
        if (safe.length() > 2048) safe = safe.substring(0, 2048);
        urlBox.setText(safe);
        urlBox.setSelection(urlBox.length());
        urlBox.requestFocus();
        updatePasteButtonAvailability();
    }

    private void captureVisiblePage() {
        if (view == null || current() == null || HOME.equals(current().url)) {
            Toast.makeText(this, "Abra uma página para capturar", Toast.LENGTH_SHORT).show();
            return;
        }
        hideKeyboard();
        view.capturePixels().accept(bitmap -> {
            if (bitmap == null) return;
            Toast.makeText(this, "Salvando captura…", Toast.LENGTH_SHORT).show();
            suggestionWorker.execute(() -> {
                try {
                    File folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Capturas");
                    if (!folder.exists() && !folder.mkdirs()) throw new java.io.IOException("Pasta indisponível");
                    File image = new File(folder, "InternetXP-" + System.currentTimeMillis() + ".png");
                    try (FileOutputStream out = new FileOutputStream(image)) {
                        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) throw new java.io.IOException("Falha ao salvar PNG");
                    }
                    runOnUiThread(() -> {
                        try {
                            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".files", image);
                            Intent share = new Intent(Intent.ACTION_SEND);
                            share.setType("image/png");
                            share.putExtra(Intent.EXTRA_STREAM, uri);
                            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            new XpDialogs.Builder(this).setTitle("Captura salva")
                                .setMessage("O PNG está no armazenamento privado do Internet XP. Para guardá-lo numa pasta sua, use Salvar cópia.")
                                .setPositiveButton("Salvar cópia", (d, w) -> exportScreenshot(image))
                                .setNeutralButton("Compartilhar", (d, w) -> startActivity(Intent.createChooser(share, "Compartilhar captura")))
                                .setNegativeButton("Fechar", null).show();
                        } catch (Exception e) { showError(e); }
                    });
                } catch (Exception e) { runOnUiThread(() -> showError(e)); }
                finally { bitmap.recycle(); }
            });
        }, error -> runOnUiThread(() -> showError(error)));
    }

    private void exportScreenshot(File image) {
        if (image == null || !image.isFile()) return;
        pendingCaptureExport = image;
        Intent save = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        save.addCategory(Intent.CATEGORY_OPENABLE);
        save.setType("image/png");
        save.putExtra(Intent.EXTRA_TITLE, image.getName());
        try {
            startActivityForResult(save, REQUEST_SAVE_CAPTURE);
        } catch (ActivityNotFoundException error) {
            pendingCaptureExport = null;
            Toast.makeText(this, "Seletor de pasta indisponível", Toast.LENGTH_LONG).show();
        }
    }

    private void invalidateOnlineSuggestions() {
        ++suggestionRequestId;
        if (pendingSuggestionRequest != null) suggestionHandler.removeCallbacks(pendingSuggestionRequest);
        pendingSuggestionRequest = null;
        lastOnlineQuery = "";
        onlineSuggestions.clear();
        if (historyAdapter != null && urlBox != null) refreshSuggestionAdapter(urlBox.getText().toString());
    }

    private void scheduleOnlineSuggestions(String input) {
        invalidateOnlineSuggestions();
        if (!onlineSuggestionsEnabled || (current() != null && current().privateMode)
                || !OnlineSuggestions.isEligibleQuery(input)) return;
        final String query = input.trim();
        final int token = suggestionRequestId;
        pendingSuggestionRequest = () -> suggestionWorker.execute(() -> {
            ArrayList<String> received = OnlineSuggestions.fetch(query);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed() || token != suggestionRequestId ||
                        !urlBox.hasFocus() || (current() != null && current().privateMode) ||
                        !query.equals(urlBox.getText().toString().trim())) return;
                onlineSuggestions.clear();
                onlineSuggestions.addAll(received);
                lastOnlineQuery = urlBox.getText().toString();
                refreshSuggestionAdapter(lastOnlineQuery);
                if (historyAdapter.getCount() > 0 && !urlBox.isPopupShowing()) urlBox.showDropDown();
            });
        });
        suggestionHandler.postDelayed(pendingSuggestionRequest, 350);
    }

    /** Lista heterogênea, com ícone do domínio para favoritos/histórico e lupa nas buscas. */
    private static final class SuggestionItem {
        final String value, label, source;
        final boolean site;
        SuggestionItem(String value, String label, String source, boolean site) {
            this.value = value; this.label = label; this.source = source; this.site = site;
        }
    }

    private final class AddressSuggestionAdapter extends BaseAdapter implements Filterable {
        // Snapshot imutável evita corrida com o filtro executado em thread auxiliar.
        private volatile List<SuggestionItem> entries = new ArrayList<>();
        void update(List<SuggestionItem> items) { entries = new ArrayList<>(items); notifyDataSetChanged(); }
        @Override public int getCount() { return entries.size(); }
        @Override public SuggestionItem getItem(int position) {
            List<SuggestionItem> snapshot = entries;
            return position < 0 || position >= snapshot.size() ? null : snapshot.get(position);
        }
        @Override public long getItemId(int position) { return position; }
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            SuggestionItem item = getItem(position);
            LinearLayout line = new LinearLayout(MainActivity.this);
            if (item == null) return line;
            line.setGravity(Gravity.CENTER_VERTICAL);
            line.setTag(item);
            line.setPadding(dp(12), dp(7), dp(12), dp(7));
            ImageView icon = new ImageView(MainActivity.this);
            icon.setImageResource(item.site ? R.drawable.ic_globe_xp : android.R.drawable.ic_menu_search);
            icon.setPadding(dp(3), dp(3), dp(3), dp(3));
            line.addView(icon, new LinearLayout.LayoutParams(dp(30), dp(30)));
            if (item.site) faviconRepository.load(item.value, icon);
            LinearLayout labels = new LinearLayout(MainActivity.this);
            labels.setOrientation(LinearLayout.VERTICAL);
            labels.setPadding(dp(10), 0, 0, 0);
            TextView name = text(item.label, 14, TEXT);
            name.setSingleLine(true);
            name.setEllipsize(android.text.TextUtils.TruncateAt.END);
            labels.addView(name);
            TextView detail = text(item.site ? item.source + " • " + item.value : item.source, 11, SUBTLE);
            detail.setSingleLine(true);
            detail.setEllipsize(android.text.TextUtils.TruncateAt.END);
            labels.addView(detail);
            line.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
            return line;
        }
        private final Filter filter = new Filter() {
            @Override protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults result = new FilterResults();
                // Os itens já foram filtrados por histórico e atualizados pela rede na UI.
                result.values = null;
                result.count = entries.size();
                return result;
            }
            @Override protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }
            @Override public CharSequence convertResultToString(Object value) {
                return value instanceof SuggestionItem ? ((SuggestionItem) value).value : "";
            }
        };
        @Override public Filter getFilter() { return filter; }
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
        updateFavoriteAddressButton();
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
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(12), dp(12), dp(12), dp(12));
        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setHint("Pesquisar título ou endereço");
        search.setPadding(dp(10), 0, dp(10), 0);
        search.setBackground(rounded(Color.WHITE, BORDER, 10));
        page.addView(search, new LinearLayout.LayoutParams(-1, dp(48)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        results.setPadding(0, dp(12), 0, dp(6));
        scroll.addView(results);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        TextView clear = text("Limpar histórico", 14, XP_BLUE);
        clear.setGravity(Gravity.CENTER);
        clear.setOnClickListener(v -> new XpDialogs.Builder(this).setTitle("Limpar histórico?")
                .setMessage("Os registros locais serão apagados. Cookies e cache permanecerão.")
                .setPositiveButton("Apagar", (d, n) -> { clearHistoryData(); populateHistoryRows(results, "", null); })
                .setNegativeButton("Cancelar", null).show());
        page.addView(clear, new LinearLayout.LayoutParams(-1, dp(52)));
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                populateHistoryRows(results, s.toString(), null);
            }
            @Override public void afterTextChanged(Editable editable) { }
        });
        populateHistoryRows(results, "", null);
        showModuleScreen("Histórico", "Pesquise páginas visitadas e remova registros", page);
        page.setFocusableInTouchMode(true);
        page.requestFocus();
    }

    private void clearHistoryData() {
        history.clear(); historyDetails.clear();
        saveStringList(PREF_HISTORY, history);
        saveHistoryDetails();
        refreshSuggestionAdapter("");
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
                if (dialog != null) dialog.dismiss();
            });
            ImageView icon = new ImageView(this);
            icon.setImageResource(R.drawable.ic_globe_xp);
            icon.setPadding(dp(4), dp(4), dp(4), dp(4));
            row.addView(icon, new LinearLayout.LayoutParams(dp(36), dp(36)));
            faviconRepository.load(entry.url, icon);
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
        new XpDialogs.Builder(this)
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
        if (downloadsPanel.getParent() instanceof ViewGroup)
            ((ViewGroup) downloadsPanel.getParent()).removeView(downloadsPanel);
        downloadsPanel.setVisibility(View.VISIBLE);
        showModuleScreen("Downloads", "Transferências gerenciadas pelo Internet XP", downloadsPanel);
        renderDownloadsPanel();
        scheduleDownloadRefresh();
    }

    private void hideDownloadsPanel() {
        if (!moduleScreens.isEmpty() && downloadsPanel.getParent() == moduleScreens.peek()) popModuleScreen();
        downloadsPanel.setVisibility(View.GONE);
        uiHandler.removeCallbacks(downloadRefresh);
    }

    private void scheduleDownloadRefresh() {
        uiHandler.removeCallbacks(downloadRefresh);
        if (downloadsPanel != null && downloadsPanel.isShown() && hasActiveDownloads()) {
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
                if (ExtensionCompatibility.isXpiFileName(item.fileName))
                    addDownloadAction(actions, "Instalar XPI", () -> installDownloadedExtension(item));
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
        new XpDialogs.Builder(this).setTitle("Limpar registros finalizados?")
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
        new XpDialogs.Builder(this).setTitle("Detalhes do download").setMessage(message)
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
        new XpDialogs.Builder(this).setTitle("Cancelar download?")
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
        new XpDialogs.Builder(this)
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
        new XpDialogs.Builder(this)
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
        if (requestCode == REQUEST_SAVE_CAPTURE) {
            File source = pendingCaptureExport;
            pendingCaptureExport = null;
            Uri destination = data == null ? null : data.getData();
            if (resultCode == RESULT_OK && source != null && destination != null && "content".equals(destination.getScheme())) {
                suggestionWorker.execute(() -> {
                    try (java.io.InputStream input = new java.io.FileInputStream(source);
                         java.io.OutputStream output = getContentResolver().openOutputStream(destination, "w")) {
                        if (output == null) throw new java.io.IOException("Destino indisponível");
                        byte[] buffer = new byte[8192];
                        int n;
                        while ((n = input.read(buffer)) != -1) output.write(buffer, 0, n);
                        runOnUiThread(() -> Toast.makeText(this, "Captura exportada", Toast.LENGTH_SHORT).show());
                    } catch (Exception error) {
                        DiagnosticLog.record(getApplicationContext(), "EXPORTAR_CAPTURA");
                        runOnUiThread(() -> Toast.makeText(this, "Erro ao salvar cópia da captura", Toast.LENGTH_LONG).show());
                    }
                });
            }
            return;
        }
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
        if (urlBox != null && urlBox.hasFocus()) hideKeyboard();

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
        AlertDialog dialog = new XpDialogs.Builder(this)
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
        if (urlBox != null && urlBox.hasFocus()) hideKeyboard();

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
            new XpDialogs.Builder(this)
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
        new XpDialogs.Builder(this)
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
        for (Tab tab : tabs) applySitePreferences(tab);
        Toast.makeText(this, trackingProtectionEnabled ? "Proteção antirrastreamento ativada" :
                "Proteção antirrastreamento desativada", Toast.LENGTH_LONG).show();
    }

    private void showSiteInfo() {
        if (urlBox != null && urlBox.hasFocus()) hideKeyboard();
        Tab tab = current();
        if (tab == null) return;
        GeckoSession.ProgressDelegate.SecurityInformation info = tab.securityInfo;
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(14), dp(16), dp(14), dp(12));
        String label = PrivacyRules.displayHost(tab.url);
        boolean secure = info != null && PrivacyRules.isVerifiedSecure(tab.url, info.isSecure,
                info.isException, info.mixedModeActive == GeckoSession.ProgressDelegate.SecurityInformation.CONTENT_LOADED);
        String state = HOME.equals(tab.url) ? "Página inicial" :
                !tab.url.startsWith("https://") ? "Sem HTTPS" : info == null ? "Verificação pendente" :
                secure ? "Conexão protegida" : "Conexão não verificada";
        addSecurityDetail(page, "Site", label);
        addSecurityDetail(page, "Estado da conexão", state);
        addSecurityDetail(page, "Protocolo", tab.url.startsWith("https://") ? "HTTPS" : "Não verificado / HTTP");
        if (info != null) {
            addSecurityDetail(page, "Certificado", info.isSecure && !info.isException ? "Aceito pelo motor" :
                    info.isException ? "Exceção presente" : "Não confirmado");
            addSecurityDetail(page, "Emissor do certificado", info.issuerCommonName == null ? "Não informado" : info.issuerCommonName);
            addSecurityDetail(page, "Organização emissora", info.issuerOrganization == null ? "Não informada" : info.issuerOrganization);
            addSecurityDetail(page, "Conteúdo misto ativo", mixedState(info.mixedModeActive));
            addSecurityDetail(page, "Conteúdo misto passivo", mixedState(info.mixedModePassive));
        }
        addSecurityDetail(page, "Privacidade", tab.privateMode ? "Aba privada, sem histórico local" : "Aba normal");
        TextView permissions = text("Gerenciar permissões deste site  ›", 15, XP_BLUE);
        permissions.setGravity(Gravity.CENTER);
        permissions.setBackground(rounded(Color.WHITE, BORDER, 12));
        permissions.setOnClickListener(v -> showCurrentSitePermissions());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(54));
        lp.topMargin = dp(12);
        page.addView(permissions, lp);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(page);
        showModuleScreen("Segurança da página", "Dados informados pelo GeckoView, sem prometer proteção absoluta", scroll);
    }

    private String mixedState(int state) {
        if (state == GeckoSession.ProgressDelegate.SecurityInformation.CONTENT_LOADED) return "Carregado (atenção)";
        if (state == GeckoSession.ProgressDelegate.SecurityInformation.CONTENT_BLOCKED) return "Bloqueado";
        return "Não informado";
    }

    private void addSecurityDetail(LinearLayout parent, String label, String value) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(14), dp(11), dp(14), dp(11));
        item.setBackground(rounded(Color.WHITE, BORDER, 10));
        TextView title = text(label, 11, SUBTLE);
        TextView body = text(value, 14, TEXT);
        body.setMaxLines(4);
        item.addView(title);
        item.addView(body);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(8);
        parent.addView(item, p);
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
        new XpDialogs.Builder(this)
                .setTitle("Apagar " + what + "?")
                .setMessage("Para limpar de maneira consistente, todas as abas serão fechadas e uma página vazia será aberta. " +
                        "Os favoritos e os arquivos já baixados serão preservados. Dados de sessão de sites e extensões podem ser afetados. Esta ação não pode ser desfeita.")
                .setPositiveButton("Limpar", (d, n) -> clearBrowserData(flags, clearLocalHistory))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void clearBrowserData(long flags, boolean clearLocalHistory) {
        if (runtime == null) return;
        closeModuleScreens();
        invalidateOnlineSuggestions();
        uiHandler.removeCallbacks(downloadRefresh);
        progressHandler.removeCallbacksAndMessages(null);
        if (extensionPopupDialog != null) extensionPopupDialog.dismiss();
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
            new XpDialogs.Builder(this).setTitle("Permissões do site")
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
        new XpDialogs.Builder(this).setTitle("Permissões • " + PrivacyRules.displayHost(url))
                .setItems(titles, (d, index) -> {
                    GeckoSession.PermissionDelegate.ContentPermission permission = permissions.get(index);
                    if (permission.permission == GeckoSession.PermissionDelegate.PERMISSION_PERSISTENT_STORAGE &&
                            permission.value == GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW) {
                        new XpDialogs.Builder(this).setTitle("Armazenamento persistente")
                                .setMessage("O GeckoView não permite revogar individualmente esta autorização. Use Privacidade e dados > Limpar todos os dados de navegação.")
                                .setPositiveButton("OK", null).show();
                        return;
                    }
                    new XpDialogs.Builder(this).setTitle(titles[index])
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
                new XpDialogs.Builder(MainActivity.this)
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
        new XpDialogs.Builder(this)
                .setTitle("Novas permissões: " + ext.id)
                .setMessage(Arrays.toString(permissions) + "\nSites: " + Arrays.toString(origins) + "\nDados: " + Arrays.toString(collected))
                .setPositiveButton("Permitir", (d, n) -> r.complete(AllowOrDeny.ALLOW))
                .setNegativeButton("Recusar", (d, n) -> r.complete(AllowOrDeny.DENY))
                .setOnCancelListener(d -> r.complete(AllowOrDeny.DENY))
                .show();
        return r;
    }

    private final WebExtension.ActionDelegate extensionActionDelegate = new WebExtension.ActionDelegate() {
        @Override public void onBrowserAction(WebExtension extension, GeckoSession session, WebExtension.Action action) {
            recordExtensionAction(extension, session, action);
        }
        @Override public void onPageAction(WebExtension extension, GeckoSession session, WebExtension.Action action) {
            recordExtensionAction(extension, session, action);
        }
        @Override public GeckoResult<GeckoSession> onOpenPopup(WebExtension extension, WebExtension.Action action) {
            return openExtensionPopup(extension, false);
        }
        @Override public GeckoResult<GeckoSession> onTogglePopup(WebExtension extension, WebExtension.Action action) {
            return openExtensionPopup(extension, true);
        }
    };

    private void recordExtensionAction(WebExtension extension, GeckoSession session, WebExtension.Action action) {
        if (session == null) defaultExtensionActions.put(extension.id, action);
        else {
            Map<GeckoSession, WebExtension.Action> map = sessionExtensionActions.get(extension.id);
            if (map == null) { map = new HashMap<>(); sessionExtensionActions.put(extension.id, map); }
            map.put(session, action);
        }
    }

    private void attachExtensionActionListeners(GeckoSession session) {
        for (WebExtension ext : activeExtensions.values())
            session.getWebExtensionController().setActionDelegate(ext, extensionActionDelegate);
    }

    private void registerExtensionActions() {
        if (runtime == null) return;
        runtime.getWebExtensionController().list().accept(list -> runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            activeExtensions.clear(); defaultExtensionActions.clear(); sessionExtensionActions.clear();
            for (WebExtension ext : list) {
                if (!ext.metaData.enabled) continue;
                activeExtensions.put(ext.id, ext);
                ext.setActionDelegate(extensionActionDelegate);
                for (Tab tab : tabs) {
                    if (tab.opened) tab.session.getWebExtensionController().setActionDelegate(ext, extensionActionDelegate);
                }
            }
        }), error -> android.util.Log.w("InternetXP", "Falha ao consultar ações de extensão", error));
    }

    private WebExtension.Action currentExtensionAction(WebExtension extension) {
        Map<GeckoSession, WebExtension.Action> specific = sessionExtensionActions.get(extension.id);
        Tab current = current();
        WebExtension.Action local = current == null || specific == null ? null : specific.get(current.session);
        WebExtension.Action standard = defaultExtensionActions.get(extension.id);
        if (local == null) return standard;
        return standard == null ? local : local.withDefault(standard);
    }

    private void showExtensionActions() {
        ArrayList<MenuAction> actions = new ArrayList<>();
        for (WebExtension ext : activeExtensions.values()) {
            WebExtension.Action action = currentExtensionAction(ext);
            if (action == null || Boolean.FALSE.equals(action.enabled)) continue;
            String title = ext.metaData.name == null ? ext.id : ext.metaData.name;
            String caption = action.title == null || action.title.isEmpty() ? "Executar botão ou abrir pop-up" : action.title;
            actions.add(new MenuAction("▣", title, caption, () -> action.click()));
        }
        actions.add(new MenuAction("⚙", "Gerenciar extensões", "Instalar, ativar, desativar ou remover.", this::showExtensions));
        showModuleMenuDialog("Ações das extensões", actions.size() == 1
                ? "Nenhuma ação de botão disponível. Complementos que funcionam automaticamente podem não criar botões."
                : "Execute a ação do complemento na aba aberta. Alguns precisam de permissão ou site compatível.", actions);
    }

    private GeckoResult<GeckoSession> openExtensionPopup(WebExtension extension, boolean toggle) {
        if (extensionPopupDialog != null && extensionPopupDialog.isShowing()) {
            if (toggle) { extensionPopupDialog.dismiss(); return GeckoResult.fromValue(null); }
            extensionPopupDialog.dismiss();
        }
        if (isFinishing() || isDestroyed()) return GeckoResult.fromValue(null);
        GeckoSession popup = new GeckoSession();
        popup.open(runtime);
        GeckoView popupView = new GeckoView(this);
        popupView.setSession(popup);
        String title = extension.metaData.name == null ? "Extensão" : extension.metaData.name;
        AlertDialog dialog = new XpDialogs.Builder(this)
                .setTitle(title)
                .setView(popupView)
                .setNegativeButton("Fechar", (d, w) -> d.dismiss())
                .create();
        extensionPopupSession = popup; extensionPopupView = popupView; extensionPopupDialog = dialog;
        dialog.setOnDismissListener(d -> {
            popupView.releaseSession(); popup.close();
            if (extensionPopupSession == popup) {
                extensionPopupSession = null; extensionPopupView = null; extensionPopupDialog = null;
            }
        });
        dialog.show();
        android.view.ViewGroup.LayoutParams popupParams = popupView.getLayoutParams();
        if (popupParams != null) {
            popupParams.height = dp(isShortPhone() ? 300 : 420);
            popupView.setLayoutParams(popupParams);
        }
        return GeckoResult.fromValue(popup);
    }

    /** Abre a central dedicada em vez de misturar extensões com o menu de ações rápidas. */
    private void showExtensions() {
        startActivity(new Intent(this, ExtensionManagerActivity.class));
    }

    static synchronized GeckoRuntime extensionRuntime(android.content.Context context) {
        if (runtime == null) runtime = GeckoRuntime.create(context.getApplicationContext());
        return runtime;
    }

    private void installDownloadedExtension(DownloadItem item) {
        Uri source = uriForDownload(item);
        if (source == null) {
            Toast.makeText(this, "Pacote XPI não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ExtensionManagerActivity.class);
        intent.putExtra("extension_xpi_uri", source);
        startActivity(intent);
    }

    private void showError(Throwable e) {
        DiagnosticLog.record(getApplicationContext(), "OPERATION_ERROR");
        LinearLayout content = styledDialogContent("Falha", "Ocorreu um erro durante a operação solicitada.");
        TextView detail = text(String.valueOf(e.getMessage()), 12, TEXT);
        detail.setPadding(dp(8), dp(10), dp(8), dp(10));
        detail.setBackground(rounded(Color.WHITE, BORDER, 10));
        content.addView(detail, new LinearLayout.LayoutParams(-1, -2));
        AlertDialog dialog = new XpDialogs.Builder(this)
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
        if (!moduleScreens.isEmpty()) { popModuleScreen(); return; }
        if (downloadsPanel != null && downloadsPanel.getVisibility() == View.VISIBLE) {
            hideDownloadsPanel();
            return;
        }
        Tab t = current();
        if (t != null && t.back) t.session.goBack();
        else new XpDialogs.Builder(this)
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
        if (runtime != null) {
            extensionPrompts();
            registerExtensionActions();
        }
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
        if (extensionPopupDialog != null) extensionPopupDialog.dismiss();
        if (view != null) view.releaseSession();
        for (Tab t : tabs) if (t.opened) t.session.close();
        tabs.clear();
        invalidateOnlineSuggestions();
        suggestionHandler.removeCallbacksAndMessages(null);
        suggestionWorker.shutdownNow();
        if (faviconRepository != null) faviconRepository.shutdown();
        super.onDestroy();
    }
}
