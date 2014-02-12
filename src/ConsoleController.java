import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleController extends Controller implements Runnable {

	private ColeBotTest main;
	
	public ConsoleController(ControllerListener rootListener, ColeBotTest main) {
		super(rootListener);
		
		this.main = main;
	}

	@Override
	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		while (true) {

			if (reader != null) {
				String command;
				try {
					command = reader.readLine();
					String output = doCommand(command);
					log(output);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public void log(String s) {
		System.out.print(s);
	}
}
