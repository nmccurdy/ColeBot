import org.apache.log4j.Logger;

import com.phidgets.PhidgetException;

public class TuneBalancePID implements ControllerListener {
	boolean runIt;

	private LogAppender logAppender;
	private Statistics statistics;
	private ConsoleController controller;

	private BlackBoxRecorder<Double> recorder;
	private BalanceLogic balanceLogic;
	private TiltSensor tiltSensor;
	private Wheels wheels;
	private ColeBotTest main;

	private double wantDeg = 0;
	
	private static final Logger LOGGER = Logger.getLogger(TuneBalancePID.class);

	public TuneBalancePID(Wheels wheels, Statistics statistics,
			ConsoleController controller, TiltSensor tiltSensor,
			BalanceLogic balanceLogic, ColeBotTest main) {
		this.wheels = wheels;
		this.statistics = statistics;
		this.controller = controller;
		this.tiltSensor = tiltSensor;
		this.balanceLogic = balanceLogic;
		this.main = main;
		
		logAppender = LogAppender.construct(controller);
		LOGGER.addAppender(logAppender);

		runIt = false;
		recorder = new BlackBoxRecorder<Double>(1000);
	}

	public void testBalancePID() throws PhidgetException, InterruptedException {
		if (!runIt) {
			reset();
		} else {
			main.maintainAngle(wantDeg);

			if (statistics.hasIntervalElaspsed()) {
				LOGGER.debug(getStats());
			}
		}
	}

	public String getStats() throws PhidgetException {
		return String.format("%s%nHave Deg: %1.2f Dist: %d%n",
				statistics.getStats(), tiltSensor.getPitchAngle(),
				wheels.getDistanceToWantPosition());
	}

	public String getPIDSettings() {
		// choose either wheel. settings should be the same.
		return balanceLogic.getSettings();
	}

	public void reset() throws PhidgetException {
		wheels.stopWheels();
	}

	public void setWantDeg(double deg) {
		this.wantDeg = deg;
	}

	public void setPIDkP(double kP) {
		balanceLogic.setKP(kP);
	}

	public void setPIDkV(double kV) {
		balanceLogic.setKV(kV);
	}

	public void setPIDkD(double kD) {
		balanceLogic.setKD(kD);
	}

	public void setPIDkE(double kE) {
		balanceLogic.setKE(kE);
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

			if (command.startsWith("d ")) {
				setWantDeg(getSecondParamInt(command));
			}

			if (command.startsWith("kp ")) {
				setPIDkP(getSecondParam(command));
			}

			if (command.startsWith("kv ")) {
				setPIDkV(getSecondParam(command));
			}

			if (command.startsWith("kd ")) {
				setPIDkD(getSecondParam(command));
			}

			if (command.startsWith("ke ")) {
				setPIDkE(getSecondParam(command));
			}

			if (command.startsWith("a ")) {
				setWantDeg(getSecondParam(command));
			}
			
			if (command.startsWith("t ")) {
				setTurnRight(getSecondParamInt(command));
			}

			return getPrompt();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	private void setTurnRight(int amount) {
		wheels.incrementRightWheelDifferential(amount);
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
		return "TuneBalance>";
	}

	@Override
	public String getHelp() {
		// TODO Auto-generated method stub
		return null;
	}
}
