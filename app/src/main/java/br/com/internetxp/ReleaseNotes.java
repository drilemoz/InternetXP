package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.18";
    public static final int VERSION_CODE = 19;
    public static final String HEADLINE = "Mais bonito. Mais preparado.";
    public static final String SUBTITLE = "A evolução do Internet XP continua, com a experiência clássica e cuidados extras na compatibilidade.";

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
        new Feature("01", "Nova tela de Novidades", "Conheça as alterações depois de cada atualização, em uma página exclusiva com estilo Windows XP."),
        new Feature("02", "Permissões mais estáveis", "Corrigido o uso da API experimental do GeckoView no diálogo de localização, mantendo a confirmação do site."),
        new Feature("03", "Visual integrado", "A tela acompanha as cores azuis, os detalhes brilhantes e o globo oficial do Internet XP."),
        new Feature("04", "Acesso quando quiser", "Abra Menu > Novidades desta versão para rever os destaques a qualquer momento.")
    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
