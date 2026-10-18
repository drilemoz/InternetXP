# Internet XP v1.0.0

Projeto Android inicial com **GeckoView 158** (motor Firefox) e interface clássica inspirada na captura do Explorador XP e no Internet Explorer 6.

## Base implementada
- Cabeçalho gradiente XP, menus Arquivo/Editar/Exibir/Favoritos/Ferramentas/Ajuda, ferramentas, barra de endereço, abas, GeckoView e barra de status.
- Voltar, Avançar, Início, Parar, Pesquisar, Nova aba, Atualizar, Favoritos.
- Até oito sessões de abas; favoritos e histórico local persistente (até 200 URLs).
- Gerenciador inicial de extensões Mozilla XPI assinadas com prompt explícito de permissões e ações ativar/desativar/remover.
- Download básico via DownloadManager de URL HTTP(S) pública, sujeito a limitações com sites autenticados.

## GitHub Actions: somente APK direto na Release

Crie o repositório `Internet-XP` e coloque os arquivos **incluindo a pasta oculta `.github`** na branch `main`.
O primeiro push executa `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug` e publica o arquivo exato `Internet XP.v1.0.0.apk` na aba Releases, sem `actions/upload-artifact` e sem artefatos ZIP de workflow. Se todos os Secrets de assinatura estiverem presentes, publica APK Release assinado.

**Estado atual:** código-fonte local criado; sem SDK Android e sem repositório GitHub acessível no ambiente, por isso **não foi compilado nem publicado um APK real**. Não instalar nenhum arquivo renomeado como APK antes de validar o build.

### Requisitos
Java 17, Gradle 8.13, AGP 8.13.2, SDK 35, Android 7.0+ e dispositivo ARM64. GeckoView `158.0.20261007201254` obtido do repositório Maven Mozilla.

### Limitações técnicas
- Não é reprodução perfeita pixel a pixel do IE6. Precisa testar aparência no aparelho.
- Falta implementação completa de permissões de sites, popups, formulários JS e sessão restaurável. Alguns sites não funcionarão ainda.
- DownloadManager não compartilha cookies automaticamente com GeckoView; downloads com sessão podem falhar.
- Limpar histórico só afeta lista do próprio app, não cookies/cache Gecko.
- Sem Secrets o APK é Debug assinado por chave efêmera, logo **não oferece atualização garantida** para futura Release assinada.
- GeckoView pode usar bastante RAM; não há medições reais de performance.

Sem afiliação à Microsoft ou Mozilla. Visual baseado em referência do usuário.
