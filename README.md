# Internet XP v1.0.10

Navegador Android com **GeckoView 158**, interface mobile baseada no visual XP e nas referências enviadas pelo usuário.

## Mudanças desta versão

- **Sem barra azul própria de título**: barra de endereço passa a ser o primeiro elemento do aplicativo, logo abaixo da status bar do Android. A miniatura do usuário mostra que o cabeçalho anterior desperdiçava altura em telas móveis.
- O endereço permanece no painel claro e o botão Atualizar fica à direita. Cadeado vetorial apenas em endereços HTTPS, globo cinza nos demais (não representa auditoria do certificado).
- Barra inferior com Voltar, Avançar, Início, Baixar, Abas e Menu. O menu abre ancorado no botão inferior.
- Favoritos, histórico, até oito abas, downloads via DownloadManager e gerenciamento inicial de extensões assinadas preservados.

## APKs por arquitetura e tamanho

`app/build.gradle` usa *splits* individuais por ABI `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64`, sem APK universal. Agora também:

1. **Release com R8** (`minifyEnabled true`, `shrinkResources true`, configuração padrão otimizada).
2. **Compactação ZIP das bibliotecas .so** (`packaging.jniLibs.useLegacyPackaging = true`), que reduz os bytes do APK distribuído, mas exige extrair bibliotecas na instalação, podendo ocupar mais armazenamento e aumentar custos de inicialização.
3. **Sem publicação de Debug**. Os quatro Secrets de assinatura são obrigatórios para publicar a Release; Debug é só para testes locais/lint.
4. `tools/report_apk.py` exige que cada APK tenha **exatamente uma ABI correta**, todas as bibliotecas nativas comprimidas e emite relatório de tamanho ZIP versus descompactado para identificar o peso do GeckoView.

O GeckoView continua grande por conter um motor completo. **Não há promessa de tamanho final**: os cerca de 190 MB citados pelo usuário não são mensuração disponível neste ambiente. Precisa compilar a v1.0.10 e comparar o tamanho de cada APK.

Os APKs esperados na aba GitHub Releases são:

- `Internet XP.v1.0.10-armeabi-v7a.apk`
- `Internet XP.v1.0.10-arm64-v8a.apk`
- `Internet XP.v1.0.10-x86.apk`
- `Internet XP.v1.0.10-x86_64.apk`

NÃO usar `actions/upload-artifact`, TAR.GZ ou Artifact ZIP. O **ZIP de código-fonte** é apenas para transferência ao repositório.

## Requisitos

- JDK 17, Gradle 9.3.1, Android Gradle Plugin 9.1.1 e SDK Android 37.0.
- `compileSdk 37`, `targetSdk 35`, `minSdk 26`, `versionCode 11` e `versionName 1.0.10`.
- GeckoView `158.0.20261007201254`, sem trocar por WebView.
- Secrets existentes: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` (somente no GitHub, nunca no ZIP).

## Publicar

Extraia o conteúdo da pasta `Internet-XP-v1.0.10` **na raiz** do repositório `InternetXP`, incluindo `.github/workflows`. Importe os quatro Secrets de assinatura no GitHub Manager antes de executar o workflow. O workflow faz testes, lint, Release otimizada e checagem por ABI; publica os quatro APKs assinados diretamente na Release.

## Limitações e verificações pendentes

- Build Android/Gradle completo e instalação em dispositivos reais pendentes: aqui houve validação estrutural e testes locais, não uma geração de APK.
- A compressão de bibliotecas nativas privilegia tamanho do arquivo **distribuído**, não espaço ocupado ou velocidade de carregamento. Verifique tempo de instalação e armazenamento após instalar.
- O R8 pode exigir regras específicas de retenção se o GeckoView expuser erros JNI/reflexão no build ou em testes; é essencial testar abertura da página, abas e extensões após a compilação.
- Gerenciador de downloads não compartilha todos os cookies do GeckoView; downloads autenticados podem falhar.
- Nem todas as extensões Firefox Desktop são compatíveis com Android.
- Sem afiliação à Microsoft ou Mozilla.

## Diagnóstico corrigido do workflow 10 (v1.0.10)
- Os logs do build 10 mostram **quatro Secrets vazios** ao entrar na etapa de assinatura. A execução interrompeu antes dos testes, Lint e compilação Android. Não é falha comprovada no motor nem no código Java.
- Confirme a importação como **Repository secrets** do repositório `InternetXP` que realmente executa o workflow. Se estiverem em um Environment, o job precisaria declarar esse Environment; esta versão espera Secrets do repositório.
- Para não ocultar defeitos de código atrás de uma falha de credenciais, o workflow roda testes Python e Lint/testes Android antes da etapa de assinatura; nenhum APK é publicado se os Secrets faltarem.
- Corrige um problema preventivo na seleção dos APKs: `x86` não pode casar com `x86_64`. O workflow usa o nome exato `app-${abi}-release.apk`, documentado pelo Android Gradle Plugin.
- O arquivo de Secrets anterior, que deve ficar **fora do repositório e do ZIP**, continua válido; não foi gerada outra chave.
