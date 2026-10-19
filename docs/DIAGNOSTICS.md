# Diagnósticos e recuperação — Internet XP 1.0.37

## Como usar

1. Abra **Menu > Diagnósticos** para consultar os eventos recentes (categorias, sem stacktrace), motivos de encerramento fornecidos pelo Android 11+ e uma estimativa de uso de memória.
2. Toque em **Salvar relatório técnico em ZIP** e escolha um destino no seletor de arquivos Android. O app não transfere o relatório pela rede.
3. Abra o ZIP e revise `diagnostico.txt` e `LEIA-ME.txt` antes de compartilhar. O relatório contém horários, categorias de falhas e uso aproximado de memória; não contém URLs, cookies, senhas, conteúdo de páginas nem stacktraces.
4. Após a falha de um processo de página, acesse **Menu > Diagnósticos > Recuperar aba atual** (ou selecione a aba interrompida). Confirme para reabrir somente o endereço. Se a página provocou a falha, ela pode falhar novamente.

## Limites

- O Android pode não oferecer o motivo do encerramento. No Android 8 a 10, o histórico `ApplicationExitInfo` não está disponível. A consulta ignora outros processos para não confundir mortes do processo principal com serviços.
- Não é possível recuperar texto não enviado, formulários, estado JavaScript, scroll ou dados de uma página após o processo GeckoView ser encerrado.
- As URLs de abas **normais** são persistidas como antes. Um snapshot anterior válido pode ser usado se o principal estiver inválido; abas **privadas** não são persistidas.
- O botão **Limpar diagnóstico local** remove só a lista interna. Registros do sistema Android podem permanecer até o próprio sistema descartá-los.
- A exportação ZIP não coleta logs de terceiros, não captura logcat completo e não exige permissões amplas de arquivos.

## Testes recomendados

- Compilar com `:app:testDebugUnitTest :app:lintDebug :app:assembleRelease`, conferir três ABIs.
- Forçar parada do app e reiniciá-lo; verificar restauração só de abas normais. Não é correto simular um crash GeckoView encerrando deliberadamente o Android.
- Testar `Diagnósticos > Salvar ZIP` e cancelar a exportação; validar os dois arquivos de texto contidos no ZIP.
- Testar em Android 10 (sem `ApplicationExitInfo`) e Android 11+ (com registros possíveis), sem bloquear a UI.
- Em falha real de GeckoView, verificar se a recuperação exige confirmação e se respeita aba privada; usar logs do GitHub Actions e logcat para investigação do erro original de sugestões se ocorrer novamente.
