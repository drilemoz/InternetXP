# CHANGELOG

## 1.0.7 (versionCode 8) - barra de ferramentas mais compacta e Downloads
- Ícones de 31 sp para 20 sp; botões de 66 dp para 49 dp, com legendas menores de 9 sp e espaçamento reduzido.
- Ícone vetorial de Downloads na barra XP, abrindo o gerenciador de downloads do sistema Android; permanece a limitação de downloads com autenticação do GeckoView.
- Largura adaptativa para nove ações em telas comuns e rolagem em telas muito estreitas. Adicionados testes JVM de cálculo de largura.
- Mantidos GeckoView, assinatura por Secrets, minSdk e o workflow de APK na Release. Nome `Internet XP.v1.0.7.apk`.
- Sincronizado versionCode 8, README, AGENTS, documentação e Sobre. Build Android completo pendente.


## 1.0.6 (versionCode 7) - correção do workflow 6
- Logs da sexta execução analisados: a compilação Java e os testes unitários completaram, mas `:app:lintDebug` falhou com 2 erros `WrongConstant` e 9 avisos.
- Ambos os erros estão em `MainActivity.java`, linhas 73 e 118, devido a `LinearLayout.setOrientation(1)`. Substitui por `LinearLayout.VERTICAL`, mesma orientação da versão anterior, mas com a constante reconhecida pelo Android Lint.
- Mantém o estilo XP, GeckoView 158, `compileSdk 37`, `targetSdk 35`, `minSdk 26`, AGP 9.1.1, Gradle 9.3.1 e fluxo de assinatura com quatro Secrets opcionais.
- Sincroniza `versionName` 1.0.6, `versionCode` 7, menu Sobre, workflow, regras e documentação.
- Continua sem `actions/upload-artifact` ou Artifact ZIP: o alvo é `Internet XP.v1.0.6.apk` diretamente no GitHub Releases.
- Avisos Lint (OldTargetApi, ChromeOsAbiSupport, DataExtractionRules, MissingApplicationIcon e SetTextI18n) permanecem registrados para avaliação posterior; não foram ignorados os erros.
- Build Android completo e publicação no GitHub Actions pendentes de validação.

## 1.0.5 (versionCode 6) - correção do workflow 5
- Logs da quinta execução: `:app:compileDebugJavaWithJavac` e `:app:testDebugUnitTest` concluíram; `:app:lintDebug` falhou com 3 erros e 9 avisos. Apenas o primeiro erro aparece detalhado no ZIP enviado.
- Corrige a chamada `URLEncoder.encode(String, Charset)`, exclusiva da API 33, para `URLEncoder.encode(String, "UTF-8")`, presente no Android 8.0 (API 26), com tratamento de `UnsupportedEncodingException`. Preserva Unicode e encoding de consultas.
- Corrige aspa ausente na linha `sha256sum` do workflow de Release, que impediria gerar o checksum do APK.
- Em caso de falha no Android Lint, imprime o relatório texto completo no log da execução para diagnosticar outros erros sem mascará-los.
- Atualiza `versionName` para 1.0.5, `versionCode` para 6, menu Sobre, workflow, documentação e nome do APK da Release `Internet XP.v1.0.5.apk`.
- Sem desativar Lint, sem elevar minSdk, sem trocar GeckoView nem publicar Artifacts ZIP.
- Build e Release 1.0.5 ainda não verificados no GitHub Actions.


## 1.0.4 (versionCode 5) - correção do workflow 4
- Logs da quarta execução: SDK 37.0 instalado, `:app:checkDebugAarMetadata` e `:app:compileDebugJavaWithJavac` aprovados; falha na tarefa `:app:processDebugMainManifest`.
- Causa exata: o GeckoView `158.0.20261007201254` declara minSdk 26, enquanto o aplicativo mantinha minSdk 24.
- Corrige minSdk 24 → 26 (Android 8.0+); mantém `compileSdk 37`, `targetSdk 35`, GeckoView 158, AGP 9.1.1 e Gradle 9.3.1.
- Não usa `tools:overrideLibrary`, que ocultaria a incompatibilidade de APIs.
- Verifica a versão e o minSdk do APK gerado no workflow antes de publicá-lo, mantendo GitHub Releases com APK direto e sem Artifacts ZIP.
- Sincroniza `versionName` 1.0.4, `versionCode` 5, menu Sobre, README, AGENTS, notas da Release e documentação.
- Ainda não existe APK 1.0.4 compilado e validado; serão necessários novos testes CI e no aparelho.

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
