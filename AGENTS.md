# Regras Internet XP

- Versionamento sequencial por mudança: v1.0.10/versionCode 11 é a versão presente, próximo incremento v1.0.11/versionCode 12. Sincronizar workflow, arquivo APK, README, CHANGELOG, docs e Sobre.
- GeckoView 158 é obrigatório: não substituir o motor Firefox por WebView.
- Layout mobile conforme screenshot aprovado: **sem barra de título duplicada**, endereço como primeiro componente abaixo do status bar do Android, barra inferior com seis ações.
- Usar Java 17, AGP 9.1.1, Gradle 9.3.1, compileSdk 37, targetSdk 35, minSdk 26.
- Gerar APKs **separados por ABI**: armeabi-v7a, arm64-v8a, x86, x86_64. Não publicar universal acidental, variante vazia ou rotulada com ABI errada.
- No GitHub, publicar APKs assinados de variante **Release otimizada com R8**, nunca Debug: `Internet XP.v1.0.10-<abi>.apk`. Sem `actions/upload-artifact` ou Artifact ZIP. O ZIP entregue ao usuário contém apenas o código-fonte.
- Nomear Secrets exigidos corretamente: KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD. Nunca inserir valores ou keystore no repositório/ZIP; reutilizar a mesma assinatura para atualizações.
- A compactação de `.so` visa reduzir download, mas pode aumentar espaço usado na instalação e tempo de inicialização; documentar essa troca e medir a cada mudança. Não afirmar redução de 190 MB sem APK real analisado.
- Nunca declarar APK gerado/publicado ou upload Drive concluído sem confirmação da ferramenta correspondente.
- Não desativar Lint para ocultar erro, nem forçar GeckoView em minSdk inferior a 26.
