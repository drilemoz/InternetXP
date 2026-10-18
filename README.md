# Internet XP v1.0.21

Navegador Android com motor Mozilla GeckoView 158, visual inspirado no Windows XP/Internet Explorer e interface compacta para celulares.

## Novidades (versionCode 22)
- Menu principal substituído por um hub em módulos com ícones e descrições.
- Barra de endereço com foco mais confiável ao toque e seleção automática do endereço.
- Ajustes de densidade e altura para telas compactas, incluindo Redmi A3.
- Painel de privacidade migrado para o mesmo estilo em módulos.
- Barra inferior e painel de downloads mais compactos em telas pequenas.


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
- `versionName 1.0.21`, `versionCode 22`, `minSdk 26`, `compileSdk 37`.
- Três APKs Release assinados: `armeabi-v7a`, `arm64-v8a`, `x86_64` diretamente no GitHub Releases; sem Artifact ZIP.
- Quatro Repository Secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
- O ZIP entregue é código-fonte, não APK instalável.

Consulte `AGENTS.md`, `CHANGELOG.md` e `docs/VALIDATION.md` antes de modificar.
