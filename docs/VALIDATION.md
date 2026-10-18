# Validação v1.0.2

- [x] Logs do workflow 2 analisados (`InternetXP-Compilar-APK-e-publicar-na-Release-2-logs.zip`). Etapa de preparação SDK e Secrets concluída; `:app:checkDebugAarMetadata` falhou, com 11 requisitos insatisfeitos.
- [x] Dependência GeckoView 158 exige `compileSdk >=37`; AndroidX Core 1.19 exige também AGP >=9.1.0; Media3 exige `compileSdk >=36`.
- [x] Fonte Android: AGP 9.1.1 é compatível com API 37, Gradle mínimo 9.3.1, Build Tools 36.0.0, JDK 17. https://developer.android.com/build/releases/agp-9-1-0-release-notes
- [x] `versionName` 1.0.2, `versionCode` 3, nomes da Release e documentação sincronizados.
- [ ] Build de CI `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug` completo e compilação Java do GeckoView.
- [ ] Instalação em aparelho, teste real da navegação/extensões, verificação visual XP e publicação da Release.
- [ ] Assinatura Release estável por Secrets quando a primeira distribuição for definida.

Importante: aprovação local de arquivos, testes Java unitários isolados e integridade ZIP **não equivale** a build Android aprovado.
