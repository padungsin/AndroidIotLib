package com.popo.laziman.cloud.iot;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.api.services.cloudiot.v1.CloudIotScopes;

import java.io.ByteArrayInputStream;

public class GoogleCloudHelper {
    public static String APP_NAME="Laziman";

    protected static CloudIot createService(String credentialString) throws Exception{

        GoogleCredential credential =  GoogleCredential.fromStream(new ByteArrayInputStream(credentialString.getBytes())).createScoped(CloudIotScopes.all());
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
        CloudIot service =
                new CloudIot.Builder(new NetHttpTransport(), jsonFactory, init)
                        .setApplicationName(GoogleCloudHelper.APP_NAME)
                        .build();

        return service;
    }
}
