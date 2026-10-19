package br.com.internetxp;

/** Códigos públicos de ApplicationExitInfo (API 30+). Sem mensagens livres do sistema. */
final class ExitReasonLabels {
    private ExitReasonLabels() { }
    static String label(int reason) {
        switch (reason) {
            case 0: return "Motivo desconhecido";
            case 1: return "Saída voluntária";
            case 2: return "Encerrado por sinal do sistema";
            case 3: return "Pouca memória";
            case 4: return "Falha Java";
            case 5: return "Falha nativa";
            case 6: return "Aplicativo sem resposta (ANR)";
            case 7: return "Inicialização interrompida";
            case 8: return "Permissões alteradas";
            case 9: return "Uso excessivo de recursos";
            case 10: return "Encerrado pelo usuário";
            case 11: return "Usuário do Android interrompido";
            case 12: return "Dependência encerrada";
            case 13: return "Outro motivo do sistema";
            case 14: return "Suspensão de app (freezer)";
            case 15: return "Estado do aplicativo alterado";
            case 16: return "Aplicativo atualizado";
            case 17: return "Falha na inicialização";
            case 18: return "Anomalia de recursos";
            default: return "Motivo não reconhecido";
        }
    }
}
