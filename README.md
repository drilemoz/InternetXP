# Internet XP v1.0.40 (versionCode 41)

Navegador Android com motor Mozilla **GeckoView 158** e aparência Internet XP. MinSdk 26, targetSdk 35.

## 1.0.40: mídias, menus contextuais e favicons (código-fonte)
- **Botão flutuante na página**: pressione uma imagem, vídeo ou áudio para registrá-lo na lista temporária da aba (até 12 mídias). O botão pode ser movido. Nada é armazenado no histórico quando a aba é privada.
- **Detalhes em pop-up XP**: formato MIME identificado por URL ou resposta HEAD, tamanho somente quando conhecido, resolução/previsão de imagem leve quando disponível (até 512 KiB), origem, botões **Salvar** e **Fechar**.
- **Salvar** usa o download próprio existente com `GeckoWebExecutor`, marcado como privado quando necessário. Recursos com `blob:`, `data:`, DRM e playlists HLS/DASH não são convertidos em mídia direta; acesso autenticado e downloads complexos dependem de testes no Android.
- **Toque longo em links**: abrir na aba, nova aba, copiar ou compartilhar. Em imagens/mídias: copiar endereço e detalhes. **Seleção de texto** usa `BasicSelectionActionDelegate` do GeckoView.
- **Favicons dos favoritos**: procura, via HTTPS do próprio domínio, `/favicon.ico`, `/favicon.png`, `/apple-touch-icon.png` e `link rel=icon` no HTML. Cache de disco limitado a 64 entradas, com expiração de 30 dias, e cache RAM existente. Não consulta serviços de terceiros nem servidores locais.
- **Limitações**: esta versão não extrai automaticamente todo o HTML/DOM nem grava streaming protegido; os controles são acionados pela seleção de mídia. Precisa passar pelo build Gradle/GitHub Actions e testes físicos antes de declarar APK funcional.

## 1.0.39: permissões e segurança por site (código-fonte)
- **Segurança da página > Gerenciar permissões** e **Configurações > Permissões por site** abrem uma tela interna com decisões reais retornadas pelo GeckoView para a origem atual. Para permissões existentes, pode-se escolher Perguntar, Bloquear e Permitir; a API não cria permissões que nunca existiram.
- **Câmera e microfone**: pode-se escolher Perguntar ou Bloquear por origem (incluindo porta), sem aprovação automática. As preferências locais usam chaves SHA-256; abas privadas não herdam esses bloqueios persistentes.
- **Localização e notificações** passam por prompt customizado do Internet XP com consentimento explícito. O sistema Android também solicita permissões de câmera, microfone, localização e notificações quando aplicável. Liberar permissão do conteúdo não substitui a autorização do Android.
- O atalho **Limpar permissões** também elimina os bloqueios locais de câmera e microfone após a limpeza do GeckoView ser concluída com sucesso. Não se afirma suporte a execução real de notificações em segundo plano.
- Servidores loopback `localhost`/`127.0.0.1`/`[::1]` são tratados como origens potencialmente confiáveis para o prompt de mídia; IPs LAN remotos continuam sem essa exceção. Isso não torna HTTP criptografado.
- A compilação Gradle, lint, execução real no Android e o comportamento do GeckoView 158 continuam pendentes de validação externa.

## 1.0.38: servidores locais e desempenho (código-fonte)
- A barra reconhece `localhost:8000`, `127.0.0.1:8000`, `192.168.1.20:8080`, `[::1]:3000`, `raspberrypi:3000`, `.local` e `.lan`. Ao digitar sem esquema, servidores locais usam `http://` e sites públicos continuam em `https://`.
- A página inicial aceita hosts locais válidos; termos de busca não viram URL. Endereços locais não são enviados ao autocomplete remoto.
- Em **Configurações > Desempenho e sites**, veja o tempo da última navegação medido na aba e encontre dicas para Termux; as opções ChatGPT/Facebook são exceções opcionais de proteção antirrastreamento, não um ganho de desempenho comprovado.
- O manifesto permite tráfego HTTP necessário para servidores na LAN. **HTTP não oferece criptografia**: use somente redes/servidores confiáveis; evite credenciais. O Internet XP não altera o servidor nem cria um automaticamente.
- Em Termux no mesmo aparelho, um servidor ouvindo em `127.0.0.1:8000` deve estar iniciado; em outro aparelho use o IP daquele aparelho, com serviço ouvindo em `0.0.0.0` ou na interface LAN, e verifique firewall e isolamento Wi-Fi.
- Ainda é necessário executar o build Gradle/GitHub Actions e testar o APK num Android físico. Veja [guia de servidores locais](docs/LOCAL_SERVERS.md) para testes práticos.

## 1.0.37: diagnóstico e recuperação de sessões (código-fonte)
- Reabertura **manual** de aba interrompida pelo GeckoView. Não restaura formulários, scroll, login ou estado JavaScript.
- Diagnóstico de encerramentos do processo principal (Android 11+) com categorias oficiais, memória aproximada e registros locais mínimos.
- Exportação de relatório ZIP via Storage Access Framework, sem permissões amplas de armazenamento nem envio automático.
- Backup de snapshot válido das abas normais como fallback para JSON principal inválido; sessões privadas nunca são persistidas.
- APK e testes físicos ainda dependem de compilação/validação no GitHub Actions.

### Histórico da versão 1.0.36

- O workflow **34**, executado com código 1.0.35, falhou em `:app:compileDebugJavaWithJavac` com quatro referências inválidas a `SecurityInformation.issuerCommonName` e `issuerOrganization` em `MainActivity.java`.
- GeckoView 158 disponibiliza `SecurityInformation.certificate` (`X509Certificate`). A tela **Segurança da página** agora extrai emissor e titular com `getIssuerX500Principal().getName()` e `getSubjectX500Principal().getName()`; caso não exista certificado, mostra **Não disponível**.
- A alteração mantém os indicadores de segurança existentes, sem deduzir validade ou proteção apenas a partir do prefixo HTTPS; abas, favoritos, extensões e downloads permanecem sem alterações funcionais.
- Acrescentadas verificações de regressão da API no validador e nos testes Python. Compilação Gradle e teste no Android ainda dependem de execução real.

## 1.0.35: gerenciamento de abas e favoritos (código-fonte)

- Tela de **Abas abertas** com contador atualizado após fechamentos, cartões mais compactos, seleção/fechamento por referência estável da sessão e distinção visual normal/privada.
- Botão **Reabrir fechada** recupera somente a URL da última aba normal fechada em memória (sem restaurar formulários, JavaScript, rolagem ou abas privadas); respeita limite de oito abas.
- Tela **Favoritos** com busca local, inclusão por campo embutido, reordenação explícita para cima/baixo e remoção com confirmação. Lista e página inicial permanecem sincronizadas e sem URLs duplicadas na inclusão.
- Favicons opcionais com cache HTTPS já existente; a navegação entre módulos mantém as sessões GeckoView.
- Necessário compilar no GitHub Actions e testar UI, principalmente fechamento da aba ativa, busca/reordenação e Voltar em telas compactas.

## 1.0.34: ergonomia e exportação de capturas (código-fonte)

- Barra inferior com ícones vetoriais proporcionais para Voltar, Avançar, Abas, Baixar, Início e Menu, mantendo contagem separada das abas.
- Barra real de progresso alinhada ao início de Extensões, sem largura fixa em dp ou percentual simulado.
- Captura de tela da área visível pode agora ser compartilhada ou exportada para pasta escolhida via Storage Access Framework; o arquivo original permanece privado no aplicativo.
- Barra de endereço: bloqueio de reabertura tardia da lista após perder o foco, navegação de sugestões com retorno visível em caso de exceção e Colar somente quando há conteúdo no clipboard.
- Informações de segurança passam a fechar o teclado antes da abertura. GeckoView, extensões, histórico e privacidade preservados.
- Pendente: compilação Gradle no GitHub Actions e testes funcionais de SAF, progressos, compatibilidade e permissões no Android real.

## 1.0.33: navegação, falhas e desempenho (código-fonte)

- Suporte a lista de sugestões com item vinculado à View selecionada; possível causa de crash mitigada. **Sem logcat não é possível provar a origem do fechamento reportado**.
- Favoritos na barra, ícone Colar próprio, captura PNG da área visível, barra de carregamento do GeckoView com porcentagem real por aba.
- Abas compactas, botões Nova/Privada, badge de quantidade separado e troca de posição Abas/Início no rodapé.
- Tela de segurança detalhada (host, certificado, emissor, conteúdo misto) e registro local mínimo de falhas técnicas, compartilhável por texto sem URL/cookies.
- Desempenho: pausa de mídia em abas inativas e modos de compatibilidade manuais para ChatGPT/Facebook. Estes **desligam antirrastreamento no domínio** e portanto reduzem privacidade; não são melhoria de velocidade comprovada.
- O atalho Adicionar da página inicial agora abre campo embutido sem grande pop-up.
- Capturas salvam no armazenamento próprio do app, em Pictures/Capturas e podem ser compartilhadas via FileProvider. Não capturam a barra do navegador.
- Pendências: compilar APK no CI, testar Android real, verificar fechamento por logcat, teclado/permissões e comportamento de sites pesados.

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

- `versionName=1.0.40`, `versionCode=41`; publicações por ABI `armeabi-v7a`, `arm64-v8a`, `x86_64`.
- Requer os quatro secrets Android: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
- O ZIP é o **código-fonte**, não um APK. O projeto contém workflow GitHub Actions para testes, lint e Release.
- As operações de download continuam no gerenciador interno e não no DownloadManager do Android.

Veja `CHANGELOG.md`, `AGENTS.md` e `docs/VALIDATION.md` para detalhes e pendências de testes em aparelhos reais.
