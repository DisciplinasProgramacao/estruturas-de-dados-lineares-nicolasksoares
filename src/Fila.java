import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

public class Fila<E> {

    private Celula<E> topo;
    private Celula<E> fundo;

    public Fila() {

        Celula<E> sentinela = new Celula<E>();
        fundo = sentinela;
        topo = sentinela;
    }

    public boolean vazia() {
        return fundo == topo;
    }

    public void inserir(E item) {

        fundo.setProximo(new Celula<E>(item, null));
        fundo = fundo.getProximo();
    }

    public E remover() {
        if (vazia()) {
            throw new NoSuchElementException("Nao ha nenhum item na fila para remover!");
        }

        Celula<E> removida = topo.getProximo();
        E itemRemovido = removida.getItem();

        topo.setProximo(removida.getProximo());


        if (topo.getProximo() == null) {
            fundo = topo;
        }

        return itemRemovido;
    }

    public E consultarPrimeiro() {
        if (vazia()) {
            throw new NoSuchElementException("Nao ha nenhum item na fila para consultar!");
        }

        return topo.getProximo().getItem();
    }

    /**
     * Calcula e retorna o valor medio de um atributo Double dos primeiros 'quantidade' elementos da fila.
     * @param extrator Funcao que extrai o valor numerico (Double) do elemento.
     * @param quantidade O numero de itens a considerar.
     * @return O valor medio.
     * @throws IllegalArgumentException se a fila nao contem 'quantidade' elementos.
     */
    public double calcularValorMedio(Function<E, Double> extrator, int quantidade) {
        if (quantidade <= 0) {
            return 0.0;
        }

        Celula<E> atual = this.topo.getProximo();
        double soma = 0.0;

        for (int i = 0; i < quantidade; i++) {
            if (atual == null) {
                throw new IllegalArgumentException("A fila nao contem " + quantidade + " elementos para calculo!");
            }
            soma += extrator.apply(atual.getItem());
            atual = atual.getProximo();
        }

        return soma / quantidade;
    }

    /**
     * Retorna uma nova fila contendo os primeiros 'quantidade' elementos da fila original
     * que satisfazem a 'condicional'.
     * @param condicional Funcao que testa se um elemento deve ser incluido.
     * @param quantidade O numero de primeiros elementos a submeter ao teste.
     * @return Uma nova Fila<E> com os elementos filtrados.
     */
    public Fila<E> filtrar(Predicate<E> condicional, int quantidade) {
        if (quantidade <= 0) {
            return new Fila<E>();
        }

        Fila<E> novaFila = new Fila<>();
        Celula<E> atual = this.topo.getProximo();

        for (int i = 0; i < quantidade; i++) {
            if (atual == null) {
                break;
            }

            if (condicional.test(atual.getItem())) {
                novaFila.inserir(atual.getItem());
            }

            atual = atual.getProximo();
        }

        return novaFila;
    }
}