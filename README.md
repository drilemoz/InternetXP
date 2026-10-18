# Internet XP v1.0.18

## Novidades nesta versão
- Corrigido o erro de Android Lint `UnsafeOptInUsageError` da execução 18: `ContentPermission.notifyShown()` do GeckoView 158 utiliza opt-in explícito na rotina que apresenta o diálogo de localização, mantendo a comunicação com o motor de navegação.
- Nova tela **Novidades**: abertura automática uma vez por `versionCode` novo (inclusive a primeira instalação), visual XP próprio com globo oficial, cabeçalho azul e cartões legíveis.
- Botão **Começar a navegar** para fechar a tela; opção **Menu > Novidades desta versão** para consultar novamente.
- Ícone monocromático para temas de ícones do launcher Android.
- `ReleaseNotes.java` é a fonte dos destaques e deve ser atualizada em toda versão futura. `tools/validate_project.py` exige versão sincronizada e a presença da tela.

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
Versão 1.0.18 / `versionCode` 19. Workflow publica APKs na GitHub Release (não Artifacts ZIP).
Secrets de assinatura necessários: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

## Descrição curta para o GitHub
Internet XP é um navegador Android com visual inspirado no Windows XP e no Internet Explorer clássico. Usa GeckoView do Firefox e oferece abas, histórico, favoritos, downloads, extensões e suporte a recursos modernos dos sites. Interface compacta, navegação prática e APKs separados por arquitetura.
