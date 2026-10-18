package br.com.internetxp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.WebExtension;
import org.mozilla.geckoview.WebExtensionController;

import java.io.File;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Tela independente: gerenciamento verdadeiro no GeckoRuntime, não apenas itens de uma lista local. */
public final class ExtensionManagerActivity extends Activity {
    private static final int SELECT_XPI = 811;
    private static final int EXPORT_FILE = 812;
    private static final int BLUE = 0xff0d68c8;
    private static final int BG = 0xffeff2f7;
    private static final int DARK = 0xff1f2c40;
    private GeckoRuntime runtime;
    private ExtensionPackageStore store;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private LinearLayout entries;
    private TextView summary;
    private TextView activityStatus;
    private boolean busy;
    private File exportSource;
    private String exportMetadata;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        runtime = MainActivity.extensionRuntime(getApplicationContext());
        store = new ExtensionPackageStore(this);
        buildScreen();
        refresh();
        Uri incoming = getIntent() == null ? null : getIntent().getParcelableExtra("extension_xpi_uri");
        if (incoming != null) importPackage(incoming);
    }

    @Override protected void onResume() {
        super.onResume();
        // Delegado deve pertencer à Activity visível para não abrir prompt atrás da tela.
        runtime.getWebExtensionController().setPromptDelegate(new WebExtensionController.PromptDelegate() {
            @Override public GeckoResult<WebExtension.PermissionPromptResponse> onInstallPromptRequest(WebExtension extension, String[] permissions, String[] origins, String[] collected) {
                GeckoResult<WebExtension.PermissionPromptResponse> result = new GeckoResult<>();
                runOnUiThread(() -> new XpDialogs.Builder(ExtensionManagerActivity.this)
                        .setTitle("Permissões da extensão")
                        .setMessage(extension.id + "\n\nAPIs: " + Arrays.toString(permissions) + "\nSites: " + Arrays.toString(origins) + "\nColeta de dados: " + Arrays.toString(collected))
                        .setPositiveButton("Permitir", (d, w) -> result.complete(new WebExtension.PermissionPromptResponse(true, false, false)))
                        .setNegativeButton("Recusar", (d, w) -> result.complete(new WebExtension.PermissionPromptResponse(false, false, false)))
                        .setOnCancelListener(d -> result.complete(new WebExtension.PermissionPromptResponse(false, false, false))).show());
                return result;
            }
            @Override public GeckoResult<AllowOrDeny> onOptionalPrompt(WebExtension extension, String[] permissions, String[] origins, String[] collected) {
                return permissionChangePrompt(extension, permissions, origins, collected);
            }
            @Override public GeckoResult<AllowOrDeny> onUpdatePrompt(WebExtension extension, String[] permissions, String[] origins, String[] collected) {
                return permissionChangePrompt(extension, permissions, origins, collected);
            }
        });
    }

    private GeckoResult<AllowOrDeny> permissionChangePrompt(WebExtension extension, String[] permissions, String[] origins, String[] collected) {
        GeckoResult<AllowOrDeny> result = new GeckoResult<>();
        runOnUiThread(() -> new XpDialogs.Builder(this)
                .setTitle("Novas permissões: " + extension.id)
                .setMessage("APIs: " + Arrays.toString(permissions) + "\nSites: " + Arrays.toString(origins) + "\nColeta: " + Arrays.toString(collected))
                .setPositiveButton("Permitir", (d, w) -> result.complete(AllowOrDeny.ALLOW))
                .setNegativeButton("Recusar", (d, w) -> result.complete(AllowOrDeny.DENY))
                .setOnCancelListener(d -> result.complete(AllowOrDeny.DENY)).show());
        return result;
    }

    private int dp(int value) { return (int) (getResources().getDisplayMetrics().density * value + .5f); }
    private GradientDrawable background(int color, int stroke, int radius) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color); bg.setCornerRadius(dp(radius));
        if (stroke != 0) bg.setStroke(dp(1), stroke);
        return bg;
    }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        return t;
    }
    private LinearLayout vertical() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }

    private void buildScreen() {
        LinearLayout root = vertical(); root.setBackgroundColor(BG);
        setContentView(root);
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL); header.setPadding(dp(14), dp(14), dp(14), dp(12));
        header.setBackgroundColor(BLUE);
        TextView back = text("‹", 33, Color.WHITE); back.setContentDescription("Voltar");
        back.setGravity(Gravity.CENTER); back.setOnClickListener(v -> finish());
        header.addView(back, new LinearLayout.LayoutParams(dp(42), dp(44)));
        LinearLayout heading = vertical();
        TextView title = text("🧩  Extensões", 20, Color.WHITE); title.setTypeface(Typeface.DEFAULT_BOLD);
        heading.addView(title);
        TextView subtitle = text("Central de complementos • GeckoView 158", 11, 0xffdfefff);
        heading.addView(subtitle); header.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));
        TextView reload = text("↻", 25, Color.WHITE);
        reload.setGravity(Gravity.CENTER); reload.setContentDescription("Atualizar lista de extensões");
        reload.setOnClickListener(v -> refresh()); header.addView(reload, new LinearLayout.LayoutParams(dp(44), dp(44)));
        root.addView(header);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout body = vertical(); body.setPadding(dp(13), dp(14), dp(13), dp(20)); scroll.addView(body);
        LinearLayout options = vertical(); options.setPadding(dp(10), dp(12), dp(10), dp(12));
        options.setBackground(background(Color.WHITE, 0xffcfdae9, 14));
        TextView startTitle = text("Adicionar complemento", 15, DARK); startTitle.setTypeface(Typeface.DEFAULT_BOLD);
        startTitle.setPadding(dp(3), 0, 0, dp(10));
        options.addView(startTitle);
        LinearLayout addRow = horizontal();
        addHorizontalAction(addRow, "↓", "Importar XPI", "Escolher arquivo do celular", this::selectFile);
        addHorizontalAction(addRow, "↗", "Por HTTPS", "Link de XPI assinado", this::enterUrl);
        addHorizontalAction(addRow, "◎", "Catálogo", "Extensões Mozilla", () -> {
            Intent data = new Intent(this, MainActivity.class);
            data.setAction(Intent.ACTION_VIEW);
            data.setData(Uri.parse("https://addons.mozilla.org/android/"));
            data.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(data); finish();
        });
        options.addView(addRow, new LinearLayout.LayoutParams(-1, -2));
        body.addView(options, new LinearLayout.LayoutParams(-1, -2));
        activityStatus = text("", 12, BLUE);
        activityStatus.setPadding(dp(4), dp(11), dp(4), dp(6)); body.addView(activityStatus);
        summary = text("Consultando extensões instaladas...", 13, DARK);
        summary.setTypeface(Typeface.DEFAULT_BOLD); summary.setPadding(dp(4), dp(6), dp(4), dp(8)); body.addView(summary);
        entries = vertical(); body.addView(entries);
        TextView note = text("Instalada e ativa significa que o GeckoView carregou a extensão, não que todas as funções já foram testadas. Para conferir um complemento com botão, abra uma página e use Menu > Ações das extensões. Teste uma função observável, como criar e apagar um cookie de teste, sem alterar dados de login. Pacotes sem assinatura Mozilla válida não são instalados.", 11, 0xff556274);
        note.setPadding(dp(4), dp(15), dp(4), dp(15)); body.addView(note);
    }

    private LinearLayout horizontal() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        return row;
    }

    /** Um conjunto de ações sempre na mesma linha, inclusive nos aparelhos estreitos. */
    private void addHorizontalAction(LinearLayout row, String glyph, String label, String help, Runnable click) {
        LinearLayout tile = vertical();
        tile.setGravity(Gravity.CENTER_HORIZONTAL);
        tile.setPadding(dp(4), dp(10), dp(4), dp(10));
        tile.setBackground(background(0xfff4f8ff, 0xffdbe6f3, 11));
        TextView icon = text(glyph, 23, BLUE);
        icon.setTypeface(Typeface.DEFAULT_BOLD);
        icon.setGravity(Gravity.CENTER);
        tile.addView(icon, new LinearLayout.LayoutParams(-1, dp(31)));
        TextView name = text(label, 11, DARK);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setGravity(Gravity.CENTER);
        name.setMaxLines(2);
        tile.addView(name, new LinearLayout.LayoutParams(-1, dp(34)));
        TextView hint = text(help, 9, 0xff61748c);
        hint.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        hint.setMaxLines(2);
        tile.addView(hint, new LinearLayout.LayoutParams(-1, dp(30)));
        tile.setContentDescription(label + ". " + help);
        tile.setFocusable(true);
        tile.setClickable(true);
        tile.setOnClickListener(v -> click.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(117), 1);
        if (row.getChildCount() != 0) params.leftMargin = dp(6);
        row.addView(tile, params);
    }

    /** Gerenciamento horizontal sem esconder os quatro comandos em uma coluna longa. */
    private void addInstalledAction(LinearLayout row, String glyph, String label, String help, boolean destructive, Runnable click) {
        LinearLayout tile = vertical();
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(2), dp(8), dp(2), dp(8));
        tile.setBackground(background(destructive ? 0xfffff5f5 : 0xfff4f8ff,
                destructive ? 0xffefcccc : 0xffdbe6f3, 10));
        TextView icon = text(glyph, 22, destructive ? 0xffae3540 : BLUE);
        icon.setGravity(Gravity.CENTER);
        tile.addView(icon, new LinearLayout.LayoutParams(-1, dp(28)));
        TextView caption = text(label, 10, destructive ? 0xff96303a : DARK);
        caption.setTypeface(Typeface.DEFAULT_BOLD);
        caption.setGravity(Gravity.CENTER);
        caption.setMaxLines(2);
        tile.addView(caption, new LinearLayout.LayoutParams(-1, dp(31)));
        tile.setContentDescription(label + ". " + help);
        tile.setFocusable(true);
        tile.setClickable(true);
        tile.setOnClickListener(v -> click.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(77), 1);
        if (row.getChildCount() != 0) params.leftMargin = dp(5);
        row.addView(tile, params);
    }

    private void setBusy(boolean value, String message) {
        busy = value; activityStatus.setText(message);
    }

    private void refresh() {
        runtime.getWebExtensionController().list().accept(list -> runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            entries.removeAllViews();
            int enabled = 0;
            for (WebExtension item : list) if (item.metaData.enabled) enabled++;
            summary.setText(list.size() + " instalada(s) • " + enabled + " ativa(s)");
            if (list.isEmpty()) {
                TextView empty = text("Nenhuma extensão instalada no motor. Um download XPI sozinho não é uma instalação.", 13, 0xff69788b);
                empty.setPadding(dp(12), dp(16), dp(12), dp(16)); entries.addView(empty);
            }
            for (WebExtension extension : list) addExtensionCard(extension);
        }), error -> runOnUiThread(() -> fail("Não foi possível consultar as extensões", error)));
    }

    private void addExtensionCard(WebExtension extension) {
        LinearLayout card = vertical(); card.setPadding(dp(13), dp(12), dp(13), dp(12));
        card.setBackground(background(Color.WHITE, 0xffcbd8e8, 13));
        String name = extension.metaData.name == null ? extension.id : extension.metaData.name;
        TextView title = text("▣  " + name, 15, DARK); title.setTypeface(Typeface.DEFAULT_BOLD); card.addView(title);
        int disabled = extension.metaData.disabledFlags;
        String state = extension.metaData.enabled ? "● Ativa no GeckoView" : "○ Desativada";
        if ((disabled & WebExtension.DisabledFlags.SIGNATURE) != 0) state += " • Assinatura inválida";
        if ((disabled & WebExtension.DisabledFlags.APP_VERSION) != 0) state += " • Incompatibilidade de versão";
        if ((disabled & WebExtension.DisabledFlags.BLOCKLIST) != 0) state += " • Bloqueada";
        TextView statusLabel = text(state + " • v" + extension.metaData.version, 12, extension.metaData.enabled ? 0xff187546 : 0xff94600a);
        statusLabel.setPadding(dp(2), dp(6), 0, dp(4)); card.addView(statusLabel);
        String desc = extension.metaData.description == null || extension.metaData.description.isEmpty()
                ? "Identificador: " + extension.id : extension.metaData.description;
        TextView details = text(desc, 12, 0xff617184); card.addView(details);
        LinearLayout actions = horizontal();
        actions.setPadding(0, dp(12), 0, 0);
        addInstalledAction(actions, "⏻", extension.metaData.enabled ? "Desativar" : "Ativar",
                "Mudar estado da extensão no GeckoView", false, () -> changeState(extension));
        addInstalledAction(actions, "ⓘ", "Diagnóstico", "Ver assinatura, versão e permissões",
                false, () -> details(extension));
        addInstalledAction(actions, "⇧", "Exportar", "Exportar XPI preservado ou diagnóstico JSON",
                false, () -> exportMenu(extension));
        addInstalledAction(actions, "×", "Excluir", "Remover a extensão e os dados",
                true, () -> confirmUninstall(extension));
        card.addView(actions, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(10); entries.addView(card, p);
    }

    private void details(WebExtension ext) {
        String text = "Nome: " + ext.metaData.name + "\nID: " + ext.id + "\nVersão: " + ext.metaData.version
                + "\nAtivada: " + (ext.metaData.enabled ? "Sim" : "Não")
                + "\nSinalizadores de desativação: " + ext.metaData.disabledFlags
                + "\nAssinatura (estado Gecko): " + ext.metaData.signedState
                + "\nRequeridas: " + Arrays.toString(ext.metaData.requiredPermissions)
                + "\nOrigens: " + Arrays.toString(ext.metaData.requiredOrigins)
                + "\nOpcionais: " + Arrays.toString(ext.metaData.optionalPermissions)
                + "\nNavegação privada: " + (ext.metaData.allowedInPrivateBrowsing ? "Permitida" : "Não permitida")
                + "\nXPI exportável: " + (store.hasArchive(ext.id) ? "Sim" : "Não")
                + "\n\nO estado Ativa não comprova que o conteúdo de toda página foi alterado.";
        new XpDialogs.Builder(this).setTitle("Diagnóstico da extensão").setMessage(text)
                .setPositiveButton("Fechar", null).show();
    }

    private void changeState(WebExtension x) {
        if (busy) return;
        setBusy(true, "Aplicando alteração no GeckoView...");
        GeckoResult<WebExtension> result = x.metaData.enabled
                ? runtime.getWebExtensionController().disable(x, WebExtensionController.EnableSource.USER)
                : runtime.getWebExtensionController().enable(x, WebExtensionController.EnableSource.USER);
        result.accept(updated -> runOnUiThread(() -> { setBusy(false, "Estado atualizado no motor."); refresh(); }),
                err -> runOnUiThread(() -> fail("Não foi possível alterar o estado", err)));
    }

    private void confirmUninstall(WebExtension x) {
        new XpDialogs.Builder(this).setTitle("Excluir extensão?")
                .setMessage("A extensão " + (x.metaData.name == null ? x.id : x.metaData.name) + " e seus dados serão removidos do GeckoView. O XPI arquivado no Internet XP também será excluído.")
                .setPositiveButton("Excluir", (d, w) -> {
                    if (busy) return;
                    setBusy(true, "Desinstalando...");
                    runtime.getWebExtensionController().uninstall(x).accept(done -> runOnUiThread(() -> {
                        store.removeArchive(x.id);
                        setBusy(false, "Extensão desinstalada."); refresh();
                    }), err -> runOnUiThread(() -> fail("Falha ao desinstalar", err)));
                }).setNegativeButton("Cancelar", null).show();
    }

    private void selectFile() {
        if (busy) return;
        Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pick.addCategory(Intent.CATEGORY_OPENABLE);
        pick.setType("*/*");
        try { startActivityForResult(pick, SELECT_XPI); }
        catch (Exception e) { fail("Seletor de arquivos indisponível", e); }
    }

    private void enterUrl() {
        if (busy) return;
        EditText edit = new EditText(this);
        edit.setSingleLine(true); edit.setTextSize(14); edit.setHint("https://.../extensao.xpi");
        new XpDialogs.Builder(this).setTitle("Instalar XPI pela internet")
                .setMessage("Informe o link HTTPS direto. O GeckoView verificará assinatura Mozilla, manifesto e permissões.")
                .setView(edit).setPositiveButton("Instalar", (d, w) -> {
                    String url = edit.getText().toString().trim();
                    if (!ExtensionCompatibility.isSecurePackageUrl(url)) {
                        Toast.makeText(this, "Informe uma URL HTTPS válida", Toast.LENGTH_LONG).show(); return;
                    }
                    installFromUrl(url);
                }).setNegativeButton("Cancelar", null).show();
    }

    private void installFromUrl(String url) {
        if (busy) return;
        setBusy(true, "Verificando assinatura e instalando pelo GeckoView...");
        runtime.getWebExtensionController().install(url).accept(ext -> runOnUiThread(() -> {
            setBusy(false, "Instalada pelo GeckoView: " + ext.id + ". Confira se está ativa.");
            refresh();
        }), err -> runOnUiThread(() -> fail("GeckoView recusou a instalação", err)));
    }

    private void importPackage(Uri source) {
        if (busy) return;
        setBusy(true, "Lendo e verificando o arquivo XPI...");
        io.execute(() -> {
            File temporary = null;
            try {
                temporary = store.copyToStage(source);
                ExtensionPackageStore.Inspection info = ExtensionPackageStore.inspect(temporary);
                File staged = temporary;
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) { staged.delete(); return; }
                    setBusy(false, "Manifesto: " + info.name + " v" + info.version + " • MV" + info.manifestVersion);
                    new XpDialogs.Builder(this).setTitle("Confirmar instalação XPI")
                            .setMessage(info.name + " v" + info.version + "\nManifesto: V" + info.manifestVersion
                                    + "\n\n" + info.warning + "\n\nInstalar agora?")
                            .setPositiveButton("Instalar", (d, w) -> installStaged(staged))
                            .setNegativeButton("Cancelar", (d, w) -> staged.delete())
                            .setOnCancelListener(d -> staged.delete()).show();
                });
            } catch (Exception e) {
                if (temporary != null) temporary.delete();
                runOnUiThread(() -> fail("Arquivo incompatível ou inválido", e));
            }
        });
    }

    private void installStaged(File file) {
        if (busy) { file.delete(); return; }
        setBusy(true, "GeckoView está validando assinatura e permissões...");
        runtime.getWebExtensionController().install(Uri.fromFile(file).toString()).accept(ext -> {
            io.execute(() -> {
                String archiveMessage = "XPI guardado para exportação.";
                try { store.archive(ext.id, file); }
                catch (Exception e) { archiveMessage = "Instalada, mas não foi possível guardar cópia do XPI: " + e.getMessage(); }
                finally { file.delete(); }
                final String message = archiveMessage;
                runOnUiThread(() -> {
                    setBusy(false, "GeckoView instalou " + ext.id + ". " + message);
                    refresh();
                });
            });
        }, e -> runOnUiThread(() -> {
            file.delete(); fail("GeckoView recusou o XPI (assinatura, manifesto ou incompatibilidade)", e);
        }));
    }

    private void exportMenu(WebExtension ext) {
        boolean archive = store.hasArchive(ext.id);
        String[] types = archive ? new String[]{"Exportar pacote original .xpi", "Exportar diagnóstico .json"}
                : new String[]{"Exportar diagnóstico .json"};
        new XpDialogs.Builder(this).setTitle("Exportar " + (ext.metaData.name == null ? ext.id : ext.metaData.name))
                .setItems(types, (d, choice) -> {
                    exportSource = archive && choice == 0 ? store.getArchive(ext.id) : null;
                    exportMetadata = exportSource == null ? buildMetadata(ext) : null;
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType(exportSource == null ? "application/json" : "application/x-xpinstall");
                    String base = AppDownloadEngine.sanitize(ext.metaData.name == null ? "extensao" : ext.metaData.name);
                    intent.putExtra(Intent.EXTRA_TITLE, base + (exportSource == null ? "-diagnostico.json" : ".xpi"));
                    try { startActivityForResult(intent, EXPORT_FILE); }
                    catch (Exception e) { fail("Não foi possível abrir a exportação", e); }
                }).show();
    }

    private String buildMetadata(WebExtension ext) {
        try {
            JSONObject json = new JSONObject();
            json.put("browser", "Internet XP"); json.put("engine", "GeckoView 158");
            json.put("id", ext.id); json.put("name", ext.metaData.name);
            json.put("version", ext.metaData.version);
            json.put("enabled", ext.metaData.enabled);
            json.put("disabledFlags", ext.metaData.disabledFlags);
            json.put("signedState", ext.metaData.signedState);
            json.put("packageIncluded", false);
            json.put("requiredPermissions", new org.json.JSONArray(Arrays.asList(ext.metaData.requiredPermissions)));
            json.put("requiredOrigins", new org.json.JSONArray(Arrays.asList(ext.metaData.requiredOrigins)));
            return json.toString(2);
        } catch (Exception e) { return "{\"error\":\"metadata indisponível\"}"; }
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (result != RESULT_OK || data == null || data.getData() == null) {
            if (request == EXPORT_FILE) { exportMetadata = null; exportSource = null; }
            return;
        }
        if (request == SELECT_XPI) { importPackage(data.getData()); return; }
        if (request == EXPORT_FILE) {
            File source = exportSource; String metadata = exportMetadata;
            exportSource = null; exportMetadata = null;
            Uri dest = data.getData();
            io.execute(() -> {
                try (OutputStream out = getContentResolver().openOutputStream(dest, "w")) {
                    if (out == null) throw new java.io.IOException("O local de destino não aceitou escrita.");
                    if (source != null) ExtensionPackageStore.copyTo(source, out);
                    else if (metadata != null) out.write(metadata.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    else throw new java.io.IOException("Não há dados de exportação.");
                    runOnUiThread(() -> Toast.makeText(this, "Exportação concluída", Toast.LENGTH_SHORT).show());
                } catch (Exception e) { runOnUiThread(() -> fail("Falha ao exportar", e)); }
            });
        }
    }

    private static String explainFailure(Throwable error) {
        if (error == null) return "Erro desconhecido";
        Throwable cause = error;
        while (cause != null) {
            if (cause instanceof WebExtension.InstallException) {
                int code = ((WebExtension.InstallException) cause).code;
                String reason;
                if (code == WebExtension.InstallException.ErrorCodes.ERROR_SIGNEDSTATE_REQUIRED) reason = "O pacote XPI não possui assinatura Mozilla válida.";
                else if (code == WebExtension.InstallException.ErrorCodes.ERROR_INCOMPATIBLE) reason = "Este complemento é incompatível com a versão do motor.";
                else if (code == WebExtension.InstallException.ErrorCodes.ERROR_CORRUPT_FILE) reason = "Pacote XPI corrompido ou formato inválido.";
                else if (code == WebExtension.InstallException.ErrorCodes.ERROR_BLOCKLISTED) reason = "Extensão bloqueada por segurança.";
                else if (code == WebExtension.InstallException.ErrorCodes.ERROR_NETWORK_FAILURE) reason = "A conexão com o servidor falhou.";
                else if (code == WebExtension.InstallException.ErrorCodes.ERROR_FILE_ACCESS) reason = "O motor não conseguiu acessar o arquivo.";
                else if (code == WebExtension.InstallException.ErrorCodes.ERROR_UNSUPPORTED_ADDON_TYPE) reason = "Tipo de complemento não suportado.";
                else if (code == WebExtension.InstallException.ErrorCodes.ERROR_USER_CANCELED) reason = "Instalação cancelada pelo usuário.";
                else reason = "Erro GeckoView na instalação (código " + code + ").";
                return reason + "\nDetalhes: " + cause.getMessage();
            }
            cause = cause.getCause();
        }
        return String.valueOf(error.getMessage());
    }

    private void fail(String title, Throwable e) {
        setBusy(false, title);
        if (isFinishing() || isDestroyed()) return;
        String description = explainFailure(e);
        new XpDialogs.Builder(this).setTitle(title).setMessage(description)
                .setPositiveButton("OK", null).show();
    }

    @Override protected void onDestroy() {
        io.shutdown();
        super.onDestroy();
    }
}
