# Internet XP v1.0.2

Projeto Android inicial com **GeckoView 158** (motor Firefox) e interface clássica inspirada na captura do Explorador XP e no Internet Explorer 6.

## Base implementada
- Cabeçalho gradiente XP, menus Arquivo/Editar/Exibir/Favoritos/Ferramentas/Ajuda, ferramentas, barra de endereço, abas, GeckoView e barra de status.
- Voltar, Avançar, Início, Parar, Pesquisar, Nova aba, Atualizar, Favoritos.
- Até oito sessões de abas; favoritos e histórico local persistente (até 200 URLs).
- Gerenciador inicial de extensões Mozilla XPI assinadas com prompt explícito de permissões e ações ativar/desativar/remover.
- Download básico via DownloadManager de URL HTTP(S) pública, sujeito a limitações com sites autenticados.

## GitHub Actions: somente APK direto na Release

Crie o repositório `Internet-XP` e coloque os arquivos **incluindo a pasta oculta `.github`** na branch `main`.
Após corrigir a configuração do Android SDK, o push executa `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug` e publica o arquivo exato `Internet XP.v1.0.2.apk` na aba Releases, sem `actions/upload-artifact` e sem artefatos ZIP de workflow. Se todos os Secrets de assinatura estiverem presentes, publica APK Release assinado.

**Estado atual:** código-fonte local criado; sem SDK Android e sem repositório GitHub acessível no ambiente, por isso **não foi compilado nem publicado um APK real**. Não instalar nenhum arquivo renomeado como APK antes de validar o build.

### Requisitos
Java 17, Gradle 9.3.1, AGP 9.1.1, compileSdk 37 (targetSdk 35), Android 7.0+ e dispositivo ARM64. GeckoView `158.0.20261007201254` obtido do repositório Maven Mozilla.

### Limitações técnicas
- Não é reprodução perfeita pixel a pixel do IE6. Precisa testar aparência no aparelho.
- Falta implementação completa de permissões de sites, popups, formulários JS e sessão restaurável. Alguns sites não funcionarão ainda.
- DownloadManager não compartilha cookies automaticamente com GeckoView; downloads com sessão podem falhar.
- Limpar histórico só afeta lista do próprio app, não cookies/cache Gecko.
- Sem Secrets o APK é Debug assinado por chave efêmera, logo **não oferece atualização garantida** para futura Release assinada.
- GeckoView pode usar bastante RAM; não há medições reais de performance.

Sem afiliação à Microsoft ou Mozilla. Visual baseado em referência do usuário.

## Correção de compilação 1.0.2
O workflow 2 (de 18/10/2026) avançou até `:app:checkDebugAarMetadata`, que encontrou 11 incompatibilidades do GeckoView 158 e suas dependências com `compileSdk 35` e AGP 8.13.2. Foram atualizados os requisitos de compilação para:
- `compileSdk 37` com SDK `platforms;android-37`, mantendo `targetSdk 35` e `minSdk 24`.
- AGP `9.1.1` (suporta API 37 e `androidx.core:core:1.19.0`).
- Gradle `9.3.1`, mínimo compatível com AGP 9.1.1.
- Android Build Tools `36.0.0`, JDK 17.
A versão do GeckoView permanece `158.0.20261007201254` e não houve remoção de dependências para esconder a incompatibilidade.
**Atenção:** os logs encerraram antes de `compileDebugJavaWithJavac`, então ainda podem existir falhas Java não observadas. APK e assinatura continuam pendentes de build na CI.

## Histórico de correção 1.0.1
O workflow 1 falhou em `android-actions/setup-android@v3` ao executar `sdkmanager tools` (`Failed to find package 'tools'`). A versão 1.0.1 usa `android-actions/setup-android@v4` com `packages: platform-tools`, sem solicitar o pacote removido. A versão de SDK e Build Tools é instalada em etapa seguinte. O build Android completo continua pendente de validação no GitHub Actions.
