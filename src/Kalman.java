import java.lang.reflect.Field;

/*
 * This code is borrowed from 
 * http://blog.tkjelectronics.dk/2012/09/a-practical-approach-to-kalman-filter-and-how-to-implement-it/
 */

public class Kalman {

	/* Kalman filter variables */
	private double Q_angle; // Process noise variance for the accelerometer
	private double Q_bias; // Process noise variance for the gyro bias
	private double R_measure; // Measurement noise variance - this is actually
								// the variance of the measurement noise

	private double angle; // The angle calculated by the Kalman filter - part of
							// the 2x1 state matrix
	private double bias; // The gyro bias calculated by the Kalman filter - part
							// of the 2x1 state matrix
	private double rate; // Unbiased rate calculated from the rate and the
							// calculated bias - you have to call getAngle to
							// update the rate

	private double P[][]; // Error covariance matrix - This is a 2x2 matrix
	private double K[]; // Kalman gain - This is a 2x1 matrix
	private double y; // Angle difference - 1x1 matrix
	private double S; // Estimate error - 1x1 matrix

	public Kalman(double Q_angle, double Q_bias, double R_measure) {
		this.Q_angle = Q_angle;
		this.Q_bias = Q_bias;
		this.R_measure = R_measure;

		/* We will set the varibles like so, these can also be tuned by the user */
		// Q_angle = 0.001;
		// Q_bias = 0.003;
		// R_measure = 0.03;

		P = new double[2][2];
		K = new double[2];

		reset();
	};

	// The angle should be in degrees and the rate should be in degrees per
	// second and the delta time in seconds
	double getAngle(double newAngle, double newRate, double dt) {
		// KasBot V2 - Kalman filter module - http://www.x-firm.com/?page_id=145
		// Modified by Kristian Lauszus
		// See my blog post for more information:
		// http://blog.tkjelectronics.dk/2012/09/a-practical-approach-to-kalman-filter-and-how-to-implement-it

		// Discrete Kalman filter time update equations - Time Update
		// ("Predict")
		// Update xhat - Project the state ahead
		/* Step 1 */
		rate = newRate - bias;
		angle += dt * rate;

		// Update estimation error covariance - Project the error covariance
		// ahead
		/* Step 2 */
		P[0][0] += dt * (dt * P[1][1] - P[0][1] - P[1][0] + Q_angle);
		P[0][1] -= dt * P[1][1];
		P[1][0] -= dt * P[1][1];
		P[1][1] += Q_bias * dt;

		// Discrete Kalman filter measurement update equations - Measurement
		// Update ("Correct")
		// Calculate Kalman gain - Compute the Kalman gain
		/* Step 4 */
		S = P[0][0] + R_measure;
		/* Step 5 */
		K[0] = P[0][0] / S;
		K[1] = P[1][0] / S;

		// Calculate angle and bias - Update estimate with measurement zk
		// (newAngle)
		/* Step 3 */
		y = newAngle - angle;
		/* Step 6 */
		angle += K[0] * y;
		
//		if (Double.isNaN(angle)) {
//			System.out.println(this.toString());
//			System.out.println(String.format("new angle: %1.2f, new rate: %1.2f, dt: %1.2f", newAngle, newRate, dt));
//		}
		
		
		bias += K[1] * y;

		// Calculate estimation error covariance - Update the error covariance
		/* Step 7 */
		P[0][0] -= K[0] * P[0][0];
		P[0][1] -= K[0] * P[0][1];
		P[1][0] -= K[1] * P[0][0];
		P[1][1] -= K[1] * P[0][1];

		return angle;
	};

	void setAngle(double newAngle) {
		angle = newAngle;
	}; // Used to set angle, this should be set as the starting angle

	double getRate() {
		return rate;
	}; // Return the unbiased rate

	/* These are used to tune the Kalman filter */
	void setQangle(double newQ_angle) {
		Q_angle = newQ_angle;
	};

	void setQbias(double newQ_bias) {
		Q_bias = newQ_bias;
	};

	void setRmeasure(double newR_measure) {
		R_measure = newR_measure;
	};

	void reset() {
		rate = 0;
		bias = 0;
		angle = 0;

		P[0][0] = 0; // Since we assume tha the bias is 0 and we know the
		// starting angle (use setAngle), the error covariance
		// matrix is set like so - see:
		// http://en.wikipedia.org/wiki/Kalman_filter#Example_application.2C_technical
		P[0][1] = 0;
		P[1][0] = 0;
		P[1][1] = 0;

	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		String newLine = System.getProperty("line.separator");

		result.append(this.getClass().getName());
		result.append(" Object {");
		result.append(newLine);

		// determine fields declared in this class only (no fields of
		// superclass)
		Field[] fields = this.getClass().getDeclaredFields();

		// print field names paired with their values
		for (Field field : fields) {
			result.append("  ");
			try {
				result.append(field.getName());
				result.append(": ");
				// requires access to private field:
				result.append(field.get(this));
			} catch (IllegalAccessException ex) {
				System.out.println(ex);
			}
			result.append(newLine);
		}
		result.append("}");

		return result.toString();
	}
}
