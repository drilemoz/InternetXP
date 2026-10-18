package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.27";
    public static final int VERSION_CODE = 28;
    public static final String HEADLINE = "Central de extensões com instalação XPI real.";
    public static final String SUBTITLE = "Agora os pacotes XPI baixados podem ser enviados ao GeckoView, com importação local, diagnóstico, controles e exportação de complementos.";

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
        new Feature("01", "Instalar XPI dos Downloads", "O arquivo XPI baixado ganhou ação Instalar XPI, que passa o pacote local ao instalador do motor Mozilla."),
        new Feature("02", "Importar do aparelho", "O seletor Android importa arquivos XPI; o Internet XP inspeciona o manifesto e o GeckoView valida assinatura e permissões."),
        new Feature("03", "Central dedicada", "Nova tela consultada diretamente do GeckoView; menu separado de ações com suporte inicial a botões e pop-ups dos complementos."),
        new Feature("04", "Diagnóstico e segurança", "Versão, ID, assinatura, permissões e motivos de desativação ajudam a investigar incompatibilidades reais."),
        new Feature("05", "Exportar e excluir", "Exportação do XPI quando o app preservou a cópia, ou JSON de diagnóstico; desinstalação real com limpeza do arquivo arquivado.")
    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
