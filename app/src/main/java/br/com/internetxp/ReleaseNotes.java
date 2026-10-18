package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.30";
    public static final int VERSION_CODE = 31;
    public static final String HEADLINE = "Extensões mais compactas e diálogos com identidade XP.";
    public static final String SUBTITLE = "Central de extensões em cartões horizontais, janelas reutilizáveis com identidade própria e barra inferior maior para facilitar os toques.";

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
        new Feature("01", "Importar lado a lado", "Importar XPI, instalar por HTTPS e acessar o catálogo Mozilla agora ocupam uma única faixa horizontal."),
        new Feature("02", "Comandos horizontais", "Ativar ou desativar, diagnóstico, exportação e exclusão compartilham uma linha em cada cartão instalado."),
        new Feature("03", "Diálogos XP", "Janelas convencionais do navegador usam um construtor de estilo comum, com cabeçalho azul, cantos arredondados e ações padronizadas."),
        new Feature("04", "Navegação ampliada", "Barra inferior com símbolos, ícone de downloads e textos mais visíveis, respeitando a largura do aparelho."),
        new Feature("05", "Teste de funcionamento", "A central lembra que uma extensão ativada precisa ser testada em uma página real para comprovar sua função.")
    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
