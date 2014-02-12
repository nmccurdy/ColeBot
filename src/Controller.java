import java.util.HashMap;

public class Controller implements LogListener, ControllerListener {


	private HashMap<String, ControllerListener> map;

	private ControllerListener rootListener;
	private ControllerListener currentListener;

	public Controller(ControllerListener rootListener) {
		this.rootListener = rootListener;
		this.currentListener = rootListener;
		map = new HashMap<String, ControllerListener>();
	}

	public void addListener(String command, ControllerListener listener) {
		map.put(command, listener);
	}


	@Override
	public void log(String s) {
		// TODO Auto-generated method stub

	}

	public String quitted() {
		currentListener = rootListener;
		return currentListener.getPrompt();
	}

	public String doCommand(String command) {
		String output = "";
		
		if (currentListener == rootListener) {
			ControllerListener listener = map.get(command);
			if (listener != null) {
				currentListener = listener;
				output = currentListener.getPrompt();
			} else {
				output = rootListener.doCommand(command);
			}
		} else {
			output = currentListener.doCommand(command);
		}
		
		if (output == null) {
			return currentListener.getPrompt();
		}
		return output;
	}
	
	public String getPrompt() {
		return currentListener.getPrompt();
	}
	
	public String getHelp() {
		return currentListener.getHelp();
	}
}
