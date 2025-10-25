import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Receptor {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        //  (substitua depois pela leitura via socket)
        byte[] vetor = gerarVetorExemplo(1000000); 
        int valorBusca = 25; 

        System.out.println("Valor a ser procurado: " + valorBusca);

        // Chama função que conta com múltiplas threads
        int totalOcorrencias = contarOcorrenciasParalelo(vetor, valorBusca);

        System.out.println("\nTotal de ocorrências do valor " + valorBusca + ": " + totalOcorrencias);
    }

    
    //  Função principal da contagem
    public static int contarOcorrenciasParalelo(byte[] vetor, int valorBusca)
            throws InterruptedException, ExecutionException {

        int numProcessadores = Runtime.getRuntime().availableProcessors();
        System.out.println("Processadores disponíveis: " + numProcessadores);

        int numThreads = numProcessadores; // usar uma thread por núcleo 
        int tamanhoParte = vetor.length / numThreads;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Integer>> resultados = new ArrayList<>();

        // Divide o vetor entre as threads
        for (int i = 0; i < numThreads; i++) {
            int inicio = i * tamanhoParte;
            int fim = (i == numThreads - 1) ? vetor.length : (inicio + tamanhoParte);

            byte[] subVetor = new byte[fim - inicio];
            System.arraycopy(vetor, inicio, subVetor, 0, fim - inicio);

            // Envia a tarefa para o pool
            Callable<Integer> tarefa = () -> contarOcorrencias(subVetor, valorBusca);
            resultados.add(executor.submit(tarefa));
        }

        // Aguarda todas as threads e soma os resultados
        int total = 0;
        for (Future<Integer> futuro : resultados) {
            total += futuro.get();
        }

        executor.shutdown();

        return total;
    }

    
    //  Função que conta ocorrências em um pedaço do vetor
    public static int contarOcorrencias(byte[] vetor, int valorBusca) {
        int contador = 0;
        for (byte b : vetor) {
            if (b == (byte) valorBusca) {
                contador++;
            }
        }
        return contador;
    }

    
    // 🔹 Função auxiliar para gerar um vetor de exemplo
    public static byte[] gerarVetorExemplo(int tamanho) {
        byte[] vetor = new byte[tamanho];
        for (int i = 0; i < tamanho; i++) {
            vetor[i] = (byte) ((Math.random() * 201) - 100); // -100 a 100
        }
        return vetor;
    }
}
