public class Statistics {
	private int numLoops;
	private double minLoopInterval;
	private double maxLoopInterval;

	private final int NANO_PER_MS = 1000000;

	private long startTime;
	private long lastTime;
	private long lastOutputTime;
	private int resetIntervalMS;
	private int reportingIntervalMS;
	
	// only works for one sized interval, but oh well.
	private boolean hasIntervalElapsed = false;
	
	public Statistics(int resetIntervalMS, int reportingIntervalMS) {
		this.resetIntervalMS = resetIntervalMS;
		this.reportingIntervalMS = reportingIntervalMS;
		reset();
	}

	public void start() {
		startTime = System.nanoTime();
	}

	public void hit() {
		hasIntervalElapsed = false;
		
		long time = System.nanoTime();
		if (lastTime == 0) {
			lastTime = time;

			return;
		}

		numLoops++;

		long dt = time - lastTime;

		if (dt < minLoopInterval) {
			minLoopInterval = dt;
		}

		if (dt > maxLoopInterval) {
			maxLoopInterval = dt;
		}

		// reset stats every resetInterval

		if (time - startTime > (long) resetIntervalMS * NANO_PER_MS) {
			reset();
		} else {
			lastTime = time;
		}

	}

	public void reset() {
		numLoops = 0;
		minLoopInterval = Double.MAX_VALUE;
		maxLoopInterval = 0;
		startTime = System.nanoTime();
		lastTime = 0;
	}

	public double getMinLoopInterval() {
		return ((double) minLoopInterval) / NANO_PER_MS;
	}

	public double getMaxLoopInterval() {
		return ((double) maxLoopInterval) / NANO_PER_MS;
	}

	public double getAvgLoopInterval() {
		return ((double) (lastTime - startTime) / numLoops) / NANO_PER_MS;
	}

	public String getStats() {
		return String.format(
				"Loop Interval ms (Avg,Min,Max): (%1.2f, %1.2f, %1.2f)",
				getAvgLoopInterval(), getMinLoopInterval(),
				getMaxLoopInterval());
	}

	public boolean hasIntervalElaspsed() {
		if (hasIntervalElapsed) {
			// want to be able to call this multiple times per loop and get the right
			// result.  This approach only works for one sized interval, but that
			// should be ok for our needs.
			return true;
		}
		
		long time = System.nanoTime();
		final long NANO_PER_MS = 1000000;
		// final long MILLI_PER_NANO = 1000000
		if (time / (reportingIntervalMS * NANO_PER_MS) > lastOutputTime
				/ (reportingIntervalMS * NANO_PER_MS)) {
			lastOutputTime = time;
			hasIntervalElapsed = true;
			return true;
		} else {
			return false;
		}
	}
	
	public int getNumLoops() {
		return numLoops;
	}
}
