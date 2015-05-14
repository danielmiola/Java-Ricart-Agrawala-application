/**
 * Autores:
 * Alexandre Braga Saldanha - R.A.: 408484
 * Daniel Miola - R.A.: 438340
 **/

package FilaProcessos;

import java.lang.Comparable;

public class Processo implements Comparable<Processo>{
	private int pid;
	private int numero;
	private int timeStamp;
	
	/**
	 * Construtor
	 * @param numero O numero da mensagem
	 * @param timeStamp O tempo em que a mensagem foi enviada
	 */
	public Processo(int pid, int numero, int timeStamp) {
		this.pid = pid;
		this.numero = numero;
		this.timeStamp = timeStamp;
	}
	
	/**
	 * getNumeroMensagem()
	 * Retorna o número da mensagem
	 */
	public int getNumeroProcesso() {
		return pid;
	}
	
	/**
	 * getTimeStamp()
	 * Retorna o tempo em que a mensagem foi enviada
	 */
	public int getTimeStamp() {
		return timeStamp;
	}

	public int compareTo(Processo m2) {
		if(this.timeStamp < m2.timeStamp)
			return -1;
		
		if(this.timeStamp > m2.timeStamp)
			return 1;
		
		return 0;
	}
}
