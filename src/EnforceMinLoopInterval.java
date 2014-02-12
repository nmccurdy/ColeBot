
public class EnforceMinLoopInterval {
	long lastLoopTime;
	int minLoopIntervalMS;
	
	private final int NANO_PER_MILLI = 1000000;
	
	public EnforceMinLoopInterval(int minLoopIntervalMS) {
		// multiplying by 3 and then dividing by 2 later seems to get the loop interval
		// to average at the desired amount.
		this.minLoopIntervalMS = minLoopIntervalMS * 3;
	}
	
	public void sleepIfNecessary() {
		long time = System.nanoTime();
		long dt = time-lastLoopTime;
		
		lastLoopTime = time;
		if (dt < minLoopIntervalMS * NANO_PER_MILLI) {
			long sleepTime = (minLoopIntervalMS * NANO_PER_MILLI - dt) / NANO_PER_MILLI / 2;
//			System.out.println("dt: " + dt + ", Sleeping for " + sleepTime);
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
