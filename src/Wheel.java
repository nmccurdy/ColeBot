import com.phidgets.MotorControlPhidget;
import com.phidgets.PhidgetException;

public class Wheel {
	final private double MAX_POWER = 100;
	
	private boolean reversed;
	private MotorControlPhidget motorControl;
	private VelocityPID velPid;
	private PositionPID posPid;
	private double wantVelocity;
	private int wantEncoder;
	private double minWorkingPower;

	public Wheel(int serialNum, boolean reversed, double minWorkingPower) throws PhidgetException {
		this.reversed = reversed;
		this.minWorkingPower = minWorkingPower;
		
		velPid = new VelocityPID(.002, 0, 2500);
		//kP:0.15000, kI:0.10000
		posPid = new PositionPID(.15, .1);
		
		motorControl = new MotorControlPhidget();
		motorControl.open(serialNum);

		motorControl.waitForAttachment();
		motorControl.setAcceleration(0, motorControl.getAccelerationMax(0)/1);
	}

	public void setVelocityAsPercent(double velocityPer)
			throws PhidgetException {
		wantVelocity = velPid.getVelocityFromPercent(velocityPer);
	}

	public void setVelocity(double velocity) throws PhidgetException {
		if (wantVelocity != velocity) {
			velPid.resetIntegral();
		}
		wantVelocity = velocity;

	}

	public void runVelocityPID() throws PhidgetException {
		double power = getPower() + velPid.calcPower(getEncoderPosition(), wantVelocity);

		setPower(power);
	}
	
	public void setWantEncoder(int encoder) {
		wantEncoder = encoder;
	}

	public void setWantEncoderOffset(int offset) throws PhidgetException {
		wantEncoder = getEncoderPosition() + offset;
	}
	
	public void runPositionPID() throws PhidgetException {
		double power = posPid.calcPower(getEncoderPosition(), wantEncoder);
		
		setPower(power);
	}
	
	public int getEncoderPosition() throws PhidgetException {
		int position = motorControl.getEncoderPosition(0);

		if (reversed) {
			return -position;
		} else {
			return position;
		}
	}

	public void resetEncoderPosition() throws PhidgetException {
		motorControl.setEncoderPosition(0, 0);
	}

	public void stopWheel() throws PhidgetException {
		wantVelocity = 0;
		motorControl.setVelocity(0, 0);
		motorControl.setBraking(0, 100);
		posPid.resetIntegral();
	}

	public void close() throws PhidgetException {
		if (motorControl.isAttached()) {
			motorControl.setVelocity(0, 0);
			motorControl.close();
		}
	}

	public void setVelPIDkP(double kP) {
		velPid.setKP(kP);
		velPid.resetIntegral();
	}
	
	public void setVelPIDkI(double kI) {
		velPid.setKI(kI);
		velPid.resetIntegral();
	}
	
	public double getVelocity() {
		return velPid.getVelocity();
	}
	
	public String getVelPIDsettings() {
		return velPid.getSettings();
	}
	
	public void setPosPIDkP(double kP) {
		posPid.setKP(kP);
		posPid.resetIntegral();
	}
	
	public void setPosPIDkI(double kI) {
		posPid.setKI(kI);
		posPid.resetIntegral();
	}
	
	public String getPosPIDsettings() {
		return posPid.getSettings();
	}
	
	public void resetPosPID() {
		posPid.resetIntegral();
	}
	
	public double getPower() throws PhidgetException {
		double rawPower;
		if (reversed) {
			rawPower = -motorControl.getVelocity(0);
		} else {
			rawPower = motorControl.getVelocity(0);
		}
		
		if (rawPower > 0) {
			return rawPower - minWorkingPower;
		} else if (rawPower < 0) {
			return rawPower + minWorkingPower;
		} else {
			return 0;
		}
	}

	public void setPower(double power) throws PhidgetException {
		double adjustedPower = 0;
		
		motorControl.setBraking(0,  0);
		if (power > 0) {
			adjustedPower = power + minWorkingPower;
		} else if (power < 0) {
			adjustedPower = power - minWorkingPower;
		}
	
		if (adjustedPower > MAX_POWER) {
			adjustedPower = MAX_POWER;
		} else if (adjustedPower < -MAX_POWER) {
			adjustedPower = -MAX_POWER;
		}
		
		if (reversed) {
			motorControl.setVelocity(0, -adjustedPower);
		} else {
			motorControl.setVelocity(0, adjustedPower);
		}
		
//		System.out.println(motorControl.getCurrent(0));
	}
	
	public int getWantEncoder() {
		return wantEncoder;
	}
}
