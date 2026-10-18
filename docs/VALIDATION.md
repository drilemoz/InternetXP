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

