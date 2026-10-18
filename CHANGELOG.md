# CHANGELOG

## 1.0.3 (versionCode 4) - correção do workflow 3
- Logs da terceira execução analisados: `sdkmanager` encerrou com `Failed to find package 'platforms;android-37'` antes do Gradle.
- Corrige o pacote para `platforms;android-37.0`, nome realmente disponibilizado pelo SDK Android 17.
- Valida a existência do `android.jar` instalado e de `aapt2` em Build Tools 36.0.0, falhando de forma explícita caso faltem.
- Adiciona alias de diretório `android-37 -> android-37.0` exclusivamente no runner para contornar a diferença de resolução documentada em GitHub Actions.
- Preserva GeckoView 158, AGP 9.1.1, Gradle 9.3.1, `compileSdk 37`, `targetSdk 35`, saída APK direta na Release e Secrets de assinatura.
- `versionCode` 4 e `versionName` 1.0.3 sincronizados. Sem build completo Android comprovado ainda.

## 1.0.2 (versionCode 3) - correção do workflow 2
- Corrige 11 incompatibilidades detectadas em `:app:checkDebugAarMetadata` envolvendo GeckoView 158, AndroidX Core 1.19 e Media3 1.11.1.
- Atualiza `compileSdk` 35 → 37; mantém `targetSdk` 35 e `minSdk` 24.
- Atualiza AGP 8.13.2 → 9.1.1 e Gradle 8.13 → 9.3.1, conforme tabela oficial de compatibilidade.
- Instala `platforms;android-37` e `build-tools;36.0.0` no Actions.
- Mantém saída direta na Release `Internet XP.v1.0.2.apk` (sem Artifacts ZIP) e assinatura opcional por Secrets.
- Não houve build completo local nem confirmação de `:app:assembleDebug`; aguardando novo workflow.

## 1.0.1 (versionCode 2) - correção do workflow 1
- Corrige falha anterior à compilação: `android-actions/setup-android@v3` tentava instalar o pacote Android SDK `tools` inexistente.
- Atualiza ação para `android-actions/setup-android@v4` e instala apenas `platform-tools`, `platforms;android-35` e `build-tools;35.0.0`.
- Gera Release com nome `Internet XP.v1.0.1.apk` e tag `v1.0.1`, sem upload de ZIP em Artifacts.
- Interrompe assinatura parcialmente configurada em vez de publicar silenciosamente um APK Debug.
- Adiciona `--stacktrace` às tarefas Gradle para próximos diagnósticos.
- O Android APK ainda **não foi compilado nem publicado** neste ambiente.

# Histórico
## 1.0.0: código inicial em 2026-10-18
- Motor GeckoView e interface XP.
- Barra de endereço, navegação, abas, favoritos, histórico local, downloads públicos e gerenciador inicial de extensões.
- Workflow para publicação de APK diretamente em GitHub Releases, sem artefato ZIP.
- Build completo, publicação e testes em aparelho pendentes.
