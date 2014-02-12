package demos;
/*
 * Copyright 2007 Phidgets Inc.  All rights reserved.
 */

import java.util.HashMap;

import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.phidgets.RFIDPhidget;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.phidgets.event.ErrorEvent;
import com.phidgets.event.ErrorListener;
import com.phidgets.event.OutputChangeEvent;
import com.phidgets.event.OutputChangeListener;
import com.phidgets.event.TagGainEvent;
import com.phidgets.event.TagGainListener;
import com.phidgets.event.TagLossEvent;
import com.phidgets.event.TagLossListener;

public class RfidDemo {

	private HashMap<String, String> map;
	private RFIDPhidget rfid;

	public RfidDemo() {
		map = new HashMap<String, String>();

		try {
			rfid = new RFIDPhidget();

			rfid.addAttachListener(new AttachListener() {
				public void attached(AttachEvent ae) {
					try {
						((RFIDPhidget) ae.getSource()).setAntennaOn(true);
						((RFIDPhidget) ae.getSource()).setLEDOn(true);
					} catch (PhidgetException ex) {
					}
					System.out.println("attachment of " + ae);
				}
			});
			rfid.addDetachListener(new DetachListener() {
				public void detached(DetachEvent ae) {
					System.out.println("detachment of " + ae);
				}
			});
			rfid.addErrorListener(new ErrorListener() {
				public void error(ErrorEvent ee) {
					System.out.println("error event for " + ee);
				}
			});
			rfid.addTagGainListener(new TagGainListener() {
				public void tagGained(TagGainEvent oe) {
					myTagGained(oe.getValue());
				}
			});
			rfid.addTagLossListener(new TagLossListener() {
				public void tagLost(TagLossEvent oe) {
					myTagLost(oe.getValue());
				}
			});
			rfid.addOutputChangeListener(new OutputChangeListener() {
				public void outputChanged(OutputChangeEvent oe) {
					System.out.println(oe);
				}
			});

			rfid.openAny();
			System.out.println("waiting for RFID attachment...");
			rfid.waitForAttachment(1000);

			System.out.println("Serial: " + rfid.getSerialNumber());
			System.out.println("Outputs: " + rfid.getOutputCount());

		} catch (PhidgetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void myTagGained(String value) {
		System.out.println("Entering: " + map.get(value));
		
	}
	
	public void myTagLost(String value) {
		System.out.println("Leaving: " + map.get(value));
		
	}
	
	private void populateMap() {
		map.put("500091a1b4", "Computer Lab");
		map.put("50009146e6", "Library");
		map.put("500091a155", "Den");
		map.put("50009150c7", "Science Lab");
		map.put("5000917b41", "Robotics Room");
	}
	

	public void close() {
		try {
			rfid.close();

			rfid = null;
			System.out.println(" ok");
			if (false) {
				System.out.println("wait for finalization...");
				System.gc();
			}
		} catch (PhidgetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static final void main(String args[]) throws Exception {

		System.out.println(Phidget.getLibraryVersion());

		RfidDemo rfid = new RfidDemo();
		rfid.populateMap();

		// How to write a tag:
		// rfid.write("A TAG!!", RFIDPhidget.PHIDGET_RFID_PROTOCOL_PHIDGETS,
		// false);

		System.out.println("Outputting events.  Input to stop.");
		System.in.read();
		System.out.print("closing...");
		
		rfid.close();
	}
}
