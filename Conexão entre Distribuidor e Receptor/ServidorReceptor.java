import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ServidorReceptor {
    // A porta será lida dos argumentos de linha de comando
    private static int PORTA_FIXA = 12345;

    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                PORTA_FIXA = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("[R] Argumento inválido. Usando porta padrão: " + PORTA_FIXA);
            }
        }
        System.out.println("[R] Servidor Receptor iniciado na porta " + PORTA_FIXA);
        try (ServerSocket serverSocket = new ServerSocket(PORTA_FIXA)) {
            while (true) {
                System.out.println("[R] Aguardando conexão de cliente...");
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("[R] Conexão estabelecida com " + clientSocket.getInetAddress().getHostAddress());
                    processarConexao(clientSocket);
                } catch (IOException e) {
                    System.err.println("[R] Erro ao aceitar ou processar conexão: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[R] Não foi possível iniciar o ServerSocket: " + e.getMessage());
        }
    }

    private static void processarConexao(Socket clientSocket) {
        try (ObjectOutputStream transmissor = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream receptor = new ObjectInputStream(clientSocket.getInputStream())) {

            // Loop de leitura de objetos do cliente
            while (true) {
                Object objetoRecebido = receptor.readObject();
                
                if (objetoRecebido instanceof Pedido) {
                    Pedido pedido = (Pedido) objetoRecebido;
                    System.out.println("[R] Pedido recebido do cliente " + clientSocket.getInetAddress().getHostAddress() + 
                                       ". Tamanho do vetor: " + pedido.getNumeros().length + 
                                       ", Valor de busca: " + pedido.getProcurado());

                    // Contagem paralela
                    int contagem = contarOcorrenciasParalelo(pedido.getNumeros(), pedido.getProcurado());

                    // Envia Resposta
                    Resposta resposta = new Resposta(contagem);
                    transmissor.writeObject(resposta);
                    transmissor.flush();
                    System.out.println("[R] Resposta enviada. Contagem: " + contagem);

                } else if (objetoRecebido instanceof ComunicadoEncerramento) {
                    System.out.println("[R] ComunicadoEncerramento recebido. Fechando conexão e voltando a aceitar novas.");
                    break; // Sai do loop e fecha a conexão 
                } else {
                    System.err.println("[R] Objeto não reconhecido recebido: " + objetoRecebido.getClass().getName());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            // Se o cliente fechar a conexão abruptamente, ObjectInputStream.readObject() lança uma IOException
            System.out.println("[R] Conexão encerrada ou erro de leitura/escrita: " + e.getMessage());
        }
    }
    
    // Implementação da contagem paralela 
    public static int 
    contarOcorrenciasParalelo(byte[] vetor, int valorBusca) {
        int numProcessadores = Runtime.getRuntime().availableProcessors();
        int numThreads = numProcessadores;
        int tamanhoParte = vetor.length / numThreads;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Integer>> resultados = new ArrayList<>();

        try {
            for (int i = 0; i < numThreads; i++) {
                int inicio = i * tamanhoParte;
                int fim = (i == numThreads - 1) ? vetor.length : (inicio + tamanhoParte);


                // No entanto, para simplificar a tarefa Callable, vamos extrair o subVetor
                byte[] subVetor = new byte[fim - inicio];
                System.arraycopy(vetor, inicio, subVetor, 0, fim - inicio);

                Callable<Integer> tarefa = () -> contarOcorrencias(subVetor, valorBusca);
                resultados.add(executor.submit(tarefa));
            }

            int total = 0;
            for (Future<Integer> futuro : resultados) {
                total += futuro.get();
            }
            return total;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("[R] Erro na execução paralela: " + e.getMessage());
            return 0;
        } finally {
            executor.shutdown();
        }
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
}

