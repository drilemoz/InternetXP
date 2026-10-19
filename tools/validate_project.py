#!/usr/bin/env python3
from pathlib import Path
from xml.etree import ElementTree
import re

root = Path(__file__).resolve().parents[1]
gradle = (root / 'app/build.gradle').read_text()
workflow_text = (root / '.github/workflows/build-release-apk.yml').read_text()
activity = (root / 'app/src/main/java/br/com/internetxp/MainActivity.java').read_text()
assert "versionCode 41" in gradle
assert "versionName '1.0.40'" in gradle
expected_abis = ("armeabi-v7a", "arm64-v8a", "x86_64")
match = re.search(r"include\s+([^\n]+)", gradle)
assert match, "ABI split ausente"
actual_abis = tuple(re.findall(r"'([^']+)'", match.group(1)))
assert actual_abis == expected_abis, (actual_abis, expected_abis)
assert 'AutoCompleteTextView' in activity
assert 'private void showDownloads()' in activity
assert 'R.drawable.ic_dropdown_xp' in activity
assert 'R.drawable.ic_refresh_ie_xp' in activity
assert 'downloadsPanel.setVisibility(View.VISIBLE)' in activity
assert 'rememberHistory(' in activity
assert 'buildSuggestions(' in activity
assert 'ReleaseNotes.VERSION_NAME' in activity
assert 'private void showModuleMenuDialog(' in activity
assert 'addressBox.setOnClickListener(v -> focusAddressBar(true))' in activity
assert 'urlBox.setOnTouchListener((v, event) -> {' in activity
assert 'private void focusAddressBar(boolean showSuggestions)' in activity
assert "Internet XP.v1.0.40-${abi}.apk" in workflow_text
assert 'upload-artifact' not in workflow_text
assert 'gh release create v1.0.40' in workflow_text
for file in (root / 'app/src/main').rglob('*.xml'):
    ElementTree.parse(file)
assert 'name: Compilar APK e publicar na Release' in workflow_text
assert 'jobs:' in workflow_text and '  apk:' in workflow_text
print('Validação local dos metadados, workflow e XML: OK')

assert 'urlBox.setSelectAllOnFocus(true)' in activity
assert 'urlBox.setOnClickListener(v -> focusAddressBar(true))' in activity
assert 'private void confirmCancelDownload(' in activity
assert 'private void retryDownload(' in activity
assert 'scheduleDownloadRefresh()' in activity
assert 'HistorySuggestions.filter(' in activity
assert 'DownloadDisplay.percent(' in activity
assert (root / 'app/src/main/java/br/com/internetxp/HistorySuggestions.java').exists()
assert (root / 'app/src/main/java/br/com/internetxp/DownloadDisplay.java').exists()

assert "versionCode='41' versionName='1.0.40'" in workflow_text
assert "v1.0.12" not in workflow_text
assert "Internet XP.v1.0.40-${abi}.apk" in workflow_text

assert 'ProgressBar progress' in activity
assert 'private void shareDownload(' in activity
assert 'private void confirmClearFinished(' in activity
assert 'startNativeDownload(' in activity
assert 'DownloadDisplay.speed(' in (root/'app/src/main/java/br/com/internetxp/AppDownloadEngine.java').read_text()
assert 'downloadReason(' in activity
assert 'android.app.DownloadManager' not in activity

# 1.0.15: abas persistidas sob demanda, histórico com metadados
assert 'if (!restoreTabs()) addTab(configuredHome())' in activity
assert 'private void saveTabs()' in activity
assert 'private void populateTabCards(' in activity
assert 'private void showHistory()' in activity
assert 'private void populateHistoryRows(' in activity
assert 'HistoryRecords.visited(' in activity
assert 'private void saveHistoryDetails()' in activity
assert 'tab.session.open(runtime)' in activity
assert 'if (!tab.loaded)' in activity
assert (root / 'app/src/main/java/br/com/internetxp/HistoryRecords.java').exists()
assert (root / 'app/src/main/java/br/com/internetxp/TabSessionStore.java').exists()

# Compatibilidade v1.0.17
assert 'onFilePrompt(' in activity
assert 'onMediaPermissionRequest(' in activity
assert 'onContentPermissionRequest(' in activity
assert 'onAndroidPermissionsRequest(' in activity
assert 'onNewSession(' in activity
assert 'onFullScreen(' in activity
assert 'onRequestPermissionsResult(' in activity
assert 'onActivityResult(' in activity
assert 'REQUEST_UPLOAD_FILES' in activity
assert (root / 'app/src/main/java/br/com/internetxp/SiteCompatibility.java').exists()

# 1.0.17: launcher icons
import struct
manifest=(root/'app/src/main/AndroidManifest.xml').read_text()
assert 'android:icon="@mipmap/ic_launcher"' in manifest
assert 'android:roundIcon="@mipmap/ic_launcher_round"' in manifest
for density, size in (('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)):
    for icon in ('ic_launcher.png','ic_launcher_round.png'):
        raw=(root/'app/src/main/res'/('mipmap-'+density)/icon).read_bytes()
        assert raw[:8] == b'\x89PNG\r\n\x1a\n'
        width,height=struct.unpack('>II',raw[16:24])
        assert (width,height) == (size,size)
for name in ('ic_launcher.xml','ic_launcher_round.xml'):
    ElementTree.parse(root/'app/src/main/res/mipmap-anydpi-v26'/name)
assert (root/'branding/internet_xp_icon_master.png').exists()

assert '1.0.40' in (root/'README.md').read_text()
assert '1.0.40' in (root/'CHANGELOG.md').read_text()
assert '1.0.40' in (root/'docs/VALIDATION.md').read_text()

# Mandatory per-update release notes: failed checks block CI before Android build.
import ast
manifest_xml = (root/'app/src/main/AndroidManifest.xml').read_text()
notes = (root/'app/src/main/java/br/com/internetxp/ReleaseNotes.java').read_text()
notes_screen = (root/'app/src/main/java/br/com/internetxp/WhatsNewActivity.java').read_text()
assert 'android:name=".WhatsNewActivity"' in manifest_xml
assert 'android:exported="false"' in manifest_xml
assert 'if (state == null && WhatsNewActivity.shouldShow(storage))' in activity
assert 'startActivity(new Intent(this, WhatsNewActivity.class))' in activity
assert '"Novidades desta versão"' in activity
assert 'KEY_LAST_SEEN' in notes_screen and 'whats_new_last_seen_version_code' in notes_screen
assert 'putInt(KEY_LAST_SEEN, ReleaseNotes.VERSION_CODE)' in notes_screen
assert 'ReleaseNotes.shouldShow' in notes_screen
assert 'ReleaseNotes.FEATURES' in notes_screen
assert 'COMEÇAR A NAVEGAR' in notes_screen
assert '@OptIn(markerClass = ExperimentalGeckoViewApi.class)' in activity
assert 'permission.notifyShown();' in activity
assert (root/'app/src/test/java/br/com/internetxp/ReleaseNotesTest.java').exists()
assert 'android:drawable="@drawable/ic_launcher_monochrome"' in (root/'app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml').read_text()
assert 'android:drawable="@drawable/ic_launcher_monochrome"' in (root/'app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml').read_text()
assert 'REGRA PERMANENTE: TELA DE NOVIDADES' in (root/'AGENTS.md').read_text()
assert 'tela de Novidades' in (root/'CHANGELOG.md').read_text()
version = re.search(r'versionName\s+[\'"]([0-9.]+)[\'"]', gradle).group(1)
code = int(re.search(r'versionCode\s+(\d+)', gradle).group(1))
assert f'VERSION_NAME = "{version}"' in notes, 'Novidades devem acompanhar versionName'
assert f'VERSION_CODE = {code}' in notes, 'Novidades devem acompanhar versionCode'
assert f'## {version} (versionCode {code})' in (root/'CHANGELOG.md').read_text()
assert f'## {version}: ' in (root/'docs/VALIDATION.md').read_text()
assert len(re.findall(r'new Feature\(', notes)) >= 3, 'Ao menos três notas reais por versão'
# Testes unitários não podem congelar versões anteriores. O validador é
# responsável pela sincronização exata com Gradle; JUnit verifica comportamento.
release_test = (root/'app/src/test/java/br/com/internetxp/ReleaseNotesTest.java').read_text()
assert 'ReleaseNotes.VERSION_CODE' in release_test, 'Testes de Novidades devem usar versionCode atual'
assert 'ReleaseNotes.VERSION_NAME' in release_test, 'Testes de Novidades devem avaliar versionName atual'
assert 'ReleaseNotes.shouldShow(current - 1)' in release_test
assert 'ReleaseNotes.shouldShow(current)' in release_test
assert 'ReleaseNotes.shouldShow(current + 1)' in release_test
assert 'public static final String HEADLINE' in notes and 'public static final String SUBTITLE' in notes

assert 'private static final int XP_BLUE' in activity
assert 'GeckoView' in activity
assert 'upload-artifact' not in workflow_text
assert 'gradle --no-daemon --stacktrace :app:assembleRelease' in workflow_text
assert 'python3 tools/validate_project.py' in workflow_text
print('Tela de Novidades, versão e regra permanente: OK')

# Regressão do workflow 19: annotation-experimental contém a @OptIn Java
# e seu @RequiresOptIn$Level, ausentes na compilação da v1.0.18.
assert "implementation 'androidx.annotation:annotation-experimental:1.6.0'" in gradle, 'Dependencia @OptIn ausente'
assert "implementation 'androidx.annotation:annotation:1.11.0'" in gradle, 'Dependencia base AndroidX ausente'
assert 'import androidx.annotation.OptIn;' in activity
assert '@OptIn(markerClass = ExperimentalGeckoViewApi.class)' in activity
assert 'permission.notifyShown();' in activity
print('Dependencia AndroidX @OptIn e notas da versão atual: OK')

# Versao 1.0.25: verificacao estrutural de privacidade
assert 'usePrivateMode(privateMode)' in activity
assert 'PrivacyRules.shouldRecordHistory(tab.privateMode, uri)' in activity
assert 'if (tab.privateMode) continue;' in activity, 'As abas privadas nao podem ser persistidas'
assert 'GeckoSession.ProgressDelegate.SecurityInformation info' in activity
assert 'onSecurityChange(GeckoSession s,' in activity
assert 'StorageController.ClearFlags.COOKIES' in activity
assert 'StorageController.ClearFlags.ALL_CACHES' in activity
assert 'runtime.getStorageController().clearData(flags).accept(' in activity
assert 'private void showCurrentSitePermissions()' in activity
assert 'trackingProtectionEnabled' in activity
assert (root/'app/src/main/java/br/com/internetxp/PrivacyRules.java').exists()
assert (root/'app/src/test/java/br/com/internetxp/PrivacyRulesTest.java').exists()
print('Privacidade, seguranca e isolamento estrutural: OK')
assert 'storage.edit().putString(PREF_OPEN_TABS,' in activity, 'Clear deve invalidar sessao persistida antes de apagar dados'

# Versao 1.0.25: menu em modulos e ajuste para telas compactas
assert 'private static final class MenuAction' in activity
assert 'showMainMenuHubDialog("Internet XP"' in activity
assert 'showModuleMenuDialog("Privacidade e dados"' in activity
assert 'downloadsPanelHeightDp()' in activity
assert 'toolbarHeightDp()' in activity
print('Menu em módulos e responsividade estrutural: OK')

# v1.0.25: Nova página inicial, favoritos e configuração devem permanecer sincronizados.
assert 'new HomePageView(' in activity
assert 'homeScreen.showFavorites(bookmarks)' in activity
assert 'private void showBrowserSettings()' in activity
assert 'private void showAddQuickAccess()' in activity
assert 'new MenuAction("⚙", "Configurações"' in activity
assert 'faviconRepository.load(t.url, faviconIcon)' in activity
assert 'showModuleScreen("Favoritos"' in activity
assert 'startActivity(new Intent(this, ExtensionManagerActivity.class))' in activity
assert 'showModuleScreen("Histórico",' in activity
assert 'private void showAbout()' in activity
assert (root/'app/src/main/java/br/com/internetxp/HomePageView.java').exists()
assert (root/'app/src/main/java/br/com/internetxp/HomePageSettings.java').exists()
assert (root/'app/src/main/java/br/com/internetxp/FaviconRepository.java').exists()
assert (root/'app/src/test/java/br/com/internetxp/HomePageSettingsTest.java').exists()
print('Página inicial, favicons, Configurações e diálogos XP: OK')

# v1.0.25: regras para próprio gerenciador e papel de navegador.
engine = (root/'app/src/main/java/br/com/internetxp/AppDownloadEngine.java').read_text()
assert 'class AppDownloadEngine' in engine
assert 'InputStream' in engine and 'FileOutputStream' in engine
assert 'FileProvider.getUriForFile' in engine
assert 'android.app.DownloadManager' not in activity
assert 'android.app.DownloadManager' not in engine
assert 'GeckoWebExecutor' in activity and 'WebResponse response' in activity
assert 'private void requestDefaultBrowserRole()' in activity
assert 'handleIncomingLink(getIntent())' in activity
assert 'protected void onNewIntent(Intent intent)' in activity
assert 'android.intent.action.VIEW' in manifest_xml
assert 'android.intent.category.BROWSABLE' in manifest_xml
assert 'android.intent.category.DEFAULT' in manifest_xml
assert 'android:scheme="http"' in manifest_xml and 'android:scheme="https"' in manifest_xml
assert 'android:name=".XpFileProvider"' in manifest_xml
assert (root/'app/src/main/java/br/com/internetxp/XpFileProvider.java').exists()
assert "implementation 'androidx.core:core:1.16.0'" in gradle
assert (root/'app/src/main/res/xml/xp_file_paths.xml').exists()
print('Downloads nativos, FileProvider e papel de navegador: OK')

# v1.0.25: foreground transfers and queue cancellation
service = (root/'app/src/main/java/br/com/internetxp/DownloadForegroundService.java').read_text()
engine = (root/'app/src/main/java/br/com/internetxp/AppDownloadEngine.java').read_text()
assert 'android.permission.FOREGROUND_SERVICE' in manifest_xml
assert 'android.permission.FOREGROUND_SERVICE_DATA_SYNC' in manifest_xml
assert 'android:foregroundServiceType="dataSync"' in manifest_xml
assert 'android:name=".DownloadForegroundService"' in manifest_xml
assert 'void onTimeout(int startId, int fgsType)' in service
assert 'startForeground(NOTIFICATION_ID' in service
assert 'DownloadForegroundService.begin(context, id, filename, expected);' in engine
assert 'DownloadForegroundService.end(context, id)' in engine
assert 'future.cancel(true) && !transfer.workerStarted' in engine
assert 'android.app.DownloadManager' not in engine
print('Servico de download e cancelamento da fila: OK')

# v1.0.25: menu XP moderno
activity = (root/'app/src/main/java/br/com/internetxp/MainActivity.java').read_text()
assert "showMainMenuHubDialog(\"Internet XP\"" in activity
assert "createHubTile(" in activity
assert "styleBottomMenuDialog(" in activity
assert "Acesso rápido" in activity
assert "Mais ferramentas" in activity
print('Menu XP moderno: OK')


# 1.0.31: regressoes dos pop-ups e das correcoes da compilacao
assert "animateDialogOpen(" in activity
assert "installDownloadedExtension(" in activity
assert "isTinyPhone()" in activity
assert 'showModuleScreen("Menu Internet XP", subtitle, scroll);' in activity
assert (root / 'app/src/main/java/br/com/internetxp/ExtensionManagerActivity.java').exists()
print('Pop-ups alinhados e animações: OK')

# 1.0.27: extensões são geridas pelo motor e não apenas pelo histórico de downloads
manager = (root/'app/src/main/java/br/com/internetxp/ExtensionManagerActivity.java').read_text()
package = (root/'app/src/main/java/br/com/internetxp/ExtensionPackageStore.java').read_text()
assert 'android:name=".ExtensionManagerActivity"' in manifest_xml
for token in ('WebExtensionController', '.install(', '.list()', '.disable(', '.enable(', '.uninstall(',
              'ACTION_OPEN_DOCUMENT', 'ACTION_CREATE_DOCUMENT', 'store.copyToStage(', 'store.archive(',
              'requiredPermissions', 'signedState', 'disabledFlags'):
    assert token in manager, f'Modulo de extensoes sem recurso: {token}'
assert 'MAX_PACKAGE_BYTES = 40L * 1024 * 1024' in package
assert 'manifest.json' in package
assert 'addDownloadAction(actions, "Instalar XPI"' in activity
assert 'void importPackage(Uri source)' in manager
assert 'void installStaged(File file)' in manager
assert 'private void changeState(WebExtension x)' in manager
assert 'private void exportMenu(WebExtension ext)' in manager
assert '1.0.33' in (root/'AGENTS.md').read_text()
print('Central real de WebExtensions, SAF e versionamento: OK')

compat = (root/'app/src/main/java/br/com/internetxp/ExtensionCompatibility.java').read_text()
assert 'isXpiFileName(' in compat and 'isSecurePackageUrl(' in compat
assert 'attachExtensionActionListeners(' in activity
assert 'private void showExtensionActions()' in activity
assert 'onOpenPopup(WebExtension extension' in activity
assert 'onTogglePopup(WebExtension extension' in activity
assert (root/'app/src/test/java/br/com/internetxp/ExtensionCompatibilityTest.java').exists()
print('Acoes de extensoes, popup e regras testaveis: OK')

# Os três erros anteriores não podem retornar: abandonar holders e contêineres de diálogos em telas inteiras.
assert 'showModuleScreen("Menu Internet XP", subtitle, scroll);' in activity
assert 'showModuleScreen(title, subtitle, scroll);' in activity
assert 'showModuleScreen("Histórico",' in activity
assert 'private void popModuleScreen()' in activity
assert 'private void closeModuleScreens()' in activity
print('Navegação por telas completas: OK')

# 1.0.30: acoes agrupadas horizontalmente, tema central e navegador legivel.
manager = (root/'app/src/main/java/br/com/internetxp/ExtensionManagerActivity.java').read_text()
shared = (root/'app/src/main/java/br/com/internetxp/XpDialogs.java').read_text()
assert 'addHorizontalAction(addRow, "↓", "Importar XPI"' in manager
assert 'addHorizontalAction(addRow, "↗", "Por HTTPS"' in manager
assert 'addHorizontalAction(addRow, "◎", "Catálogo"' in manager
assert 'options.addView(addRow' in manager
assert manager.count('addInstalledAction(actions,') == 4
assert 'card.addView(actions,' in manager
assert 'new XpDialogs.Builder(' in manager and 'new XpDialogs.Builder(' in activity
assert 'extends AlertDialog.Builder' in shared
assert 'setOnShowListener' in shared and 'setOnCancelListener' in manager
assert 'return isCompactPhone() ? 68 : 74' in activity
assert 'compactPhone ? 26 : 29' in activity
assert 'compactPhone ? 9.5f : 10.5f' in activity
assert 'metaData.enabled' in manager and '.getWebExtensionController().install(' in manager
assert '1.0.33' in (root/'AGENTS.md').read_text()
print('1.0.30: faixa horizontal, tema compartilhado e barra ampliada: OK')

# 1.0.31: privacidade, barra inteligente e ícones no histórico.
assert 'private void scheduleOnlineSuggestions(String input)' in activity
assert 'DuckDuckGo' in activity
assert 'PREF_ONLINE_SUGGESTIONS' in activity
assert 'copyAddressButton.setVisibility(focused ? View.VISIBLE : View.GONE)' in activity
assert 'pasteAddressButton.setVisibility(focused ? View.VISIBLE : View.GONE)' in activity
assert 'faviconRepository.load(entry.url, icon)' in activity
assert 'private void showModuleScreen(' in activity
assert 'new XpDialogs.Builder(this).setView(wrapper).create()' not in activity
assert (root/'app/src/main/java/br/com/internetxp/OnlineSuggestions.java').exists()
print('1.0.31: barra inteligente, privacidade e telas completas: OK')

# 1.0.33: regressão CI 31 (android.R.drawable.ic_menu_copy não existe).
copy_icon = root / 'app/src/main/res/drawable/ic_copy_address_xp.xml'
assert copy_icon.is_file(), 'Ícone Copiar deve existir no projeto'
ElementTree.parse(copy_icon)
assert 'addressTool(R.drawable.ic_copy_address_xp, "Copiar endereço"' in activity
assert 'android.R.drawable.ic_menu_copy' not in activity, 'Não usar ícone Android inexistente'
print('1.0.33: ícone Copiar próprio e sem referência inválida: OK')

# 1.0.33: novos controles, versionamento e regressão dos elementos reportados.
for resource in ('ic_paste_address_xp.xml', 'ic_star_outline_xp.xml', 'ic_star_filled_xp.xml', 'ic_screenshot_xp.xml'):
    assert (root / 'app/src/main/res/drawable' / resource).exists(), resource
assert 'onProgressChange(GeckoSession s, int progress)' in activity
assert 'line.setTag(item);' in activity and 'v.getTag()' in activity
assert 'R.drawable.ic_paste_address_xp' in activity
assert 'private void createTabNavigation' in activity
assert 'private void captureVisiblePage()' in activity
assert 'private void showDiagnostics()' in activity
assert 'private void showPerformanceSettings()' in activity
assert 'private void updateFavoriteAddressButton()' in activity
assert (root / 'app/src/main/java/br/com/internetxp/DiagnosticLog.java').exists()
assert (root / 'app/src/main/java/br/com/internetxp/SitePerformanceRules.java').exists()
assert 'showAddFavoriteField' in (root / 'app/src/main/java/br/com/internetxp/HomePageView.java').read_text()
print('1.0.33: recursos, URL estável, diagnóstico e novos vetores: OK')

# 1.0.34: exportação de captura por SAF, ícones iguais e barra responsiva.
for resource in ('ic_nav_back_xp.xml', 'ic_nav_forward_xp.xml', 'ic_nav_home_xp.xml',
                 'ic_nav_tabs_xp.xml', 'ic_nav_menu_xp.xml'):
    ElementTree.parse(root / 'app/src/main/res/drawable' / resource)
assert 'navImage(bottomBar, R.drawable.ic_nav_back_xp' in activity
assert 'navImage(bottomBar, R.drawable.ic_nav_forward_xp' in activity
assert 'navImage(bottomBar, R.drawable.ic_nav_home_xp' in activity
assert 'navImage(bottomBar, R.drawable.ic_nav_menu_xp' in activity
assert 'icon.setImageResource(R.drawable.ic_nav_tabs_xp);' in activity
assert 'private ImageView backIcon;' in activity and 'private ImageView forwardIcon;' in activity
assert 'backIcon.setColorFilter(' in activity and 'forwardIcon.setColorFilter(' in activity
assert 'footer.addView(loadColumn, new LinearLayout.LayoutParams(0, dp(28), 1));' in activity
assert 'loadBarParams.rightMargin' not in activity
assert 'REQUEST_SAVE_CAPTURE' in activity
assert 'Intent.ACTION_CREATE_DOCUMENT' in activity and 'save.setType("image/png")' in activity
assert 'private void exportScreenshot(File image)' in activity
assert 'pendingCaptureExport = null;' in activity
assert 'private void updatePasteButtonAvailability()' in activity
assert 'if (!urlBox.hasFocus() || isFinishing() || isDestroyed()) return;' in activity
assert '1.0.40' in (root/'AGENTS.md').read_text()
print('1.0.34: ícones vetoriais, progresso adaptativo, SAF e sugestão estável: OK')

# 1.0.35: favoritos editáveis e abas com identidade estável.
assert 'int freshIndex = tabs.indexOf(tab);' in activity
assert 'private void populateBookmarkRows(' in activity
assert 'private void persistBookmarkOrder()' in activity
assert 'recentlyClosedNormalTab = closing.url;' in activity
assert 'if (!closing.privateMode && TabSessionStore.restorable(closing.url))' in activity
assert 'showModuleScreen("Favoritos"' in activity
assert 'showModuleScreen("Abas abertas"' in activity
assert (root/'app/src/main/java/br/com/internetxp/BookmarkOrganizer.java').exists()
assert (root/'app/src/test/java/br/com/internetxp/BookmarkOrganizerTest.java').exists()
assert '1.0.40' in (root/'AGENTS.md').read_text()
print('1.0.35: busca/reordenação de favoritos, abas e privacidade: OK')

# 1.0.36: API de certificado GeckoView 158 (falha real do workflow 34).
import re as _re
assert _re.search(r"\bX509Certificate\s+certificate\s*=\s*info\.certificate\s*;", activity)
assert 'certificate.getIssuerX500Principal().getName()' in activity
assert 'certificate.getSubjectX500Principal().getName()' in activity
assert 'certificate == null ? "Não disponível"' in activity
assert not _re.search(r'info\.(?:issuerCommonName|issuerOrganization)\b', activity), 'Campos removidos da API GeckoView 158'
print('1.0.36: certificado X.509 e API GeckoView 158: OK')

# 1.0.37: diagnóstico seguro, exportação ZIP e recuperação manual.
assert 'REQUEST_SAVE_DIAGNOSTIC' in activity
assert 'Intent.ACTION_CREATE_DOCUMENT' in activity
assert 'DiagnosticLog.writeZip(' in activity
assert 'DiagnosticLog.recentExits(' in activity
assert 'DiagnosticLog.memorySummary(' in activity
assert 'private void offerRecoverTab(Tab tab)' in activity
assert 'private void recoverTab(Tab tab)' in activity
assert 'PREF_OPEN_TABS_BACKUP' in activity
log = (root/'app/src/main/java/br/com/internetxp/DiagnosticLog.java').read_text()
assert 'getHistoricalProcessExitReasons' in log
assert 'ZipOutputStream' in log
assert 'getDescription()' not in log and 'getTraceInputStream()' not in log
assert (root/'app/src/main/java/br/com/internetxp/ExitReasonLabels.java').exists()
assert (root/'app/src/test/java/br/com/internetxp/ExitReasonLabelsTest.java').exists()
print('1.0.37: exportacao segura, recuperacao de abas e Android 11+: OK')

# 1.0.40: serviços locais / privacidade e medição de navegação.
address = (root/'app/src/main/java/br/com/internetxp/BrowserAddress.java').read_text()
localtests = (root/'app/src/test/java/br/com/internetxp/LocalAddressTest.java').read_text()
assert 'isDirectAddress' in address and 'isLocalHost' in address
assert 'BrowserAddress.isDirectAddress(query)' in (root/'app/src/main/java/br/com/internetxp/OnlineSuggestions.java').read_text()
assert 'BrowserAddress.isDirectAddress(entered)' in (root/'app/src/main/java/br/com/internetxp/HomePageSettings.java').read_text()
assert 'android:usesCleartextTraffic="true"' in manifest_xml
for local in ('127.0.0.1:8000', 'localhost:8000', '192.168.1.20:8080', '[::1]:3000'):
    assert local in localtests
assert 'SiteLoadMetrics' in activity and 'onPageStop' in activity
assert (root/'app/src/test/java/br/com/internetxp/SiteLoadMetricsTest.java').exists()
assert 'showLocalSiteHelp' in activity
assert (root/'docs/LOCAL_SERVERS.md').exists()
print('1.0.40: enderecos locais, HTTP, privacidade e medicao de carregamento: OK')

# 1.0.40: controles reais por origem, sem permissões automáticas.
assert (root/'app/src/main/java/br/com/internetxp/SitePermissionRules.java').exists()
assert (root/'app/src/test/java/br/com/internetxp/SitePermissionRulesTest.java').exists()
assert 'private void renderSitePermissions(' in activity
assert 'private void clearSiteMediaChoices()' in activity
assert 'GeckoSession.PermissionDelegate.PERMISSION_DESKTOP_NOTIFICATION' in activity
assert 'SitePermissionRules.isTrustworthyMediaOrigin(uri)' in activity
assert 'setPermission(permission, selected)' in activity
assert 'android.permission.POST_NOTIFICATIONS' in manifest_xml
print('1.0.40: central de permissoes, preferencias locais e notificacoes: OK')

# 1.0.40: mídia sob seleção explícita, sem alegar varredura DOM ou DRM.
assert 'onContextMenu(GeckoSession s, int screenX, int screenY' in activity
assert 'setSelectionActionDelegate(new BasicSelectionActionDelegate(this, true))' in activity
assert 'private void showMediaCenter()' in activity
assert 'private void showMediaDetails(MediaResource media)' in activity
assert 'private void downloadSelectedMedia(MediaResource media)' in activity
assert (root/'app/src/main/java/br/com/internetxp/MediaResource.java').is_file()
assert (root/'app/src/main/java/br/com/internetxp/MediaInspector.java').is_file()
assert (root/'app/src/test/java/br/com/internetxp/MediaResourceTest.java').is_file()
assert 'new FaviconRepository(getApplicationContext())' in activity
assert 'xp_favicons_v2' in (root/'app/src/main/java/br/com/internetxp/FaviconRepository.java').read_text()
assert 'MediaResource.isDownloadableUrl' in activity
print('1.0.40: menu contextual, mídias selecionadas e favicon persistente: OK')
