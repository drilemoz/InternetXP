# Internet XP v1.0.22

Navegador Android com motor Mozilla GeckoView 158, visual inspirado no Windows XP/Internet Explorer e interface compacta para celulares.

## Novidades (versionCode 23)
- Página inicial nativa leve em `about:blank`, com busca centralizada e grade de favoritos.
- Atalho **Adicionar** na página inicial e diálogo para salvar um endereço, sem perder a lista existente.
- **Configurações** no menu: escolha entre a página inicial nativa do Internet XP ou a URL de um site.
- Favicons buscados por HTTPS diretamente do domínio do favorito (sem terceiros), em segundo plano, com cache limitado e fallback por inicial.
- Barra de endereço mantém cadeado separado do favicon, quando disponível.
- Menus de Favoritos e Extensões em cartões com ícones; Sobre e Histórico estilizados; menu principal mais estreito e menos alto.
- Barra de endereço seleciona o URL inteiro ao toque; botões compactos mantidos para Redmi A3.
- Tela de Novidades atualizada para v1.0.22.


- Abas privadas reais pelo `GeckoSessionSettings.Builder.usePrivateMode(true)`, sem gravar URLs no histórico nem restaurá-las após reinício.
- Cor roxa discreta para identificar abas privadas, com opção de fechar todas as abas privadas.
- Estado de segurança do site mostrado a partir de `ProgressDelegate.onSecurityChange`, não só de `https://`.
- Limpeza efetiva de cookies e autenticação, cache, permissões ou dados completos com `StorageController.clearData` após fechar todas as abas.
- Controle para ligar/desligar proteção antirrastreamento por sessão, com preferência local persistente.
- Consulta e revogação/bloqueio de permissões armazenadas para o site atual, quando a API suporta, com aviso para armazenamento persistente.
- Aviso explícito sobre downloads iniciados em abas privadas: continuam salvos e visíveis ao Android.
- Tela de Novidades atualizada obrigatoriamente nesta versão.

### Limitações honestas
A navegação privada evita salvar histórico/abas no Internet XP, mas não oculta atividade da rede, operadora, sites, sistema Android ou de eventuais extensões; arquivos baixados permanecem no aparelho. Fechar o app elimina abas privadas, mas testar em dispositivo é indispensável para confirmar o isolamento de cookies/dados do GeckoView.

## Compilação e distribuição
- `versionName 1.0.22`, `versionCode 23`, `minSdk 26`, `compileSdk 37`.
- Três APKs Release assinados: `armeabi-v7a`, `arm64-v8a`, `x86_64` diretamente no GitHub Releases; sem Artifact ZIP.
- Quatro Repository Secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
- O ZIP entregue é código-fonte, não APK instalável.

Consulte `AGENTS.md`, `CHANGELOG.md` e `docs/VALIDATION.md` antes de modificar.
