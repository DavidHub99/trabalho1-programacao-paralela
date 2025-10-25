import java.util.Random;
import java.util.Scanner;

public class Distribuidor {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        // 1) Gera vetor aleatório
        int tamanho = 1000000; 
        byte[] vetor = gerarVetorAleatorio(tamanho);

        // 2) Pergunta ao usuário
        System.out.println("Deseja procurar um valor existente (-100 a 100) ou inexistente (111)?");
        System.out.println("Digite 'E' para existente ou 'I' para inexistente:");
        String opcao = scanner.nextLine().trim().toUpperCase();

        // 3) Determina valor de busca
        int valorBusca;
        if (opcao.equals("E")) {
            // escolhe posição aleatória e pega valor do vetor
            int posicao = random.nextInt(tamanho);
            valorBusca = vetor[posicao];
            System.out.println("Valor escolhido (existente): " + valorBusca);
        } else {
            valorBusca = 111; // inexistente
            System.out.println("Valor escolhido (inexistente): " + valorBusca);
        }

        // 4) Divide vetor em 3 partes
        byte[][] partes = dividirVetor(vetor, 3);

        // 5) Exibe informações sobre as partes (como se fosse preparar para envio)
        for (int i = 0; i < partes.length; i++) {
            System.out.println("\n--- Parte " + (i + 1) + " ---");
            System.out.println("Tamanho: " + partes[i].length);
            System.out.println("Valor de busca: " + valorBusca);

        }

        scanner.close();
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

    // Divide o vetor em 'partes' iguais (última pode ser ligeiramente maior)
    public static byte[][] dividirVetor(byte[] vetor, int partes) {
        int tamanho = vetor.length;
        int tamanhoParte = tamanho / partes;
        byte[][] resultado = new byte[partes][];

        for (int i = 0; i < partes; i++) {
            int inicio = i * tamanhoParte;
            int fim = (i == partes - 1) ? tamanho : (inicio + tamanhoParte);
            int tamanhoAtual = fim - inicio;

            resultado[i] = new byte[tamanhoAtual];
            System.arraycopy(vetor, inicio, resultado[i], 0, tamanhoAtual);
        }

        return resultado;
    }
}
