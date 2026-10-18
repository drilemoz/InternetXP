#!/usr/bin/env python3
from pathlib import Path
from xml.etree import ElementTree
import re

root = Path(__file__).resolve().parents[1]
gradle = (root / 'app/build.gradle').read_text()
workflow_text = (root / '.github/workflows/build-release-apk.yml').read_text()
activity = (root / 'app/src/main/java/br/com/internetxp/MainActivity.java').read_text()
assert "versionCode 19" in gradle
assert "versionName '1.0.18'" in gradle
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
assert 'Internet XP 1.0.18' in activity
assert "Internet XP.v1.0.18-${abi}.apk" in workflow_text
assert 'upload-artifact' not in workflow_text
assert 'gh release create v1.0.18' in workflow_text
for file in (root / 'app/src/main').rglob('*.xml'):
    ElementTree.parse(file)
assert 'name: Compilar APK e publicar na Release' in workflow_text
assert 'jobs:' in workflow_text and '  apk:' in workflow_text
print('Validação local dos metadados, workflow e XML: OK')

assert 'urlBox.setSelectAllOnFocus(true)' in activity
assert 'urlBox.setOnClickListener(v -> urlBox.selectAll())' in activity
assert 'private void confirmCancelDownload(' in activity
assert 'private void retryDownload(' in activity
assert 'scheduleDownloadRefresh()' in activity
assert 'HistorySuggestions.filter(' in activity
assert 'DownloadDisplay.percent(' in activity
assert (root / 'app/src/main/java/br/com/internetxp/HistorySuggestions.java').exists()
assert (root / 'app/src/main/java/br/com/internetxp/DownloadDisplay.java').exists()

assert "versionCode='19' versionName='1.0.18'" in workflow_text
assert "v1.0.12" not in workflow_text
assert "Internet XP.v1.0.18-${abi}.apk" in workflow_text

assert 'ProgressBar progress' in activity
assert 'private void shareDownload(' in activity
assert 'private void confirmClearFinished(' in activity
assert 'setAllowedOverMetered(!unmeteredOnly)' in activity
assert 'DownloadDisplay.speed(' in activity
assert 'downloadReason(' in activity
assert 'DownloadManager.ACTION_VIEW_DOWNLOADS' not in activity

# 1.0.15: abas persistidas sob demanda, histórico com metadados
assert 'if (!restoreTabs()) addTab(HOME)' in activity
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

assert '1.0.18' in (root/'README.md').read_text()
assert '1.0.18' in (root/'CHANGELOG.md').read_text()
assert '1.0.18' in (root/'docs/VALIDATION.md').read_text()

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
