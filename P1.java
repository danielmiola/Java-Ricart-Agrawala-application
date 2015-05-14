/**
 * Autores:
 * Alexandre Braga Saldanha - R.A.: 408484
 * Daniel Ramos Miola - R.A.: 438340
 **/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.ArrayList;
import FilaProcessos.*;

public class P1 {

    private static Clock clock;
	private static int pid;
	private static int porta;
	private static ArrayList<TreeSet<Processo>> fila;
	private static int recursos[];
	private static int oks[];

    public static void main(String[] args) throws IOException {

        pid = 1;
		porta = 60011;
		clock = new Clock(0, 1);
		recursos = new int[5];
		oks = new int[5];

		fila = new ArrayList<TreeSet<Processo>>();

		for(int i = 0; i < 5; i++) recursos[i] = -2;
		for(int i = 0; i < 5; i++) oks[i] = 0;
		for(int i = 0; i < 5; i++){
			TreeSet<Processo> ts = new TreeSet<Processo>(new ProcessComparator());
			fila.add(ts);
		}
		
        server();
        try{ 
            Thread.sleep(10000); 
        }catch(Exception e){}
		
		// envio de mensagem, incrementa o clock
		clock.add();
		
		//envia mensagem pedindo acesso ao recurso 0
		int tempoAtual = clock.getTime();
		recursos[0] = tempoAtual; //deseja acessar recurso
		
		sendMessageMulticast(0, 0, tempoAtual);

		//envia mensagem pedindo acesso ao recurso 2
		
		
		// envio de mensagem, incrementa o clock
		clock.add();
		
		tempoAtual = clock.getTime();
		
		recursos[2] = tempoAtual; //deseja acessar recurso
		
		sendMessageMulticast(2, 0, tempoAtual);	
    }

    /**
	 * sendMessageMulticast()
	 * Envia uma mensagem em multicast para o grupo requisitando acesso ao recurso(0 a 4)
	 * @param recurso O número do recurso que deseja ser acessado
	 * @param tipo O tipo da mensagem (0 = requisicao de acesso, 1 = ok)
	 * @param time O tempo do relógio do processo no momento de envio do multicast
	 */
	public static void sendMessageMulticast(int recurso, int tipo, int time) {
		System.out.println("Solicitacao do recurso " + recurso + " pelo processo no tempo " + time);
		client(60012, recurso, tipo, time);
		client(60013, recurso, tipo, time);
	}
	
	/**
	 * client()
	 * Thread para envio de mensagens
	 * @param destino A porta de destino da mensagem
	 * @param recurso O número do recurso que deseja ser acessado
	 * @param tipo O tipo da mensagem (0 = requisicao de acesso, 1 = ok)
	 * @param time O tempo do relógio do processo no momento de envio do multicast
	 */
	public synchronized static void client(final int destino, final int recurso, final int tipo, final int time) {
        (new Thread() {
            @Override
            public void run() {
                try {
                    String envio;
					
					/* Monta a mensagem a ser enviada, contendo o número do recurso a ser acessado e o tempo de relógio
					 * do processo no momento do envio */
					
					// requisicao de recurso
					if(tipo == 0){
						envio = "RECURSO#" + recurso + "#" + pid + "#" + time;
					}
					// OK
					else
						envio = "OK#" + recurso + "#" + pid + "#" + time;

                    // Envia a requisicao (ou ok) para o processo, 
					// A marca de tempo da mensagem é a marca de tempo atual
					Socket s;
                    s = new Socket("localhost", destino);
                    BufferedWriter out = new BufferedWriter(
                        new OutputStreamWriter(s.getOutputStream()));

                    out.write(envio);    
                    out.newLine();
                   	out.flush();
					
					/* Tirar os comentários aqui para visualizar quando o processo
					 * enviou uma mensagem */
					
					/*
					if(tipo == 0)
						System.out.println("Solitação do recurso " + recurso + " pelo processo no tempo " + time + " para o processo na porta " + destino);
					else {
					 	System.out.println("Processo " + pid + " enviou ok de " + recurso + " para o destino " + destino);
					} */
					

                	} catch (UnknownHostException e) {
                    	e.printStackTrace();
                	} catch (IOException e) {
                    	e.printStackTrace();
                 }
            }
        }).start();
    }
	
	/**
	 * server()
	 * Thread servidor para receber mensagens
	 */
	public synchronized static void server() {
        (new Thread() {
            @Override
            public void run() {
                ServerSocket ss;
                try {                
                    ss = new ServerSocket(60011);

					// Servidor fica ouvindo
                    while(true){ 
                        Socket s = ss.accept();

						// Coloca a thread para dormir para evitar concorrencia
                        try{ 
							int t = (int)(Math.random()*2000);
			            	Thread.sleep(t); 
			        	}catch(Exception e){}	

						// Trata a requisição em outra thread enquanto o servidor volta a ouvir
                        tratar(s);
						
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

	/**
	 * tratar()
	 * Trata uma requisição (recebimento e tratamento da mensagem)
	 * @param s O socket que recebeu a requisição
	 */
    public synchronized static void tratar(final Socket s) {
        (new Thread() {
            @Override
            public void run() {
                try {
                    // Lê a mensagem
                    BufferedReader in = new BufferedReader(
                        new InputStreamReader(s.getInputStream()));
								
                    String line = null;
					
					line = in.readLine();
						
					// Quebra a mensagem
					String lineQuebrada[] = line.split("#");
						
					// Evento (recebeu uma mensagem), ajusta o relogio interno
					int numeroRecurso = Integer.parseInt(lineQuebrada[1]);
					int timeMensagem = Integer.parseInt(lineQuebrada[3]);
					int remetente = Integer.parseInt(lineQuebrada[2]);
					
					clock.adjust(timeMensagem);
						
					// Caso seja requisicao, e tratado
					if(lineQuebrada[0].equals("RECURSO")){	
						Processo prc = new Processo(remetente, numeroRecurso, timeMensagem);
						
						//recurso livre
						if (recursos[numeroRecurso] == -2) {
							// Evento (envio de mensagem), incrementa o clock
							clock.add();
						
							switch (remetente) {
								case 2:
									client(60012, numeroRecurso, 1, clock.getTime());
									//System.out.println("Recurso " + numeroRecurso + " livre / enviou OK para " + remetente);
									break;
								case 3:
									client(60013, numeroRecurso, 1, clock.getTime());
									//System.out.println("Recurso " + numeroRecurso + " livre / enviou OK para " + remetente);
									break;
							}
						}
						
						// recurso esta sendo utilizado pelo processo
						else if ( recursos[numeroRecurso] == -1) {
							//System.out.println("Recurso " + numeroRecurso + " sendo utilizado / enfileirou " + remetente);
							
							// Evento (adiciona na fila), incrementa o clock
							clock.add();
							
							if(!fila.get(numeroRecurso).add(prc)) {
								System.out.println("Ocorreu um erro ao adicionar na fila");
							}
						}
						
						// deseja acessar recurso
						else {
							// se tempo da mensagem for maior do que quando a requisição foi feita, enfileira
							if (timeMensagem > recursos[numeroRecurso]) {
								// Evento (adiciona na fila), incrementa o clock
								clock.add();

								if(!fila.get(numeroRecurso).add(prc)) {
									System.out.println("Ocorreu um erro ao inserir na fila");
								}
								
								//System.out.println("Recurso " + numeroRecurso + " deseja ser acessado e ganhou clock / enfileirou " + remetente + " (" + recursos[numeroRecurso] + ")");
							} 
							
							// se nao, envia ok
							else {
								// Evento (envio de mensagem), incrementa o clock
								clock.add();							
							
								switch (remetente) {
									case 2:
										client(60012, numeroRecurso, 1, clock.getTime());
										//System.out.println("Recurso " + numeroRecurso + " deseja ser acessado e perdeu clock / enviou OK para " + remetente + " (" + recursos[numeroRecurso] + " - " + timeMensagem + ")");
										break;
									case 3:
										client(60013, numeroRecurso, 1, clock.getTime());
										//System.out.println("Recurso " + numeroRecurso + " deseja ser acessado e perdeu clock / enviou OK para " + remetente + " (" + recursos[numeroRecurso] + " - " + timeMensagem + ")");
										break;
								}
							}
						}							
					}
						
					// Caso seja um OK
					else if (lineQuebrada[0].equals("OK")) {							
						// Adiciona um aos OKS recebidos
						// Evento interno (recebeu OK e alterou o contador), adiciona um ao clock
						clock.add();	
						
						oks[numeroRecurso]++;
						
						//System.out.println("Recebeu Ok do recurso " + numeroRecurso + " de " + remetente);

						// Caso o processo tenha recebido o segundo OK para o recurso desejado
						if(oks[numeroRecurso] == 2) {							
							//utiliza recurso por um tempo
							
							// Evento (utilização do recurso), incrementa o clock
							clock.add();
							
							recursos[numeroRecurso] = -1;
							Thread.sleep(3000); 

							clock.add();

							System.out.println("Recurso " + numeroRecurso + " foi utilizado e liberado no tempo " + clock.getTime());

							// libera recurso
							recursos[numeroRecurso] = -2;
							oks[numeroRecurso] = 0;

							// envia OKs para fila
							while ( !fila.get(numeroRecurso).isEmpty() ) {

								// Evento interno (entregou a mensagem à aplicação), adiciona um ao clock
								clock.add();
									
								switch (fila.get(numeroRecurso).first().getNumeroProcesso()) {
									case 2:
										clock.add();
										client(60012, numeroRecurso, 1, clock.getTime());
										//System.out.println("Recurso " + numeroRecurso + " terminou acesso / enviou OK para " + remetente);
										break;
									case 3:
										clock.add();
										client(60013, numeroRecurso, 1, clock.getTime());
										//System.out.println("Recurso " + numeroRecurso + " terminou acesso / enviou OK para " + remetente);
										break;
								}
								fila.get(numeroRecurso).remove(fila.get(numeroRecurso).first());
							}
						}
					}

                } catch (UnknownHostException e) {
                    	e.printStackTrace();
                } catch (IOException e) {
                   	e.printStackTrace();
                }catch(Exception e){}
            }
        }).start();
    }
}
