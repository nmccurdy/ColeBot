public class TiltPID {
	private static final double MIN_VELOCITY = 0;
	private double velocityScale;
	private double kP;
	private double kI;

	private double integral;

	private long lastTime;

	private int maxDistance; // translates to speed

	public TiltPID(double kP, double kI, double velocityScale, int maxDistance) {
		this.kP = kP;
		this.kI = kI;
		this.velocityScale = velocityScale;
		this.maxDistance = maxDistance;

		lastTime = 0;
		integral = 0;
	}

	public void setVelocityScale(double scale) {
		this.velocityScale = scale;
		resetIntegral();
	}

	public void setMaxDistance(int maxDistance) {
		this.maxDistance = maxDistance;
		resetIntegral();
	}

	public double calcTilt(int distanceToWant, double velocity) {
		final long NANO_PER_SEC = 1000000000;

		long time = System.nanoTime();
		if (lastTime == 0) {
			lastTime = time;
		}

		long dt = time - lastTime;
		double dtSec = ((double) dt) / NANO_PER_SEC;

		double error = distanceToWant;

		if (Math.abs(error) > maxDistance) {
			if (error > 0) {
				error = maxDistance;
			} else {
				error = -maxDistance;
			}
		}

		double output = 0;

		// if (Math.abs(error) < CLOSE_ENOUGH) {
		// output = 0;
		// // System.out.println("huh?");
		// } else {
		integral += error * dtSec;
		output = kP * error + kI * integral;
		// }

		// System.out.println("output: " + output + ", kp: " + kP + ", error: "
		// + error + "math: " + kP * error);

		// adjust the tilt by a fraction of the current velocity because we
		// don't need to tilt as much if we're
		// already moving. The fast we're moving the less we need to tilt.

		if (Math.abs(velocity) < MIN_VELOCITY) {
			velocity = 0;
		}
		
		output -= velocity * velocityScale;

		// System.out.println("PID want: " + wantVelocity + ", have: " +
		// haveVelocity + ", power: " + output);
		// System.out.println("PID lastEnc: " + lastEncoder+ ", Enc: " +
		// encoder);
		// System.out.println("PID lastTime: " + lastTime+ ", time: " + time);

		lastTime = time;

		return output;
	}

	public void resetIntegral() {
		// System.out.println("resetting integral");
		integral = 0;
	}

	public void setKP(double kP) {
		this.kP = kP;
		resetIntegral();
	}

	public void setKI(double kI) {
		this.kI = kI;
		resetIntegral();
	}

	public String getSettings() {
		return String.format("kP:%1.9f, kI:%1.9f, v:%1.9f, d:%d", kP, kI, velocityScale, maxDistance);
	}
}
