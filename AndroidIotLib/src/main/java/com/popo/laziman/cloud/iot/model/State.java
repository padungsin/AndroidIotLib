package com.popo.laziman.cloud.iot.model;

import java.util.Date;

import com.popo.laziman.cloud.iot.model.Command.CommandType;
import com.popo.laziman.cloud.iot.model.Command.ControlType;

public class State {
	private static enum DeviceState{
		on,off
	}
	
	private String deviceId;
	private ControlType control;
	private CommandType commandType;
	private DeviceState state;
	private Date completeTime;
	
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public ControlType getControl() {
		return control;
	}
	public void setControl(ControlType control) {
		this.control = control;
	}
	public CommandType getCommandType() {
		return commandType;
	}
	public void setCommandType(CommandType commandType) {
		this.commandType = commandType;
	}
	public DeviceState getState() {
		return state;
	}
	public void setState(DeviceState state) {
		this.state = state;
	}
	public Date getCompleteTime() {
		return completeTime;
	}
	public void setCompleteTime(Date completeTime) {
		this.completeTime = completeTime;
	}
	
	
}
