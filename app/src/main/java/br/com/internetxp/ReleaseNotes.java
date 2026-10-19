package br.com.internetxp;

/** Fonte única dos destaques exibidos após a instalação/atualização.
 * Atualizar junto de TODA mudança do versionCode e do CHANGELOG.
 */
public final class ReleaseNotes {
    public static final String VERSION_NAME = "1.0.34";
    public static final int VERSION_CODE = 35;
    public static final String HEADLINE = "Barra XP refinada e capturas exportáveis.";
    public static final String SUBTITLE = "Ícones proporcionais, progresso alinhado, exportação PNG e sugestões com proteção adicional.";

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
        new Feature("01", "Navegação inferior uniforme", "Ícones vetoriais de Voltar, Avançar, Abas, Início e Menu na mesma escala visual do botão Baixar."),
        new Feature("02", "Rodapé mais preciso", "Barra de progresso real agora termina exatamente antes do atalho Extensões em diferentes tamanhos de tela."),
        new Feature("03", "Salvar capturas onde quiser", "Após capturar um site, além de compartilhar a imagem é possível exportar PNG para uma pasta escolhida pelo Android."),
        new Feature("04", "Edição e sugestões mais seguras", "Evita reabrir sugestões depois de perder o foco, informa falhas de navegação e só oferece Colar com conteúdo na área de transferência."),
        new Feature("05", "Segurança ao editar endereços", "Botão Segurança fecha o teclado antes de abrir detalhes da conexão, evitando conflitos visuais.")
    };

    private ReleaseNotes() { }

    public static boolean shouldShow(int lastSeenCode) {
        return lastSeenCode < VERSION_CODE;
    }
}
