import java.util.LinkedList;

/**
 * The TiltFilter stores the last X number of gyro readings.  The readings
 * are the raw values which happen to be the sin of the tilt angle.  We need
 * this value in later calculations, so we never bother converting into degrees.
 * 
 * @author neil.mccurdy
 *
 */
public class AccFilter {
	private LinkedList<Double> list;
	private int size;
	private double zero;
	private int reverse = 1;
	
	public AccFilter(int size, boolean reversed) {
		this.size = size;
		list = new LinkedList<Double>();
		
		if (reversed) {
			reverse = -1;
		} else {
			reverse = 1;
		}
	}
	
	public void add(double newValue) {
		while (list.size() >= size) {
			list.removeFirst();
		}
		list.addLast(newValue * reverse);
	}
	
	public double getAverage() {
		double sum = 0;
		for (double value : list) {
			sum += value;
		}
		
		return sum / list.size() - zero;
	}
	
	public void clear() {
		list.clear();
	}
	
	public void zero(double newZero) {
		this.zero = newZero * reverse;
	}

	public boolean closeToZero() {
		double average = getAverage();
		if (Math.abs(average) <= .01) {
			return true;
		} else {
			return false;
		}
	}
}
