public class PositionPID {
	private double kP;
	private double kI;

	private double integral;

	private long lastTime;

	private int lastWantEnconder = Integer.MAX_VALUE;

	private final double CLOSE_ENOUGH = 10;
	
	public PositionPID(double kP, double kI) {
		this.kP = kP;
		this.kI = kI;

		lastTime = 0;
		integral = 0;
	}

	public double calcPower(int haveEncoder, int wantEncoder) {
		final long NANO_PER_SEC = 1000000000;

		if (wantEncoder != lastWantEnconder) {
			resetIntegral();
		}

		long time = System.nanoTime();
		if (lastTime == 0) {
			lastTime = time;
		}

		long dt = time - lastTime;
		double dtSec = ((double) dt) / NANO_PER_SEC;

		double error = wantEncoder - haveEncoder;

		double output = 0;

		if (Math.abs(error) < CLOSE_ENOUGH) {
			output = 0;
		} else {
			integral += error * dtSec;
			output = kP * error + kI * integral;
		}
		
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
	}

	public void setKI(double kI) {
		this.kI = kI;
	}

	public String getSettings() {
		return String.format("kP:%1.5f, kI:%1.5f", kP, kI);
	}
}
