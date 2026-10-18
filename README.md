# Internet XP v1.0.19

## Novidades nesta versão
- Corrige a falha da execução 19 (`cannot find symbol: androidx.annotation.OptIn`) incluindo `androidx.annotation:annotation-experimental:1.6.0` e `androidx.annotation:annotation:1.11.0` explicitamente no Gradle, como orienta a documentação AndroidX.
- Mantém o opt-in localizado em `ContentPermission.notifyShown()`, sem suprimir o Lint ou reduzir as confirmações de permissões do usuário.
- Atualiza a tela **Novidades** para 1.0.19: texto e cartões específicos desta correção, exibidos uma única vez após atualizar e disponíveis pelo menu.
- Acrescenta validação de regressão das bibliotecas AndroidX/opt-in em `tools/validate_project.py`, executada no começo do workflow.
- Mantém o GeckoView, o ícone com globo azul, a assinatura existente e os três APKs por arquitetura.

**Observação:** esta entrega corrige o código-fonte da falha relatada. A compilação Android, o Lint e a instalação ainda precisam ser validados no GitHub Actions e em aparelho.

Navegador Android com GeckoView 158 e interface mobile inspirada no Windows XP/Internet Explorer.

## Ícone oficial (v1.0.17)
Globo azul com anel dourado, integrado ao Android como ícone tradicional e adaptativo (launcher). Arquivo-fonte: `branding/internet_xp_icon_master.png`.

## Compatibilidade com sites (introduzida na v1.0.16)
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
Versão 1.0.19 / `versionCode` 20. Workflow publica APKs na GitHub Release (não Artifacts ZIP).
Secrets de assinatura necessários: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

## Descrição curta para o GitHub
Internet XP é um navegador Android com visual inspirado no Windows XP e no Internet Explorer clássico. Usa GeckoView do Firefox e oferece abas, histórico, favoritos, downloads, extensões e suporte a recursos modernos dos sites. Interface compacta, navegação prática e APKs separados por arquitetura.
