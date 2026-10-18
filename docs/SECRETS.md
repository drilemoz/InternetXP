# Assinatura Android no GitHub Manager
Segredos a importar após gerar keystore de assinatura segura e fazer backup em local independente:
- KEYSTORE_BASE64: PKCS12 em base64 em uma única linha
- KEYSTORE_PASSWORD: senha da keystore
- KEY_ALIAS: alias
- KEY_PASSWORD: senha da chave

Crie os quatro Secrets no repositório, depois execute novamente o workflow. Evite distribuir o APK Debug inicial se você pretende atualizar o mesmo pacote: assinaturas diferentes não são compatíveis. Nunca comite keystore, Secrets ou valores reais ao GitHub.

O `GITHUB_TOKEN` nativo, com contents: write, publica a Release; não exige um PAT adicional.

## Atenção ao workflow 10
O GitHub Actions informou que os quatro Secrets estavam vazios. Confirme a importação dos arquivos privados gerados anteriormente para o repositório **InternetXP** (exatamente o repositório que aparece nos logs), em **Settings > Secrets and variables > Actions > Repository secrets**. Não use Secrets de Environment sem atribuir o Environment ao job. O arquivo TXT sensível não deve entrar no repositório ou no ZIP do código-fonte.
