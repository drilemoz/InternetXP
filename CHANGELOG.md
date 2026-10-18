## 1.0.27 (versionCode 28) - central especial de extensões WebExtensions
- Instalador real para arquivos `.xpi` já salvos em Downloads usando GeckoView, sem repetir o download.
- Importação local SAF com limite de tamanho, inspeção do manifesto ZIP e validação efetiva da assinatura Mozilla pelo motor.
- Gerenciador de extensões em Activity independente: estados lidos no GeckoView, alternância de atividade, desinstalação, permissões, metadados e diagnóstico.
- Novo menu Ações das extensões registra browser_action/page_action e oferece abertura inicial de pop-ups pela API GeckoView, para complementos que precisam de interação.
- Exportação do XPI arquivado em importações locais, ou JSON de diagnóstico quando o pacote original não está disponível pela API.
- Controles de confirmação, tarefas de I/O fora da thread de interface e documentação de limitações de compatibilidade.
- Atualizadas tela de Novidades, README, AGENTS, workflow, validações e versão sequencial.

# CHANGELOG

## 1.0.26 (versionCode 27) - pop-ups alinhados ao menu XP
- Padronizado o visual de pop-ups e caixas de diálogo para combinar com o menu principal XP Moderno.
- Refinado o cabeçalho azul do menu e adicionada animação suave de abertura em painéis e diálogos.
- Reduzidas as margens laterais e ajustadas tipografia, grade e alturas para telas menores como o Redmi A3.
- Ações de extensões migradas para painéis e confirmações no mesmo estilo do aplicativo.
- Atualizadas tela de Novidades, README, AGENTS, workflow e validações para a versão 1.0.26.

## 1.0.22 (versionCode 23) - página inicial, favoritos e diálogos XP
- Adicionada página inicial nativa (`about:blank`) com campo de pesquisa centralizado e grade de atalhos para favoritos.
- Botão Adicionar disponível diretamente na página inicial, com confirmação de URL para salvar favoritos.
- Tela modular Configurações permite escolher a página inicial nativa ou uma URL personalizada, persistida localmente.
- Favicons HTTPS carregados de `/favicon.ico` do próprio domínio, com cache de memória, limites de conexão e fallback por letra; sem terceiros.
- Mostra favicon opcional ao lado do indicador de segurança, preservando o cadeado verificado pelo GeckoView.
- Menus de Favoritos e Extensões convertidos em cartões estilo XP; Sobre e Histórico com layout estilizado.
- Menu modular com largura e altura reduzidas para facilitar uso em telas como Redmi A3.
- Novidades, AGENTS, documentação, testes, workflow e Release sincronizados.

## 1.0.21 (versionCode 22) - navegação privada e segurança
- GeckoSession privado, sem persistência no histórico e no backup local de abas.
- Proteção antirrastreamento configurável com preferência persistente.
- Limpeza de cookies, sessões de autenticação, cache, permissões e dados completos via GeckoView StorageController, com fechamento prévio das sessões.
- Indicador HTTPS usa a informação de segurança real do GeckoView e informa conteúdo misto/exceção quando aplicável.
- Gerenciamento de permissões da página e alerta sobre arquivos baixados em abas privadas.
- Tela de Novidades 1.0.21 atualizada; README, AGENTS, validação e workflow sincronizados.

# CHANGELOG

## 1.0.19 (versionCode 20) - corrige dependência @OptIn (workflow 19)
- Logs da execução 19: `:app:compileDebugJavaWithJavac` falhou por falta de `androidx.annotation.OptIn` e `androidx.annotation.RequiresOptIn$Level`; o Lint nem chegou a executar.
- Adicionadas dependências explícitas `androidx.annotation:annotation:1.11.0` e `androidx.annotation:annotation-experimental:1.6.0`, conforme documentação oficial AndroidX, para compilar @OptIn no código Java com GeckoView.
- Preservado `@OptIn(markerClass = ExperimentalGeckoViewApi.class)` somente no método que chama `permission.notifyShown()`; não desabilita Lint.
- Tela de Novidades atualizada com destaques reais da versão 1.0.19; teste de versão e checagens estruturais atualizados.
- README, AGENTS, VALIDATION, Gradle, GitHub workflow e notas de release sincronizados; permanece a publicação de três APKs Release assinados por ABI.
- Compilação Android/Gradle, Lint, testes de instalação e release pendentes de execução real.


## 1.0.18 (versionCode 19) - correção de Lint e tela de Novidades
- Logs da execução 18: compilação Java passou, mas o `:app:lintDebug` falhou com `UnsafeOptInUsageError` em `permission.notifyShown()` na linha 1598.
- Aplicado opt-in explícito com `@OptIn(markerClass = ExperimentalGeckoViewApi.class)` no método de diálogo de localização. A chamada de `notifyShown()` é preservada.
- Implementada Activity personalizada **Novidades** com cabeçalho azul clássico, globo, destaques em cartões e botão de continuar.
- Exibição automática após primeira instalação ou incremento do `versionCode`; vista apenas uma vez por versão, mas revisável pelo menu.
- Criada fonte centralizada `ReleaseNotes.java` e teste unitário; documentação `AGENTS.md` exige atualizar notas em TODA alteração que incremente a versão.
- Adicionado ícone monocromático aos ícones adaptativos do launcher.
- Mantidos GeckoView 158, três APKs Release assinados por ABI e armazenamento privado dos Secrets.
- Compilação Android real, Lint e testes em aparelho ainda precisam ser executados no GitHub Actions.


## 1.0.17 (versionCode 18) - identidade visual do aplicativo
- Novo ícone exclusivo: globo azul em acabamento clássico XP com anel dourado.
- Ícones PNG para densidades mdpi, hdpi, xhdpi, xxhdpi e xxxhdpi.
- Suporte a ícone adaptativo Android 8+ (foreground/background) e roundIcon.
- Atualiza o manifesto Android, versão, workflow e metadados, mantendo o funcionamento anterior.
- Não inclui neste ciclo o módulo de privacidade planejado, que permanece para a próxima versão.


## 1.0.16 (versionCode 17) - compatibilidade com sites
- Integra `FilePrompt` com seletor nativo Android (arquivos individuais e seleção múltipla).
- Trata pedidos de permissões Android de câmera, microfone e localização e confirmação explícita por site HTTPS.
- Adiciona tela cheia para conteúdo GeckoView e saída por Voltar.
- Solicita confirmação para links externos iniciados por gesto e suporta abertura de novas janelas do site em abas internas.
- Preserva gerenciamento de downloads, histórico, session restore, GeckoView, assinatura e split por ABI.
- Build completo e testes funcionais em hardware permanecem necessários.

## 1.0.15 (versionCode 16) - abas visuais, restauração leve e histórico pesquisável
- Seletor visual de abas em cartões com título, URL, indicação da aba ativa, abertura por toque, criação e fechamento individual.
- Persistência local de URLs, títulos e aba ativa; restaura até 8 abas com abertura diferida das inativas para reduzir pico de memória.
- Tratamento de índice ativo ao fechar abas em segundo plano e persistência adicional no ciclo de vida `onStop`.
- Histórico enriquecido com títulos e horários reais, pesquisa por título/domínio/URL, agrupamento por datas e exclusão de registros individuais.
- Migração conservadora do histórico antigo sem atribuir data ou título inventado a páginas já visitadas.
- Novas classes Java testáveis `TabSessionStore` e `HistoryRecords` e testes de restauração, filtragem e limites.
- Mantidos downloads, seleção automática de URL, GeckoView e 3 APKs Release assinados por ABI, publicados no GitHub Releases sem Artifact ZIP.
- Compilação Gradle/Android e testes de interface no aparelho permanecem pendentes.

# CHANGELOG

## 1.0.14 (versionCode 15) - Downloads 2.0
- Progresso horizontal por transferência, velocidade calculada por amostras reais e ETA aproximada apenas se o tamanho total for conhecido.
- Filtros Todos/Ativos/Finalizados, resumo de transferências, detalhes e motivos de falha do Android DownloadManager.
- Compartilhamento de arquivo concluído com URI de conteúdo, confirmação de limpeza de registros finalizados sem excluir arquivos.
- Configuração por download para redes não tarifadas, com roaming desativado; persistência da preferência na lista local e em novas tentativas.
- Otimizações: leituras periódicas restritas ao painel aberto, status em memória e apresentação isolados para testes.
- Mantidas seleção da URL ao toque, histórico, GeckoView 158, três APKs assinados por ABI e Secrets anteriores.
- Limitações: pausa manual e retomada parcial não são suportadas pelo DownloadManager; não foi copiado código-fonte inacessível do Baixa Fácil.
- Verificações locais passam, mas compilação Gradle Android completa e testes físicos continuam pendentes.

## 1.0.13 (versionCode 14) - seleção do endereço e primeira otimização
- Seleção integral da URL ao tocar no campo de endereço, para substituição imediata ao digitar. Implementados `setSelectAllOnFocus` e `selectAll` no toque e foco, sem alterar as ações Ir, seta do histórico nem navegação do GeckoView.
- Download interno atualizado: atualização periódica enquanto o painel estiver visível e houver downloads ativos; interrupção das consultas quando o painel é ocultado, a Activity é pausada ou destruída.
- Botão **Cancelar** com confirmação e descarte do arquivo parcial via DownloadManager; **Tentar novamente** para falhas/cancelamentos, iniciando uma nova transferência (não retomada), com status registrado.
- Progresso e formatação em classe Java isolada; histórico/sugestões em classe Java isolada, com testes unitários.
- Evita gravações repetidas do registro de downloads se o status e contadores não mudaram; URI para abrir download é obtida do provedor Android, sem fallback inseguro de `file://`.
- GeckoView e estilo XP preservados. Build continua em 3 APKs Release assinados por ABI, sem Artifacts ZIP. Sem reutilização literal de código não inspecionado do Baixa Fácil.
- Versões e documentos sincronizados, build Android completo e testes físicos pendentes.


## 1.0.12 (versionCode 13) - barra de endereço compacta, histórico e downloads internos
- Reduz a altura da barra de endereço e da barra inferior principal, liberando espaço para o site.
- Aumenta a largura útil da barra de endereço, reduzindo margens laterais desnecessárias.
- Troca o botão Atualizar por um ícone mais coerente com o tema Internet Explorer/XP.
- Adiciona a seta de lista no campo de endereço, abrindo rapidamente endereços do histórico.
- Implementa sugestões de histórico e favoritos durante a digitação no campo de endereço.
- Cria o módulo interno de downloads na parte inferior, com painel embutido, lista de downloads recentes, status, ação para abrir arquivos concluídos e remoção da lista local.
- Mantém GeckoView 158, release assinada obrigatória e três APKs por ABI: armeabi-v7a, arm64-v8a e x86_64.

# CHANGELOG

## 1.0.11 (versionCode 12) - corrigido build por ABI
- Workflow 11: testes Java, Android Lint, assinatura e `:app:assembleRelease` aprovados com R8; a publicação falhou somente porque a variante x86 de 32 bits não inclui bibliotecas nativas do GeckoView 158.
- Excluída ABI `x86` do split e do upload; mantidas `armeabi-v7a`, `arm64-v8a` e `x86_64`.
- O workflow passa a exigir exatamente 3 APKs Release assinados, sempre validados com `report_apk.py` e SHA-256 antes da publicação, sem Artifacts ZIP.
- O analisador rejeita `x86` e ganha testes explícitos de aceitação de `x86_64` e rejeição de x86 32 bits.
- `versionName=1.0.11`, `versionCode=12`, menu Sobre, workflow, README, AGENTS e validações sincronizados.
- Tamanhos medidos na CI anterior: ARM32 84,40 MiB, ARM64 87,00 MiB. O tamanho de x86_64 ainda precisa de confirmação na execução 12.
- Não há APK v1.0.11 compilado ou Release publicada nesta entrega local.


## 1.0.10 (versionCode 11) - diagnóstico dos Secrets e correção do split ABI
- Logs da execução 10: as quatro variáveis de assinatura chegaram vazias; build parou antes de testar/compilar.
- Continua proibido publicar APK Debug no lugar de Release assinada.
- Move testes e Lint antes da exigência dos Secrets para coletar erros de código de forma independente e produzir log informativo sobre Secrets ausentes, sem registrar os valores.
- Usa nomes exatos `app-${abi}-release.apk` para distinguir `x86` de `x86_64` na publicação dos APKs separados.
- Mantém GeckoView 158, interface compacta, R8, ABI split, `minSdk 26` e assinatura anterior. Sincroniza v1.0.10/versionCode 11.
- APK Release e tamanho real ainda dependem de executar o workflow após importar os Secrets no repositório correto.


## 1.0.9 (versionCode 10): interface sem barra de título e Release menor
- Remove totalmente o cabeçalho azul do app, incluindo ícones de minimizar/maximizar e nome, preservando o status bar do Android.
- O painel de endereço vira a primeira barra da interface. Cadeado emoji substituído por vetores; não sinaliza HTTPS onde o URL não usa HTTPS. Botão de atualizar virou vetor.
- Mantém barra inferior, reduzida para 72 dp, menu ancorado no botão Menu e funcionalidades GeckoView.
- Release passa a usar R8 (`minifyEnabled true`, `shrinkResources true`, `proguard-android-optimize.txt`) e compactação de bibliotecas nativas `useLegacyPackaging = true`.
- Publicação exige os quatro Secrets de assinatura e **não publica APK Debug**. Os builds `debug` continuam disponíveis para testes/lint.
- Pipeline gera quatro APKs por ABI, valida conteúdo ZIP, mede tamanho por categoria e publica somente APKs nativos corretos diretamente na Release (sem Artifact ZIP).
- `versionName` 1.0.9, `versionCode` 10, release tag e documentação sincronizados.
- **Pendente:** compilar no GitHub Actions, verificar se R8 preservou GeckoView, medir redução real de tamanho e testar instalação/consumo em aparelho.

## 1.0.8 (versionCode 9) - layout mobile XP e APKs por ABI
- Reorganiza a interface para celular conforme o mockup do usuário: cabeçalho azul compacto, barra de endereço destacada, GeckoView central amplo e navegação principal fixa na parte inferior.
- Remove a antiga composição com menu clássico e barra superior extensa, priorizando uso em telas de telefone sem perder a identidade visual XP.
- Mantém as funções centrais em seis ações inferiores: Voltar, Avançar, Início, Baixar, Abas e Menu.
- O botão Abas agora abre um seletor em diálogo; Menu concentra favoritos, histórico, compartilhar, downloads, extensões e ações de aba.
- Build atualizado para gerar APKs separados por arquitetura: `armeabi-v7a`, `arm64-v8a`, `x86` e `x86_64`, reduzindo o tamanho de cada arquivo.
- Workflow continua publicando diretamente na Release do GitHub, sem Artifact ZIP; com Secrets publica Release assinada por ABI, sem Secrets publica Debug por ABI.
- Sincronizados `versionName` 1.0.8, `versionCode` 9, README, AGENTS e workflow. Build Android completo ainda depende de nova execução no GitHub Actions.

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
