package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.35";
    public static final int VERSION_CODE = 36;
    public static final String HEADLINE = "Abas e favoritos mais organizados.";
    public static final String SUBTITLE = "Gerencie favoritos na mesma tela, recupere abas normais fechadas e navegue com controles compactos.";

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
        new Feature("01", "Gerenciador de favoritos", "Pesquise por endereço, abra sites, organize com setas e remova favoritos com confirmação, em tela própria."),
        new Feature("02", "Abas mais compactas", "Lista com título, origem, indicação da aba atual, favicon opcional e cartão visual uniforme."),
        new Feature("03", "Ações de abas mais estáveis", "Abertura e fechamento encontram a aba real no momento do toque, sem utilizar índice desatualizado."),
        new Feature("04", "Reabrir aba normal fechada", "Retorna à URL da última aba normal fechada nesta execução, sem restaurar conteúdo dinâmico ou histórico privado."),
        new Feature("05", "Privacidade preservada", "Abas privadas nunca entram na lista de reabertura nem passam a ser salvas nas preferências.")
    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
