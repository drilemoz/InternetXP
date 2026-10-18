package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.26";
    public static final int VERSION_CODE = 27;
    public static final String HEADLINE = "Pop-ups alinhados ao novo menu.";
    public static final String SUBTITLE = "O Internet XP ficou mais consistente visualmente, com diálogos menos secos, abertura animada e melhor adaptação a telas compactas.";

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
        new Feature("01", "Pop-ups coerentes", "As janelas de Favoritos, Extensões, Sobre, erros e ações rápidas agora seguem melhor o visual azul e os cartões do novo menu."),
        new Feature("02", "Cabeçalho refinado", "O menu ganhou um cabeçalho mais forte e a família de diálogos recebeu acabamento visual mais próximo da identidade do Internet XP."),
        new Feature("03", "Abertura animada", "Painéis e pop-ups passaram a abrir com uma transição suave para deixar a navegação mais agradável."),
        new Feature("04", "Mais compacto no Redmi A3", "Margens, textos e grade foram ajustados para caber melhor em telas menores sem perder legibilidade."),
        new Feature("05", "Extensões integradas", "As ações de ativar, desativar, instalar e remover extensões passaram a usar diálogos próprios do aplicativo.")
    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
