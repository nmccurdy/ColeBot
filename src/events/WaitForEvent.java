package events;

public class WaitForEvent implements EventListener {

	private Event event = null;
	
	@Override
	public synchronized void eventFired(Event event) {
		this.event = event;
		notifyAll();
	}
	
	public Event getEvent() {
		return event;
	}

}
