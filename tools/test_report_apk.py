import tempfile
import unittest
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED, ZIP_STORED

from report_apk import inspect_apk


class ReportApkTest(unittest.TestCase):
    def test_correct_abi_deflated(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "test.apk"
            with ZipFile(path, 'w', ZIP_DEFLATED) as z:
                z.writestr('lib/arm64-v8a/libxul.so', b'A' * 800)
                z.writestr('classes.dex', b'B' * 160)
            result = inspect_apk(path, 'arm64-v8a')
            self.assertEqual(result['native_count'], 1)
            self.assertGreater(result['total'], 0)

    def test_rejects_universal(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'test.apk'
            with ZipFile(path, 'w', ZIP_DEFLATED) as z:
                z.writestr('lib/arm64-v8a/libxul.so', b'A' * 300)
                z.writestr('lib/x86_64/libxul.so', b'B' * 300)
            with self.assertRaisesRegex(ValueError, 'ABI esperada'):
                inspect_apk(path, 'arm64-v8a')

    def test_rejects_uncompressed(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'test.apk'
            with ZipFile(path, 'w', ZIP_STORED) as z:
                z.writestr('lib/armeabi-v7a/libxul.so', b'A' * 300)
            with self.assertRaisesRegex(ValueError, 'não compactada'):
                inspect_apk(path, 'armeabi-v7a')

    def test_rejects_missing_native_libraries(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'test.apk'
            with ZipFile(path, 'w', ZIP_DEFLATED) as z:
                z.writestr('classes.dex', b'A' * 300)
            with self.assertRaisesRegex(ValueError, 'sem bibliotecas nativas'):
                inspect_apk(path, 'arm64-v8a')

    def test_rejects_wrong_abi(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'test.apk'
            with ZipFile(path, 'w', ZIP_DEFLATED) as z:
                z.writestr('lib/x86/libxul.so', b'A' * 300)
            with self.assertRaisesRegex(ValueError, 'ABI esperada'):
                inspect_apk(path, 'x86_64')


if __name__ == '__main__':
    unittest.main()
