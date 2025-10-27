import java.util.NoSuchElementException;

public class Pilha<E> {

	private Celula<E> topo;
	private Celula<E> fundo;

	public Pilha() {

		Celula<E> sentinela = new Celula<E>();
		fundo = sentinela;
		topo = sentinela;

	}

	public boolean vazia() {
		return fundo == topo;
	}

	public void empilhar(E item) {

		topo = new Celula<E>(item, topo);
	}

	public E desempilhar() {

		E desempilhado = consultarTopo();
		topo = topo.getProximo();
		return desempilhado;

	}

	public E consultarTopo() {

		if (vazia()) {
			throw new NoSuchElementException("Nao ha nenhum item na pilha!");
		}

		return topo.getItem();

	}

	public Pilha<E> subPilha(int numItens) {

		if (numItens < 0) {
			throw new IllegalArgumentException("O numero de itens deve ser positivo.");
		}

		Pilha<E> sub = new Pilha<E>();
		Pilha<E> auxiliar = new Pilha<E>();

		Celula<E> atual = this.topo;
		int count = 0;

		while (atual != this.fundo && count < numItens) {
			if (atual.getItem() == null) {
				break;
			}

			auxiliar.empilhar(atual.getItem());
			atual = atual.getProximo();
			count++;
		}

		if (count < numItens) {
			throw new IllegalArgumentException("A pilha original nao contem " + numItens + " elementos.");
		}

		while (!auxiliar.vazia()) {
			sub.empilhar(auxiliar.desempilhar());
		}

		return sub;
	}
}