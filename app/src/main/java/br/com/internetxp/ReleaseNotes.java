package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.28";
    public static final int VERSION_CODE = 29;
    public static final String HEADLINE = "Correção de compilação e estabilidade dos painéis.";
    public static final String SUBTITLE = "Corrigidas três referências Java que impediam o APK da 1.0.27 de compilar. A central de extensões GeckoView permanece incluída.";

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
        new Feature("01", "Compilação do menu", "Corrigida a captura do diálogo nos atalhos do menu principal: a referência agora existe antes da criação dos cartões."),
        new Feature("02", "Diálogo de ferramentas", "Eliminada referência inexistente durante a animação do painel modular."),
        new Feature("03", "Histórico corrigido", "Animação do histórico agora utiliza o contêiner correto e deixa de acessar variável fora do escopo."),
        new Feature("04", "Extensões preservadas", "Importação XPI, ações dos complementos, ativação, desativação, remoção e exportação continuam disponíveis para validação no Android."),
        new Feature("05", "Validação preventiva", "Incluídas verificações específicas contra o retorno das três falhas de escopo encontradas no workflow 27.")

    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
