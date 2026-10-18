# Regras Internet XP
- Versionamento sequencial obrigatório em toda alteração, com sincronização de README, CHANGELOG, workflow e demais metadados.
- GeckoView é obrigatório; não substituir por WebView.
- Interface prioritária: layout mobile XP para telefone, compacto e inspirado no Internet Explorer clássico.
- A barra de endereço deve permanecer como elemento principal do topo; ajustes finos de altura, largura, histórico e usabilidade são prioridade atual.
- Publicar APKs direto em GitHub Releases, sem upload-artifact e sem artifact ZIP.
- Manter três APKs por ABI: `armeabi-v7a`, `arm64-v8a` e `x86_64`.
- Secrets exigidos: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
- Não anunciar APK pronto sem build bem-sucedido real.

- Na v1.0.13, priorizar estabilidade antes de acrescentar recursos: seleção total de URL no toque, melhorias conservadoras nos downloads e testes de regressão.
- Para recursos inspirados no projeto Baixa Fácil, não declarar código copiado sem ter acesso e inspecionar o fonte; o DownloadManager não suporta pausa/retomada manual verdadeira.
- Progresso de download deve ser consultado apenas com painel visível e transferências ativas, sem polling contínuo em background.

- Na v1.0.14, Downloads 2.0 usa apenas `DownloadManager`: não prometer pausa/retomada manual; não compartilhar `file://`; não inventar ETA sem leituras reais e tamanho conhecido.
- Preferência de rede não tarifada deve respeitar política real do Android e bloquear roaming quando configurado.
- `versionCode 16` / `versionName 1.0.15`.

- Na v1.0.15, salvar URLs/títulos e aba ativa, mas restaurar somente a página ativa imediatamente. Nunca prometer restauração completa de formulário/scroll ou do estado JavaScript.
- Endereços de abas restaurados devem restringir-se a `http`, `https` e `about:blank`; históricos antigos não devem ganhar horários fictícios.
- Seleção integral do endereço ao toque e Downloads 2.0 devem continuar funcionais.

## 1.0.16
- Manter delegados de permissões e prompts coerentes com GeckoView 158; não aceitar permissões silenciosas de páginas.
- Testar seletor de arquivos, tela cheia, câmera, microfone, localização e links externos antes de anunciar compatibilidade total.

## 1.0.17
- Branding atualizado: launcher icon azul com globo e anel dourado, assets por densidade e adaptive icon.
- Próxima etapa planejada: privacidade e gerenciamento de cookies/cache/segurança.


## REGRA PERMANENTE: TELA DE NOVIDADES (OBRIGATÓRIO EM TODA VERSÃO)
- Sempre que qualquer arquivo do aplicativo mudar, incrementar `versionCode` e `versionName` sequencialmente.
- **Em TODA nova versão, atualizar `ReleaseNotes.VERSION_CODE`, `ReleaseNotes.VERSION_NAME`, `ReleaseNotes.HEADLINE`, `ReleaseNotes.SUBTITLE` e as entradas de `ReleaseNotes.FEATURES` para refletir alterações reais da versão**, não reutilizar textos antigos.
- A Activity `WhatsNewActivity` abre automaticamente na primeira instalação e uma vez após cada atualização, controlada por `SharedPreferences` com a última versão exibida. Não exibir repetidamente na mesma versão; oferecer Menu > Novidades desta versão para consulta manual.
- Nunca remover a tela, o registro no `AndroidManifest.xml`, o atalho do menu nem a verificação de sincronização de `tools/validate_project.py` sem pedido explícito do usuário.
- Manter visual mobile XP refinado (cabeçalho azul, globo, cartões, botão de continuação); preservar acessibilidade, baixo consumo de RAM e armazenamento local das preferências.
- A atualização das Novidades é parte do checklist de release: atualizar junto a README, CHANGELOG, docs/VALIDATION, notas GitHub Release e metadados Gradle.

## 1.0.18
- Execução 18 falhou em `lintDebug` por uso de `ContentPermission.notifyShown()` experimental; solução é opt-in localizado, sem desativar Lint.
- Tela de Novidades e teste de conteúdo/versionamento adicionados; atualização automática por versionCode.
- O recurso de privacidade planejado fica para a próxima versão após validar o build 1.0.18.
