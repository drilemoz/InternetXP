# Internet XP v1.0.8

Projeto Android com **GeckoView 158** (motor Firefox) e novo **layout mobile XP** baseado no mockup enviado pelo usuário, pensado para telas de telefone.

## Base implementada
- Barra superior azul compacta com identidade visual XP.
- Campo de endereço logo abaixo, com destaque visual, cadeado e botão de atualizar ao lado.
- Área central dedicada ao GeckoView para maximizar o espaço útil do site.
- Barra inferior fixa com seis ações principais: **Voltar, Avançar, Início, Baixar, Abas e Menu**.
- Favoritos, histórico local, downloads públicos e gerenciador inicial de extensões Firefox assinadas.
- Até oito abas para conter uso de memória.

## Build e publicação
O workflow publica **APKs separados por arquitetura**, diretamente em **GitHub Releases**, sem `upload-artifact` e sem ZIP de artifacts:
- `Internet XP.v1.0.8-armeabi-v7a.apk`
- `Internet XP.v1.0.8-arm64-v8a.apk`
- `Internet XP.v1.0.8-x86.apk`
- `Internet XP.v1.0.8-x86_64.apk`

Sem os quatro Secrets de assinatura, os arquivos são publicados em variante **debug** com sufixo `-debug`.

## Requisitos
Java 17, Gradle 9.3.1, AGP 9.1.1, `compileSdk 37`, `targetSdk 35`, `minSdk 26`, Android 8.0+ e GeckoView `158.0.20261007201254`.

## Limitações atuais
- O layout busca ficar **muito próximo** do mockup, mas ainda precisa de validação visual no aparelho real.
- O botão de Abas abre seletor em diálogo, não uma faixa de abas sempre visível.
- O DownloadManager não herda todos os cookies do GeckoView; downloads com login ainda podem falhar.
- Sem Secrets, os APKs debug não garantem atualização futura sobre um release assinado.
- O repositório GitHub ainda não está acessível por esta conexão, então a publicação depende do seu push.

Sem afiliação à Microsoft ou Mozilla.
