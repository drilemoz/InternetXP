package br.com.internetxp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
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

/** Layout mobile XP inspirado no mockup do usuário, mantendo GeckoView e recursos atuais. */
public final class MainActivity extends Activity {
    private static final int XP_BLUE = 0xff0d68c8;
    private static final int LIGHT_BG = 0xfff1f2f6;
    private static final int PANEL_BG = 0xffedf0f5;
    private static final int BORDER = 0xffbcc8d6;
    private static final int TEXT = 0xff222222;
    private static final int SUBTLE = 0xff6c7885;
    private static final int SUCCESS = 0xff2d8a57;
    private static final String HOME = "about:blank";

    private static GeckoRuntime runtime;

    private final ArrayList<Tab> tabs = new ArrayList<>();
    private final ArrayList<String> bookmarks = new ArrayList<>();
    private final ArrayList<String> history = new ArrayList<>();

    private SharedPreferences storage;
    private int active = -1;
    private EditText urlBox;
    private GeckoView view;
    private TextView status;
    private TextView backIcon;
    private TextView forwardIcon;
    private TextView tabsBadge;
    private ImageView securityIcon;
    private View menuAnchor;

    private static final class Tab {
        final GeckoSession session;
        String url = HOME;
        String title = "Nova aba";
        boolean back;
        boolean forward;
        Tab(GeckoSession s) { session = s; }
    }

    private int dp(int n) { return (int) (n * getResources().getDisplayMetrics().density + 0.5f); }

    private TextView text(String s, int sp, int color) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(sp);
        v.setTextColor(color);
        v.setGravity(Gravity.CENTER_VERTICAL);
        return v;
    }

    private GradientDrawable background(int color, int outline) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(0));
        if (outline != 0) d.setStroke(dp(1), outline);
        return d;
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
        loadList("bookmarks", bookmarks);
        loadList("history", history);
        createLayout();
        try {
            if (runtime == null) runtime = GeckoRuntime.create(getApplicationContext());
            extensionPrompts();
            addTab(HOME);
        } catch (RuntimeException e) {
            showError(e);
        }
    }

    private void createLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(LIGHT_BG);
        setContentView(root);

        // A barra de título foi removida. A faixa azul restante é a própria status bar do Android.
        // O endereço é o primeiro controle visível, conforme solicitado para telas pequenas.

        // Painel de endereço.
        LinearLayout addressPanel = new LinearLayout(this);
        addressPanel.setBackgroundColor(PANEL_BG);
        addressPanel.setGravity(Gravity.CENTER_VERTICAL);
        addressPanel.setPadding(dp(12), dp(12), dp(12), dp(12));

        LinearLayout addressBox = new LinearLayout(this);
        addressBox.setOrientation(LinearLayout.HORIZONTAL);
        addressBox.setGravity(Gravity.CENTER_VERTICAL);
        addressBox.setBackground(rounded(Color.WHITE, BORDER, 0));
        addressBox.setPadding(dp(12), 0, dp(8), 0);

        securityIcon = new ImageView(this);
        securityIcon.setImageResource(R.drawable.ic_globe_xp);
        securityIcon.setColorFilter(SUBTLE);
        securityIcon.setContentDescription("Segurança da conexão ainda não verificada");
        securityIcon.setPadding(dp(3), dp(11), dp(3), dp(11));
        addressBox.addView(securityIcon, new LinearLayout.LayoutParams(dp(28), dp(44)));

        urlBox = new EditText(this);
        urlBox.setSingleLine();
        urlBox.setTextSize(14);
        urlBox.setTextColor(TEXT);
        urlBox.setHint("https://www.exemplo.com");
        urlBox.setBackgroundColor(Color.TRANSPARENT);
        urlBox.setPadding(dp(4), 0, dp(4), 0);
        urlBox.setImeOptions(EditorInfo.IME_ACTION_GO);
        urlBox.setOnEditorActionListener((tv, action, event) -> {
            if (action == EditorInfo.IME_ACTION_GO ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                submitAddress();
                return true;
            }
            return false;
        });
        addressBox.addView(urlBox, new LinearLayout.LayoutParams(0, dp(44), 1));

        TextView open = text("↗", 16, TEXT);
        open.setGravity(Gravity.CENTER);
        open.setOnClickListener(v -> submitAddress());
        addressBox.addView(open, new LinearLayout.LayoutParams(dp(26), dp(44)));

        addressPanel.addView(addressBox, new LinearLayout.LayoutParams(0, dp(46), 1));

        ImageView refresh = new ImageView(this);
        refresh.setImageResource(R.drawable.ic_refresh_xp);
        refresh.setColorFilter(XP_BLUE);
        refresh.setPadding(dp(13), dp(11), dp(13), dp(11));
        refresh.setContentDescription("Atualizar página");
        refresh.setOnClickListener(v -> {
            Tab t = current();
            if (t != null) t.session.reload();
        });
        LinearLayout.LayoutParams refreshParams = new LinearLayout.LayoutParams(dp(54), dp(46));
        refreshParams.leftMargin = dp(10);
        addressPanel.addView(refresh, refreshParams);

        root.addView(addressPanel);
        divider(root, 1);

        view = new GeckoView(this);
        view.setBackgroundColor(Color.WHITE);
        root.addView(view, new LinearLayout.LayoutParams(-1, 0, 1));

        divider(root, 2);

        // Barra de status simples.
        LinearLayout footer = new LinearLayout(this);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.setPadding(dp(12), 0, dp(12), 0);
        footer.setBackgroundColor(LIGHT_BG);
        status = text("Concluído", 12, SUBTLE);
        footer.addView(status, new LinearLayout.LayoutParams(0, dp(30), 1));
        TextView addons = text("▧ Extensões", 12, XP_BLUE);
        addons.setOnClickListener(v -> showExtensions());
        footer.addView(addons, new LinearLayout.LayoutParams(-2, dp(30)));
        root.addView(footer);

        divider(root, 1);

        // Barra inferior principal.
        LinearLayout bottomBar = new LinearLayout(this);
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomBar.setGravity(Gravity.CENTER_VERTICAL);
        bottomBar.setBackgroundColor(PANEL_BG);
        bottomBar.setPadding(dp(6), dp(8), dp(6), dp(8));

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

        root.addView(bottomBar, new LinearLayout.LayoutParams(-1, dp(72)));
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
        TextView symbol = text(icon, 24, XP_BLUE);
        symbol.setTypeface(Typeface.DEFAULT_BOLD);
        symbol.setGravity(Gravity.CENTER);
        cell.addView(symbol, new LinearLayout.LayoutParams(-1, dp(32)));
        TextView name = text(caption, 8, TEXT);
        name.setGravity(Gravity.CENTER);
        cell.addView(name, new LinearLayout.LayoutParams(-1, dp(22)));
        return symbol;
    }

    private void navImage(LinearLayout parent, int drawable, String caption, Runnable action) {
        LinearLayout cell = navCell(parent, caption, action);
        ImageView icon = new ImageView(this);
        icon.setImageResource(drawable);
        icon.setPadding(dp(10), dp(3), dp(10), dp(3));
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        cell.addView(icon, new LinearLayout.LayoutParams(-1, dp(32)));
        TextView name = text(caption, 8, TEXT);
        name.setGravity(Gravity.CENTER);
        cell.addView(name, new LinearLayout.LayoutParams(-1, dp(22)));
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
        item(p, "Histórico", this::showHistory);
        item(p, "Downloads", this::showDownloads);
        item(p, "Extensões", this::showExtensions);
        item(p, "Limpar histórico local", this::clearHistory);
        item(p, "Sobre", () -> new AlertDialog.Builder(this)
                .setTitle("Internet XP 1.0.9")
                .setMessage("Layout mobile XP baseado no mockup do usuário, usando GeckoView. Projeto independente, sem vínculo com Microsoft ou Mozilla.")
                .setPositiveButton("OK", null).show());
        p.show();
    }

    private void item(PopupMenu p, String caption, Runnable run) {
        p.getMenu().add(caption).setOnMenuItemClickListener(m -> {
            run.run();
            return true;
        });
    }

    private Tab current() { return active >= 0 && active < tabs.size() ? tabs.get(active) : null; }

    private void submitAddress() {
        navigate(BrowserAddress.resolve(urlBox.getText().toString()));
        urlBox.clearFocus();
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(urlBox.getWindowToken(), 0);
    }

    private void navigate(String url) {
        Tab t = current();
        if (t != null) t.session.loadUri(url);
    }

    private void addTab(String url) {
        if (tabs.size() >= 8) {
            Toast.makeText(this, "Limite de 8 abas para poupar memória", Toast.LENGTH_SHORT).show();
            return;
        }
        GeckoSession session = new GeckoSession();
        Tab tab = new Tab(session);
        session.setContentDelegate(new GeckoSession.ContentDelegate() {
            @Override public void onTitleChange(GeckoSession s, String title) {
                if (title != null && !title.trim().isEmpty()) tab.title = title;
                if (tab == current()) updateChrome();
            }
            @Override public void onCrash(GeckoSession s) {
                if (tab == current()) status.setText("Falha da página. Reabra a aba.");
            }
            @Override public void onExternalResponse(GeckoSession s, WebResponse response) { offerDownload(response); }
        });
        session.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
            @Override
            public void onLocationChange(GeckoSession s, String uri, List<GeckoSession.PermissionDelegate.ContentPermission> perms, Boolean gesture) {
                if (uri == null) return;
                tab.url = uri;
                if (tab == current()) {
                    if (!urlBox.hasFocus()) urlBox.setText(uri.equals(HOME) ? "" : uri);
                    updateSecurity(uri);
                }
                if (uri.startsWith("http://") || uri.startsWith("https://")) rememberHistory(uri);
            }
            @Override public void onCanGoBack(GeckoSession s, boolean b) { tab.back = b; if (tab == current()) updateArrows(); }
            @Override public void onCanGoForward(GeckoSession s, boolean b) { tab.forward = b; if (tab == current()) updateArrows(); }
        });
        session.setProgressDelegate(new GeckoSession.ProgressDelegate() {
            @Override public void onPageStart(GeckoSession s, String uri) { if (tab == current()) status.setText("Carregando…"); }
            @Override public void onPageStop(GeckoSession s, boolean success) { if (tab == current()) status.setText(success ? "Concluído" : "Erro ao carregar"); }
        });
        session.open(runtime);
        tabs.add(tab);
        selectTab(tabs.size() - 1);
        session.loadUri(url);
    }

    private void selectTab(int index) {
        if (index < 0 || index >= tabs.size()) return;
        GeckoSession old = view.releaseSession();
        if (old != null) old.setActive(false);
        active = index;
        Tab tab = current();
        tab.session.setActive(true);
        view.setSession(tab.session);
        if (!urlBox.hasFocus()) urlBox.setText(tab.url.equals(HOME) ? "" : tab.url);
        updateSecurity(tab.url);
        updateChrome();
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
        Tab t = tabs.remove(index);
        if (view.getSession() == t.session) view.releaseSession();
        t.session.close();
        if (tabs.isEmpty()) {
            active = -1;
            addTab(HOME);
        } else {
            selectTab(Math.min(index, tabs.size() - 1));
        }
    }

    private void updateArrows() {
        Tab t = current();
        backIcon.setTextColor(t != null && t.back ? XP_BLUE : BORDER);
        forwardIcon.setTextColor(t != null && t.forward ? XP_BLUE : BORDER);
    }

    private void drawTabs() {
        if (tabsBadge == null) return;
        String caption = tabs.size() == 1 ? "1 aba" : tabs.size() + " abas";
        tabsBadge.setContentDescription(caption);
    }

    private void showTabs() {
        if (tabs.isEmpty()) return;
        String[] items = new String[tabs.size()];
        for (int i = 0; i < tabs.size(); i++) {
            Tab t = tabs.get(i);
            String label = t.title == null || t.title.trim().isEmpty() ? t.url : t.title;
            if (label == null || label.trim().isEmpty()) label = "Nova aba";
            items[i] = (i == active ? "✓ " : "○ ") + trim(label, 40);
        }
        new AlertDialog.Builder(this)
                .setTitle("Abas abertas")
                .setItems(items, (d, n) -> selectTab(n))
                .setPositiveButton("Nova aba", (d, n) -> addTab(HOME))
                .setNeutralButton("Fechar atual", (d, n) -> closeTab(active))
                .setNegativeButton("Fechar", null)
                .show();
    }

    private String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }

    private void loadList(String key, ArrayList<String> list) {
        try {
            JSONArray a = new JSONArray(storage.getString(key, "[]"));
            for (int i = 0; i < a.length(); i++) list.add(a.getString(i));
        } catch (Exception ignored) { }
    }

    private void saveList(String key, ArrayList<String> list) {
        storage.edit().putString(key, new JSONArray(list).toString()).apply();
    }

    private void rememberHistory(String url) {
        history.remove(url);
        history.add(0, url);
        while (history.size() > 200) history.remove(history.size() - 1);
        saveList("history", history);
    }

    private void toggleBookmark() {
        Tab t = current();
        if (t == null || !t.url.startsWith("http")) return;
        if (!bookmarks.remove(t.url)) bookmarks.add(0, t.url);
        saveList("bookmarks", bookmarks);
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
        if (history.isEmpty()) {
            Toast.makeText(this, "Histórico vazio", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] items = history.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Histórico")
                .setItems(items, (d, n) -> navigate(items[n]))
                .setPositiveButton("Limpar", (d, n) -> clearHistory())
                .setNegativeButton("Fechar", null)
                .show();
    }

    private void clearHistory() {
        new AlertDialog.Builder(this)
                .setTitle("Limpar histórico local?")
                .setMessage("Cookies e cache do GeckoView não serão apagados.")
                .setPositiveButton("Apagar", (d, n) -> {
                    history.clear();
                    saveList("history", history);
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
        try {
            startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
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
        new AlertDialog.Builder(this)
                .setTitle("Baixar arquivo?")
                .setMessage("Origem: " + uri + "\n\nArquivos com login/cookies podem falhar nesta versão.")
                .setPositiveButton("Baixar", (d, n) -> {
                    try {
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                                android.webkit.URLUtil.guessFileName(uri, null, null));
                        ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(request);
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

    private GeckoResult<AllowOrDeny> reviewPermissions(WebExtension ext, String[] p, String[] o, String[] c) {
        GeckoResult<AllowOrDeny> r = new GeckoResult<>();
        new AlertDialog.Builder(this)
                .setTitle("Novas permissões: " + ext.id)
                .setMessage(Arrays.toString(p) + "\nSites: " + Arrays.toString(o) + "\nDados: " + Arrays.toString(c))
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
        EditText link = new EditText(this);
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
        Tab t = current();
        if (t != null && t.back) t.session.goBack();
        else new AlertDialog.Builder(this)
                .setTitle("Sair do Internet XP?")
                .setPositiveButton("Sair", (d, n) -> finish())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        if (view != null) view.releaseSession();
        for (Tab t : tabs) t.session.close();
        tabs.clear();
        super.onDestroy();
    }
}
