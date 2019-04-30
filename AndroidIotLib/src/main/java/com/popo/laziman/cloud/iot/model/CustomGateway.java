package com.popo.laziman.cloud.iot.model;

import java.util.ArrayList;
import java.util.List;

import com.google.api.services.cloudiot.v1.model.Device;

public class CustomGateway {
	public CustomGateway(Device device) {
		this.gatewayDevice = new CustomDevice(device);
		this.gatewayId = device.getId();
		this.gatewayName = device.getMetadata().get(String.valueOf(DeviceMetadata.name));

	}
	
	public CustomGateway() {
	}

	private CustomDevice gatewayDevice;
	private String gatewayId;
	private String gatewayName;

	private List<CustomDevice> devices = new ArrayList<>();



	public CustomDevice getGatewayDevice() {
		return gatewayDevice;
	}

	public void setGatewayDevice(CustomDevice gatewayDevice) {
		this.gatewayDevice = gatewayDevice;
	}

	public String getGatewayId() {
		return gatewayId;
	}

	public void setGatewayId(String gatewayId) {
		this.gatewayId = gatewayId;
	}

	public String getGatewayName() {
		return gatewayName;
	}

	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}

	public List<CustomDevice> getDevices() {
		return devices;
	}

	public void setDevices(List<CustomDevice> devices) {
		this.devices = devices;
	}
	
	

}
