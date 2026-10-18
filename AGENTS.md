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
