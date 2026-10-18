package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.22";
    public static final int VERSION_CODE = 23;
    public static final String HEADLINE = "Seu Internet XP, com um início personalizado.";
    public static final String SUBTITLE = "Página inicial com pesquisa central e favoritos, configurações próprias, favicons e caixas de diálogo no visual XP.";

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
        new Feature("01", "Nova página inicial", "Abra uma página em branco com pesquisa centralizada e atalhos rápidos para seus sites favoritos."),
        new Feature("02", "Favoritos em ícones", "Adicione favoritos diretamente na página inicial e abra seus sites pelos cartões de acesso rápido."),
        new Feature("03", "Configurações da página inicial", "Escolha entre a página inicial do Internet XP ou um endereço de site personalizado."),
        new Feature("04", "Favicons dos sites", "O navegador tenta buscar o ícone HTTPS do próprio site para mostrar nos atalhos e na barra de endereço, quando disponível."),
        new Feature("05", "Menus e diálogos refinados", "Favoritos, extensões, histórico e informações ganharam aparência mais alinhada ao XP, com menus menores e mais confortáveis no celular.")
    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
