package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.23";
    public static final int VERSION_CODE = 24;
    public static final String HEADLINE = "Downloads agora são do próprio Internet XP.";
    public static final String SUBTITLE = "Novo gerenciador independente do Android e opção para tornar o Internet XP seu navegador padrão.";

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
        new Feature("01", "Gerenciador de downloads próprio", "O Internet XP grava os arquivos diretamente, com progresso e cancelamento, sem delegar transferências ao DownloadManager do Android."),
        new Feature("02", "Download pelo GeckoView", "Ao receber um arquivo, o aplicativo utiliza os dados da própria conexão do navegador, evitando abrir outro navegador ou repetir o pedido de login."),
        new Feature("03", "Abrir, compartilhar e salvar cópia", "Arquivos concluídos podem ser abertos, compartilhados ou exportados para outra pasta escolhida pelo usuário."),
        new Feature("04", "Internet XP como navegador", "O Android agora pode identificar o Internet XP para abrir links HTTP/HTTPS, com atalho para solicitar a função de navegador padrão."),
        new Feature("05", "Histórico preservado e migração segura", "Downloads antigos não são confundidos com arquivos do novo gerenciador; a lista permite reiniciar a transferência quando necessário.")
    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
