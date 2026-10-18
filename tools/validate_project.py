#!/usr/bin/env python3
"""Checks that do not require the Android SDK."""
from pathlib import Path
from xml.etree import ElementTree
import re
import yaml

root = Path(__file__).resolve().parents[1]
gradle = (root / 'app/build.gradle').read_text()
workflow_text = (root / '.github/workflows/build-release-apk.yml').read_text()
activity = (root / 'app/src/main/java/br/com/internetxp/MainActivity.java').read_text()
assert "versionCode 11" in gradle
assert "versionName '1.0.10'" in gradle
assert all(x in gradle for x in ("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
assert "universalApk false" in gradle
assert "minifyEnabled true" in gradle
assert "shrinkResources true" in gradle
assert "useLegacyPackaging = true" in gradle
assert not re.search(r'\btitleBar\b', activity)
assert not re.search(r'\bminimize\b|\bmaximize\b', activity)
assert "private void updateSecurity(" in activity
assert 'R.drawable.ic_lock_xp' in activity
assert "Internet XP 1.0.10" in activity
assert "Internet XP.v1.0.10-${abi}.apk" in workflow_text
assert "upload-artifact" not in workflow_text
assert 'app-debug.apk' not in workflow_text
assert 'release-assets.txt' in workflow_text
assert 'python3 tools/report_apk.py' in workflow_text
for file in (root / 'app/src/main').rglob('*.xml'):
    ElementTree.parse(file)
workflow = yaml.load(workflow_text, Loader=yaml.BaseLoader)
assert workflow['name'] == 'Compilar APK e publicar na Release'
assert 'apk' in workflow['jobs']
steps = workflow['jobs']['apk']['steps']
assert any('gradle --no-daemon --stacktrace :app:assembleRelease' in step.get('run','') for step in steps)
assert any('gh release create v1.0.10' in step.get('run','') for step in steps)
print('Validação local dos metadados, workflow e XML: OK')

# A detecção de APKs por ABI deve usar caminho exato, não glob contendo x86_64 quando espera x86.
assert 'src="$src_dir/app-${abi}-release.apk"' in workflow_text
assert 'candidates=("$src_dir"/*"$abi"*-release.apk)' not in workflow_text
assert workflow_text.index('      - name: Testes, lint e compilação Debug (não publicada)') < workflow_text.index('      - name: Preparar assinatura obrigatória para Release')
