import org.apache.log4j.Logger;

import com.phidgets.PhidgetException;

public class TuneTiltPID implements ControllerListener {

	private Wheels wheels;
	private TiltSensor tiltSensor;
	private BalanceLogic balanceLogic;
	private ColeBotTest main;
	private TiltPID tiltPID;
	
	boolean runIt;

	private LogAppender logAppender;
	private Statistics statistics;
	private ConsoleController controller;

	private BlackBoxRecorder<Double> recorder;


	private static final Logger LOGGER = Logger
			.getLogger(TuneTiltPID.class);

	public TuneTiltPID(Wheels wheels, Statistics statistics,
			ConsoleController controller, TiltSensor tiltSensor,
			TiltPID tiltPID, BalanceLogic balanceLogic, ColeBotTest main) {
		this.wheels = wheels;
		this.statistics = statistics;
		this.controller = controller;
		this.tiltSensor = tiltSensor;
		this.tiltPID = tiltPID;
		this.balanceLogic = balanceLogic;
		this.main = main;
		
		logAppender = LogAppender.construct(controller);
		LOGGER.addAppender(logAppender);

		runIt = false;
		recorder = new BlackBoxRecorder<Double>(1000);
	}

	public void testTiltPID() throws PhidgetException, InterruptedException {
		if (!runIt) {
			reset();
		} else {
			main.maintainPosition(0);
			if (statistics.hasIntervalElaspsed()) {
				LOGGER.debug(getStats());
			}
		}
	}

	public String getStats() throws PhidgetException {
		return String
				.format("Distance: %d, HaveAngle: %1.2f, WantAngle: %1.2f%n", wheels.getDistanceToWantPosition(), tiltSensor.getPitchAngle(), balanceLogic.getWantDeg());
	}

	public String getPIDSettings() {
		// choose either wheel. settings should be the same.
		return tiltPID.getSettings();
	}

	public void reset() throws PhidgetException {
		wheels.resetWantPositionOffset(0);
		wheels.stopWheels();
	}

	public void setEncoderOffset(int offset) throws PhidgetException {
		wheels.resetWantPositionOffset(offset);
	}

	public void setPIDkP(double kP) {
		tiltPID.setKP(kP);
	}

	public void setPIDkI(double kI) {
		tiltPID.setKI(kI);
	}

	@Override
	public String doCommand(String command) {
		try {
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
				return getStats();
			}

			if (command.equalsIgnoreCase("pid")) {
				return getPIDSettings();
			}

			if (command.equalsIgnoreCase("bb")) {
				return recorder.getTabbedData();
			}

			if (command.startsWith("p ")) {
				setEncoderOffset(getSecondParamInt(command));
			}

			if (command.startsWith("kp ")) {
				setPIDkP(getSecondParam(command));
			}

			if (command.startsWith("ki ")) {
				setPIDkI(getSecondParam(command));
			}
			if (command.startsWith("d ")) {
				setMaxDistance(getSecondParamInt(command));
			}
			if (command.startsWith("v ")) {
				setVelocityScale(getSecondParam(command));
			}


			
			return getPrompt();
		} catch (PhidgetException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	private void setVelocityScale(double scale) {
		tiltPID.setVelocityScale(scale);
		
	}

	private void setMaxDistance(int dist) {
		tiltPID.setMaxDistance(dist);
		
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
		return "TuneTilt>";
	}

	@Override
	public String getHelp() {
		// TODO Auto-generated method stub
		return null;
	}
}
