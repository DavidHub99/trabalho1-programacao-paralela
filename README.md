1. Explicação Detalhada do Funcionamento
O seu sistema é um exemplo clássico de Programação Paralela e Distribuída que utiliza o modelo Cliente-Servidor para resolver um problema de processamento de dados em paralelo.
A. Componentes de Comunicação (Classes Comunicado, Pedido, Resposta, ComunicadoEncerramento)
Para que o cliente e o servidor possam trocar informações complexas (como o vetor de bytes), eles utilizam a serialização de objetos em Java via ObjectOutputStream e ObjectInputStream.
Comunicado: É a classe base que implementa a interface Serializable. Isso garante que qualquer objeto que herde dela possa ser convertido em uma sequência de bytes para ser transmitido pela rede e depois reconstruído no destino.
Pedido: É o objeto que o ClienteDistribuidor envia para o ServidorReceptor. Ele encapsula os dados necessários para o trabalho:
byte[] numeros: A sub-parte do vetor que o servidor deve processar.
int procurado: O valor que deve ser contado.
Resposta: É o objeto que o ServidorReceptor envia de volta para o cliente. Ele contém o resultado do processamento:
Integer contagem: O número de ocorrências do valor encontrado na sub-parte do vetor.
ComunicadoEncerramento: É um sinal especial enviado pelo cliente para indicar que a comunicação terminou e que o servidor pode fechar a conexão atual e voltar a aguardar novas conexões.
B. O Lado do Servidor (ServidorReceptor.java)
O servidor atua como um processador de trabalho que espera por requisições:
Inicialização: O servidor cria um ServerSocket em uma porta específica (ex: 12345).
Aceitação de Conexão: Ele entra em um loop infinito (while(true)) aguardando que um cliente se conecte através do método serverSocket.accept().
Comunicação Persistente: Ao aceitar uma conexão, ele associa um ObjectInputStream e um ObjectOutputStream à essa conexão.
Processamento do Pedido:
O servidor recebe um objeto. Se for um Pedido, ele extrai o vetor parcial e o valor de busca.
Paralelismo Interno: Para otimizar a contagem, o servidor utiliza um ExecutorService (um pool de threads). Ele divide o vetor parcial recebido em sub-partes menores (o número de divisões é igual ao número de núcleos de processador disponíveis na máquina) e atribui a contagem de cada sub-parte a uma thread separada.
Soma dos Resultados: O servidor aguarda o término de todas as threads e soma os resultados parciais para obter a contagem total daquele 1/3 do vetor.
Envio da Resposta: O servidor encapsula a contagem total em um objeto Resposta e o envia de volta ao cliente.
Encerramento: Se o objeto recebido for um ComunicadoEncerramento, o servidor fecha a conexão atual e volta para o passo 2, aguardando uma nova conexão.
C. O Lado do Cliente (ClienteDistribuidor.java)
O cliente atua como um coordenador que divide o trabalho e agrega os resultados:
Preparação de Dados: O cliente gera o vetor grande e o valor de busca.
Divisão do Trabalho: O vetor é dividido em 3 partes iguais (byte[][] partes).
Distribuição Paralela:
Para cada um dos 3 servidores, o cliente cria uma ThreadServidor dedicada.
A ThreadServidor é responsável por estabelecer a conexão TCP/IP com o servidor, enviar a sub-parte do vetor (o Pedido) e esperar pela Resposta.
Sincronização e Agregação:
O thread principal do ClienteDistribuidor inicia as três ThreadServidor e, em seguida, chama o método thread.join() para cada uma. O join() é crucial, pois ele bloqueia a execução do cliente até que aquela thread específica termine seu trabalho (ou seja, até que a resposta do servidor seja recebida).
Após todas as threads terminarem, o cliente itera sobre elas, somando as contagens parciais para obter o resultado final.
Medição de Desempenho: O tempo de execução é medido desde o início da distribuição até a agregação final dos resultados.
Finalização: O cliente envia um ComunicadoEncerramento para cada servidor, garantindo que as conexões sejam fechadas de forma limpa.