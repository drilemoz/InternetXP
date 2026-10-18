package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.20";
    public static final int VERSION_CODE = 21;
    public static final String HEADLINE = "Mais controle sobre sua privacidade.";
    public static final String SUBTITLE = "Abas privadas, informações de segurança verificadas pelo GeckoView e novas ferramentas para proteger seus dados.";

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
        new Feature("01", "Abas privadas", "Navegue em abas privadas separadas das normais, sem salvar histórico, sugestões ou restauração dessas páginas."),
        new Feature("02", "Limpeza real de dados", "Apague cookies, sessões de login, cache, permissões ou todos os dados do GeckoView após fechar as abas."),
        new Feature("03", "Segurança mais confiável", "O cadeado agora depende da verificação real da conexão pelo GeckoView, não apenas do prefixo HTTPS."),
        new Feature("04", "Proteção contra rastreamento", "Ative ou desative a proteção do GeckoView e consulte as permissões armazenadas para o site atual."),
        new Feature("05", "Atenção aos downloads", "Arquivos baixados em abas privadas permanecem visíveis ao Android. Um aviso explicita essa limitação.")
    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
