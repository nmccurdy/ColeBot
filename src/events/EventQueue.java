package events;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Queue;

import events.Event.Priority;

public class EventQueue {
	private EnumMap<Priority, Queue<Event>> queues;

	private static EventQueue instance = null;
	
	public static EventQueue getInstance() {
		if (instance == null) {
			instance = new EventQueue();
		}
		
		return instance;
	}
	
	private EventQueue() {
		queues = new EnumMap<Priority, Queue<Event>>(Priority.class);
		for (Priority priority : Priority.values()) {
			queues.put(priority, new LinkedList<Event>());
		}
	}

	public synchronized void add(Event event) {
		queues.get(event.getPriority()).add(event);
		notifyAll();
	}

	public boolean areThereEvents() {
		for (Priority priority : Priority.values()) {
			if (queues.get(priority).size() > 0) {
				return true;
			}
		}

		return false;
	}

	public boolean areThereEvents(Priority priority) {
		return (queues.get(priority).size() > 0);
	}

	public synchronized Event getNextEvent() {
		for (Priority priority : Priority.values()) {
			Queue<Event> queue = queues.get(priority);
			if (queue.size() > 0) {
				return queue.remove();
			}
		}

		return null;
	}

	public synchronized Event getNextEvent(Priority priority) {
		Queue<Event> queue = queues.get(priority);
		if (queue.size() > 0) {
			return queue.remove();
		}

		return null;
	}
}
