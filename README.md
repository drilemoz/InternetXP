# Internet XP v1.0.16

Navegador Android com GeckoView 158 e interface mobile inspirada no Windows XP/Internet Explorer.

## Compatibilidade com sites (v1.0.16)
- `<input type=file>` com seletor nativo de arquivos Android (um arquivo ou múltiplos).
- Permissões em duas etapas: Android e confirmação do site para câmera, microfone e localização. Apenas origens HTTPS podem receber essas permissões nesta versão.
- Reprodução HTML em tela cheia sem barras, com retorno por Voltar.
- Links `tel:`, `mailto:`, `sms:`, `geo:` e `market:` com gesto de usuário e confirmação antes de sair do Internet XP.
- Links que abrem nova janela/aba no GeckoView passam a criar uma aba interna, com bloqueio de popups sem gesto do usuário.
- Histórico, abas restauradas, sugestões e Downloads 2.0 mantidos.

### Limites conhecidos
- Upload de **pastas** e captura direta em formulários com atributo `capture` não implementados; seleção de arquivos existentes funciona.
- Permissões não são concedidas automaticamente, e são solicitadas ao usuário conforme necessidade.
- Esta entrega requer compilação Android real e teste com sites e dispositivos para confirmar todos os fluxos.

## Build e distribuição
Três APKs Release assinados por ABI: `armeabi-v7a`, `arm64-v8a`, `x86_64`.
Versão 1.0.16 / `versionCode` 17. Workflow publica APKs na GitHub Release (não Artifacts ZIP).
Secrets de assinatura necessários: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
