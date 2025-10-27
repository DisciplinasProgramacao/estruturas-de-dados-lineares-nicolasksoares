import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class App {

    static String nomeArquivoDados = "produtos.txt";

    static Scanner teclado;

    static Produto[] produtosCadastrados;

    static int quantosProdutos = 0;

    static Pilha<Pedido> pilhaPedidos = new Pilha<>();

    static Fila<Pedido> filaPedidos = new Fila<>();

    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

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

    static int menu() {
        cabecalho();
        System.out.println("1 - Listar todos os produtos");
        System.out.println("2 - Procurar por um produto, por codigo");
        System.out.println("3 - Procurar por um produto, por nome");
        System.out.println("4 - Iniciar novo pedido");
        System.out.println("5 - Fechar pedido (LIFO - Pilha)");
        System.out.println("6 - Listar produtos dos pedidos mais recentes (Pilha)");
        System.out.println("7 - Finalizar e Enfileirar Pedido (FIFO - Fila)");
        System.out.println("8 - Processar Pedido Mais Antigo (Fila)");
        System.out.println("9 - Valor Medio dos N primeiros pedidos (Fila)");
        System.out.println("10 - Filtrar pedidos com valor > X (Fila)");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opcao: ");
        return Integer.parseInt(teclado.nextLine());
    }

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

    static void listarTodosOsProdutos() {

        cabecalho();
        System.out.println("\nPRODUTOS CADASTRADOS:");
        for (int i = 0; i < quantosProdutos; i++) {
            System.out.println(String.format("%02d - %s", (i + 1), produtosCadastrados[i].toString()));
        }
    }

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

    // Método 5: Pilha LIFO (Projeto 2A - Finalizar e Empilhar)
    public static void finalizarPedido(Pedido pedido) {

        if (pedido == null || pedido.getQuantosProdutos() == 0) {
            System.out.println("Nenhum pedido valido para finalizar.");
            return;
        }

        pilhaPedidos.empilhar(pedido);
        System.out.println("--- PEDIDO FINALIZADO (ID: " + pedido.getIdPedido() + ") ---");
        System.out.println("Pedido adicionado a PILHA (LIFO) de pedidos recentes.");
    }

    // Método 6: Pilha LIFO (Projeto 2A - Listar)
    public static void listarProdutosPedidosRecentes() {

        cabecalho();
        System.out.println("\n--- LISTANDO PRODUTOS DOS PEDIDOS MAIS RECENTES (PILHA) ---\n");

        Integer numPedidos = lerOpcao("Quantos pedidos mais recentes (N) deseja consultar?", Integer.class);
        if (numPedidos == null || numPedidos <= 0) return;

        if (pilhaPedidos.vazia()) {
            System.out.println("A pilha de pedidos esta vazia.");
            return;
        }

        try {
            Pilha<Pedido> pedidosRecentes = pilhaPedidos.subPilha(numPedidos);

            int count = 0;
            while (!pedidosRecentes.vazia()) {
                Pedido p = pedidosRecentes.desempilhar();
                System.out.println("\nPedido ID: " + p.getIdPedido() + " - Valor: R$ " + String.format("%.2f", p.valorFinal()));

                Produto[] produtos = p.getProdutos();
                for (int i = 0; i < p.getQuantosProdutos(); i++) {
                    System.out.println("   - " + produtos[i].toString());
                }
                count++;
            }
            if (count > 0) {
                System.out.println("\nTotal de " + count + " pedidos recentes listados. A pilha original permanece intacta.");
            }

        } catch (IllegalArgumentException e) {
            System.out.println("ERRO: " + e.getMessage());
        } catch (NoSuchElementException e) {
            System.out.println("ERRO: Ocorreu um problema ao desempilhar. " + e.getMessage());
        }
    }

    // Método 7: Fila FIFO (Projeto 2B - Finalizar e Enfileirar)
    public static void finalizarPedidoFila(Pedido pedido) {

        if (pedido == null || pedido.getQuantosProdutos() == 0) {
            System.out.println("Nenhum pedido valido para finalizar.");
            return;
        }

        filaPedidos.inserir(pedido);
        System.out.println("--- PEDIDO FINALIZADO (ID: " + pedido.getIdPedido() + ") ---");
        System.out.println("Pedido adicionado a FILA (FIFO) de processamento.");
    }

    // Método 8: Fila FIFO (Projeto 2B - Processar)
    public static void processarPedidoFila() {

        System.out.println("\n--- PROCESSANDO PEDIDO MAIS ANTIGO DA FILA (FIFO) ---\n");

        try {
            Pedido pedidoProcessado = filaPedidos.remover();
            System.out.println(pedidoProcessado.toString());
            System.out.println("\n*** Pedido removido da fila para processamento/entrega. ***");
        } catch (NoSuchElementException e) {
            System.out.println("A fila de pedidos esta vazia.");
        }
    }

    // Método 9: Fila Avançado (Projeto 2B - Valor Médio)
    public static void calcularValorMedioPedidosFila() {
        Integer N = lerOpcao("Digite a quantidade de primeiros pedidos (N) para calcular o valor medio:", Integer.class);
        if (N == null || N <= 0) return;

        try {
            double valorMedio = filaPedidos.calcularValorMedio(Pedido::valorFinal, N);

            System.out.printf("\n--- VALOR MEDIO DOS %d PRIMEIROS PEDIDOS: R$ %.2f ---\n", N, valorMedio);

        } catch (IllegalArgumentException e) {
            System.out.println("ERRO: " + e.getMessage());
        }
    }

    // Método 10: Fila Avançado (Projeto 2B - Filtrar)
    public static void filtrarPedidosPorValorFila() {
        Integer N = lerOpcao("Digite a quantidade de primeiros pedidos (N) a serem verificados:", Integer.class);
        if (N == null || N <= 0) return;

        Double valorMinimo = lerOpcao("Digite o valor minimo (X) para a filtragem:", Double.class);
        if (valorMinimo == null) return;

        Predicate<Pedido> condicao = p -> p.valorFinal() > valorMinimo;

        Fila<Pedido> pedidosFiltrados = filaPedidos.filtrar(condicao, N);

        System.out.printf("\n--- PEDIDOS FILTRADOS (Valor > R$ %.2f entre os %d primeiros) ---\n", valorMinimo, N);

        if (pedidosFiltrados.vazia()) {
            System.out.println("Nenhum pedido atende ao criterio.");
            return;
        }

        int count = 0;
        while (!pedidosFiltrados.vazia()) {
            System.out.println("\nPedido " + (++count) + ":");
            System.out.println(pedidosFiltrados.remover().toString());
        }
    }


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
                case 7 -> finalizarPedidoFila(pedido);
                case 8 -> processarPedidoFila();
                case 9 -> calcularValorMedioPedidosFila();
                case 10 -> filtrarPedidosPorValorFila();
            }
            if (opcao != 0) {
                pausa();
            }
        }while(opcao != 0);

        teclado.close();
    }
}
