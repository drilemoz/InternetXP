# Mídias e menu contextual (Internet XP 1.0.40)

## Teste em aparelho

1. Abra um site HTTPS contendo uma foto JPEG/PNG, vídeo MP4 ou áudio MP3 com URL HTTP(S) direta.
2. Pressione a imagem ou vídeo por alguns instantes. O GeckoView deve informar a origem ao Internet XP.
3. Escolha **Detalhes e salvar**. Verifique se o popup mostra **Formato**, **Tamanho** (somente com Content-Length real), **Resolução** (somente se foi possível gerar prévia limitada) e **Fonte**.
4. Toque **Salvar** e abra Downloads; confira o resultado, inclusive cancelamento e falha. O download usa o gerenciador próprio GeckoWebExecutor.
5. Repita com vídeo HLS (arquivo `.m3u8`), DASH (`.mpd`), recurso `blob:` e conteúdo protegido. O app não deve anunciar download direto como mídia reproduzível nesses casos.
6. Pressione um link e verifique abrir no mesmo navegador, em aba nova, copiar e compartilhar. Na aba privada, uma nova aba aberta por essa ação deve continuar privada.
7. Pressione e selecione texto normal e de formulário; observe barra de ações nativa GeckoView. Senhas não devem aparecer no relatório de diagnóstico.
8. Toque no ícone de download flutuante, mova-o pela página, alterne abas, retorne, navegue a outro domínio e verifique que a lista de mídias pertence somente à aba/página e não fica salva em histórico.
9. Na página inicial, confira favicons de sites com `/favicon.ico`, somente `link rel=icon` e sites sem favicon. Reinicie o app e confira cache local; confirme que não existem pedidos a um provedor externo de favicons.

## Limites intencionais

- O botão flutuante lista até 12 mídias que **foram selecionadas pelo usuário por pressão longa**. Não faz varredura de todo o DOM, não detecta automaticamente mídias carregadas por scripts, e não converte playlists de streaming em arquivos de vídeo.
- Metadados são consultados por requisição HEAD **separada**, sem cookies e sem redirecionamentos. Tamanho, tipo e resolução podem ficar indisponíveis em sites autenticados ou que rejeitam HEAD.
- A prévia de imagem usa uma requisição GET separada limitada a 512 KiB, sem cookies, e só decodifica imagens com dimensões razoáveis. A prévia não interfere no conteúdo original do GeckoView.
- O download real usa GeckoWebExecutor e pode depender da autenticação/sessão e das regras de servidor. Não há quebra de DRM, nem suporte declarado a `blob:` ou `data:`.
- Favicons são consultados somente por HTTPS no mesmo domínio, com sem redirects e limites; cache em memória e disco do próprio app. Favicons não indicam confiabilidade do certificado da página.

## Validação pendente

Compilar e executar `:app:testDebugUnitTest`, `:app:lintDebug` e `:app:assembleRelease` no GitHub Actions; testar em dispositivos reais e compartilhar logs de falhas se o build ou runtime apresentar erros.
