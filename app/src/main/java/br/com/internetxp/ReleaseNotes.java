package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.21";
    public static final int VERSION_CODE = 22;
    public static final String HEADLINE = "Mais conforto visual em telas pequenas.";
    public static final String SUBTITLE = "Novo menu em módulos com ícones, ajustes para aparelhos como o Redmi A3 e foco mais confiável na barra de endereços.";

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
        new Feature("01", "Menu em módulos", "O antigo menu simples virou um painel com ícones, descrições e visual mais próximo de um hub de recursos do aplicativo."),
        new Feature("02", "Barra de endereço mais confiável", "Toques na barra agora forçam foco, seleção total e teclado, reduzindo falhas vistas em aparelhos como o Redmi A3."),
        new Feature("03", "Layout adaptativo", "Alturas, espaçamentos e ícones foram reduzidos em telas compactas para evitar cortes e botões espremidos."),
        new Feature("04", "Privacidade em cartões", "A seção de privacidade também passou a usar o visual em módulos, com explicações curtas para cada ação."),
        new Feature("05", "Downloads e navegação mais leves", "A barra inferior e o painel de downloads foram ajustados para ocupar menos espaço útil em aparelhos menores.")
    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
