# CHANGELOG

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
