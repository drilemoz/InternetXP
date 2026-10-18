# Validação v1.0.3

- [x] Log do workflow 3 analisado: etapa `SDK` executou `sdkmanager --install 'platforms;android-37' 'build-tools;36.0.0'` e falhou com `Failed to find package 'platforms;android-37'`, código 1.
- [x] Consulta à documentação Android e issue `actions/runner-images#13859`: SDK Android 17 é `platforms;android-37.0`, com possível diferença de hash `android-37` no Gradle.
- [x] Workflow corrigido para instalar `platforms;android-37.0` e verificar `android.jar`, alias condicionado, `aapt2` e versão do release.
- [x] `versionName` 1.0.3, `versionCode` 4, README, AGENTS, CHANGELOG, release workflow e tela Sobre sincronizados.
- [x] XML, YAML, teste Java independente e integridade ZIP verificados localmente.
- [ ] SDK Android, Gradle/AGP e GeckoView reais: build completo no GitHub Actions.
- [ ] Verificar ausência de erros Java/GeckoView, instalar em aparelho, testar interface e extensões, publicar Release.
- [ ] Assinatura persistente pelos quatro Secrets antes de distribuir APK destinado a receber atualizações.

**Documentação:** https://developer.android.com/build/releases/agp-9-1-0-release-notes e https://github.com/actions/runner-images/issues/13859

---

# Validação v1.0.2

- [x] Logs do workflow 2 analisados (`InternetXP-Compilar-APK-e-publicar-na-Release-2-logs.zip`). Etapa de preparação SDK e Secrets concluída; `:app:checkDebugAarMetadata` falhou, com 11 requisitos insatisfeitos.
- [x] Dependência GeckoView 158 exige `compileSdk >=37`; AndroidX Core 1.19 exige também AGP >=9.1.0; Media3 exige `compileSdk >=36`.
- [x] Fonte Android: AGP 9.1.1 é compatível com API 37, Gradle mínimo 9.3.1, Build Tools 36.0.0, JDK 17. https://developer.android.com/build/releases/agp-9-1-0-release-notes
- [x] `versionName` 1.0.2, `versionCode` 3, nomes da Release e documentação sincronizados.
- [ ] Build de CI `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug` completo e compilação Java do GeckoView.
- [ ] Instalação em aparelho, teste real da navegação/extensões, verificação visual XP e publicação da Release.
- [ ] Assinatura Release estável por Secrets quando a primeira distribuição for definida.

Importante: aprovação local de arquivos, testes Java unitários isolados e integridade ZIP **não equivale** a build Android aprovado.
