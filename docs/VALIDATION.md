# Validação 1.0.1
- [x] Logs do workflow 1 examinados: falha real `sdkmanager tools` em `setup-android@v3` antes de Gradle.
- [x] Documentação oficial `android-actions/setup-android` verificada: `tools` obsoleto, v4 usa `platform-tools`.
- [x] Dependência `org.mozilla.geckoview:geckoview:158.0.20261007201254` confirmada no repositório Maven Mozilla (índice público).
- [x] Testes locais Java de resolução de endereços e XML/YAML/ZIP após a correção.
- [ ] `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug` no GitHub Actions.
- [ ] Assinatura Release por Secrets, instalação em aparelho, extensões reais, APK na Release.

A falha anterior não avalia a validade de compilação do código Java, pois o workflow não executou Gradle.
