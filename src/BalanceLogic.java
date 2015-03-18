import com.phidgets.PhidgetException;

public class BalanceLogic {
	private double kP;

	private long lastTime;
	private double lastWantDeg;

	private double maxPower;

	private final double CLOSE_ENOUGH = 0;

	private double wantDeg = 0;

	private double kD;
	private double kV;
	private double kE;

	public BalanceLogic(double kP, double kD, double kV, double kE, double maxPower) {
		this.kP = kP;
		this.kV = kV;
		this.kD = kD;
		this.kE = kE;

		this.maxPower = maxPower;

		lastTime = 0;
	}

	public void setWantDeg(double wantDeg) {
		if (wantDeg != lastWantDeg) {
			lastWantDeg = wantDeg;
			reset();
		}

		this.wantDeg = wantDeg;
	}

	public double getWantDeg() {
		return wantDeg;
	}

	public double calcPower(double haveDeg, double velocityDeg, double velocity, double wantPosOffset)
			throws PhidgetException {

		final long MAX_WANT_POS_OFFSET = 1050;

//		System.out.println("v: " + velocity);
		
		if (wantPosOffset > MAX_WANT_POS_OFFSET) {
			wantPosOffset = MAX_WANT_POS_OFFSET;
		} else if (wantPosOffset < -MAX_WANT_POS_OFFSET) {
			wantPosOffset = -MAX_WANT_POS_OFFSET;
		}

		long time = System.nanoTime();
		if (lastTime == 0) {
			lastTime = time;
		}

		double degDiff = haveDeg - wantDeg;

		double output = 0;

		if (Math.abs(degDiff) < CLOSE_ENOUGH) {
			output = 0;
		} else {
			output = kP * degDiff + kD * velocityDeg + kV * velocity - kE * wantPosOffset;
		}

		if (output > maxPower) {
			output = maxPower;
		} else if (output < -maxPower) {
			output = -maxPower;
		}

		// System.out.println(String.format("deg:%1.2f, vdeg:%1.2f, v:%1.5f, kE:%1.5f, output:%1.2f",
		// haveDeg, velocityDeg, velocity, kE, output));
		// System.out.println("PID want: " + wantVelocity + ", have: " +
		// haveVelocity + ", power: " + output);
		// System.out.println("PID lastEnc: " + lastEncoder+ ", Enc: " +
		// encoder);
		// System.out.println("PID lastTime: " + lastTime+ ", time: " + time);

		lastTime = time;

		return output;
	}

	public void reset() {
		// System.out.println("resetting integral");
		lastTime = 0;
	}

	public void setKP(double kP) {
		this.kP = kP;
		reset();
	}

	public void setKV(double kV) {
		this.kV = kV;
		reset();
	}

	public void setKD(double kD) {
		this.kD = kD;
		reset();
	}

	public void setKE(double kE) {
		this.kE = kE;
		reset();
	}

	public String getSettings() {
		return String.format("kP:%1.5f, kD:%1.5f, kV:%1.5f, kE:%1.5f", kP, kD, kV, kE);
	}

}
