package com.popo.laziman.cloud.iot.model;

import com.google.api.services.cloudiot.v1.model.Device;

public class CustomDevice {




	public CustomDevice(Device device) {
		this.device = device;
		this.deviceId = device.getId();
		this.deviceName = device.getMetadata().get(String.valueOf(DeviceMetadata.name));
		this.deviceType = DeviceType.valueOf(device.getMetadata().get(String.valueOf(DeviceMetadata.type)));
	}
	
	public CustomDevice(){}

	private Device device;
	private String deviceId;
	private String deviceName;
	private DeviceType deviceType;
	public Device getDevice() {
		return device;
	}
	public void setDevice(Device device) {
		this.device = device;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public DeviceType getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	
}
