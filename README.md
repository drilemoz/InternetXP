# Internet XP v1.0.13

Navegador Android com **GeckoView 158** e interface Internet Explorer/XP compacta para celular.

## Novidades da v1.0.13
- **Endereço:** tocar no campo seleciona a URL inteira, inclusive se o campo já estiver em foco; a primeira tecla substitui a seleção. Mantém seleção normal, colar, sugestões e seta de histórico.
- **Downloads internos:** progresso consultado automaticamente no painel a cada 2 segundos enquanto houver transferências ativas. Com o painel oculto ou app pausado, a consulta é interrompida.
- **Downloads:** agora permite **Cancelar** (avisa que o DownloadManager apagará arquivo parcial) e **Tentar novamente** (reinicia desde o começo após falha ou cancelamento). Remover da lista não exclui arquivos já concluídos.
- **Otimização:** histórico/sugestões e apresentação de bytes/percentuais extraídos para classes Java independentes, testáveis sem Android.
- **Persistência:** registros do download são gravados quando houver alteração, evitando gravar no armazenamento a cada ciclo de consulta inalterado.
- **Segurança:** abrir downloads concluídos passa somente pela URI de conteúdo obtida do DownloadManager, sem usar URI `file://` como fallback inseguro.

Inspirado em funções úteis do projeto **Baixa Fácil** (progresso, controles e recuperação de falhas). Não houve cópia literal do código do Baixa Fácil nesta versão, pois o código-fonte não estava acessível nesta conversa. `DownloadManager` **não oferece pausa/retomada manual real**: a opção de nova tentativa reinicia a transferência. O suporte a downloads com login/cookies do GeckoView ainda é limitado.

## Publicação Android
Mantidos três APKs Release assinados com os mesmos Secrets existentes:
- `Internet XP.v1.0.13-armeabi-v7a.apk`
- `Internet XP.v1.0.13-arm64-v8a.apk`
- `Internet XP.v1.0.13-x86_64.apk`

O GitHub Actions publica os APKs diretamente na Release `v1.0.13`, sem Artifacts ZIP. **O código-fonte ZIP não é um APK.**

## Requisitos
Android 8.0+ (minSdk 26), compileSdk 37, targetSdk 35, Gradle 9.3.1, AGP 9.1.1, Java 17, GeckoView `158.0.20261007201254`.

## Limitações / verificações pendentes
- Necessário compilar e testar a v1.0.13 no GitHub Actions e verificar no aparelho o comportamento do teclado e downloads.
- Históricos e abas ainda não foram totalmente migrados para persistência robusta / isolamento de sessões; próximas versões devem completar essa etapa.
- A integração direta de controles de rede e pausas retomáveis do Baixa Fácil exige arquitetura própria de transferência, não foi prometida nesta primeira leva.
- Extensões são compatíveis apenas quando suportadas pelo GeckoView Android e assinadas conforme política Mozilla.
