#!/usr/bin/env python3
from pathlib import Path
from xml.etree import ElementTree
import re
import yaml

root = Path(__file__).resolve().parents[1]
gradle = (root / 'app/build.gradle').read_text()
workflow_text = (root / '.github/workflows/build-release-apk.yml').read_text()
activity = (root / 'app/src/main/java/br/com/internetxp/MainActivity.java').read_text()
assert "versionCode 17" in gradle
assert "versionName '1.0.16'" in gradle
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
assert 'Internet XP 1.0.16' in activity
assert "Internet XP.v1.0.16-${abi}.apk" in workflow_text
assert 'upload-artifact' not in workflow_text
assert 'gh release create v1.0.16' in workflow_text
for file in (root / 'app/src/main').rglob('*.xml'):
    ElementTree.parse(file)
workflow = yaml.load(workflow_text, Loader=yaml.BaseLoader)
assert workflow['name'] == 'Compilar APK e publicar na Release'
assert 'apk' in workflow['jobs']
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

assert "versionCode='17' versionName='1.0.16'" in workflow_text
assert "v1.0.12" not in workflow_text
assert "Internet XP.v1.0.16-${abi}.apk" in workflow_text

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

# Compatibilidade v1.0.16
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
