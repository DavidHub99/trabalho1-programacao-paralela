import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class ClienteDistribuidor {

    // IPs dos servidores (hard coded conforme requisito 5.1)
    // Para teste local, usaremos localhost (127.0.0.1) e portas diferentes
    private static final String[] IPS_SERVIDORES = {"127.0.0.1", "127.0.0.1", "127.0.0.1"};
    private static final int[] PORTAS_SERVIDORES = {12345, 12346, 12347}; // Usaremos portas diferentes para simular 3 servidores na mesma máquina

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        // 1) Gera vetor aleatório
        int tamanho = 1_000_000;
        byte[] vetor = gerarVetorAleatorio(tamanho);

        // 2) Pergunta ao usuário
        System.out.println("Deseja procurar um valor existente (-100 a 100) ou inexistente (111)?");
        System.out.println("Digite 'E' para existente ou 'I' para inexistente:");
        String opcao = scanner.nextLine().trim().toUpperCase();

        // 3) Determina valor de busca
        int valorBusca;
        if (opcao.equals("E")) {
            int posicao = random.nextInt(tamanho);
            valorBusca = vetor[posicao];
            System.out.println("[D] Valor escolhido (existente): " + valorBusca);
        } else {
            valorBusca = 111; // inexistente
            System.out.println("[D] Valor escolhido (inexistente): " + valorBusca);
        }

        // 4) Divide vetor em 3 partes
        byte[][] partes = dividirVetor(vetor, IPS_SERVIDORES.length);

        // 5) Cria e inicia threads para cada servidor
        List<ThreadServidor> threads = new ArrayList<>();
        int totalServidores = IPS_SERVIDORES.length;

        long inicio = System.currentTimeMillis(); // Início da medição de tempo

        for (int i = 0; i < totalServidores; i++) {
            ThreadServidor thread = new ThreadServidor(
                IPS_SERVIDORES[i],
                PORTAS_SERVIDORES[i],
                partes[i],
                valorBusca,
                i + 1
            );
            threads.add(thread);
            thread.start();
        }

        // 6) Aguarda o término de todas as threads (requisito 5.5 - Thread.join())
        int contagemTotal = 0;
        try {
            for (ThreadServidor thread : threads) {
                thread.join();
                contagemTotal += thread.getResultadoContagem();
            }
        } catch (InterruptedException e) {
            System.err.println("[D] A thread principal foi interrompida: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        long fim = System.currentTimeMillis(); // Fim da medição de tempo

        // 7) Exibe a resposta final e o tempo de execução
        System.out.println("\n[D] --- RESULTADO FINAL ---");
        System.out.println("[D] Valor de busca: " + valorBusca);
        System.out.println("[D] Contagem total de ocorrências: " + contagemTotal);
        System.out.printf("[D] Tempo total de execução: %.3f segundos%n", (fim - inicio) / 1000.0);

        // 8) Envia ComunicadoEncerramento
        System.out.println("\n[D] Enviando ComunicadoEncerramento para os servidores...");
        for (ThreadServidor thread : threads) {
            try {
                enviarEncerramento(thread.getIp(), thread.getPorta());
            } catch (IOException e) {
                // Ignora exceções, pois o servidor pode já ter fechado a conexão
            }
        }
        
        scanner.close();
    }
    
    // Thread interna para lidar com a comunicação com um servidor
    private static class ThreadServidor extends Thread {
        private final String ip;
        private final int porta;
        private final byte[] subVetor;
        private final int valorBusca;
        private final int id;
        private int resultadoContagem = 0;

        public ThreadServidor(String ip, int porta, byte[] subVetor, int valorBusca, int id) {
            this.ip = ip;
            this.porta = porta;
            this.subVetor = subVetor;
            this.valorBusca = valorBusca;
            this.id = id;
        }

        public String getIp() {
            return ip;
        }

        public int getPorta() {
            return porta;
        }

        public int getResultadoContagem() {
            return resultadoContagem;
        }

        @Override
        public void run() {
            try (Socket socket = new Socket(ip, porta);
                 ObjectOutputStream transmissor = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream receptor = new ObjectInputStream(socket.getInputStream())) {

                System.out.println("[D] Thread " + id + ": Conectada ao servidor em " + ip + ":" + porta);

                // Envia Pedido
                Pedido pedido = new Pedido(subVetor, valorBusca);
                transmissor.writeObject(pedido);
                transmissor.flush();
                System.out.println("[D] Thread " + id + ": Pedido enviado (Tamanho: " + subVetor.length + ")");

                // Recebe Resposta
                Object objetoRecebido = receptor.readObject();
                if (objetoRecebido instanceof Resposta) {
                    Resposta resposta = (Resposta) objetoRecebido;
                    resultadoContagem = resposta.getContagem();
                    System.out.println("[D] Thread " + id + ": Resposta recebida. Contagem: " + resultadoContagem);
                } else {
                    System.err.println("[D] Thread " + id + ": Objeto inesperado recebido: " + objetoRecebido.getClass().getName());
                }

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("[D] Thread " + id + ": Erro de comunicação com o servidor " + ip + ":" + porta + ": " + e.getMessage());
            }
        }
    }

    // Função para enviar o ComunicadoEncerramento
    private static void enviarEncerramento(String ip, int porta) throws IOException {
        // Cria uma nova conexão apenas para enviar o encerramento, pois a thread original já fechou
        try (Socket socket = new Socket(ip, porta);
             ObjectOutputStream transmissor = new ObjectOutputStream(socket.getOutputStream())) {
            
            transmissor.writeObject(new ComunicadoEncerramento());
            transmissor.flush();
            System.out.println("[D] ComunicadoEncerramento enviado para " + ip + ":" + porta);
        }
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

