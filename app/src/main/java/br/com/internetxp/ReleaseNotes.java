package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.25";
    public static final int VERSION_CODE = 26;
    public static final String HEADLINE = "Menu rápido com mais personalidade.";
    public static final String SUBTITLE = "O menu principal ficou mais vivo, mais alto e com menos espaço lateral, destacando atalhos em formato de hub.";

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
        new Feature("01", "Menu rápido redesenhado", "O botão Menu agora abre um painel mais vivo, inspirado na proposta XP Moderno, com destaque para os atalhos mais usados."),
        new Feature("02", "Atalhos principais em grade", "Nova aba, privada, favoritos, histórico, downloads e configurações ganharam cartões compactos, mais fáceis de localizar."),
        new Feature("03", "Mais altura, menos margens", "O painel ocupa melhor a tela, sobe mais no eixo vertical e reduz o espaço lateral sem cobrir tudo."),
        new Feature("04", "Ferramentas extras abaixo", "As demais ações continuam acessíveis em uma lista compacta, mais organizada e menos seca visualmente."),
        new Feature("05", "Pop-ups coerentes", "Os diálogos modulares também ficaram um pouco maiores para seguir melhor a identidade do restante do navegador.")
    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
