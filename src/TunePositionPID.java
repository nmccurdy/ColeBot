import org.apache.log4j.Logger;

import com.phidgets.PhidgetException;

public class TunePositionPID implements ControllerListener {

	private Wheel rightWheel;
	private Wheel leftWheel;

	int offset;
	boolean runIt;
	boolean testLeftWheel = true;
	boolean testRightWheel = true;

	private LogAppender logAppender;
	private Statistics statistics;
	private ConsoleController controller;

	private static final Logger LOGGER = Logger
			.getLogger(TunePositionPID.class);

	private class BBWheelPosition {
		public int left = 0;
		public int right = 0;

		@Override
		public String toString() {
			return String.format("%d\t%d", left, right);
		}
	}

	private BlackBoxRecorder<BBWheelPosition> recorder;

	public TunePositionPID(Wheel leftWheel, Wheel rightWheel,
			Statistics statistics, ConsoleController controller) {
		this.leftWheel = leftWheel;
		this.rightWheel = rightWheel;
		this.statistics = statistics;
		this.controller = controller;

		logAppender = LogAppender.construct(controller);
		LOGGER.addAppender(logAppender);

		runIt = false;
		offset = 0;
		recorder = new BlackBoxRecorder<BBWheelPosition>(1000);
	}

	public void testPositionPID() throws PhidgetException, InterruptedException {
		if (!runIt) {
			reset();
		} else {

			BBWheelPosition bb = new BBWheelPosition();

			if (testRightWheel) {
				rightWheel.runPositionPID();
				bb.right = rightWheel.getEncoderPosition();
			}
			if (testLeftWheel) {
				leftWheel.runPositionPID();
				bb.left = leftWheel.getEncoderPosition();
			}

			recorder.add(bb);

			if (statistics.hasIntervalElaspsed()) {
				LOGGER.debug(getStats());
			}
		}
	}

	public String getStats() throws PhidgetException {
		return String
				.format("Want: %d, HaveL: %d, HaveR: %d%n", offset,
						leftWheel.getEncoderPosition(),
						rightWheel.getEncoderPosition());
	}

	public String getPIDSettings() {
		// choose either wheel. settings should be the same.
		return leftWheel.getPosPIDsettings();
	}

	public void reset() throws PhidgetException {
		rightWheel.stopWheel();
		leftWheel.stopWheel();
	}

	public void setOffset(int offset) throws PhidgetException {
		leftWheel.resetEncoderPosition();
		leftWheel.resetPosPID();
		rightWheel.resetEncoderPosition();
		rightWheel.resetPosPID();

		rightWheel.setWantEncoderOffset(offset);
		leftWheel.setWantEncoderOffset(offset);

		this.offset = offset;
	}

	public void setPIDkP(double kP) {
		rightWheel.setPosPIDkP(kP);
		leftWheel.setPosPIDkP(kP);
	}

	public void setPIDkI(double kI) {
		rightWheel.setPosPIDkI(kI);
		leftWheel.setPosPIDkI(kI);
	}

	@Override
	public String doCommand(String command) {
		if (command.equalsIgnoreCase("debug")) {
			logAppender.useDebugFilter();
		}

		if (command.equalsIgnoreCase("info")) {
			logAppender.useInfoFilter();
		}

		if (command.equalsIgnoreCase("q")) {
			logAppender.useInfoFilter();
			runIt = false;
			return controller.quitted();
		}

		if (command.equalsIgnoreCase("run")) {
			runIt = true;
		}

		if (command.equalsIgnoreCase("stop")) {
			runIt = false;
		}

		if (command.equalsIgnoreCase("stats")) {
			try {
				return getStats();
			} catch (PhidgetException e) {
				e.printStackTrace();
			}
		}

		if (command.equalsIgnoreCase("pid")) {
			return getPIDSettings();
		}

		if (command.equalsIgnoreCase("bb")) {
			return recorder.getTabbedData();
		}

		if (command.startsWith("p ")) {
			try {
				setOffset(getSecondParamInt(command));
			} catch (PhidgetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (command.startsWith("kp ")) {
			setPIDkP(getSecondParam(command));
		}

		if (command.startsWith("ki ")) {
			setPIDkI(getSecondParam(command));
		}

		return getPrompt();
	}

	private Double getSecondParam(String command) {
		String splits[] = command.split(" ");
		return Double.valueOf(splits[1]);
	}

	private Integer getSecondParamInt(String command) {
		String splits[] = command.split(" ");
		return Integer.valueOf(splits[1]);
	}

	@Override
	public String getPrompt() {
		return "TunePos>";
	}

	@Override
	public String getHelp() {
		// TODO Auto-generated method stub
		return null;
	}
}
