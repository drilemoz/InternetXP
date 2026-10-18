## 1.0.23: download nativo e identificação como navegador
- Validar `tools/validate_project.py`, testes JVM e integridade ZIP.
- Testar download HTTP e HTTPS no Redmi A3 e Redmi Note 11 Pro+, verificando que outro navegador não é acionado.
- Testar PDF, APK, ZIP e XPI, incluindo respostas com login (cookies) e arquivo sem corpo da resposta.
- Testar progresso, cancelamento, falta de espaço, reinício de arquivo e seleção **Salvar cópia**.
- Testar reconhecimento como navegador padrão com seletor Android, RoleManager e link externo de outro app.
- Confirmar que downloads antigos continuam no local original e o aplicativo não os identifica falsamente como arquivos próprios.
- Testar encerramento de processo durante download: deve marcar como interrompido no próximo início.
- Verificar Tela de Novidades 1.0.23, assinatura e APKs por ABI no GitHub Actions.
- A compilação Android Gradle completa e testes em aparelho **não foram executados localmente**; dependem de CI e teste real.

## 1.0.22: página inicial, atalhos e diálogos XP
- Conferir a primeira instalação com página inicial em branco, busca central e tile Adicionar.
- Testar a busca na página inicial e a navegação por favoritos, mantendo URLs pré-existentes.
- Configurar uma URL de página inicial e confirmar persistência após reiniciar e ao criar abas.
- Verificar favicon em sites que disponibilizem `/favicon.ico` em PNG/ICO com PNG interno, inclusive fallback sem imagem.
- Confirmar que o indicador de segurança continua independente do favicon.
- Comparar largura/altura de Menu, Histórico, Favoritos, Extensões e Sobre no Redmi A3 e Redmi Note 11 Pro+.
- Confirmar teclado e seleção do endereço após tocar a barra.
- Validar Novidades 1.0.22 uma vez por versionCode e manualmente via Menu.
- O build Android/Lint e testes físicos dependem de execução real no GitHub Actions e aparelho.

## 1.0.21: menu em módulos e ajustes para telas pequenas
- Verificar no Redmi A3 se a barra de endereço aceita toque, seleciona tudo e abre o teclado.
- Confirmar que o botão Menu abre um painel com módulos, ícones e descrições, sem PopupMenu simples.
- Conferir que a barra inferior e o painel de downloads ficam mais compactos em telas pequenas.
- Validar a tela de Novidades 1.0.21 e a exibição única por versionCode.

