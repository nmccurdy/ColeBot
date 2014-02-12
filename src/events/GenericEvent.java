package events;

public class GenericEvent implements Event, Comparable<Event> {

	private long time;
	
	public GenericEvent() {
		time = System.nanoTime();
	}
		
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public long getTime() {
		return time;
	}

	@Override
	public Priority getPriority() {
		return Priority.Normal;
	}

	@Override
	public Integer getIntParam() {
		return null;
	}

	@Override
	public String getStringParam() {
		return null;
	}

	@Override
	public Double getDoubleParam() {
		return null;
	}

	@Override
	public int compareTo(Event other) {
		if (this.getClass().equals(other.getClass())) {
			return 0;
		} else {
			return -1;
		}
	}

}
