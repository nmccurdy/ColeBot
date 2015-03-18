import org.apache.log4j.Logger;

import com.phidgets.PhidgetException;
import com.phidgets.SpatialEventData;
import com.phidgets.SpatialPhidget;
import com.phidgets.event.SpatialDataEvent;
import com.phidgets.event.SpatialDataListener;

public class TiltSensor {
	private SpatialPhidget spatial;
	private double pitchAngleZero;
	private double pitchAngle;
	private long lastTime = 0;
	private Kalman kalman;

	private static final Logger LOGGER = Logger.getLogger(TiltSensor.class);
	private static final long NS_PER_SEC = 1000000000;
	
	private boolean resetAverageAngle = true;
	private double averagePitchAngle;
	private int averageCount = 0;
	
	public TiltSensor() throws PhidgetException, Exception {

		LOGGER.addAppender(LogAppender.getInstance());

		kalman = new Kalman(0.001, 0.003, 0.03);
		spatial = new SpatialPhidget();

		spatial.openAny();
		LOGGER.info("Waiting for accelerometer");
		spatial.waitForAttachment();

	}

	public void calcAngle() throws PhidgetException {
		double accX = spatial.getAcceleration(0);
		double velocity = getPitchVelocity();
		

		long time = System.nanoTime();
		if (lastTime == 0) {
			lastTime = time;
		}

		double dt = ((double)(time - lastTime)) / NS_PER_SEC;

		// occasionally there is a bug where the acceleration exceeds 1
		// just treat it as an anomaly and ignore it
		if (Math.abs(accX) > 1) {
			accX = 0;
		}

		double newAngle = Math.toDegrees(Math.asin(accX));

		pitchAngle = kalman.getAngle(newAngle, velocity, dt);

//		System.out.println(String.format("accx: %1.2f, velocity:%1.2f, pitch:%1.2f", accX, velocity, pitchAngle));

		calcAveragePitchAngle(pitchAngle);
		
		lastTime = time;
	}

	private void calcAveragePitchAngle(double angle) {
		// do a running average.
		
		// if we just reset the running average, return the current angle
		if (resetAverageAngle) {
			resetAverageAngle = false;
			averagePitchAngle = angle;
			averageCount = 1;
		} else {
			averageCount++;

			// D2+((A3-D2)/(row(D1) + 1))
			averagePitchAngle = averagePitchAngle + ((angle - averagePitchAngle)/averageCount);
		}
	}

	public double getAccelerationX() throws PhidgetException {
		return spatial.getAcceleration(0);
	}

	public void zero() throws PhidgetException, InterruptedException {
		spatial.zeroGyro();
		Thread.sleep(100);
		pitchAngleZero = pitchAngle;
		lastTime = 0;
	}

	public double getPitchAngle() {
		return pitchAngle - pitchAngleZero;
	}
	
	public double getPitchVelocity() throws PhidgetException {
		return -spatial.getAngularRate(1);
	}

	public double getPitchAngleZero() {
		return pitchAngleZero;
	}
	
	public void close() throws PhidgetException {
		if (spatial.isAttached()) {
			spatial.close();
		}
	}
	
	public void setPitchAngleZero(double angle) {
		pitchAngleZero = angle;
	}
	
	public void resetAverageAngleCalc() {
		resetAverageAngle = true;
		averagePitchAngle = 0;
	}
	
	public double getAveragePitchAngle() {
		return averagePitchAngle;
	}
}
