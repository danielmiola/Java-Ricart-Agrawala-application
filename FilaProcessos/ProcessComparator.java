/**
 * Autores:
 * Alexandre Braga Saldanha - R.A.: 408484
 * Daniel Miola - R.A.: 438340
 **/

package FilaProcessos;

import java.util.Comparator;

public class ProcessComparator implements Comparator<Processo> {
	@Override
	public int compare(Processo m1, Processo m2) {
		return m1.compareTo(m2);
	}
}
