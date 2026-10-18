# CHANGELOG

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
