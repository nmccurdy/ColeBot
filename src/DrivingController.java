import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DrivingController extends Controller implements Runnable {

	public DrivingController(ControllerListener rootListener) {
		super(rootListener);
	}

	@Override
	public void run() {
		try {
			InputStreamReader reader;
			try {
				reader = new FileReader("/dev/tty0");
			} catch (FileNotFoundException e) {
				System.out
						.println("Reverting to console keyboard listener.  You have to press enter after each keystroke!");
				reader = new InputStreamReader(System.in);

			}
			while (!reader.ready()) {
				Thread.sleep(100);
			}
			while (true) {
				char c = (char) reader.read();
				doCommand(String.valueOf(c));
			}
		} catch (IOException e) {
		} catch (InterruptedException e) {

		}
	}

	@Override
	public void log(String s) {
		System.out.print(s);
	}
}
