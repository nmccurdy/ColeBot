import java.util.LinkedList;


public class BlackBoxRecorder <X> {
	private class Data {
		public long time;
		public X data;
		
		public Data(long time, X data) {
			this.time = time;
			this.data = data;
		}
	}
	
	private int maxRecords;
	private LinkedList<Data> list;
	
	public BlackBoxRecorder(int maxRecords) {
		this.maxRecords = maxRecords;
		
		list = new LinkedList<Data>();
	}
	
	public void add(X data) {
		long time = System.nanoTime();
		Data timeData = new Data(time, data);
		
		if (list.size() >= maxRecords) {
			list.removeFirst();
		}
		
		list.addLast(timeData);
	}
	
	public String getTabbedData() {
		StringBuilder builder = new StringBuilder();
		
		for (Data data : list) {
			builder.append(data.time);
			builder.append('\t');
			builder.append(data.data);
			builder.append('\n');
		}
		
		return builder.toString();
	}
}
