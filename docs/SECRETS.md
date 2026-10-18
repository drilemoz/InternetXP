# Assinatura Android no GitHub Manager
Segredos a importar após gerar keystore de assinatura segura e fazer backup em local independente:
- KEYSTORE_BASE64: PKCS12 em base64 em uma única linha
- KEYSTORE_PASSWORD: senha da keystore
- KEY_ALIAS: alias
- KEY_PASSWORD: senha da chave

Crie os quatro Secrets no repositório, depois execute novamente o workflow. Evite distribuir o APK Debug inicial se você pretende atualizar o mesmo pacote: assinaturas diferentes não são compatíveis. Nunca comite keystore, Secrets ou valores reais ao GitHub.

O `GITHUB_TOKEN` nativo, com contents: write, publica a Release; não exige um PAT adicional.
