package com.saic.easydrive.obd.obd;


import com.saic.easydrive.obd.obdreader.ObdCommand;

public class BusInitObdCommand extends ObdCommand {
	/**
	 * * @param command
	 * */
	public BusInitObdCommand() {
		super("01 00");
	}

	/**
	 * * @param other
	 * */
	public BusInitObdCommand(ObdCommand other) {
		super(other);
	}

	@Override
	public String getFormattedResult() {
		return getResult();
	}

	@Override
	public String getName() {
		return "Bus Init";
	}
}
