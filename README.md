# Internet XP v1.0.32 (versionCode 33)

Navegador Android com motor Mozilla **GeckoView 158** e aparência Internet XP. MinSdk 26, targetSdk 35.

## 1.0.32: correção de compilação do workflow 31

- O workflow 31 falhou em `:app:compileDebugJavaWithJavac`, na linha 439 de `MainActivity.java`, porque `android.R.drawable.ic_menu_copy` não é um recurso público existente.
- Substituído por `R.drawable.ic_copy_address_xp`, ícone vetorial próprio em `res/drawable`, preservando a lógica de cópia e o comportamento contextual de foco da barra.
- Validador atualizado para impedir o retorno de `android.R.drawable.ic_menu_copy` e garantir que o recurso próprio seja empacotado.
- Mantidos autocompletar, favicons, módulos de tela, downloads e GeckoView. O APK e o funcionamento no Android ainda precisam passar pelo GitHub Actions.

## 1.0.31: navegação por telas e barra inteligente

- O **Menu**, **Privacidade**, **Sobre**, **Configurações**, **Histórico**, **Favoritos**, **Abas** e **Downloads** agora abrem como telas internas completas com cabeçalho e retorno, sem recarregar a página GeckoView ao navegar entre módulos.
- O menu de endereço mostra URLs de histórico e favoritos com favicon carregado do próprio site via HTTPS, mais sugestões de pesquisa recebidas do **DuckDuckGo**, com *debounce* de 350ms e limite de resposta.
- **Privacidade**: sugestões online vêm ativadas nas abas normais, podem ser desligadas em Configurações, nunca consultam o provedor em aba privada nem são enviadas quando o usuário digita endereço URL. Histórico e favoritos não são enviados diretamente para o provedor.
- **Copiar** e **Colar** aparecem somente durante o foco no endereço, ocultando quando a barra perde o foco. Colagem limita texto a 2048 caracteres para evitar travamento por conteúdos gigantes.
- Favicons são cacheados em memória e têm ícone padrão quando indisponíveis. Módulos de permissão, confirmação e pop-up de extensão continuam como diálogos.
- O build Android completo e os testes de UI seguem pendentes de GitHub Actions e validação em aparelho físico.


## 1.0.30: refinamento visual da central e dos diálogos

- **Adicionar complemento** mostra Importar arquivo XPI, Instalar por HTTPS e Catálogo Mozilla em três cartões lado a lado, com ícones e textos curtos.
- As quatro ações de cada extensão instalada ficam numa única fileira: Ativar/Desativar, Diagnóstico, Exportar e Excluir.
- `XpDialogs.Builder` centraliza a identidade visual dos diálogos comuns do Internet XP (cabeçalho, contornos, botões e tipografia), sem substituir callbacks ou permissões do GeckoView.
- Barra inferior ampliada para ícones de 26/29sp e legendas de 9,5/10,5sp, com altura adaptativa de 68/74dp.
- **Funcionamento real**: extensão instalada e ativa ainda precisa de teste funcional na página. Em Menu > Ações das extensões, abra o Cookie Editor e faça alteração controlada em cookie de teste; compare antes e depois.
- O APK e a execução em aparelho desta revisão ainda dependem do workflow e de testes de interface reais.

## 1.0.29: testes JUnit corrigidos após o workflow 28

- O workflow 28 comprovou que o Java do aplicativo compilou e encontrou **2 falhas em 56 testes** no `ReleaseNotesTest.java`, por expectativas da v1.0.26/versionCode 27 em um app v1.0.28/versionCode 29.
- `ReleaseNotesTest` usa a versão corrente para verificar instalação, atualização, mesma versão e códigos futuros, sem congelar números de releases anteriores.
- O validador continua conferindo versão exata entre Gradle, `ReleaseNotes`, documentação e GitHub Actions; removeu apenas verificações de títulos específicos de notas antigas.
- O módulo de extensões permaneceu sem alterações de implementação; sua execução continua pendente de testes reais de APK e aparelho.

## 1.0.28: correção do workflow 27

- Resolvidos os três erros de compilação Java em `MainActivity.java` indicados pelo workflow 27: `holder`, `column` e `wrapper` fora de escopo.
- Atualizado o validador com guardas por método, sem remover funções da central de extensões.
- Os recursos de extensões continuam **pendentes de validação real** após compilar o APK e executar testes no celular.

## Extensões: implementação original 1.0.27

- **Menu > Extensões** abre uma central independente para importar XPI do aparelho (SAF), instalar por URL HTTPS direta, abrir catálogo Mozilla e gerenciar complementos.
- **Downloads > Instalar XPI** aparece nos downloads concluídos com nome `.xpi`: conecta o arquivo já baixado ao instalador **real** `GeckoRuntime.getWebExtensionController().install(fileUri)`.
- Importação local: cópia privada temporária com limite de 40 MiB, verificação básica do ZIP e `manifest.json` (MV2 ou MV3), `strict_min_version` quando presente, aviso sobre compatibilidade, depois confirmação de permissões e de assinatura pelo GeckoView.
- A lista de complementos instalados é consultada do **runtime** do motor Mozilla, não de uma lista fictícia. Cada extensão permite ativar/desativar, examinar identificador/versão/permissões/flags de desativação, excluir e exportar.
- **Menu > Ações das extensões** permite executar ações de botão de extensões ativas, com suporte inicial a pop-ups GeckoView e associação de ações à aba atual. Nem todas as APIs de um navegador Firefox completo estão implementadas pelo host.
- Pacotes locais importados são preservados no armazenamento privado **somente após instalação bem-sucedida** e podem ser exportados como `.xpi` via SAF. Extensões instaladas diretamente da rede ou anteriores à atualização podem exportar informações de diagnóstico `.json`, mas o GeckoView não expõe seus arquivos XPI originais diretamente por API pública.
- A remoção usa `uninstall` e apaga a cópia XPI privada associada, quando existir. A ativação/desativação também opera no `WebExtensionController`.

## O que a compatibilidade realmente significa

- A análise do manifesto é **preliminar**. O GeckoView faz as verificações efetivas de assinatura e instalação. Uma extensão de desktop ou específica de APIs Chromium pode não ser compatível, mesmo tendo manifest válido.
- **Instalada e ativa** significa que a extensão está registrada e habilitada no motor. **Não comprova** que os scripts, menus ou ações dela modificam todas as páginas: isso depende de APIs, permissões, sites e restrições da própria extensão.
- É necessário um **XPI assinado pela Mozilla** para o método de instalação normal. Pacotes XPI arbitrários de terceiros podem ser recusados. URLs devem apontar para o próprio pacote XPI, não apenas para a página de catálogo.
- Não há exportação fiel de código ou configuração de extensões já instaladas sem que o pacote tenha sido preservado previamente.

## Builds

- `versionName=1.0.32`, `versionCode=33`; publicações por ABI `armeabi-v7a`, `arm64-v8a`, `x86_64`.
- Requer os quatro secrets Android: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
- O ZIP é o **código-fonte**, não um APK. O projeto contém workflow GitHub Actions para testes, lint e Release.
- As operações de download continuam no gerenciador interno e não no DownloadManager do Android.

Veja `CHANGELOG.md`, `AGENTS.md` e `docs/VALIDATION.md` para detalhes e pendências de testes em aparelhos reais.
