# Servidores locais no Internet XP 1.0.38

## No mesmo Android (Termux)

1. No Termux, execute `python -m http.server 8000 --bind 127.0.0.1` no diretório de testes.
2. No Internet XP, digite `localhost:8000` ou `127.0.0.1:8000` na barra e confirme. O navegador deve usar `http://` automaticamente.
3. Interrompa o servidor pelo Termux quando terminar. O navegador não cria nem mantém serviços por conta própria.

## Outro aparelho na mesma rede

- Inicie o servidor no dispositivo de destino, escutando na interface LAN (por exemplo, com `--bind 0.0.0.0`, *se* você confia nessa rede).
- Descubra o IP real dele e abra `192.168.1.20:8000` substituindo pelo IP e porta reais.
- Pode falhar por firewall, isolamento de clientes do roteador, diferença de redes ou se o servidor escutar só no loopback. `127.0.0.1` aponta sempre para o próprio aparelho no qual o Internet XP está rodando.
- Num **emulador** Android, para acessar o localhost do computador hospedeiro normalmente use `10.0.2.2:8000`.

## Segurança e resolução de problemas

- Sem esquema, servidores loopback, IPs privados, `.local`, `.lan`, `.localhost` e hosts simples acompanhados de porta recebem `http://`; domínios públicos normalmente recebem `https://`.
- Uma URL com `http://` ou `https://` explícito mantém o protocolo escolhido. Uma conexão HTTP **não é criptografada**; não use credenciais sensíveis em redes desconhecidas.
- O AndroidManifest permite HTTP para possibilitar hosts e IPs locais dinâmicos, mas a política vale também para uma navegação HTTP explicitamente solicitada a domínio público. O navegador não força o uso de HTTP em sites públicos.
- Certificados TLS autoassinados podem exigir aprovação específica no GeckoView; liberar HTTP não corrige certificados HTTPS inválidos.
- `localhost` em um navegador do próprio celular acessa serviços daquele celular, não os do PC.
- A configuração *Compatibilidade ChatGPT/Facebook* desliga proteção antirrastreamento de modo opcional apenas nos domínios selecionados. Não garante velocidade maior.
- O tempo do último carregamento corresponde ao intervalo relatado por callbacks de página do GeckoView; não é uma medição completa de execução de scripts.
