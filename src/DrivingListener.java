public class DrivingListener implements ControllerListener {
	private boolean offsetChanged = false;
	private int offset = 0;
	private int rightWheelDifferential = 0;
	
	private boolean quit = false;

	boolean shouldStop = false;
	
	public static enum ControlType {
		TunePositionPID, TuneTiltPID, TuneBalancePID, Drive
	};

	private ControlType controlType = ControlType.TuneBalancePID;
	
	public DrivingListener() {
	}

	public boolean shouldStop() {
		return shouldStop;
	}
	
	public void setShouldStop(boolean value) {
		shouldStop = value;
	}
	
	public int getRightWheelDifferentialAndReset() {
		int output = rightWheelDifferential;
		rightWheelDifferential = 0;
		return output;
	}

	public int getOffsetAndReset() {
		int output = offset;
		offset = 0;
		offsetChanged = false;
		return output;
	}

	public void quitted() {
	}

	public String doCommand(String command) {
		if (command.equalsIgnoreCase("W")) {
//			incTilt(1);
			offset += 20000;
			offsetChanged = true;
			shouldStop = false;
		}

		if (command.equalsIgnoreCase("S")) {
//			incTilt(-1);
			offset -= 20000;
			offsetChanged = true;
			shouldStop = false;
		}

		if (command.equalsIgnoreCase("A")) {
			rightWheelDifferential += 800;
		}

		if (command.equalsIgnoreCase("D")) {
			rightWheelDifferential -= 800;
		}

		if (command.equalsIgnoreCase("X")) {
			offset = 0;
			offsetChanged = true;
			shouldStop = true;
			rightWheelDifferential = 0;
		}

		if (command.equalsIgnoreCase("Q")) {
			quit = true;
		}
		
		if (command.equalsIgnoreCase("?")) {
			return getHelp();
		}
		
		if (command.startsWith("run ")) {
			String splits[] = command.split(" ");
			String subCommand = splits[1];
			
			if (subCommand.equalsIgnoreCase("drive")) {
				controlType = ControlType.Drive;
				return "running Drive\n";
			} else if (subCommand.equalsIgnoreCase("TunePositionPID")) {
				controlType = ControlType.TunePositionPID;
				return "running TunePositionPID\n";
			} else if (subCommand.equalsIgnoreCase("TuneTiltPID")) {
				controlType = ControlType.TuneTiltPID;
				return "running TuneTiltPID\n";
			} else if (subCommand.equalsIgnoreCase("TuneBalancePID")) {
				controlType = ControlType.TuneBalancePID;
				return "running TuneBalancePID\n";
			}
		}

		return getState() + getPrompt();
	}

	public String getPrompt() {
		return ">";
	}

	public String getHelp() {
		return "w: tilt forward (1 deg), s: tilt back, a: turn left, d: turn right, x: reset";
	}

	public String getState() {
		return String.format("Offset: %df, RightWheelDiff: %d%n",
				offset, rightWheelDifferential);
	}
	
	public boolean shouldQuit() {
		return quit;
	}
	
	public ControlType getControlType() {
		return controlType;
	}
	
	public boolean didOffsetChange() {
		return offsetChanged;
	}
}
