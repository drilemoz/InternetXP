#!/usr/bin/env python3
from pathlib import Path
from xml.etree import ElementTree
import re

root = Path(__file__).resolve().parents[1]
gradle = (root / 'app/build.gradle').read_text()
workflow_text = (root / '.github/workflows/build-release-apk.yml').read_text()
activity = (root / 'app/src/main/java/br/com/internetxp/MainActivity.java').read_text()
assert "versionCode 27" in gradle
assert "versionName '1.0.26'" in gradle
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
assert "Internet XP.v1.0.26-${abi}.apk" in workflow_text
assert 'upload-artifact' not in workflow_text
assert 'gh release create v1.0.26' in workflow_text
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

assert "versionCode='27' versionName='1.0.26'" in workflow_text
assert "v1.0.12" not in workflow_text
assert "Internet XP.v1.0.26-${abi}.apk" in workflow_text

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

assert '1.0.26' in (root/'README.md').read_text()
assert '1.0.26' in (root/'CHANGELOG.md').read_text()
assert '1.0.26' in (root/'docs/VALIDATION.md').read_text()

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
assert 'new Feature("01", "Pop-ups coerentes"' in notes, 'Novidades desatualizadas'
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
assert 'new Feature("05", "Extensões integradas"' in notes
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
assert 'showModuleMenuDialog("Favoritos"' in activity
assert 'showModuleMenuDialog("Extensões Firefox"' in activity
assert 'styledDialogContent("Histórico de navegação"' in activity
assert 'private void showAbout()' in activity
assert (root/'app/src/main/java/br/com/internetxp/HomePageView.java').exists()
assert (root/'app/src/main/java/br/com/internetxp/HomePageSettings.java').exists()
assert (root/'app/src/main/java/br/com/internetxp/FaviconRepository.java').exists()
assert (root/'app/src/test/java/br/com/internetxp/HomePageSettingsTest.java').exists()
assert 'new Feature("05", "Extensões integradas"' in notes
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


# v1.0.26: pop-ups alinhados, animações e compactação
assert "animateDialogOpen(" in activity
assert "confirmUninstallExtension(" in activity
assert "isTinyPhone()" in activity
assert "styleBottomMenuDialog(dialog);\n        animateDialogOpen(wrapper, true);" in activity
assert "showModuleMenuDialog(name, x.metaData.enabled ?" in activity
print('Pop-ups alinhados e animações: OK')
