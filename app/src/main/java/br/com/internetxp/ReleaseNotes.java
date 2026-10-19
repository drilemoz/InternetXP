package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.38";
    public static final int VERSION_CODE = 39;
    public static final String HEADLINE = "Acesso a servidores locais e diagnóstico de carregamento.";
    public static final String SUBTITLE = "Navegue em localhost:8000, IPs da rede e serviços do Termux; medições locais e modos de compatibilidade configuráveis.";

    public static final class Feature {
        public final String number;
        public final String title;
        public final String description;
        public Feature(String number, String title, String description) {
            this.number = number;
            this.title = title;
            this.description = description;
        }
    }

    public static final Feature[] FEATURES = {
        new Feature("01", "Localhost e rede Wi-Fi", "Reconhece 127.0.0.1:8000, localhost:8000, IPs privados, IPv6 local e hosts da rede."),
        new Feature("02", "HTTP local", "Usa HTTP por padrão para servidores locais; endereços HTTPS explícitos continuam respeitados."),
        new Feature("03", "Página inicial local", "Permite configurar serviços do Termux e endereços LAN como página inicial, com validação."),
        new Feature("04", "Desempenho visível", "Informa tempo de carregamento da aba, em memória, junto aos ajustes ChatGPT/Facebook e mídia em segundo plano."),
        new Feature("05", "Privacidade do autocompletar", "Não envia endereços locais digitados para o serviço online de sugestões.")
    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
