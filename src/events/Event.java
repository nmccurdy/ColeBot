package events;

public interface Event {
	public enum Priority {
		Stat(0), Urgent(1), Normal(2);
		
		int index;
		Priority(int index) {
			this.index = index;
		}
	};

	public String getName();
	public long getTime();
	public Priority getPriority();
	public Integer getIntParam();
	public String getStringParam();
	public Double getDoubleParam();
}
