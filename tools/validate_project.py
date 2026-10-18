#!/usr/bin/env python3
from pathlib import Path
from xml.etree import ElementTree
import re
import yaml

root = Path(__file__).resolve().parents[1]
gradle = (root / 'app/build.gradle').read_text()
workflow_text = (root / '.github/workflows/build-release-apk.yml').read_text()
activity = (root / 'app/src/main/java/br/com/internetxp/MainActivity.java').read_text()
assert "versionCode 13" in gradle
assert "versionName '1.0.12'" in gradle
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
assert 'Internet XP 1.0.12' in activity
assert "Internet XP.v1.0.12-${abi}.apk" in workflow_text
assert 'upload-artifact' not in workflow_text
assert 'gh release create v1.0.12' in workflow_text
for file in (root / 'app/src/main').rglob('*.xml'):
    ElementTree.parse(file)
workflow = yaml.load(workflow_text, Loader=yaml.BaseLoader)
assert workflow['name'] == 'Compilar APK e publicar na Release'
assert 'apk' in workflow['jobs']
print('Validação local dos metadados, workflow e XML: OK')
