
/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.popo.laziman.cloud.iot;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.joda.time.DateTime;

import com.popo.laziman.cloud.iot.model.CustomDevice;

// [START iot_mqtt_includes]
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class MqttMobileImpl {
	private static MqttMobileImpl me;
	private static MqttAsyncClient client;
	private static CustomDevice mobileDevice;
	private static MqttCallback callback;

	public static MqttMobileImpl getInstance(CustomDevice inDvice, MqttCallback inCallback) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, MqttException, InterruptedException {
		if (me == null) {
			mobileDevice = inDvice;
			callback = inCallback;
			me = new MqttMobileImpl();
		}

		return me;
	}

	private MqttMobileImpl() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, MqttException, InterruptedException {
		startMqtt(CloudConfig.mqttBridgeHost, CloudConfig.mqttBridgePort, CloudConfig.projectId, CloudConfig.cloudRegion, CloudConfig.registryId, CloudConfig.privateKey, CloudConfig.algorithm);
	}


	/**
	 * Create a Cloud IoT Core JWT for the given project id, signed with the
	 * given RSA key.
	 */
	private String createJwtRsa(String projectId, byte[] keyBytes) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		DateTime now = new DateTime();
		// Create a JWT to authenticate this device. The device will be
		// disconnected after the token
		// expires, and will have to reconnect with a new token. The audience
		// field should always be set
		// to the GCP project id.
		JwtBuilder jwtBuilder = Jwts.builder().setIssuedAt(now.toDate()).setExpiration(now.plusMinutes(20).toDate()).setAudience(projectId);

		// byte[] keyBytes = privateKey.getBytes();
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");

		return jwtBuilder.signWith(SignatureAlgorithm.RS256, kf.generatePrivate(spec)).compact();
	}
	// [END iot_mqtt_jwt]

	/**
	 * Create a Cloud IoT Core JWT for the given project id, signed with the
	 * given ES key.
	 */
	private String createJwtEs(String projectId, byte[] keyBytes) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		DateTime now = new DateTime();
		// Create a JWT to authenticate this device. The device will be
		// disconnected after the token
		// expires, and will have to reconnect with a new token. The audience
		// field should always be set
		// to the GCP project id.
		JwtBuilder jwtBuilder = Jwts.builder().setIssuedAt(now.toDate()).setExpiration(now.plusMinutes(20).toDate()).setAudience(projectId);

		// byte[] keyBytes = privateKey.getBytes();
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("EC");

		return jwtBuilder.signWith(SignatureAlgorithm.ES256, kf.generatePrivate(spec)).compact();
	}

	/** Connects the gateway to the MQTT bridge. */
	public void startMqtt(String mqttBridgeHostname, int mqttBridgePort, String projectId, String cloudRegion, String registryId, byte[] keyBytes, String algorithm)
			throws NoSuchAlgorithmException, IOException, MqttException, InterruptedException, InvalidKeySpecException {
		// [START iot_gateway_start_mqtt]

		// Build the connection string for Google's Cloud IoT Core MQTT server.
		// Only SSL
		// connections are accepted. For server authentication, the JVM's root
		// certificates
		// are used.
		final String mqttServerAddress = String.format("ssl://%s:%s", mqttBridgeHostname, mqttBridgePort);

		// Create our MQTT client. The mqttClientId is a unique string that
		// identifies this device. For
		// Google Cloud IoT Core, it must be in the format below.
		final String mqttClientId = String.format("projects/%s/locations/%s/registries/%s/devices/%s", projectId, cloudRegion, registryId, mobileDevice.getDeviceId());

		MqttConnectOptions connectOptions = new MqttConnectOptions();
		// Note that the Google Cloud IoT Core only supports MQTT 3.1.1, and
		// Paho requires that we
		// explictly set this. If you don't set MQTT version, the server will
		// immediately close its
		// connection to your device.
		connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
		connectOptions.setAutomaticReconnect(true);

		Properties sslProps = new Properties();
		sslProps.setProperty("com.ibm.ssl.protocol", "TLSv1.2");
		connectOptions.setSSLProperties(sslProps);

		// With Google Cloud IoT Core, the username field is ignored, however it
		// must be set for the
		// Paho client library to send the password field. The password field is
		// used to transmit a JWT
		// to authorize the device.
		connectOptions.setUserName("unused");

		DateTime iat = new DateTime();
		if (algorithm.equals("RS256")) {
			connectOptions.setPassword(createJwtRsa(projectId, keyBytes).toCharArray());
		} else if (algorithm.equals("ES256")) {
			connectOptions.setPassword(createJwtEs(projectId, keyBytes).toCharArray());
		} else {
			throw new IllegalArgumentException("Invalid algorithm " + algorithm + ". Should be one of 'RS256' or 'ES256'.");
		}

		System.out.println(String.format(mqttClientId));

		// Create a client, and connect to the Google MQTT bridge.
		client = new MqttAsyncClient(mqttServerAddress, mqttClientId, new MemoryPersistence());

		// Both connect and publish operations may fail. If they do, allow
		// retries but with an
		// exponential backoff time period.
		long initialConnectIntervalMillis = 500L;
		long maxConnectIntervalMillis = 6000L;
		long maxConnectRetryTimeElapsedMillis = 900000L;
		float intervalMultiplier = 1.5f;

		long retryIntervalMs = initialConnectIntervalMillis;
		long totalRetryTimeMs = 0;

		while (!client.isConnected() && totalRetryTimeMs < maxConnectRetryTimeElapsedMillis) {
			try {
				client.connect(connectOptions);
			} catch (MqttException e) {
				int reason = e.getReasonCode();

				// If the connection is lost or if the server cannot be
				// connected, allow retries, but with
				// exponential backoff.
				System.out.println("An error occurred: " + e.getMessage());
				if (reason == MqttException.REASON_CODE_CONNECTION_LOST || reason == MqttException.REASON_CODE_SERVER_CONNECT_ERROR) {
					System.out.println("Retrying in " + retryIntervalMs / 1000.0 + " seconds.");
					Thread.sleep(retryIntervalMs);
					totalRetryTimeMs += retryIntervalMs;
					retryIntervalMs *= intervalMultiplier;
					if (retryIntervalMs > maxConnectIntervalMillis) {
						retryIntervalMs = maxConnectIntervalMillis;
					}
				} else {
					throw e;
				}
			}
		}

		client.setCallback(callback);

		// The topic gateways receive error updates on. QoS must be 0.
		String errorTopic = String.format("/devices/%s/errors", mobileDevice.getDeviceId());
		System.out.println(String.format("Listening on %s", errorTopic));

		client.subscribe(errorTopic, 0);

		// [END iot_gateway_start_mqtt]
	}

	public void sendDataFromDevice(CustomDevice device, String messageType, String data) throws MqttException {

		if (!messageType.equals("events") && !messageType.equals("state")) {
			System.err.println("Invalid message type, must ether be 'state' or events'");
			return;
		}
		final String dataTopic = String.format("/devices/%s/%s", device.getDeviceId(), messageType);
		MqttMessage message = new MqttMessage(data.getBytes());
		message.setQos(1);
		client.publish(dataTopic, message);
		System.out.println("Data sent: " + message);

	}



	public void listenForConfigMessages(List<CustomDevice> devices) throws MqttException, IOException, InvalidKeySpecException, InterruptedException, NoSuchAlgorithmException {
		for (CustomDevice device : devices) {

			String commandTopic = String.format("/devices/%s/commands/#", device.getDeviceId());
			System.out.println(String.format("Listening on %s", commandTopic));

			String configTopic = String.format("/devices/%s/config", device.getDeviceId());
			System.out.println(String.format("Listening on %s", configTopic));

			client.subscribe(configTopic, 1);
			client.subscribe(commandTopic, 1);


		}

	}

	public void listenForEvent(CustomDevice device)
			throws MqttException, IOException, InvalidKeySpecException, InterruptedException, NoSuchAlgorithmException {

		String stateTopic = String.format("/devices/%s/state", device.getDeviceId());
		System.out.println(String.format("Listening on %s", stateTopic));

		String eventTopic = String.format("/devices/%s/event", device.getDeviceId());
		System.out.println(String.format("Listening on %s", eventTopic));

		client.subscribe(stateTopic, 1);
		client.subscribe(eventTopic, 1);

	}

}