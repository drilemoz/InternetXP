# Internet XP v1.0.14

Navegador Android com Mozilla GeckoView 158, visual mobile XP e três APKs assinados por ABI.

## Downloads 2.0
- Painel interno acima da barra de navegação, oculto fora de uso. Filtros **Todos / Ativos / Finalizados**, resumo e ação de limpeza dos registros finalizados (não exclui os arquivos reais).
- Barra de progresso real por download. Se o tamanho for desconhecido, progresso indeterminado, sem inventar percentual.
- Velocidade em B/s/KB/s/MB/s calculada pela diferença de bytes de duas leituras do DownloadManager, com base em relógio monotônico. Tempo restante **aproximado** somente quando tamanho e velocidade são válidos. Valores reiniciam quando o processo reabre.
- Status e motivo de falha: rede ausente, espera de Wi-Fi, HTTP 4xx/5xx, falta de espaço e outros códigos da API.
- Abrir e compartilhar arquivos concluídos por URI de conteúdo do DownloadManager e permissão temporária de leitura.
- Cancelar com confirmação, tentar novamente desde o zero, remover registro sem apagar arquivo e limpar finalizados.
- Ao iniciar o download, opção por arquivo de usar **somente redes não tarifadas**. Roaming desativado por padrão. A política efetiva de rede é aplicada pelo DownloadManager do Android.
- Consultas periódicas apenas enquanto o painel está visível e há transferências ativas, a cada 2 segundos; o DownloadManager continua trabalhando quando o painel é fechado.

## Navegação e aparência
Mantém barra de endereço compacta, seta para histórico, sugestões e seleção automática de toda a URL ao tocar; barra inferior compacta, GeckoView e extensões Firefox.

## Segurança e limites conhecidos
- `DownloadManager` **não implementa pausa/retomada manual sob controle do app**. Tentar novamente sempre inicia do zero.
- Downloads exigindo cookies da sessão GeckoView ainda podem falhar; o app não copia cookies sensíveis automaticamente.
- Velocidade e ETA são estimativas pontuais observadas, não garantias de tempo.
- Nome de destino repetido pode resultar em erro do DownloadManager; trate o erro na interface.
- APK Release deve ser testado no aparelho e o tamanho final depende da compilação GitHub Actions.
- Requisitos: Java 17, Gradle 9.3.1, AGP 9.1.1, `compileSdk 37`, `targetSdk 35`, `minSdk 26`.

## Release
Arquivo-fonte ZIP e publicação direta na Release `v1.0.14` de **três APKs assinados**: `Internet XP.v1.0.14-armeabi-v7a.apk`, `Internet XP.v1.0.14-arm64-v8a.apk`, `Internet XP.v1.0.14-x86_64.apk`, mais checksums, sem upload-artifact. Exige os quatro Secrets existentes; não coloque Secrets ou `.p12` no GitHub.
