# Validação v1.0.5

- [x] Logs do workflow 5 analisados: Java compilado e testes executados. `:app:lintDebug` falhou com 3 erros e 9 avisos; único erro detalhado é `BrowserAddress.java:16` com sobrecarga de API 33, incompatível com minSdk 26.
- [x] Encoding de buscas corrigido usando sobrecarga presente em APIs antigas (`URLEncoder.encode(String, "UTF-8")`).
- [x] Corrigido defeito de aspas na geração do checksum SHA-256 do APK que seria executado após o Lint.
- [x] Workflow agora mostra o relatório completo do Lint após falha, incluindo erros que antes ficavam ocultos.
- [x] Versão 1.0.5 / versionCode 6 e notas sincronizadas; APK somente na Release, sem `upload-artifact`.
- [ ] Rodar `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug` no GitHub Actions para identificar/solucionar os demais erros de lint, se persistirem.
- [ ] Verificar relatório `aapt dump badging`, asset `Internet XP.v1.0.5.apk` publicado e assinatura.
- [ ] Testar instalação/navegação/extensões em smartphone Android 8+ ARM64.
- [ ] Configurar Secrets de assinatura estável antes da distribuição.

---

# Validação v1.0.4

- [x] Logs da execução 4 lidos no ZIP enviado: a instalação do Android SDK 37.0 concluiu, `:app:checkDebugAarMetadata` passou e `:app:compileDebugJavaWithJavac` concluiu com aviso de API obsoleta (sem erro).
- [x] Erro objetivo em `:app:processDebugMainManifest`: `uses-sdk:minSdkVersion 24 cannot be smaller than version 26 declared in library org.mozilla.geckoview:geckoview:158.0.20261007201254`.
- [x] minSdk 24 → 26; nenhuma utilização de `tools:overrideLibrary`.
- [x] `versionName` 1.0.4, `versionCode` 5, README, AGENTS, CHANGELOG, workflow, app e Sobre sincronizados.
- [x] Workflow mantém publicação de APK direto na GitHub Release, sem ZIP em Artifacts.
- [ ] Aguardar a compilação completa v1.0.4 no GitHub Actions e verificar o relatório `aapt dump badging`, APK e Release.
- [ ] Validar instalação/lançamento, interface, navegação e extensões em aparelho Android 8.0+ ARM64.
- [ ] Definir assinatura persistente com KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS e KEY_PASSWORD antes de distribuir atualização garantida.

---

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
