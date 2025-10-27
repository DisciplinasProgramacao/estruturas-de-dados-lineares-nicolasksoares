import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

public class App {

    /** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados = "produtos.txt";

    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Vetor de produtos cadastrados */
    static Produto[] produtosCadastrados;

    /** Quantidade de produtos cadastrados atualmente no vetor */
    static int quantosProdutos = 0;

    /** Fila de pedidos (Armazenamento FIFO por ordem de chegada) */
    static Fila<Pedido> filaPedidos = new Fila<>();

    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho() {
        System.out.println("AEDs II COMERCIO DE COISINHAS");
        System.out.println("=============================");
    }

    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {

        T valor;

        System.out.println(mensagem);
        try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        return valor;
    }

    /** Imprime o menu principal, lê a opção do usuário e a retorna (int).
     * @return Um inteiro com a opção do usuário.
     */
    static int menu() {
        cabecalho();
        System.out.println("1 - Listar todos os produtos");
        System.out.println("2 - Procurar por um produto, por codigo");
        System.out.println("3 - Procurar por um produto, por nome");
        System.out.println("4 - Iniciar novo pedido");
        System.out.println("5 - Fechar pedido");
        System.out.println("6 - Listar produtos dos pedidos mais recentes");
        // Adicionando opcoes opcionais para testar a Fila (Tarefas 2, 3 e 4)
        System.out.println("7 - Valor Medio dos N primeiros pedidos (ValorFinal)");
        System.out.println("8 - Filtrar pedidos com valor > X (N primeiros)");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opcao: ");
        return Integer.parseInt(teclado.nextLine());
    }

    /**
     * Lê os dados de um arquivo-texto e retorna um vetor de produtos.
     * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
     * @return Um vetor com os produtos carregados, ou vazio em caso de problemas de leitura.
     */
    static Produto[] lerProdutos(String nomeArquivoDados) {

        Scanner arquivo = null;
        int numProdutos;
        String linha;
        Produto produto;
        Produto[] produtosCadastrados;

        try {
            arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));

            numProdutos = Integer.parseInt(arquivo.nextLine());
            produtosCadastrados = new Produto[numProdutos];

            for (int i = 0; i < numProdutos; i++) {
                linha = arquivo.nextLine();
                produto = Produto.criarDoTexto(linha);
                produtosCadastrados[i] = produto;
            }
            quantosProdutos = numProdutos;

        } catch (IOException excecaoArquivo) {
            produtosCadastrados = null;
        } finally {
            if (arquivo != null) {
                arquivo.close();
            }
        }

        return produtosCadastrados;
    }

    /** Localiza um produto no vetor de produtos cadastrados, a partir do código de produto informado pelo usuário, e o retorna.
     * Em caso de não encontrar o produto, retorna null
     */
    static Produto localizarProduto() {

        Produto produto = null;
        Boolean localizado = false;

        cabecalho();
        System.out.println("Localizando um produto...");
        Integer idProduto = lerOpcao("Digite o codigo identificador do produto desejado: ", Integer.class);
        if (idProduto == null) return null;

        for (int i = 0; (i < quantosProdutos && !localizado); i++) {
            if (produtosCadastrados[i].hashCode() == idProduto) {
                produto = produtosCadastrados[i];
                localizado = true;
            }
        }

        return produto;
    }

    /** Localiza um produto no vetor de produtos cadastrados, a partir do nome de produto informado pelo usuário, e o retorna.
     * A busca não é sensível ao caso. Em caso de não encontrar o produto, retorna null
     * @return O produto encontrado ou null, caso o produto não tenha sido localizado no vetor de produtos cadastrados.
     */
    static Produto localizarProdutoDescricao() {

        Produto produto = null;
        Boolean localizado = false;
        String descricao;

        cabecalho();
        System.out.println("Localizando um produto...");
        System.out.println("Digite o nome ou a descricao do produto desejado:");
        descricao = teclado.nextLine();
        for (int i = 0; (i < quantosProdutos && !localizado); i++) {
            if (produtosCadastrados[i].descricao.equalsIgnoreCase(descricao)) {
                produto = produtosCadastrados[i];
                localizado = true;
            }
        }

        return produto;
    }

    private static void mostrarProduto(Produto produto) {

        cabecalho();
        String mensagem = "Dados invalidos para o produto ou produto nao encontrado!";

        if (produto != null){
            mensagem = String.format("Dados do produto:\n%s", produto);
        }

        System.out.println(mensagem);
    }

    /** Lista todos os produtos cadastrados, numerados, um por linha */
    static void listarTodosOsProdutos() {

        cabecalho();
        System.out.println("\nPRODUTOS CADASTRADOS:");
        for (int i = 0; i < quantosProdutos; i++) {
            System.out.println(String.format("%02d - %s", (i + 1), produtosCadastrados[i].toString()));
        }
    }

    /** * Inicia um novo pedido.
     * Permite ao usuário escolher e incluir produtos no pedido.
     * @return O novo pedido
     */
    public static Pedido iniciarPedido() {

        Integer formaPagamento = lerOpcao("Digite a forma de pagamento do pedido (1 para a vista e 2 para a prazo)", Integer.class);
        if (formaPagamento == null || (formaPagamento != 1 && formaPagamento != 2)) {
            System.out.println("Forma de pagamento invalida. Pedido cancelado.");
            return null;
        }

        Pedido pedido = new Pedido(LocalDate.now(), formaPagamento);
        Produto produto;
        Integer numProdutos = lerOpcao("Quantos produtos serao incluidos no pedido?", Integer.class);
        if (numProdutos == null || numProdutos <= 0) {
            System.out.println("Nenhum produto a ser incluido. Pedido cancelado.");
            return null;
        }

        listarTodosOsProdutos();
        System.out.println("\nIncluindo produtos no pedido...");

        for (int i = 0; i < numProdutos; i++) {
            produto = localizarProdutoDescricao();
            if (produto == null) {
                System.out.println("Produto nao encontrado. Tentando novamente.");
                i--;
            } else {
                if(pedido.incluirProduto(produto)) {
                    System.out.println("Produto incluido: " + produto.descricao);
                } else {
                    System.out.println("Limite maximo de produtos por pedido atingido.");
                    break;
                }
            }
        }

        return pedido;
    }

    /**
     * Finaliza um pedido, momento no qual ele deve ser armazenado em uma fila de pedidos (FIFO).
     * @param pedido O pedido que deve ser finalizado.
     */
    public static void finalizarPedido(Pedido pedido) {

        if (pedido == null || pedido.getQuantosProdutos() == 0) {
            System.out.println("Nenhum pedido valido para finalizar.");
            return;
        }

        filaPedidos.inserir(pedido); // Adiciona o pedido na fila
        System.out.println("--- PEDIDO FINALIZADO (ID: " + pedido.getIdPedido() + ") ---");
        System.out.println("Pedido adicionado a fila de processamento.");
    }

    /**
     * Lista os produtos dos pedidos mais recentes (na verdade, os que estao no inicio da fila,
     * pois o processamento de pedidos normalmente segue o FIFO).
     * Aqui, vamos remover e exibir o pedido mais antigo na fila.
     */
    public static void listarProdutosPedidosRecentes() {

        System.out.println("\n--- PROCESSANDO PEDIDO MAIS ANTIGO DA FILA ---\n");

        try {
            Pedido pedidoProcessado = filaPedidos.remover();
            System.out.println(pedidoProcessado.toString());
            System.out.println("\n*** Pedido removido da fila para processamento/entrega. ***");
        } catch (NoSuchElementException e) {
            System.out.println("A fila de pedidos esta vazia.");
        }
    }

    // --- IMPLEMENTACOES ADICIONAIS PARA TESTAR A CLASSE FILA (TAREFAS 2, 3 e 4) ---

    /**
     * Opcao 7: Calcula e exibe o valor total medio dos N primeiros pedidos na fila.
     */
    public static void calcularValorMedioPedidos() {
        Integer N = lerOpcao("Digite a quantidade de primeiros pedidos (N) para calcular o valor medio:", Integer.class);
        if (N == null || N <= 0) return;

        try {
            // Usa o metodo calcularValorMedio da Fila.
            // A funcao extratora extrai o valorFinal (Double) de cada Pedido.
            double valorMedio = filaPedidos.calcularValorMedio(Pedido::valorFinal, N);

            System.out.printf("\n--- VALOR MEDIO DOS %d PRIMEIROS PEDIDOS: R$ %.2f ---\n", N, valorMedio);

        } catch (IllegalArgumentException e) {
            System.out.println("ERRO: " + e.getMessage());
        }
    }

    /**
     * Opcao 8: Filtra e exibe os N primeiros pedidos com valor total acima de um determinado valor (X).
     */
    public static void filtrarPedidosPorValor() {
        Integer N = lerOpcao("Digite a quantidade de primeiros pedidos (N) a serem verificados:", Integer.class);
        if (N == null || N <= 0) return;

        Double valorMinimo = lerOpcao("Digite o valor minimo (X) para a filtragem:", Double.class);
        if (valorMinimo == null) return;

        // Predicado: Condicao que testa se o valor final do Pedido e maior que o valorMinimo (X).
        Predicate<Pedido> condicao = p -> p.valorFinal() > valorMinimo;

        // Filtra os pedidos e recebe uma nova fila.
        Fila<Pedido> pedidosFiltrados = filaPedidos.filtrar(condicao, N);

        System.out.printf("\n--- PEDIDOS FILTRADOS (Valor > R$ %.2f entre os %d primeiros) ---\n", valorMinimo, N);

        if (pedidosFiltrados.vazia()) {
            System.out.println("Nenhum pedido atende ao criterio.");
            return;
        }

        // Para listar os pedidos na fila filtrada, removemos um por um.
        int count = 0;
        while (!pedidosFiltrados.vazia()) {
            System.out.println("\nPedido " + (++count) + ":");
            System.out.println(pedidosFiltrados.remover().toString());
        }
    }

    // --- MAIN METHOD ---
    public static void main(String[] args) {

        teclado = new Scanner(System.in, Charset.forName("UTF-8"));

        produtosCadastrados = lerProdutos(nomeArquivoDados);

        Pedido pedido = null;

        int opcao = -1;

        do{
            opcao = menu();
            switch (opcao) {
                case 1 -> listarTodosOsProdutos();
                case 2 -> mostrarProduto(localizarProduto());
                case 3 -> mostrarProduto(localizarProdutoDescricao());
                case 4 -> pedido = iniciarPedido();
                case 5 -> finalizarPedido(pedido);
                case 6 -> listarProdutosPedidosRecentes();
                case 7 -> calcularValorMedioPedidos(); // Opcao extra
                case 8 -> filtrarPedidosPorValor();    // Opcao extra
            }
            if (opcao != 0) {
                pausa();
            }
        }while(opcao != 0);

        teclado.close();
    }
}