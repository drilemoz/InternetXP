package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.31";
    public static final int VERSION_CODE = 32;
    public static final String HEADLINE = "Barra inteligente e módulos de navegação em tela inteira.";
    public static final String SUBTITLE = "Sugestões opcionais do DuckDuckGo, favicons e copiar/colar contextual, com módulos que abrem como telas próprias.";

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
        new Feature("01", "Barra com sugestões", "Sugestões online opcionais pelo DuckDuckGo, com dados transmitidos apenas em abas normais e com opção de desligar em Configurações."),
        new Feature("02", "Copiar e colar", "Botões de copiar e colar aparecem só quando a barra de endereço está selecionada."),
        new Feature("03", "Favicons no navegador", "Ícones HTTPS de sites nos resultados locais do endereço, histórico, favoritos e abas, com cache limitado."),
        new Feature("04", "Telas próprias", "Menu, privacidade, histórico, favoritos, downloads, abas, configurações e Sobre abrem como telas com Voltar."),
        new Feature("05", "Extensões preservadas", "A central de WebExtensions e os diálogos de confirmação e permissão permanecem intactos.")
    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
