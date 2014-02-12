package events;

public class ReachedPositionEvent extends GenericEvent {

	private int position;
	
	public ReachedPositionEvent() {
		super();
	}
	
	public ReachedPositionEvent(int position) {
		super();
		this.position = position;
	}

	@Override
	public Integer getIntParam() {
		return position;
	}


}
