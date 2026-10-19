package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.36";
    public static final int VERSION_CODE = 37;
    public static final String HEADLINE = "Detalhes de segurança compatíveis com GeckoView 158.";
    public static final String SUBTITLE = "Correção de compilação da tela Segurança da página, mantendo as melhorias recentes em abas e favoritos.";

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
        new Feature("01", "Compilação da segurança corrigida", "Informações do certificado usam a API X.509 disponível no GeckoView 158."),
        new Feature("02", "Emissor e titular reais", "Detalhes exibem os nomes completos do emissor e titular quando o certificado está disponível."),
        new Feature("03", "Ausência de certificado tratada", "Sites sem certificado disponível mostram aviso explícito, sem inventar dados de segurança."),
        new Feature("04", "Abas e favoritos preservados", "Mantidos gerenciador de favoritos, reabertura limitada de abas e indicadores do navegador."),
        new Feature("05", "Regressão de API verificada", "Validações detectam os campos GeckoView antigos antes do build e sincronizam esta versão.")
    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
