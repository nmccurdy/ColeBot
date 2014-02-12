public class ResetLogic {
	private static final long NANO_SECONDS_PER_MS = 1000000;

	private long timeKnockedOver = 0;
	private long timeStopped = 0;
	private long timeStartedStandingUp;

	double maxAngle;
	int maxAllowedKnockedOverMS;
	int sleepTimeAfterKnockedDownMS;

	private long maxTryingToStandUpMS;

	public ResetLogic(double maxAngle, int maxAllowedKnockedOverMS,
			int sleepTimeAfterKnockedDownMS, int maxTryingToStandUpMS) {
		this.maxAngle = maxAngle;
		this.maxAllowedKnockedOverMS = maxAllowedKnockedOverMS;
		this.sleepTimeAfterKnockedDownMS = sleepTimeAfterKnockedDownMS;
		this.maxTryingToStandUpMS = maxTryingToStandUpMS;
	}

	public boolean amIKnockedOver(double angle) {

		double absAngle = Math.abs(angle);
		long now = System.nanoTime();
		
		if (absAngle < maxAngle || (now - timeStartedStandingUp < maxTryingToStandUpMS * NANO_SECONDS_PER_MS)) {
			timeKnockedOver = 0;
			timeStartedStandingUp = 0;
			return false;
		} else {
			if (timeStopped == 0) {
				// we haven't already stopped
				if (timeKnockedOver == 0) {
					timeKnockedOver = now;
				}

				if ((now - timeKnockedOver) < maxAllowedKnockedOverMS * NANO_SECONDS_PER_MS) {
					return false;
				} else {
					timeStopped = now;
					return true;
				}
			} else {
				return true;
			}
		}
	}

	public boolean shouldIStandUp() {
		if (System.nanoTime() - timeStopped < sleepTimeAfterKnockedDownMS * NANO_SECONDS_PER_MS) {
			return false;
		} else {
			timeStopped = 0;
			timeStartedStandingUp = System.nanoTime();
			return true;
		}
	}
}
