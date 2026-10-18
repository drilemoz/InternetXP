package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.29";
    public static final int VERSION_CODE = 30;
    public static final String HEADLINE = "Testes de atualização sincronizados.";
    public static final String SUBTITLE = "Corrigidas verificações antigas da tela de Novidades que bloqueavam o workflow 28. O gerenciador de extensões GeckoView foi preservado.";

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
        new Feature("01", "Testes de Novidades", "Os testes agora acompanham a versão atual e não rejeitam atualizações por números antigos fixos."),
        new Feature("02", "Exibição após atualização", "Cobertura corrigida para mostrar Novidades somente quando o último versionCode visto é anterior ao atual."),
        new Feature("03", "Proteção do versionamento", "Validação cruzada mantém Gradle, Novidades, nomes dos APKs e GitHub Release sincronizados."),
        new Feature("04", "Extensões mantidas", "A central GeckoView conserva importação de XPI, listagem, ativação, desativação, remoção e exportação."),
        new Feature("05", "Verificação de conteúdo", "Os testes conferem destaques únicos, textos não vazios e identificação de versão sem valores legados.")

    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
