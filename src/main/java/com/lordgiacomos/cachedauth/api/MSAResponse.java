package com.lordgiacomos.cachedauth.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URISyntaxException;

public class MSAResponse { //should be able to slim this code down and bury it in another class once I'm done with development
    String userCode;
    String deviceCode;
    URI verificationUri;
    int expiresInSeconds;
    int interval; // how often (in seconds), client should check if code has been submitted through microsoft account
    String message;

    MSAResponse(String responseString) throws URISyntaxException {
        JsonObject json = JsonParser.parseString(responseString).getAsJsonObject();
        userCode = json.get("user_code").getAsString();
        deviceCode = json.get("device_code").getAsString();
        verificationUri = new URI(json.get("verification_uri").getAsString());
        expiresInSeconds = json.get("expires_in").getAsInt();
        interval = json.get("interval").getAsInt();
        message = json.get("message").getAsString();
    }
}