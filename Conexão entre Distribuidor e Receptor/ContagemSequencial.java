import java.util.Random;
import java.util.Scanner;

public class ContagemSequencial {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        // 1) Gera vetor aleatório
        int tamanho = 1_000_000;
        byte[] vetor = gerarVetorAleatorio(tamanho);

        // 2) Pergunta ao usuário e determina valor de busca
        System.out.println("Deseja procurar um valor existente (-100 a 100) ou inexistente (111)?");
        System.out.println("Digite 'E' para existente ou 'I' para inexistente:");
        String opcao = scanner.nextLine().trim().toUpperCase();

        int valorBusca;
        if (opcao.equals("E")) {
            int posicao = random.nextInt(tamanho);
            valorBusca = vetor[posicao];
            System.out.println("[S] Valor escolhido (existente): " + valorBusca);
        } else {
            valorBusca = 111; // inexistente
            System.out.println("[S] Valor escolhido (inexistente): " + valorBusca);
        }

        long inicio = System.currentTimeMillis();

        // 3) Contagem sequencial
        int contagem = contarOcorrencias(vetor, valorBusca);

        long fim = System.currentTimeMillis();

        System.out.println("\n[S] --- RESULTADO SEQUENCIAL ---");
        System.out.println("[S] Valor de busca: " + valorBusca);
        System.out.println("[S] Contagem total de ocorrências: " + contagem);
        System.out.printf("[S] Tempo total de execução: %.3f segundos%n", (fim - inicio) / 1000.0);

        scanner.close();
    }

    // Função que conta ocorrências em um pedaço do vetor
    public static int contarOcorrencias(byte[] vetor, int valorBusca) {
        int contador = 0;
        for (byte b : vetor) {
            if (b == (byte) valorBusca) {
                contador++;
            }
        }
        return contador;
    }

    // Gera vetor aleatório de bytes entre -100 e 100
    public static byte[] gerarVetorAleatorio(int tamanho) {
        Random random = new Random();
        byte[] vetor = new byte[tamanho];

        for (int i = 0; i < tamanho; i++) {
            int valor = random.nextInt(201) - 100; // -100 a 100
            vetor[i] = (byte) valor;
        }

        return vetor;
    }
}

