package com.popo.laziman.cloud.iot.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;


public class Command {
	public static enum CommandType{
		control,schedule
	}
	public static enum ControlType{
		on,off
	}
	
	private String deviceId;
	private CommandType type;
	private ControlType control;
	
	@JsonFormat(pattern= "yyyyMMddHHmmssSSS")
	private Date requestTime;
	@JsonFormat(pattern= "yyyyMMddHHmmssSSS")
	private Date expireTime;
	private int duration;
	private String cron;
	
	
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public CommandType getType() {
		return type;
	}
	public void setType(CommandType type) {
		this.type = type;
	}
	public ControlType getControl() {
		return control;
	}
	public void setControl(ControlType control) {
		this.control = control;
	}
	public Date getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}
	public Date getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String getCron() {
		return cron;
	}
	public void setCron(String cron) {
		this.cron = cron;
	}
	
	
	
}
