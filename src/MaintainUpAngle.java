public class MaintainUpAngle {
	private static final int MS_PER_NS = 1000000;
	private Wheels wheels;
	private TiltSensor tiltSensor;
	private TiltPID tiltPID;
	
	private double stoppedVelocity;
	private boolean waitingForAverageAngleCalculation;
	private long timeSinceStopped;
	private long timeSinceLastCalibrated;
	private int msRequiredForCalibration;
	private int msBetweenCalibrations;

	
	/*
	 * Strictly speaking, we don't really ever need to know what the value for up is.  This is
	 * because the PID loop for tilt (tiltPID) corrects for this with the accumulated error in kI.
	 * 
	 * The problem, though, is that we sometimes want to make decisions based on whether or not we
	 * are balanced and the easisest way of thinking about balanced is that the tilt = 0.  This
	 * way we can only decide to turn if the tilt is within a threshold of 0.  I tried doing it
	 * based off of the wheel velocity, but you can have a velocity of 0 and be precariously close
	 * to tipping over.
	 */
	
	public MaintainUpAngle(Wheels wheels, TiltSensor tiltSensor, TiltPID tiltPID,
			double stoppedVelocity, int msRequiredForCalibration,
			int msBetweenCalibrations) {
		this.wheels = wheels;
		this.tiltSensor = tiltSensor;
		this.tiltPID = tiltPID;
		this.stoppedVelocity = stoppedVelocity;
		this.msRequiredForCalibration = msRequiredForCalibration;
		this.msBetweenCalibrations = msBetweenCalibrations;
	}

	public void maintainUp() {
		long now = System.nanoTime();

		if (now - timeSinceLastCalibrated > msBetweenCalibrations * MS_PER_NS) {
			// it's time to start thinking about calibrating again
			if (Math.abs(wheels.getVelocity(Wheels.Interval.OneSecond)) < stoppedVelocity) {
				// if we've stopped, calculate the
				if (!waitingForAverageAngleCalculation) {
					tiltSensor.resetAverageAngleCalc();
					waitingForAverageAngleCalculation = true;
					timeSinceStopped = now;
				} else {
					// if we've been stopped long enough, grab the average tilt
					// angle and make that be the
					// new zero
					if (now - timeSinceStopped > msRequiredForCalibration
							* MS_PER_NS) {
						waitingForAverageAngleCalculation = false;
						
						double newZero = tiltSensor.getAveragePitchAngle();
						
						System.out.println("New zero found: " + newZero);
						
						tiltSensor.setPitchAngleZero(newZero);
						// since we have a new definition of zero, reset the error
						// accumulation in the PID
						tiltPID.resetIntegral();
					}

				}
			} else {
				// no longer averaging 0 velocity, so abort the average
				// tilt calc
				waitingForAverageAngleCalculation = false;
			}
		}
	}

}
