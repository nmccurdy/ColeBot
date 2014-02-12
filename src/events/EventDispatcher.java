package events;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class EventDispatcher implements Runnable {
	private boolean running;

	private Map<Event, LinkedList<WeakReference<EventListener>>> mapListeners;
	private LinkedList<WeakReference<EventListener>> listenersAllEvents;

	private EventQueue eventQueue;

	public EventDispatcher(EventQueue eventQueue) {
		this.eventQueue = eventQueue;
		mapListeners = new HashMap<Event, LinkedList<WeakReference<EventListener>>>();
		listenersAllEvents = new LinkedList<WeakReference<EventListener>>();
	}

	public synchronized void addEventListener(EventListener listener, Event type) {
		WeakReference<EventListener> weakListener = new WeakReference<EventListener>(
				listener);
		LinkedList<WeakReference<EventListener>> list = mapListeners.get(type);
		if (list == null) {
			list = new LinkedList<WeakReference<EventListener>>();
			mapListeners.put(type, list);
		}

		list.add(weakListener);
	}

	public synchronized void addEventListener(EventListener listener) {
		WeakReference<EventListener> weakListener = new WeakReference<EventListener>(
				listener);

		listenersAllEvents.add(weakListener);
	}

	private synchronized void removeEventListener(EventListener listener,
			LinkedList<WeakReference<EventListener>> listeners) {
		Iterator<WeakReference<EventListener>> itr = listeners.iterator();
		while (itr.hasNext()) {
			WeakReference<EventListener> weakListener = itr.next();
			EventListener listener2 = weakListener.get();
			if (listener2 == null) {
				itr.remove();
			} else {
				if (listener2.equals(listener)) {
					itr.remove();
				}
			}
		}
	}

	public synchronized void removeEventListener(EventListener listener) {
		// iterate through all listeners looking for this one
		// this will also clear weak references while it is at it

		// first go through the listeners that are listening to all events
		removeEventListener(listener, listenersAllEvents);

		// now go through the map that has event-specific listeners
		Iterator<LinkedList<WeakReference<EventListener>>> itr = mapListeners
				.values().iterator();
		while (itr.hasNext()) {
			removeEventListener(listener, itr.next());
		}
	}

	@Override
	public void run() {
		running = true;

		try {
			while (running) {
				synchronized (eventQueue) {
					eventQueue.wait();
				}
				// we know that there is at least one event so drain them

				while (eventQueue.areThereEvents()) {
					Event event = eventQueue.getNextEvent();
					dispatchEvent(event);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void dispatchToListeners(Event event,
			LinkedList<WeakReference<EventListener>> listeners) {
		Iterator<WeakReference<EventListener>> itr = listeners.iterator();
		while (itr.hasNext()) {
			WeakReference<EventListener> weakListener = itr.next();
			EventListener listener = weakListener.get();
			if (listener != null) {
				listener.eventFired(event);
			}
		}
	}

	private synchronized LinkedList<WeakReference<EventListener>> makeCopyOfListenersAllEvents() {
		LinkedList<WeakReference<EventListener>> copy = (LinkedList<WeakReference<EventListener>>) listenersAllEvents
				.clone();
		return copy;
	}

	private synchronized void appendOtherListeners(
			LinkedList<WeakReference<EventListener>> copy,
			LinkedList<WeakReference<EventListener>> toAppend) {
		if (toAppend != null) {
			copy.addAll(toAppend);
		}
	}

	private void dispatchEvent(Event event) {
		// make a copy of all the listeners so that we can iterate through the
		// listeners and call them in a non-synchronized function. This will
		// avoid
		// potential dead-lock occuring if someone tries to register a new
		// listener
		// in an EventListener.

		LinkedList<WeakReference<EventListener>> copy = makeCopyOfListenersAllEvents();
		appendOtherListeners(copy, mapListeners.get(event));

		dispatchToListeners(event, copy);
	}

	public Event waitForEvent(Event event) throws InterruptedException {
		// the idea of this function is to provide a blocking wait for a
		// particular
		// event type.

		WaitForEvent waitForEvent = new WaitForEvent();
		addEventListener(waitForEvent, event);
		synchronized (waitForEvent) {
			waitForEvent.wait();
		}
		Event eventReturned = waitForEvent.getEvent();
		removeEventListener(waitForEvent);

		return eventReturned;
	}

	public void stop() {
		running = false;
	}
}
