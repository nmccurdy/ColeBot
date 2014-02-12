
public class VelocityPID {
	private double kP;
	private double kI;

	private double integral;
	
	private int lastEncoder;
	private long lastTime;
	
	private double maxVelocity;
	private double haveVelocity;
		
	public VelocityPID(double kP, double kI, double maxVelocity) {
		this.kP = kP;
		this.kI = kI;
		this.maxVelocity = maxVelocity;
		
		lastEncoder = 0;
		lastTime = 0;
		integral = 0;
	}
	
	public double getPercentFromVelocity(double velocity) {
		return velocity/maxVelocity;
	}
	
	public double getVelocityFromPercent(double percent) {
		return percent * maxVelocity;
	}
	
	public double calcPower(int encoder, double wantVelocity) {
		final long NANO_PER_SEC = 1000000000;
		
		if (wantVelocity > maxVelocity) {
			wantVelocity = maxVelocity;
		} else if (wantVelocity < -maxVelocity) {
			wantVelocity = -maxVelocity;
		}
		
		
		long time = System.nanoTime();
		if (lastTime == 0) {
			lastTime = time;
		}
		
		long dt = time - lastTime;
		double dtSec = ((double)dt)/NANO_PER_SEC;
//		System.out.println("dt: " + dtSec);
		if (lastEncoder == 0) {
			lastEncoder = encoder;
		}
		
		int dEncoder= encoder - lastEncoder;
		
				
		if (dtSec == 0) {
			haveVelocity = 0;
		} else {
			haveVelocity = dEncoder/dtSec;
		}
		
		double error = wantVelocity - haveVelocity;
	
		integral += error * dtSec;

		
		
		double output = kP * error + kI * integral;
		
		if (output > 100) {
			output = 100;
		} else if (output < -100) {
			output = -100;
		}
		
		// if we're not moving and we don't want to move, don't give the
		// motors any power
		if (wantVelocity == 0 && dEncoder == 0) {
			output = 0;
		}
		
//		System.out.println("PID want: " + wantVelocity + ", have: " + haveVelocity + ", power: " + output);
//		System.out.println("PID lastEnc: " + lastEncoder+ ", Enc: " + encoder);
//		System.out.println("PID lastTime: " + lastTime+ ", time: " + time);
		
		lastTime = time;
		lastEncoder = encoder;
		
		return output;
	}
	
	public double calcPowerFromPercent(int encoder, double wantVelocityPercent) {
		return calcPower(encoder, getVelocityFromPercent(wantVelocityPercent));
	}
	
	public double getVelocity() {
		return haveVelocity;
	}
	
	public double getVelocityAsPercent() {
		return getPercentFromVelocity(getVelocity());
	}
	
	public void resetIntegral() {
//		System.out.println("resetting integral");
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
