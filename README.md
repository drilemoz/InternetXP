# Internet XP v1.0.26

Navegador Android com motor Mozilla GeckoView 158, visual inspirado no Windows XP/Internet Explorer e interface compacta para celulares.

## Novidades da v1.0.26
- Outros pop-ups agora seguem melhor o novo menu, com cabeçalhos azulados, cartões brancos e visual mais coerente.
- Cabeçalho do menu refinado e animação de abertura aplicada ao painel principal e aos diálogos modulares.
- Menus e pop-ups mais compactos em telas menores como o Redmi A3, com menos margens laterais e ajustes de tipografia.
- Ações de extensões agora usam diálogos no estilo do app, em vez de caixas secas do sistema.
- Tela de Novidades, README, CHANGELOG, validações e workflow sincronizados com a versão 1.0.26.

## Novidades anteriores da v1.0.25 (versionCode 26)
- Gerenciador de downloads próprio escrito em Java, sem Android DownloadManager.
- Fluxo recebido por `GeckoSession.ContentDelegate.onExternalResponse` salvo pelo Internet XP, sem abrir navegador do sistema; fallback de download pelo `GeckoWebExecutor` nos casos em que não há corpo de resposta.
- Progresso, velocidade observada, cancelamento, nova tentativa (reinicia do começo), visualização, compartilhamento e exportação SAF (Salvar cópia).
- Arquivos salvos na área externa privativa do Internet XP, com `FileProvider`; não são enviados automaticamente para a pasta pública Downloads. Se desejar manter uma cópia visível no gerenciador de arquivos, use **Salvar cópia**.
- Downloads anteriores gerenciados pelo Android não são migrados fisicamente nem apagados; a lista local é marcada para nova tentativa, e arquivos anteriores continuam onde o Android os salvou.
- Navegador reconhecido pelo Android: intenção `ACTION_VIEW`/`BROWSABLE`/`DEFAULT` para `http` e `https`, tratamento de links recebidos e menu **Navegador padrão** usando `RoleManager` quando disponível.
- Tela estilizada de Novidades 1.0.25 atualizada, com explicações das cinco mudanças.

### Limitações
- Transferências são processadas em um executor dentro do processo do aplicativo. **Não há serviço foreground persistente**; o Android pode interromper downloads se encerrar o processo. Ao reabrir, transferências interrompidas são marcadas como falhas e podem ser reiniciadas.
- Não foi implementada retomada HTTP Range de transferências parciais; nova tentativa é um download novo.
- A instalação de extensões XPI continua pelo `WebExtensionController` do GeckoView, separada do download de pacotes XPI.
- Não é possível impedir que o Android ofereça outros navegadores ou force links verificados de determinados aplicativos; escolha o Internet XP no seletor ou em Apps padrão.

## Compilação e distribuição
- `versionName 1.0.25`, `versionCode 27`, `minSdk 26`, `compileSdk 37`.
- Três APKs Release assinados: `armeabi-v7a`, `arm64-v8a`, `x86_64` diretamente no GitHub Releases; sem Artifact ZIP.
- Quatro Repository Secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
- O ZIP entregue é código-fonte, não APK instalável.

Consulte `AGENTS.md`, `CHANGELOG.md` e `docs/VALIDATION.md` antes de modificar.
