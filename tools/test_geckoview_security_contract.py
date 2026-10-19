"""Evita regressão do workflow 34 (GeckoView 158 usa SecurityInformation.certificate)."""
from pathlib import Path
import unittest
import re

ROOT = Path(__file__).resolve().parents[1]
MAIN = ROOT / 'app/src/main/java/br/com/internetxp/MainActivity.java'

class GeckoViewSecurityContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.source = MAIN.read_text(encoding='utf-8')

    def test_removed_fields_are_not_used(self):
        self.assertIsNone(re.search(r'\binfo\.(?:issuerCommonName|issuerOrganization)\b', self.source))

    def test_certificate_access_uses_x509_contract(self):
        self.assertRegex(self.source, r'X509Certificate\s+certificate\s*=\s*info\.certificate\s*;')
        self.assertIn('certificate.getIssuerX500Principal().getName()', self.source)
        self.assertIn('certificate.getSubjectX500Principal().getName()', self.source)

    def test_unavailable_certificate_has_explicit_fallback(self):
        self.assertEqual(2, self.source.count('certificate == null ? "Não disponível"'))

if __name__ == '__main__':
    unittest.main()
