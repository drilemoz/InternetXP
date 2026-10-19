package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.40";
    public static final int VERSION_CODE = 41;
    public static final String HEADLINE = "Mídias, ações por toque e favicons mais completos.";
    public static final String SUBTITLE = "Selecione imagens, vídeos e áudios para inspecionar e salvar arquivos diretos; toque longo em links e seleção de texto nativa.";

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
        new Feature("01", "Botão flutuante de mídias", "Atalho movível na página para listar mídias selecionadas por pressão longa."),
        new Feature("02", "Prévia e detalhes", "Formato, tamanho HTTP conhecido, resolução de imagem leve e salvamento direto pelo gerenciador existente."),
        new Feature("03", "Segurar imagens e links", "Abrir, copiar, compartilhar e acessar detalhes no menu contextual do GeckoView."),
        new Feature("04", "Seleção de texto", "Toolbar nativa do GeckoView para copiar e selecionar, preservando os campos de texto."),
        new Feature("05", "Favicons persistentes", "Mais caminhos HTTPS no próprio site, ícones declarados e cache local com limite e expiração.")
    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
