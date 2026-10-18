package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.19";
    public static final int VERSION_CODE = 20;
    public static final String HEADLINE = "Correção importante na compilação.";
    public static final String SUBTITLE = "Esta atualização corrige uma dependência necessária ao GeckoView e mantém a tela de Novidades e seus recursos.";

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
        new Feature("01", "Compilação corrigida", "Adicionada a dependência AndroidX experimental necessária para compilar a anotação de permissão do GeckoView."),
        new Feature("02", "Proteção para próximas versões", "O processo de validação agora verifica a presença das bibliotecas usadas pelas anotações experimentais."),
        new Feature("03", "Novidades em dia", "Esta tela acompanha a versão 1.0.19 e continua aparecendo uma vez depois de cada atualização."),
        new Feature("04", "Navegação preservada", "Mantidos o globo azul, as abas, os downloads e a distribuição assinada por arquitetura.")
    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
