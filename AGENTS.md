# Regras Internet XP
- Versionamento sequencial obrigatório em toda alteração, com sincronização de README, CHANGELOG, workflow e demais metadados.
- GeckoView é obrigatório; não substituir por WebView.
- Interface prioritária: layout mobile XP para telefone, compacto e inspirado no Internet Explorer clássico.
- A barra de endereço deve permanecer como elemento principal do topo; ajustes finos de altura, largura, histórico e usabilidade são prioridade atual.
- Publicar APKs direto em GitHub Releases, sem upload-artifact e sem artifact ZIP.
- Manter três APKs por ABI: `armeabi-v7a`, `arm64-v8a` e `x86_64`.
- Secrets exigidos: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
- Não anunciar APK pronto sem build bem-sucedido real.

- Na v1.0.13, priorizar estabilidade antes de acrescentar recursos: seleção total de URL no toque, melhorias conservadoras nos downloads e testes de regressão.
- Para recursos inspirados no projeto Baixa Fácil, não declarar código copiado sem ter acesso e inspecionar o fonte; o DownloadManager não suporta pausa/retomada manual verdadeira.
- Progresso de download deve ser consultado apenas com painel visível e transferências ativas, sem polling contínuo em background.

- Na v1.0.14, Downloads 2.0 usa apenas `DownloadManager`: não prometer pausa/retomada manual; não compartilhar `file://`; não inventar ETA sem leituras reais e tamanho conhecido.
- Preferência de rede não tarifada deve respeitar política real do Android e bloquear roaming quando configurado.
- `versionCode 16` / `versionName 1.0.15`.

- Na v1.0.15, salvar URLs/títulos e aba ativa, mas restaurar somente a página ativa imediatamente. Nunca prometer restauração completa de formulário/scroll ou do estado JavaScript.
- Endereços de abas restaurados devem restringir-se a `http`, `https` e `about:blank`; históricos antigos não devem ganhar horários fictícios.
- Seleção integral do endereço ao toque e Downloads 2.0 devem continuar funcionais.

## 1.0.16
- Manter delegados de permissões e prompts coerentes com GeckoView 158; não aceitar permissões silenciosas de páginas.
- Testar seletor de arquivos, tela cheia, câmera, microfone, localização e links externos antes de anunciar compatibilidade total.

## 1.0.17
- Branding atualizado: launcher icon azul com globo e anel dourado, assets por densidade e adaptive icon.
- Próxima etapa planejada: privacidade e gerenciamento de cookies/cache/segurança.


## REGRA PERMANENTE: TELA DE NOVIDADES (OBRIGATÓRIO EM TODA VERSÃO)
- Sempre que qualquer arquivo do aplicativo mudar, incrementar `versionCode` e `versionName` sequencialmente.
- **Em TODA nova versão, atualizar `ReleaseNotes.VERSION_CODE`, `ReleaseNotes.VERSION_NAME`, `ReleaseNotes.HEADLINE`, `ReleaseNotes.SUBTITLE` e as entradas de `ReleaseNotes.FEATURES` para refletir alterações reais da versão**, não reutilizar textos antigos.
- A Activity `WhatsNewActivity` abre automaticamente na primeira instalação e uma vez após cada atualização, controlada por `SharedPreferences` com a última versão exibida. Não exibir repetidamente na mesma versão; oferecer Menu > Novidades desta versão para consulta manual.
- Nunca remover a tela, o registro no `AndroidManifest.xml`, o atalho do menu nem a verificação de sincronização de `tools/validate_project.py` sem pedido explícito do usuário.
- Manter visual mobile XP refinado (cabeçalho azul, globo, cartões, botão de continuação); preservar acessibilidade, baixo consumo de RAM e armazenamento local das preferências.
- A atualização das Novidades é parte do checklist de release: atualizar junto a README, CHANGELOG, docs/VALIDATION, notas GitHub Release e metadados Gradle.

## 1.0.18
- Execução 18 falhou em `lintDebug` por uso de `ContentPermission.notifyShown()` experimental; solução é opt-in localizado, sem desativar Lint.
- Tela de Novidades e teste de conteúdo/versionamento adicionados; atualização automática por versionCode.
- O recurso de privacidade planejado fica para a próxima versão após validar o build 1.0.18.

## 1.0.19: correção de build da execução 19
- `@OptIn` é fornecido por `androidx.annotation:annotation-experimental`, dependência EXPLÍCITA obrigatória no `app/build.gradle` enquanto o GeckoView usar `ContentPermission.notifyShown()` experimental.
- Manter também `androidx.annotation:annotation` em versão compatível para evitar aviso de `RequiresOptIn$Level` ausente.
- `tools/validate_project.py` DEVE verificar ambas dependências, o uso localizado de @OptIn, e as notas reais da versão.
- Qualquer mudança futura DEVE atualizar `ReleaseNotes`, README, CHANGELOG, docs/VALIDATION e notas da Release.
- Próxima etapa planejada de privacidade permanece adiada até validar compilação/instalação real.

## 1.0.21: menu em módulos e responsividade
- Nunca armazenar URLs, títulos ou histórico de abas privadas em SharedPreferences, nem restaurá-las ao reiniciar o app. Novas janelas criadas de abas privadas devem manter privacidade.
- Informação de cadeado deve vir de GeckoView ProgressDelegate.onSecurityChange, não apenas de prefixo HTTPS.
- Para limpar dados persistentes via StorageController.clearData, encerrar todas as sessões antes de executar o comando assíncrono; não fingir sucesso antes do callback.
- Fechar e limpar cache/cookies pode desconectar sites; downloads e favoritos não são apagados automaticamente. Avisar que downloads privados continuam fora do isolamento do navegador.
- Em toda mudança atualizar ReleaseNotes.FEATURES e metadados, inclusive a versão 1.0.21 em todos os pontos.

## 1.0.22: página inicial e consistência visual
- Manter a página inicial nativa por padrão: fundo branco, pesquisa centralizada e grade de favoritos com Adicionar.
- A página inicial personalizada deve ser persistida nas preferências e validada para URLs HTTP(S); não carregar termos de busca como sites iniciais.
- Favicons opcionais devem ser buscados apenas por HTTPS diretamente do domínio, sem serviços de terceiros, com cache limitado e fallback.
- Manter o indicador de segurança do GeckoView separado dos favicons; nenhum favicon comprova HTTPS confiável.
- Favoritos, Extensões, Histórico, Sobre e Configurações devem seguir padrão visual XP; menu principal compacto e legível em Redmi A3.
- Em todas as futuras alterações, atualizar ReleaseNotes, Novidades, Gradle, README, CHANGELOG, docs/VALIDATION, workflow e versionCode sequencialmente.


## 1.0.23: gerenciador próprio e reconhecimento como navegador
- **Não voltar a utilizar `android.app.DownloadManager` para baixar**. O código do Internet XP deve gerenciar o fluxo de download.
- Prefira `WebResponse.body` vindo do GeckoView para preservar a resposta autenticada, fechando sempre o stream ao terminar ou cancelar.
- Um fallback com `GeckoWebExecutor` pode reiniciar a requisição, inclusive na área privada quando informado; nunca alegar que a retomada parcial está implementada.
- Compartilhar arquivos apenas por `content://` via `FileProvider`, sem `file://` e sem permissões globais do armazenamento.
- O armazenamento principal de downloads é privativo do app. Para salvar em pasta escolhida pelo usuário, use SAF; não prometer que todo download ficará na pasta pública.
- Até implementar serviço foreground com persistência real, não prometer sobrevivência a encerramento de processo Android.
- No `AndroidManifest.xml`, manter `ACTION_VIEW`, `CATEGORY_BROWSABLE`, `CATEGORY_DEFAULT` e esquemas HTTP/HTTPS para ser candidato a navegador padrão. Tratar `onNewIntent` e novos links sem abrir navegador externo.
- A instalação de XPI continua com GeckoView WebExtensionController (não basta baixar o arquivo).
- Toda versão futura DEVE sincronizar ReleaseNotes, AGENTS, README, CHANGELOG, docs/VALIDATION, workflow e versionCode.

## 1.0.25: foreground download service
- Para download próprio iniciar serviço dataSync apenas após gesto explícito e concluir/encerrar ao terminar transferências.
- Manter permissões FOREGROUND_SERVICE e FOREGROUND_SERVICE_DATA_SYNC e `android:foregroundServiceType="dataSync"` no manifesto.
- O Android 15 (target 35) impõe cotas a dataSync; tratar `onTimeout` e não prometer downloads ilimitados em segundo plano.
- Cancelamento antes de o worker iniciar deve fechar stream e remover entrada do mapa/serviço.
- Não reintroduzir DownloadManager do Android, nem prometer retomada por bytes sem validação HTTP Range.
- Toda versão exige atualização da tela Novidades, ReleaseNotes, README, CHANGELOG, docs, workflow e versionCode.


## 1.0.26: pop-ups alinhados ao menu
- Os pop-ups e diálogos secundários devem seguir o mesmo idioma visual do menu principal, evitando caixas secas do sistema sempre que viável.
- Refinar cabeçalhos e animações sem transformar os painéis em tela cheia.
- Em telas compactas como Redmi A3, reduzir margens, tipografia e altura de elementos quando necessário.
- Toda alteração futura deve continuar atualizando a tela de Novidades e os metadados sincronizados.

## 1.0.27: central de WebExtensions
- Extensões são efetivamente instaladas/listadas/ativadas/desativadas/removidas pelo `WebExtensionController` do mesmo `GeckoRuntime` das abas; não inventar ativação a partir de um arquivo baixado.
- Inspeção local de `.xpi` é preliminar; o GeckoView precisa validar assinatura Mozilla e permissões. Não afirmar suporte a toda extensão do Firefox Desktop.
- Não exportar como XPI um JSON ou arquivo sintetizado: preservar pacote importado após sucesso para exportar, ou exportar somente metadados JSON.
- Na nova versão manter serviço de downloads existente, listas privadas e sincronização obrigatória dos arquivos/versionCode da Release.

- Extensões com browser_action/page_action precisam ter ação registrada por runtime/aba e apresentar pop-up em GeckoView próprio. Não afirmar que todas as APIs Firefox de UI já são cobertas pelo host.

## 1.0.28: regressão de compilação
- Corrigidos três símbolos inexistentes no `MainActivity.java` mostrados pelo workflow 27: referência a `holder` antes da declaração; animação de `column` inexistente no diálogo modular; animação de `wrapper` inexistente no Histórico.
- Os diálogos devem animar apenas views declaradas no próprio método; `holder` precisa existir antes do primeiro uso, inclusive ao criar cards com lambdas.
- `tools/validate_project.py` verifica cada um dos três contextos para barrar regressão de escopo.
- Em toda próxima alteração incrementar a versão e sincronizar Novidades, Gradle, workflow, README, CHANGELOG e docs.

## 1.0.29: testes de Novidades sem versões congeladas
- `ReleaseNotesTest` deve avaliar `shouldShow` com `ReleaseNotes.VERSION_CODE` em vez de versões numéricas antigas; cobrir primeira instalação, atualização, mesma versão e versão futura.
- Validação de sincronização Gradle, `ReleaseNotes`, workflow e documentação permanece obrigatória no script `tools/validate_project.py`.
- Não exigir textos de destaques de releases anteriores no validador; validar a existência dos destaques e seus conteúdos pelos testes JUnit para evitar falsos bloqueios.
- Após mudar qualquer código, incrementar versionCode/versionName e sincronizar arquivos de release. Testar Java/JUnit em CI antes de anunciar APK pronto.

## 1.0.30: design consistente de extensões e diálogos
- As três ações de adicionar extensões e os quatro comandos por extensão instalada permanecem em faixas horizontais, com rótulos curtos e descrições de acessibilidade.
- Novos diálogos comuns em `MainActivity` e `ExtensionManagerActivity` devem usar `XpDialogs.Builder` para herdar identidade visual, tipografia e botões; menus especiais com `setView` conservam os próprios contêineres e ações.
- Não remover os prompts de permissão nem os callbacks GeckoView ao reformular a aparência.
- Barra de navegação inferior mantém seis itens e ícones/legendas ampliados.
- A condição `metaData.enabled` não é diagnóstico de execução: validar funcionamento de cada complemento com efeitos observáveis em página real.
- Novas mudanças exigem incremento de versionCode, Novidades, docs, validações e Release, conforme regra permanente.

## 1.0.31: telas internas e sugestões com controle de privacidade
- Preferir telas completas via pilha própria para Menu, Sobre, Privacidade, Histórico, Favoritos, Downloads, Configurações e Abas; deixar pop-ups para confirmação, entrada curta, permissões e pop-ups reais de extensões.
- Manter a mesma GeckoSession ao transitar entre módulos, com Back visual e do Android retornando à tela anterior.
- Favicons só por HTTPS do domínio como antes; falha é ícone genérico. Não buscar ícones por serviços agregadores externos.
- Autocomplete remoto opcional e desligável, bloquear em modo privado e durante URLs digitadas, executar fora da thread principal e ignorar resultados antigos.
- Ações Copiar/Colar da barra devem aparecer somente enquanto a barra estiver em foco.
- Metadados desta próxima revisão seguem sequência 1.0.32/versionCode 33.

## 1.0.32: correção de recurso drawable inválido
- O Android não expõe `android.R.drawable.ic_menu_copy`: ícones adicionais precisam existir sob `R.drawable` no próprio app ou ser IDs públicos de API comprovada.
- Botão Copiar agora aponta para `R.drawable.ic_copy_address_xp`; manter recurso vetorial validado e guarda no validador.
- Em próximas revisões subir para 1.0.33/versionCode 34 e sincronizar Gradle, Novidades, workflow, README, CHANGELOG, docs/VALIDATION e testes.

## 1.0.33: estabilidade, progresso e diagnóstico local
- O valor de sugestões selecionadas deve vir do item vinculado à linha clicada, não de índice móvel do adapter. Falha reportada pelo usuário ainda precisa de logcat.
- onProgressChange atualiza a aba real (0–100), sem porcentagem fictícia; evitar uso de timers para inventar progresso.
- Capturas devem permanecer no diretório da aplicação e ser compartilhadas por FileProvider; não capturar dados sem gesto do usuário.
- Logs de crashes não podem conter URLs, cookies, tokens nem mensagens arbitrárias de exceção.
- Compatibilidade para ChatGPT e Facebook é opcional, transparente e reduz proteção contra rastreadores; não alegar otimização provada sem benchmark.
- Próxima versão deve ser 1.0.34/versionCode 35 com metadados e Novidades sincronizados.

## 1.0.34: refinamento da barra e armazenamento externo opt-in
- Todas as seis ações da barra inferior usam ícones vetoriais na mesma escala; não reintroduzir emojis enormes nem contador unido ao ícone.
- A barra de progresso deve medir a largura restante na linha antes de Extensões, sem margem fixa em dp, conservando medições GeckoView por aba.
- Capturas de tela ficam inicialmente em diretório próprio; exportação para pasta externa só após gesto explícito e SAF ACTION_CREATE_DOCUMENT. Não usar permissões globais de armazenamento.
- Resultados assíncronos do autocomplete não devem trazer o popup de volta quando o campo perde foco ou a Activity encerra. Não prometer que crash reportado foi eliminado sem teste físico/logcat.
- A próxima alteração deverá incrementar para 1.0.35/versionCode 36 e sincronizar ReleaseNotes, Gradle, README, CHANGELOG, workflow e docs.

## 1.0.35: gerenciamento de abas/favoritos
- Em cartões de abas, resolver `tabs.indexOf(tab)` ao tocar. Não armazenar índice que possa mudar após fechamento.
- Reabrir aba fechada não deve restaurar informação de abas privadas nem persistir a URL recuperável em disco; URL normal em memória é suficiente.
- Busca, ordenação e exclusão de favoritos devem preservar preferências, sincronizar página inicial/estrela e não acessar histórico privado.
- Próxima versão: 1.0.36/versionCode 37 com metadados/Novidades e workflow sincronizados; verificar GitHub Actions antes de afirmar APK pronto.

## 1.0.36: compatibilidade da API de certificados
- GeckoView 158 expõe `SecurityInformation.certificate` (`X509Certificate`), não `issuerCommonName` nem `issuerOrganization` (removidos em versões antigas da API).
- Nunca voltar a acessar campos ausentes; validar a superfície da API pela documentação da versão fixada no Gradle e pelo workflow Android real.
- Tratar certificado ausente sem inventar emissor, titular ou estado verificado; preservar separação de indicadores `isSecure`, `isException` e conteúdo misto.
- Na próxima alteração, avançar para 1.0.37/versionCode 38 e sincronizar todos os metadados, docs e Novidades.
