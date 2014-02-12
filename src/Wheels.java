import com.phidgets.PhidgetException;

import events.EventQueue;
import events.ReachedPositionEvent;
import events.ReachedTurnOffsetEvent;

public class Wheels {
	public static enum Interval {
		BalanceLoop(0), TiltLoop(1), SafetyLoop(2), OneSecond(3);
		
		int index;
		private Interval(int index) {
			this.index = index;
		}
	}; 
	
	private static final long NS_PER_SEC = 1000000000;
	private Wheel leftWheel;
	private Wheel rightWheel;

	private class VelocityInfo {
		
		long lastTime;
		int lastEncoder;
		double velocity;
		
		public VelocityInfo() {
			lastTime = 0;
			lastEncoder = 0;
			velocity = 0;
		}
		
		public void setValues(long lastTime, int lastEncoder, double velocity) {
			this.lastTime = lastTime;
			this.lastEncoder = lastEncoder;
			this.velocity = velocity;
		}
	}

	private VelocityInfo velocity[];
	
	private int rightWheelDifferential = 0;

	// This determines if we are close enough to the target position to warrant throwing
	// an event
	private int closeEnoughThreshold;
	
	// this determines if we are going to do a hard turn (requiring a stop and dramatic wheel
	// adjustments) or if we will just incrementally adjust the power to get the wheels to
	// eventually get to the right encoder position
	private int turnVsDriftThreshold;
	
	private int wantPosition;
	private boolean newPosition = false;
	private boolean newDifferential = false;
	
	private double turnVelocityMax;
	private boolean turning;
	private boolean preparedForTurn;


//	private boolean equalizeWheels = true;
	
	public Wheels(Wheel leftWheel, Wheel rightWheel, int closeEnoughThreshold, int turnVsDriftThreshold, double turnVelocityMax) throws PhidgetException {
		super();
		this.leftWheel = leftWheel;
		this.rightWheel = rightWheel;
		this.closeEnoughThreshold = closeEnoughThreshold;
		this.turnVsDriftThreshold = turnVsDriftThreshold;
		this.turnVelocityMax = turnVelocityMax;

		velocity = new VelocityInfo[Wheels.Interval.values().length];
		
		for (int i = 0; i < velocity.length; i++) {
			velocity[i] = new VelocityInfo();
		}
		
		turning = false;
		preparedForTurn = false;
		resetWantPositionOffset(0);
	}

	public void calcAvgVelocity(Interval interval) throws PhidgetException {
		
		final long NS_PER_MS = 1000;
		
		long time = System.nanoTime();

		if (time - velocity[interval.index].lastTime < 10 * NS_PER_MS) {
			// only calculate the averageVelocity if at least 10ms have elapsed.
			// this way we can call this routine multiple times per loop

			return;
		}
		
		int encoder = (leftWheel.getEncoderPosition() + rightWheel
				.getEncoderPosition()) / 2;


		double newVelocity = 0;

		long lastTime = velocity[interval.index].lastTime;
		int lastEncoder = velocity[interval.index].lastEncoder;
		
		if (lastTime == 0) {
			velocity[interval.index].lastTime = time;
		} else {
			double dt = ((double) (time - lastTime)) / NS_PER_SEC;
			newVelocity = (encoder - lastEncoder) / dt;
		}

		velocity[interval.index].setValues(time, encoder, newVelocity);
	}

	public double getVelocity(Interval interval) {
		return velocity[interval.index].velocity;
	}

	private int getRightWheelError() throws PhidgetException {
		return rightWheel.getEncoderPosition() - leftWheel.getEncoderPosition()
				- rightWheelDifferential;
	}

	private double getPowerAdjustment(int wheelError) {

		final double MAX_ADJUSTMENT = 25;
		final double PROPORTION = .07;

		double power;

		power = PROPORTION * wheelError;
		if (power > MAX_ADJUSTMENT) {
			power = MAX_ADJUSTMENT;
		} else if (power < -MAX_ADJUSTMENT) {
			power = -MAX_ADJUSTMENT;
		}

		return power;
	}

	public void setPower(double power) throws PhidgetException {
		double powerAdjust = 0;
		
		if (!shouldITurn()) {
			// only calculate a poweradjust if we're not setting up for a turn
			// the power adjust is for drift mode which is used to make small
			// adjustments to wheel differential
			int rightWheelError = getRightWheelError();
			powerAdjust = getPowerAdjustment(rightWheelError);
		}

		leftWheel.setPower(power + powerAdjust);
		rightWheel.setPower(power - powerAdjust);
	}

	public void stopWheels() throws PhidgetException {
		leftWheel.stopWheel();
		rightWheel.stopWheel();
	}

	public void resetWantPositionOffset(int offset) throws PhidgetException {
		wantPosition = (leftWheel.getEncoderPosition() + rightWheel
				.getEncoderPosition()) / 2 + offset;
		newPosition = true;
	}
	
	public void incWantPositionOffset(int increment) {
		wantPosition += increment;
		if (increment != 0) {
			newPosition = true;
		}
	}

	public int getDistanceToWantPosition() throws PhidgetException {
		return wantPosition
				- (leftWheel.getEncoderPosition() + rightWheel
						.getEncoderPosition()) / 2;
	}

	public void incrementRightWheelDifferential(int amount) {
		rightWheelDifferential += amount;
		newDifferential = true;
	}
	
	public void setRightWheelDifferential(int offset) {
		rightWheelDifferential = offset;
		newDifferential = true;
	}
	
	public boolean hasPositionBeenReached() throws PhidgetException {
		boolean result = false;
		if (Math.abs(getDistanceToWantPosition()) < closeEnoughThreshold) {
			result = true;
			notifyPositionReached();
		}
		
		return result;
	}
	private void notifyPositionReached() {
		if (newPosition) {
			// notify everyone that we reached the desired position
			EventQueue eventQueue = EventQueue.getInstance();
			eventQueue.add(new ReachedPositionEvent());
			newPosition = false;
		}

	}

	public boolean shouldITurn() throws PhidgetException {
		if (Math.abs(getRightWheelError()) > turnVsDriftThreshold) {
			return true;
		} else {
			if (turning) {
				// we have just completed a turn, so fire an event
				turning = false;
				preparedForTurn = false;
				EventQueue eventQueue = EventQueue.getInstance();
				eventQueue.add(new ReachedTurnOffsetEvent());
			}
			return false;
		}
	}
	
	public void prepareForTurn() throws PhidgetException {
		if (!preparedForTurn) {
			preparedForTurn = true;
			resetWantPositionOffset(0);
			leftWheel.resetPosPID();
			rightWheel.resetPosPID();
		}
	}

	public boolean amIAtTurnVelocity() {
		if (Math.abs(getVelocity(Interval.OneSecond)) < turnVelocityMax) {
			return true;
		} else {
			return false;
		}
	}

	public void turn() throws PhidgetException {
		if (!turning) {
			// Figure out the desired encoder position for each wheel based on the
			// current error
			int offset = getRightWheelError()/2;
			
			leftWheel.setWantEncoderOffset(offset);
			rightWheel.setWantEncoderOffset(-offset);
			
			turning = true;
		}
		
		leftWheel.runPositionPID();
		rightWheel.runPositionPID();
	}
}
