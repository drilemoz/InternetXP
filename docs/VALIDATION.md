## 1.0.35: validação de abas e favoritos

- Abrir **Menu > Abas**: conferir título, favicon, domínio resumido, indicador atual/privado e contador. Fechar primeira, última e ativa; selecionar outra aba após cada fechamento, sem abrir sessão errada.
- Fechar uma aba normal e usar **Reabrir fechada**: verificar recarregamento da URL, nunca prometer restauração de scroll/formulário. Fechar uma privada e confirmar que ela não substitui a URL recuperável anterior.
- Com oito abas abertas, confirmar limite; fechar a última aba e verificar que página inicial é criada quando necessário. Reiniciar aplicativo e confirmar que abas privadas não são restauradas.
- Abrir **Favoritos**: buscar case-insensitive, mover para cima e para baixo, tentar mover extremos, remover com confirmação e verificar atualização dos atalhos na página inicial.
- Adicionar favorito pelo formulário e testar entrada inválida, endereço repetido, falta de rede e aba privada. Abrir favorito e confirmar que retorna à página web sem empilhar telas internas.
- Confirmar GeckoView 158, importação XPI, downloads e histórico intactos. Rodar `python3 tools/validate_project.py`, testes Python, `:app:testDebugUnitTest :app:lintDebug :app:assembleRelease` no GitHub Actions.
- Pendente: build e testes em aparelho; verificações locais não provam compatibilidade de execução do APK.

## 1.0.34: plano de validação
- Rodar `python3 tools/validate_project.py` e `python3 -m unittest discover -s tools -p test_*.py -v`.
- Compilar `:app:testDebugUnitTest :app:lintDebug :app:assembleRelease` em CI e verificar APKs para arm64-v8a, armeabi-v7a e x86_64.
- Testar barra inferior em 320/360/430 dp, ícones com dimensões visuais similares e contador de abas separado.
- Abrir site com carregamento: percentual e barra real devem terminar exatamente junto ao botão Extensões, inclusive com fontes grandes.
- Tocar Capturar > Salvar cópia: selecionar pasta via ACTION_CREATE_DOCUMENT, conferir PNG, cancelar seletor sem perder imagem privada. Compartilhar e confirmar URI content://.
- Durante navegação entre abas, voltar teclado e sugestões não podem ressurgir quando o campo já perdeu foco. Verificar sites reais, erro anterior de globo.com e permissões.
- Verificar status do cadeado do GeckoView e funcionamento de extensões após alterações. Sem logcat do problema de fechamento não confirmar causa raiz.
- Build e testes visuais reais ainda dependem de GitHub Actions/Android.

## 1.0.33: verificação planejada
- Verificar compilação, lint e testes reais via GitHub Actions.
- Teste UI: autocomplete globo.com sem fechamento; navegar retorno entre telas; permissões com teclado; carregamento 0–100 por aba; alternar normal/privada; botão Estrela; arquivo PNG por FileProvider; logs de crash; ativação opcional ChatGPT/Facebook e diferenças de proteção.
- Não afirmar que falha por seleção foi resolvida em aparelhos sem logcat/reprodução.

## 1.0.32: correção da compilação no workflow 31
- O log da execução 31 falhou por `android.R.drawable.ic_menu_copy` inexistente no SDK Android, em `MainActivity.java:439`.
- Verificar que a ação Copiar usa `R.drawable.ic_copy_address_xp`, cujo vetor existe em `app/src/main/res/drawable/`.
- Executar `python3 tools/validate_project.py` e `python3 -m unittest discover -s tools -p 'test_*.py' -v`.
- Rodar `:app:compileDebugJavaWithJavac :app:testDebugUnitTest :app:lintDebug :app:assembleRelease` no GitHub Actions; registrar erros reais caso ocorram.
- Em aparelho: focar barra, conferir ícones Copiar/Colar e ações; desfocar e verificar ocultação; testar abas, menus e extensões.
- Não considerar o erro corrigido como prova de build Android completo ou funcionamento do APK até o workflow terminar.

## 1.0.31: fluxo de navegação e autocompletar
- Tocar na barra: Copiar e Colar aparecem; sair dela: ambos desaparecem. Testar seleção integral, digitação, Enter e aba privada.
- Digitar termo comum: histórico/favoritos aparecem com ícones; após 350ms podem aparecer sugestões DuckDuckGo, se habilitadas. Desabilitar em Configurações e testar sem rede.
- Em aba privada, confirmar ausência de histórico local e ausência de envio de texto ao provedor. URLs com esquema ou endereços de domínio não devem iniciar sugestão remota.
- Abrir Menu > Privacidade > Permissões, usar Voltar para retornar à Privacidade, Voltar para Menu e novamente Voltar para página carregada, sem reload.
- Abrir Sobre, Configurações, Histórico, Favoritos, Downloads, Abas. Validar legibilidade e rolagem em 320/360/430dp; testar Android Voltar e Voltar visual.
- Em Downloads, iniciar transferência, abrir painel novo, voltar e reabrir sem perder download; confirmar que atualização periódica só roda quando painel está visível e há downloads ativos.
- Em Histórico, pesquisar, navegar, excluir entradas e limpar com confirmação; favicons são opcionais e não substituem cadeado HTTPS.
- Testar Menu > Extensões, instalação de XPI e ações GeckoView, assegurando que pop-ups reais de extensões continuam funcionando.
- Executar CI: `:app:testDebugUnitTest :app:lintDebug :app:assembleRelease` nas três ABIs. Ainda não executados no ambiente atual.

## 1.0.30: layout horizontal, janelas XP e navegação ampliada
- Conferir que os três comandos de Adicionar complemento aparecem em **uma única linha** na largura de 320 a 430dp.
- Instalar Cookie Editor, verificar quatro comandos horizontais no cartão, sem truncamento grave em 320dp.
- Abrir os diálogos de URL, confirmação XPI, permissão GeckoView, diagnóstico, exportação e exclusão; verificar cabeçalho azul, botões e fechamento/cancelamento.
- Verificar menus e confirmações de Downloads, Favoritos e privacidade para garantir que o construtor visual global não remova callbacks ou obscureça telas especiais.
- Testar seis botões inferiores com ícones e legendas maiores em Redmi A3 e Redmi Note 11 Pro+.
- Com Cookie Editor ativo, acessar site HTTPS de teste e executar Menu > Ações das extensões; ler e modificar cookie não sensível, atualizar e confirmar, depois removê-lo.
- Executar `python3 tools/validate_project.py`, `python3 -m unittest discover -s tools`, `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleRelease` no CI; conferir assinatura e APKs por ABI.
- **Pendente**: execução real dos testes Android e confirmação visual no aparelho.

## 1.0.29: correção dos testes JUnit da execução 28
- Log real do workflow 28: `:app:compileDebugJavaWithJavac` concluído; `:app:testDebugUnitTest` falhou com 56 testes, 2 falhas em `ReleaseNotesTest`.
- `releaseEntriesAreUsefulAndUnique`: ainda esperava `VERSION_NAME = 1.0.26` e `VERSION_CODE = 27` da v1.0.26. Teste reescrito para validar nome, código, descrições, identificadores e títulos sem versão congelada.
- `displaysAfterFreshInstallAndUpdate`: ainda exigia `shouldShow(27) == false`, mesmo após o código já estar em 29. Agora usa códigos relativos a `ReleaseNotes.VERSION_CODE`.
- O validador Python compara `VERSION_NAME`/`VERSION_CODE` com Gradle e com o workflow, mantendo validação rigorosa de correspondência sem obrigar títulos de outras versões.
- Rodar: `python3 tools/validate_project.py` e `python3 -m unittest discover -s tools -p 'test_*.py' -v`.
- Rodar no GitHub Actions: `:app:testDebugUnitTest :app:lintDebug` antes de `:app:assembleRelease` por ABI e instalar o APK no Android.
- **Pendente**: compilação Android completa desta versão, instalação XPI e eficácia dos scripts de extensões em páginas reais.

## 1.0.28: correção da compilação do workflow 27
- O workflow 27 falhou em `:app:compileDebugJavaWithJavac` com três símbolos inexistentes em `MainActivity.java` (linhas originais 654, 870 e 1572).
- `holder` agora é declarado antes de seu uso nos cartões principais de Menu; `showModuleMenuDialog` anima `wrapper`; `showHistory` anima `column`.
- Executar `python3 tools/validate_project.py`: valida regressões por escopo/método e consistência de versões e workflow.
- Executar `python3 -m unittest discover -s tools -p 'test_*.py'` e testes JVM do projeto.
- Confirmar no GitHub Actions `:app:compileDebugJavaWithJavac`, `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug` e Release por ABI. Rodar testes de instalação XPI em celular.
- A correção dos símbolos é baseada diretamente nos logs; **não equivale a build Gradle local nem a instalação de extensões validada**.

## 1.0.27: instalação e gerenciamento de WebExtensions
- Em **Downloads** baixar um XPI assinado Mozilla, aguardar conclusão e tocar **Instalar XPI**. Conferir prompt de permissões, sucesso/erro efetivo da instalação e persistência na lista depois de reiniciar o navegador.
- Em **Menu > Extensões** importar o mesmo XPI pelo SAF. Validar arquivo corrompido, sem `manifest.json`, sem assinatura, manifesto V2/V3, tamanho acima de 40 MiB e URL que não é um XPI.
- **Extensões instaladas**: alternar estado e observar `enabled` retornado por `list`; excluir e conferir remoção; testar sob bloqueio de assinatura ou compatibilidade.
- **Exportar** XPI só após importação local; conferir bytes do arquivo e reimportação. Instalada por URL: exportar apenas JSON de diagnóstico, sem inventar cópia do XPI.
- **Aplicação real**: instalar extensão simples com content script conhecido em página de teste compatível, conferir efeito na página; verificar que apenas `enabled=true` não demonstra execução de scripts em cada site.
- **Ações**: testar um complemento com browser_action/page_action e pop-up (ex.: Cookie Editor), clicar em Menu > Ações das extensões, conferir que o pop-up abre na aba atual e que pode ser fechado sem travar o navegador. Testar também abas novas, inativas e privadas.
- Testar Mozilla AMO aberto no Internet XP, incluindo links com redirecionamento e downloads com nome `.xpi`.
- Conferir permissão de leitura SAF e `content://` de download interno, exportação, rotação e saída da Activity durante I/O.
- Executar `python3 tools/validate_project.py`, testes JVM, `:app:lintDebug :app:assembleDebug` e `:app:assembleRelease` no GitHub Actions. O build Android e a instalação em aparelho real permanecem **não verificados localmente**.

## 1.0.26: pop-ups, animação e compactação
- Verificar se Favoritos, Extensões, Sobre, Erros e diálogos de ações seguem o novo visual azul com cartões.
- Confirmar animação de abertura no menu principal e nos diálogos modulares.
- Validar redução de margens laterais e melhor compactação em telas como Redmi A3.
- Conferir se a tela de Novidades 1.0.26 aparece apenas uma vez.

## 1.0.25: serviço de downloads em primeiro plano
- Verificar `:app:testDebugUnitTest :app:lintDebug :app:assembleRelease` no GitHub Actions.
- Testar download após ir para a tela inicial do Android, aguardando conclusão sem fechar o processo.
- Testar dois downloads simultâneos, cancelamento imediato ainda na fila e cancelamento durante gravação.
- Testar notificações no Android 8, Android 13+ com notificações desativadas e Android 15 com limites de `dataSync`.
- **Não garante continuidade após reinicialização ou parada forçada**; downloads interrompidos permanecem marcados como falhos.
- Conferir Novidades 1.0.25 exibida uma vez e APKs assinados por ABI.

## 1.0.23: download nativo e identificação como navegador
- Validar `tools/validate_project.py`, testes JVM e integridade ZIP.
- Testar download HTTP e HTTPS no Redmi A3 e Redmi Note 11 Pro+, verificando que outro navegador não é acionado.
- Testar PDF, APK, ZIP e XPI, incluindo respostas com login (cookies) e arquivo sem corpo da resposta.
- Testar progresso, cancelamento, falta de espaço, reinício de arquivo e seleção **Salvar cópia**.
- Testar reconhecimento como navegador padrão com seletor Android, RoleManager e link externo de outro app.
- Confirmar que downloads antigos continuam no local original e o aplicativo não os identifica falsamente como arquivos próprios.
- Testar encerramento de processo durante download: deve marcar como interrompido no próximo início.
- Verificar Tela de Novidades 1.0.23, assinatura e APKs por ABI no GitHub Actions.
- A compilação Android Gradle completa e testes em aparelho **não foram executados localmente**; dependem de CI e teste real.

## 1.0.22: página inicial, atalhos e diálogos XP
- Conferir a primeira instalação com página inicial em branco, busca central e tile Adicionar.
- Testar a busca na página inicial e a navegação por favoritos, mantendo URLs pré-existentes.
- Configurar uma URL de página inicial e confirmar persistência após reiniciar e ao criar abas.
- Verificar favicon em sites que disponibilizem `/favicon.ico` em PNG/ICO com PNG interno, inclusive fallback sem imagem.
- Confirmar que o indicador de segurança continua independente do favicon.
- Comparar largura/altura de Menu, Histórico, Favoritos, Extensões e Sobre no Redmi A3 e Redmi Note 11 Pro+.
- Confirmar teclado e seleção do endereço após tocar a barra.
- Validar Novidades 1.0.22 uma vez por versionCode e manualmente via Menu.
- O build Android/Lint e testes físicos dependem de execução real no GitHub Actions e aparelho.

## 1.0.21: menu em módulos e ajustes para telas pequenas
- Verificar no Redmi A3 se a barra de endereço aceita toque, seleciona tudo e abre o teclado.
- Confirmar que o botão Menu abre um painel com módulos, ícones e descrições, sem PopupMenu simples.
- Conferir que a barra inferior e o painel de downloads ficam mais compactos em telas pequenas.
- Validar a tela de Novidades 1.0.21 e a exibição única por versionCode.

