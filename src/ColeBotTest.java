/*
 pscp RFIDExample.java pi@10.1.10.94:/home/pi/rotobot/RFID.java
 
 javac -classpath ./libs/phidget21.jar ColeBotTest.java
 sudo java -classpath ./libs/phidget21.jar ColeBotTest
 */

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.phidgets.PhidgetException;

import events.Event;
import events.EventDispatcher;
import events.EventListener;
import events.EventQueue;

public class ColeBotTest implements ControllerListener, EventListener {

	private static final double STOPPED_VELOCITY = 100;

	public static final int STATISTICS_INTERVAL = 1000;

	private Wheel leftWheel, rightWheel;
	private Wheels wheels;
	private TiltSensor tiltSensor;

	private DrivingListener drivingListener;
	private ConsoleController controller;
	private EnforceMinLoopInterval enforceMinLoopInterval;

	private LogAppender logAppender;
	private Statistics statistics;

	private TunePositionPID tunePositionPID;
	private TuneTiltPID tuneTiltPID;
	private TuneBalancePID tuneBalancePID;

	private boolean running = true;

	private TiltPID tiltPID;
	private BalanceLogic balanceLogic;
	private ResetLogic resetLogic;

	private MaintainUpAngle maintainUpAngle;

	private double wantDeg = 0;

	private static int LOOP_INTERVAL = 20;
	private static int TILT_LOOP_INTERVAL = 500;
	private static int ONE_SECOND_INTERVAL = 1000;

	private static int TILT_LOOP_SKIPS = TILT_LOOP_INTERVAL / LOOP_INTERVAL;
	private static int ONE_SECOND_SKIPS = ONE_SECOND_INTERVAL / LOOP_INTERVAL;

	private static final Logger LOGGER = Logger.getLogger(ColeBotTest.class);

	public static final void main(String args[]) throws Exception {

		// double a = 2.0452;
		// double b = 1.9;
		// System.out.println(BalancePID.pow(a, b) + " " + Math.pow(a, b));
		System.out.println("Welcome to ColeBot v2!");
		try {
			ColeBotTest test = new ColeBotTest();
			test.runLoop();

		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public ColeBotTest() throws PhidgetException, InterruptedException,
			Exception {
		statistics = new Statistics(10000, STATISTICS_INTERVAL);

		drivingListener = new DrivingListener();
		controller = new ConsoleController(drivingListener, this);
		controller.addListener("test", this);

		EventQueue eventQueue = EventQueue.getInstance();
		EventDispatcher eventDispatcher = new EventDispatcher(eventQueue);

		eventDispatcher.addEventListener(this);

		Executor executor = Executors.newFixedThreadPool(2);
		executor.execute(controller);
		executor.execute(eventDispatcher);

		logAppender = LogAppender.construct(controller);
		LOGGER.addAppender(logAppender);

		leftWheel = new Wheel(298807, true, 0);
		rightWheel = new Wheel(298806, true, 0);
		wheels = new Wheels(leftWheel, rightWheel, 200, 2000, 100);

		enforceMinLoopInterval = new EnforceMinLoopInterval(LOOP_INTERVAL);

		// tiltPID = new TiltPID(.0008, 0.0003, 5); kind of works but moves
		// really slowly
		// kP:0.00150, kI:0.00010, v:0.00000, d:1000 slowly on tile floor
		// tiltPID = new TiltPID(.002, 0.002, 0.001, 1000); kind of works on
		// carpet
		// kP:0.00080, kI:0.00050, v:0.00000, d:1500 kind of works at school.
		// jerky and slow
		// kP:0.00080, kI:0.00050, v:0.00050, d:1200
		// 0.00100, kI:0.00010, v:0.00005, d:1000
		// kP:0.00140, kI:0.00010, v:0.00001, d:2000 works really well!
		// colebot v2
		// kP:0.00700, kI:0.00080
		// topheavy colebot
		// kP:0.00400, kI:0.00080, v:0.00001  still not right
		// top heavy version at camp tech
		//.00001, 0.00004, .0000005, 2000
		tiltPID = new TiltPID(.000001, 0.000004, .0000005, 2000);
		// :8.00000, kD:1.50000, kV:0.12000q
		// kP:4.00000, kD:1.20000, kV:0.11000 camp techer's alue
		// kP:3.80000, kD:1.00000, kV:0.10000
		// for big wheeled colebot (3.8, 1, .1, .04, 100)
		// for top-heavy colebot (4.2, .95, .085, .04, 100)

		// new colebot camptech2014
		//kP:8.00000, kD:1.00000, kV:0.10000, kE:0.00000
		// works!
		// 6.00000, kD:1.00000, kV:0.07000, kE:0.04000
		// better
		//kP:6.00000, kD:0.85000, kV:0.07000, kE:0.04000
		
		// new chain driven one
		// kp: around 4-5 (more towards 4)works, .02, .01, 0
		
	    //kP:4.20000, kD:0.40000, kV:0.03000, kE:0.00000
		//kP:4.20000, kD:0.50000, kV:0.05000, kE:0.02000
		//kP:4.20000, kD:0.60000, kV:0.04000, kE:0.00000
		//almost
		//kP:2.00000, kD:0.30000, kV:0.06000, kE:0.00000
		//kP:2.00000, kD:0.30000, kV:0.06200, kE:0.00000
		//kP:2.00000, kD:0.35000, kV:0.06200, kE:0.00500
		balanceLogic = new BalanceLogic(2, .35, .062, .005, 100);
		resetLogic = new ResetLogic(40, 100, 1000, 1000);

		tiltSensor = new TiltSensor();

		tunePositionPID = new TunePositionPID(leftWheel, rightWheel,
				statistics, controller);
		tuneTiltPID = new TuneTiltPID(wheels, statistics, controller,
				tiltSensor, tiltPID, balanceLogic, this);

		tuneBalancePID = new TuneBalancePID(wheels, statistics, controller,
				tiltSensor, balanceLogic, this);

		controller.addListener("tunetiltpid", tuneTiltPID);
		controller.addListener("tunepositionpid", tunePositionPID);
		controller.addListener("tunebalancepid", tuneBalancePID);

		maintainUpAngle = new MaintainUpAngle(wheels, tiltSensor, tiltPID, 100,
				1000, 10000);

		zeroGyro();
	}

	public void runLoop() throws PhidgetException, InterruptedException {
		while (running) {
			tiltSensor.calcAngle();
//			System.out.println(tiltSensor.getPitchAngle());

			// maintainUpAngle.maintainUp();

			if (drivingListener.shouldQuit()) {
				quit();
			}

			statistics.hit();

			if (statistics.getNumLoops() % TILT_LOOP_SKIPS == 0) {
				wheels.calcAvgVelocity(Wheels.Interval.TiltLoop);
			}

			if (statistics.getNumLoops() % ONE_SECOND_SKIPS == 0) {
				wheels.calcAvgVelocity(Wheels.Interval.OneSecond);
			}

			switch (drivingListener.getControlType()) {
			case Drive:
				drive();
				break;
			case TunePositionPID:
				tunePositionPID.testPositionPID();
				break;
			case TuneTiltPID:
				tuneTiltPID.testTiltPID();
				break;
			case TuneBalancePID:
				tuneBalancePID.testBalancePID();
				break;
			}

			if (statistics.hasIntervalElaspsed()) {
				LOGGER.debug(statistics.getStats());
			}

			enforceMinLoopInterval.sleepIfNecessary();
		}
	}

	public void drive() throws PhidgetException, InterruptedException {

		int newOffset = 0;

		if (drivingListener.shouldStop()) {
			wheels.resetWantPositionOffset(0);
			// it will drift if we keep setting the 0 position. Once we
			// set it, go back to normal driving so that it will maintain
			// that 0 position
			drivingListener.setShouldStop(false);
		} else {
			newOffset = drivingListener.getOffsetAndReset();
		}

		wheels.incrementRightWheelDifferential(drivingListener
				.getRightWheelDifferentialAndReset());

		if (wheels.shouldITurn()) {
			// if (wheels.amIAtTurnVelocity()) {
			if (Math.abs(tiltSensor.getPitchAngle()) < 1) {
				wheels.turn();
			} else {
				// stop and maintain balance to prepare for turn
				wheels.prepareForTurn();
				maintainPosition(0);
			}
		} else {
			maintainPosition(newOffset);
		}
	}

	public void maintainPosition(int offsetIncrement) throws PhidgetException {

		wheels.incWantPositionOffset(offsetIncrement);

		wheels.hasPositionBeenReached();

		// recalculate the desired tilt at a slower interval because it takes
		// time for the desired angle to be reached.
		if (statistics.getNumLoops() % TILT_LOOP_SKIPS == 0) {

			double wheelVelocity = wheels.getVelocity(Wheels.Interval.TiltLoop);

			int distanceToWant = wheels.getDistanceToWantPosition();
			if (Math.abs(distanceToWant) < 1000) {
				this.wantDeg = tiltPID.calcTilt(distanceToWant, wheelVelocity);
			}
			
			if (statistics.hasIntervalElaspsed()) {
				LOGGER.debug(String.format("dist:%d,  wantDeg:%1.2f",
						distanceToWant, wantDeg));
			}
		}

		maintainAngle(this.wantDeg);
	}

	public void maintainAngle(double wantDeg) throws PhidgetException {
		double haveDeg = tiltSensor.getPitchAngle();
		double velocityDeg = tiltSensor.getPitchVelocity();

		wheels.calcAvgVelocity(Wheels.Interval.BalanceLoop);
		double wheelVelocity = wheels.getVelocity(Wheels.Interval.BalanceLoop);

		balanceLogic.setWantDeg(wantDeg);
		wheels.incrementRightWheelDifferential(drivingListener
				.getRightWheelDifferentialAndReset());
		double power = balanceLogic.calcPower(haveDeg, velocityDeg,
				wheelVelocity, wheels.getDistanceToWantPosition());

		if (resetLogic.amIKnockedOver(haveDeg)) {
			LOGGER.debug("I've fallen!");
			if (!resetLogic.shouldIStandUp()) {
				LOGGER.debug("waiting");
				wheels.stopWheels();
			} else {
				LOGGER.debug("trying to get up");
				// wheels.resetWantPositionOffset(0);
				wheels.setPower(power);
			}
		} else {
			wheels.setPower(power);
//			System.out.println(power);
		}
	}

	public void zeroGyro() throws PhidgetException, InterruptedException {
		System.out.print("Zeroing gyro in: ");

		wheels.stopWheels();

		wheels.setRightWheelDifferential(0);
		wheels.resetWantPositionOffset(0);

		for (int i = 5; i > 0; i--) {
			System.out.print(i + "...");

			// the kalman filter needs to be running every 20 ms
			// so that the correct angle can be calculated
			int tiltSensorIntervalMS = 20;
			for (int j = 0; j <= 1000 / tiltSensorIntervalMS; j++) {
				tiltSensor.calcAngle();
				Thread.sleep(tiltSensorIntervalMS);
			}

		}

		tiltSensor.zero();
		System.out.println(String.format(" offset: %1.2f",
				tiltSensor.getPitchAngleZero()));
	}

	@Override
	public String doCommand(String s) {
		if (s.equalsIgnoreCase("debug")) {
			logAppender.useDebugFilter();
		}

		if (s.equalsIgnoreCase("info")) {
			logAppender.useInfoFilter();
		}

		if (s.equalsIgnoreCase("q")) {
			logAppender.useInfoFilter();
			return controller.quitted();
		}

		return null;
	}

	@Override
	public String getPrompt() {
		return "Test>";
	}

	@Override
	public String getHelp() {
		return "debug | info";
	}

	public void quit() {
		try {
			running = false;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("closing left wheel");

			leftWheel.close();
			System.out.println("closing right wheel");

			rightWheel.close();

			System.out.println("closing tilt sensor");
			tiltSensor.close();
		} catch (PhidgetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("exiting...");

		System.exit(0);
	}

	@Override
	public void eventFired(Event event) {
		LOGGER.info(event.getName());
	}
}
